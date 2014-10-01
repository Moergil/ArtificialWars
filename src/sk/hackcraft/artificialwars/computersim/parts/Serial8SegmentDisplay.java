package sk.hackcraft.artificialwars.computersim.parts;

import sk.hackcraft.artificialwars.computersim.Device;
import sk.hackcraft.artificialwars.computersim.PinUtil;
import sk.hackcraft.artificialwars.computersim.Pins;

/**
 * W 0
 * D 2-9
 * CS 10
 */
public class Serial8SegmentDisplay implements Device
{
	private static final int
		WRITE_PIN = 0,
		DATA_START = WRITE_PIN + 1,
		DATA_LEN = 8,
		CHIP_SELECT = DATA_START + DATA_LEN;
	
	private Pins pins = Pins.DUMMY;
	
	private final byte dataBuffer[];
	
	private final boolean dataBits[] = new boolean[DATA_LEN];
	private final int dataIndexes[] = PinUtil.createSequenceIndexes(DATA_START, DATA_LEN);

	public Serial8SegmentDisplay(int segmentPartsCount)
	{
		this.dataBuffer = new byte[segmentPartsCount];
	}
	
	@Override
	public String getName()
	{
		return "Segment display 4 by 8"; 
	}
	
	@Override
	public int getPinsCount()
	{
		return 1 + 1 + DATA_LEN;
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
		shiftBuffer();
		
		pins.readPins(dataIndexes, dataBits);
		byte value = (byte)PinUtil.decodeValue(dataBits);
		
		dataBuffer[0] = value;
	}
	
	private void shiftBuffer()
	{
		for (int i = dataBuffer.length - 2; i >= 0; i--)
		{
			dataBuffer[i + 1] = dataBuffer[i];
		}
	}
	
	public byte[] getDataBuffer()
	{
		return dataBuffer;
	}
	
	@Override
	public String toString()
	{
		// TODO testing, real values will not use ascii codes
		return String.format("Display: %02X%02X", dataBuffer[1], dataBuffer[0]);
	}
}
