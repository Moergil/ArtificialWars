package sk.hackcraft.artificialwars.computersim.parts;

import sk.hackcraft.artificialwars.computersim.PinUtil;
import sk.hackcraft.artificialwars.computersim.Pins;

/**
 * Data 0-7
 * Address 8-11
 * Read 12
 * Write 13
 * Chip select 14
 */
public class MEXTIOChip extends MemoryChip
{
	private interface Pin
	{
		public static final int
			DATA_START = 0,
			DATA_LEN = 8,
			ADDRESS_START = DATA_START + DATA_LEN,
			ADDRESS_LEN = 4,
			READWRITE = ADDRESS_START + ADDRESS_LEN,
			CHIP_SELECT = READWRITE + 1;
	}
	
	public interface Address
	{
		public static final int
			ABS_ROTATION_HI = 0,
			ABS_ROTATION_LO = 1,
			ROTATION_ORDER_HI = 2,
			ROTATION_ORDER_LO = 3,
			MOVE_ORDER_VALUE = 4,
			DETECTION_SEGMENT = 6,
			DETECTION_GRADIENT = 7,
			FLAGS = 8,
			SET_FLAGS = 9,
			NOISE = 10;
	}
	
	public interface Flag
	{
		public static final byte
			MOVING = 1, // moving
			ROTATING = 2, // rotating
			GUN_READY = 4, // gun can fire
			DETECTION_SEGMENT = 8, // enemy detected on segments
			DETECTION_GRADIENT = 16, // enemy detected on gradient
			FIRE_ORDER = 32, // fire order. cleared after firing
			NOISE = 64; // if true, noise is regenerated, stays on the same value otherwise
	}
	
	private static final byte flagsWriteMask = Flag.FIRE_ORDER;

	private Pins pins = Pins.DUMMY;
	
	protected byte
		absoluteRotationHibyte,
		absoluteRotationLobyte,
		rotationOrderHibyte,
		rotationOrderLobyte,
		moveOrderValue,
		flags,
		noise,
		detectionSegment,
		detectionGradient;
	
	@Override
	protected int[] createAddressIndexes()
	{
		return PinUtil.createSequenceIndexes(Pin.ADDRESS_START, Pin.ADDRESS_LEN);
	}
	
	@Override
	protected int[] createDataIndexes()
	{
		return PinUtil.createSequenceIndexes(Pin.DATA_START, Pin.DATA_LEN);
	}
	
	@Override
	public String getName()
	{
		return "MEXTIO Exterminator IO Chip";
	}
	
	@Override
	public int getPinsCount()
	{
		/*
		 * 0-7 data bus
		 * 8-11 address bus
		 * 12 read / write
		 * 13 chip select
		 */
		return 15;
	}

	@Override
	public void setBusConnection(Pins pins)
	{
		this.pins = pins;
	}
	
	public void setAbsoluteRotation(short rotation)
	{
		setAbsoluteRotation((byte)(rotation >>> 8), (byte)rotation);
	}
	
	public void setAbsoluteRotation(byte hibyte, byte lobyte)
	{
		this.absoluteRotationHibyte = hibyte;
		this.absoluteRotationLobyte = lobyte;
	}
	
	public void travelled(int units)
	{
		int value = Byte.toUnsignedInt(moveOrderValue);
		
		value -= units;
		
		if (value < 0)
		{
			value = 0;
		}
	}
	
	public void rotated(int units)
	{
		rotationOrderLobyte = (byte)units;
		rotationOrderHibyte = (byte)(units >>> 8);
	}
	
	public int getMoveOrderValue()
	{
		return Byte.toUnsignedInt(moveOrderValue);
	}
	
	public int getRotationOrderValue()
	{
		return Short.toUnsignedInt((short)(rotationOrderHibyte << 8 | rotationOrderLobyte));
	}
	
	public void setNoise(byte noise)
	{
		this.noise = noise;
	}
	
	public void setDetectionSegment(byte detectionSegment)
	{
		this.detectionSegment = detectionSegment;
	}
	
	public void setDetectionGradient(byte detectionGradient)
	{
		this.detectionGradient = detectionGradient;
	}
	
	@Override
	protected Mode getMode()
	{
		return pins.readPin(Pin.READWRITE) ? Mode.WRITE : Mode.READ;
	}
	
	@Override
	protected boolean isSelected()
	{
		return pins.readPin(Pin.CHIP_SELECT);
	}
	
	public boolean areSet(int flags)
	{
		return (this.flags & flags) != 0;
	}
	
	public void setFlags(int flags, boolean value)
	{
		if (value)
		{
			this.flags |= flags;
		}
		else
		{
			this.flags &= ~flags;
		}
	}
	
	@Override
	protected byte readFromChip(int address)
	{
		switch (address)
		{
			case Address.ABS_ROTATION_HI:
				return absoluteRotationHibyte;
			case Address.ABS_ROTATION_LO:
				return absoluteRotationLobyte;
			case Address.MOVE_ORDER_VALUE:
				return moveOrderValue;
			case Address.ROTATION_ORDER_HI:
				return rotationOrderHibyte;
			case Address.ROTATION_ORDER_LO:
				return rotationOrderLobyte;
			case Address.DETECTION_SEGMENT:
				return detectionSegment;
			case Address.DETECTION_GRADIENT:
				return detectionGradient;
			case Address.FLAGS:
				return flags;
			case Address.NOISE:
				return noise;
			default:
				return 0;
		}
	}
	
	@Override
	protected void writeToChip(int address, byte value)
	{
		switch (address)
		{
			case Address.ROTATION_ORDER_HI:
				this.rotationOrderHibyte = value;
				break;
			case Address.ROTATION_ORDER_LO:
				this.rotationOrderLobyte = value;
				break;
			case Address.MOVE_ORDER_VALUE:
				this.moveOrderValue = value;
				break;
			case Address.SET_FLAGS:
				flags |= value & flagsWriteMask;
				break;
		}
	}
}
