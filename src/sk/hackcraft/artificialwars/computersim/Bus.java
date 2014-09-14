package sk.hackcraft.artificialwars.computersim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Bus
{	
	private final boolean[] bits, bitsCache;
	private final Map<Integer, Circuit> circuits = new HashMap<>();
	
	private final List<PinsBusConnection> connections = new ArrayList<>();

	public Bus(int bits)
	{
		this.bits = new boolean[bits];
		this.bitsCache = new boolean[bits];
	}

	public void update()
	{
		Arrays.fill(bits, false);
		
		for (PinsBusConnection connection : connections)
		{
			PinUtil.combine(connection.localBits, bits);
		}
	}
	
	public boolean readBusPin(int pin)
	{
		return bits[pin];
	}
	
	private boolean readBusPin2(int pin)
	{
		if (pin >= 0 && pin < bits.length)
		{
			return bits[pin];
		}
		else if (circuits.containsKey(pin))
		{
			return circuits.get(pin).readOutput(this);
		}
		else
		{
			return false;
		}
	}
	
	public void addCircuit(Circuit circuit, int outputPin)
	{
		circuits.put(outputPin, circuit);
	}
	
	public void connectDevice(Device device, int[] pinout)
	{
		PinsBusConnection connection = new PinsBusConnection(pinout);
		connections.add(connection);
		
		device.setBusConnection(connection);
	}
	
	public class PinsBusConnection implements Pins
	{
		private final int[] devicePinout;
		private final boolean[] localBits = new boolean[bits.length];
		
		public PinsBusConnection(int[] pinout)
		{
			this.devicePinout = pinout;
		}

		@Override
		public void setPin(int index, boolean value)
		{
			int busPin = devicePinout[index];
			
			if (busPin != Pins.UNCONNECTED)
			{
				localBits[busPin] = value;
			}
		}

		@Override
		public boolean readPin(int index)
		{
			int busPin = devicePinout[index];
			
			if (busPin != Pins.UNCONNECTED)
			{
				return readBusPin2(busPin);
			}
			else
			{
				return false;
			}
		}
		
		@Override
		public void setAllPins(boolean value)
		{
			Arrays.fill(localBits, value);
		}
		
		@Override
		public String toString()
		{
			return "LocalBits: " + PinUtil.bitsToString(localBits) + " BusBits: " + PinUtil.bitsToString(bits);
		}
	}
	
	@FunctionalInterface
	public interface Circuit
	{
		boolean readOutput(Bus bus);
	}
}
