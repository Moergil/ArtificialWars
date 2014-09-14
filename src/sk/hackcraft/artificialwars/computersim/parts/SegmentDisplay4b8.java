package sk.hackcraft.artificialwars.computersim.parts;

import sk.hackcraft.artificialwars.computersim.Device;
import sk.hackcraft.artificialwars.computersim.Part;
import sk.hackcraft.artificialwars.computersim.PinUtil;
import sk.hackcraft.artificialwars.computersim.Pins;

/**
 * W 0
 * A 1
 * D 2-9
 * CS 10
 */
public class SegmentDisplay4b8 implements Device
{
	private static final int
		WRITE_PIN = 0,
		ADDRESS_PIN = WRITE_PIN + 1,
		DATA_START = ADDRESS_PIN + 1,
		DATA_LEN = 8,
		CHIP_SELECT = DATA_START + DATA_LEN;
	
	private Pins pins = Pins.DUMMY;
	
	private byte dataBuffer[] = new byte[2];
	
	private boolean dataBits[] = new boolean[DATA_LEN];

	@Override
	public String getName()
	{
		return "Segment display 4 by 8"; 
	}
	
	@Override
	public int getPinsCount()
	{
		return 1 + 1 + 1 + DATA_LEN;
	}

	@Override
	public void setBusConnection(Pins pins)
	{
		this.pins = pins;
	}
	
	@Override
	public void update()
	{
		if (pins.readPin(CHIP_SELECT) && pins.readPin(WRITE_PIN))
		{
			writeToBuffer();
		}
	}
	
	private void writeToBuffer()
	{
		pins.readPins(dataBits, DATA_START, DATA_LEN);
		byte value = (byte)PinUtil.decodeValue(dataBits);
		
		int index = pins.readPin(ADDRESS_PIN) ? 1 : 0;
		
		dataBuffer[index] = value;
	}
	
	public byte[] getDataBuffer()
	{
		return dataBuffer;
	}
	
	@Override
	public String toString()
	{
		return String.format("Display: %02X%02X", dataBuffer[1], dataBuffer[0]);
	}
}
