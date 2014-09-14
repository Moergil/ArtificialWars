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
public class MemChip1024 implements Device
{
	private static final int
		READ_PIN = 0,
		WRITE_PIN = 1,
		CHIP_SELECT_PIN = 2,
		ADDRESS_START = 3,
		ADDRESS_LEN = 10,
		DATA_START = ADDRESS_START + ADDRESS_LEN,
		DATA_LEN = 8;
	
	private final byte[] memory;
	
	private final int addressWidth = 10, dataWidth = 8;
	
	private Pins pins = Pins.DUMMY;
	private boolean dataBits[] = new boolean[DATA_LEN];
	private boolean addressBits[] = new boolean[ADDRESS_LEN];
	
	public MemChip1024()
	{
		memory = new byte[1024];
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
		else if (index == READ_PIN)
		{
			return "Read";
		}
		else if (index == WRITE_PIN)
		{
			return "Write";
		}

		return Device.super.getPinName(index);
	}
	
	@Override
	public int getPinsCount()
	{
		return addressWidth + dataWidth + 2;
	}
	
	@Override
	public void setBusConnection(Pins pins)
	{
		this.pins = pins;
	}

	@Override
	public void update()
	{
		if (!pins.readPin(CHIP_SELECT_PIN))
		{
			pins.setAllPins(false);
			return;
		}

		boolean write = pins.readPin(WRITE_PIN);
		boolean read = pins.readPin(READ_PIN);
		
		byte dataValue = 0;
		
		if (write)
		{
			dataValue |= readDataBus();
		}
		
		if (read)
		{
			dataValue |= readMemory();
		}
		
		if (write)
		{
			int address = readAddressBus();
			writeToMemory(address, dataValue);
		}
		
		if (read)
		{
			writeToDataBus(dataValue);
		}
		else
		{
			pins.setAllPins(false);
		}
	}

	private byte readMemory()
	{
		int address = readAddressBus();
		
		return memory[address];
	}
	
	private void writeToDataBus(byte value)
	{
		PinUtil.codeValue(value, dataBits);
		pins.setPins(dataBits, DATA_START, DATA_LEN);
	}
	
	private byte readDataBus()
	{
		pins.readPins(dataBits, DATA_START, DATA_LEN);
		return (byte)PinUtil.decodeValue(dataBits);
	}
	
	private void writeToMemory(int address, byte value)
	{
		memory[address] = value;
	}
	
	private int readAddressBus()
	{
		pins.readPins(addressBits, ADDRESS_START, ADDRESS_LEN);
		return (int)PinUtil.decodeValue(addressBits);
	}
}
