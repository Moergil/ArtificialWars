package sk.hackcraft.artificialwars.computersim.parts;

import sk.hackcraft.artificialwars.computersim.Device;
import sk.hackcraft.artificialwars.computersim.PinUtil;
import sk.hackcraft.artificialwars.computersim.Pins;


/**
 * Memory chip with 8bit data and 10bit address bus, containing 1024 bytes.
 * 
 * <pre>
 * READ     0
 * WRITE    1
 * CHIP SELECT   2
 * ADDRESS  3-12
 * DATA     13-20
 * </pre>
 */
public class MemChip1024 extends MemoryChip
{
	private static final int
		READWRITE_PIN = 0,
		CHIP_SELECT_PIN = READWRITE_PIN + 1,
		ADDRESS_START = CHIP_SELECT_PIN + 1,
		ADDRESS_LEN = 10,
		DATA_START = ADDRESS_START + ADDRESS_LEN,
		DATA_LEN = 8;
	
	private final byte[] memory;
	
	public MemChip1024()
	{
		memory = new byte[1024];
	}
	
	@Override
	protected int[] createDataIndexes()
	{
		return PinUtil.createSequenceIndexes(DATA_START, DATA_LEN);
	}

	@Override
	protected int[] createAddressIndexes()
	{
		return PinUtil.createSequenceIndexes(ADDRESS_START, ADDRESS_LEN);
	}
	
	@Override
	public String getName()
	{
		return "MemChip1024";
	}

	public byte[] getMemory()
	{
		return memory;
	}
	
	public void writeData(byte data[], int offset)
	{
		System.arraycopy(data, 0, memory, offset, data.length);
	}
	
	@Override
	public String getPinName(int index)
	{
		if (index >= ADDRESS_START && index < ADDRESS_START + ADDRESS_LEN)
		{
			return "A" + (index - ADDRESS_START);
		}
		else if (index >= DATA_START && index < DATA_START + DATA_LEN)
		{
			return "D" + (index - DATA_START);
		}
		else if (index == READWRITE_PIN)
		{
			return "R/W";
		}

		return super.getPinName(index);
	}
	
	@Override
	public int getPinsCount()
	{
		return READWRITE_PIN + CHIP_SELECT_PIN + ADDRESS_LEN + DATA_LEN;
	}
	
	@Override
	public void setBusConnection(Pins pins)
	{
		this.pins = pins;
	}
	
	@Override
	protected boolean isSelected()
	{
		return pins.readPin(CHIP_SELECT_PIN);
	}

	@Override
	protected Mode getMode()
	{
		return pins.readPin(READWRITE_PIN) ? Mode.WRITE : Mode.READ;
	}
	
	@Override
	protected byte readFromChip(int address)
	{
		return memory[address];
	}

	@Override
	protected void writeToChip(int address, byte value)
	{
		memory[address] = value;
	}
}
