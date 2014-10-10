package sk.hackcraft.artificialwars.computersim.parts;

import sk.hackcraft.artificialwars.computersim.Device;
import sk.hackcraft.artificialwars.computersim.PinUtil;
import sk.hackcraft.artificialwars.computersim.Pins;

/**
 * W 0
 * D 1-8
 * CS 9
 */
public class TextLineDisplay implements Device
{
	private static final int
		WRITE_PIN = 0,
		DATA_START = WRITE_PIN + 1,
		DATA_LEN = 8,
		CHIP_SELECT = DATA_START + DATA_LEN;
	
	private Pins pins = Pins.DUMMY;
	private boolean writeInitiated;
	
	private final byte dataBuffer[];
	
	private final boolean dataBits[] = new boolean[DATA_LEN];
	private final int dataIndexes[] = PinUtil.createSequenceIndexes(DATA_START, DATA_LEN);

	public TextLineDisplay(int lineSize)
	{
		this.dataBuffer = new byte[lineSize];
	}
	
	@Override
	public String getName()
	{
		return "Text display"; 
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
			if (!writeInitiated)
			{
				writeInitiated = true;
			}
			else
			{
				writeToBuffer();
			}
		}
		else
		{
			writeInitiated = false;
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
		StringBuilder b = new StringBuilder("Display: ");
		for (int i : dataBuffer)
		{
			b.append(i);
		}
		
		return b.toString();
	}
}
