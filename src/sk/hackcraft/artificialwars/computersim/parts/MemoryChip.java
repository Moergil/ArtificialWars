package sk.hackcraft.artificialwars.computersim.parts;

import sk.hackcraft.artificialwars.computersim.Device;
import sk.hackcraft.artificialwars.computersim.PinUtil;
import sk.hackcraft.artificialwars.computersim.Pins;

public abstract class MemoryChip implements Device
{
	protected Pins pins = Pins.DUMMY;
	
	protected final boolean dataBits[], addressBits[];
	protected final int dataIndexes[], addressIndexes[];
	
	protected MemoryChip()
	{
		dataIndexes = createDataIndexes();
		dataBits = new boolean[dataIndexes.length];
		
		addressIndexes = createAddressIndexes();
		addressBits = new boolean[addressIndexes.length];
	}
	
	protected abstract int[] createDataIndexes();
	protected abstract int[] createAddressIndexes();
	
	private Mode activeMode;
	
	@Override
	public void update()
	{
		pins.setAllPins(false);
		
		if (!isSelected())
		{
			return;
		}
		
		Mode mode = getMode();
		
		byte value = 0;
		int address = readAddressBus();
		
		switch (mode)
		{
			case WRITE:
				if (activeMode != Mode.WRITE)
				{
					activeMode = Mode.WRITE;
				}
				else
				{
					value = readDataBus();
					writeToChip(address, value);
				}
				break;
			case READ:
				activeMode = Mode.READ;
				value = readFromChip(address);
				writeToDataBus(value);
				break;
		}
	}
	
	protected abstract boolean isSelected();
	protected abstract Mode getMode();
	
	protected abstract byte readFromChip(int address);
	protected abstract void writeToChip(int address, byte value);
	
	protected void writeToDataBus(byte value)
	{
		PinUtil.codeValue(value, dataBits);
		pins.setPins(dataIndexes, dataBits);
	}
	
	protected byte readDataBus()
	{
		pins.readPins(dataIndexes, dataBits);
		return (byte)PinUtil.decodeValue(dataBits);
	}
	
	protected int readAddressBus()
	{
		pins.readPins(addressIndexes, addressBits);
		return (int)PinUtil.decodeValue(addressBits);
	}
	
	protected enum Mode
	{
		READ,
		WRITE;
	}
}
