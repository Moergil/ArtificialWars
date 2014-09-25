package sk.hackcraft.artificialwars.computersim.parts;

import sk.hackcraft.artificialwars.computersim.Device;
import sk.hackcraft.artificialwars.computersim.PinUtil;
import sk.hackcraft.artificialwars.computersim.Pins;

public class BusProbe implements Device
{
	private final int pinsCount;
	private final Formatter formatter;
	
	private Pins pins = Pins.DUMMY;
	
	public BusProbe(int pinsCount)
	{
		this.pinsCount = pinsCount;
		this.formatter = (builder, bits) -> PinUtil.bitsToString(bits);
	}
	
	public BusProbe(int pinsCount, Formatter formatter)
	{
		this.pinsCount = pinsCount;
		this.formatter = formatter;
	}
	
	@Override
	public String getName()
	{
		return "BusProbe";
	}
	
	@Override
	public int getPinsCount()
	{
		return pinsCount;
	}

	@Override
	public void setBusConnection(Pins pins)
	{
		this.pins = pins;
	}
	
	public void setPin(int index, boolean value)
	{
		pins.setPin(index, value);
	}
	
	public boolean readPin(int index)
	{
		return pins.readPin(index);
	}
	
	public void setAllPins(boolean value)
	{
		for (int i = 0; i < pinsCount; i++)
		{
			pins.setPin(i, value);
		}
	}
	
	@Override
	public void update()
	{
	}
	
	@Override
	public String toString()
	{
		int indexes[] = PinUtil.createSequenceIndexes(0, pinsCount);
		boolean values[] = new boolean[pinsCount];
		pins.readPins(indexes, values);

		StringBuilder builder = new StringBuilder();
		formatter.format(builder, values);
		
		return builder.toString();
	}
	
	@FunctionalInterface
	public interface Formatter
	{
		void format(StringBuilder builder, boolean[] bits);
	}
}
