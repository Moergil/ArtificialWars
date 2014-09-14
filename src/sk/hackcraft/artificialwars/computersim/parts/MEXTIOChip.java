package sk.hackcraft.artificialwars.computersim.parts;

import java.util.Random;

import sk.hackcraft.artificialwars.computersim.Device;
import sk.hackcraft.artificialwars.computersim.Part;
import sk.hackcraft.artificialwars.computersim.PinUtil;
import sk.hackcraft.artificialwars.computersim.Pins;

/**
 * Data 0-7
 * Address 8-11
 * Read 12
 * Write 13
 * Chip select 14
 */
public class MEXTIOChip implements Device
{
	private interface Pin
	{
		public static final int
			DATA_START = 0,
			DATA_LEN = 8,
			ADDRESS_START = DATA_START + DATA_LEN,
			ADDRESS_LEN = 4,
			READ = ADDRESS_START + ADDRESS_LEN,
			WRITE = READ + 1,
			CHIP_SELECT = WRITE + 1;
	}
	
	private interface Address
	{
		public static final int
			POS_X = 0,
			POS_Y = 1,
			MOVE_TARGET_X = 2,
			MOVE_TARGET_Y = 3,
			FLAGS = 4,
			SET_FLAGS = 5,
			UNSET_FLAGS = 6,
			NOISE = 7,
			ENEMY_X = 8,
			ENEMY_Y = 9;
	}
	
	public interface Flag
	{
		public static final int
			MOVE = 1,	// if true, movement is active
			SHOT = 2,	// if possible, robot will shot to enemy position. Cleared after shooting
			LOCK = 4,	// if set, enemy position will be loaded to enemy registers, cleared after load
			MOVING = 8,		// true if moving
			GUN_READY = 16;	// true if gun can fire
	}
	
	private Random random;
	private Pins pins = Pins.DUMMY;
	
	private final boolean dataBits[] = new boolean[Pin.DATA_LEN];
	private final boolean addressBits[] = new boolean[Pin.ADDRESS_LEN];
	
	private byte posX, posY, targetX, targetY, flags, noise, enemyX, enemyY;
	
	private boolean positionChanged;
	
	public MEXTIOChip(long seed)
	{
		this.random = new Random(seed);
	}
	
	@Override
	public String getName()
	{
		return "MEXTIO Exterminator IO Chip";
	}
	
	public byte getPosX()
	{
		return posX;
	}
	
	public byte getPosY()
	{
		return posY;
	}
	
	public void setPosX(byte posX)
	{
		positionChanged |= (this.posX != posX);
		this.posX = posX;
	}
	
	public void setPosY(byte posY)
	{
		positionChanged |= (this.posY != posY);
		this.posY = posY;
	}
	
	public byte getTargetX()
	{
		return targetX;
	}
	
	public byte getTargetY()
	{
		return targetY;
	}
	
	public void setTargetX(byte targetX)
	{
		this.targetX = targetX;
	}
	
	public void setTargetY(byte targetY)
	{
		this.targetY = targetY;
	}
	
	public byte getEnemyX()
	{
		return enemyX;
	}
	
	public byte getEnemyY()
	{
		return enemyY;
	}
	
	public void setEnemyX(byte enemyX)
	{
		this.enemyX = enemyX;
	}
	
	public void setEnemyY(byte enemyY)
	{
		this.enemyY = enemyY;
	}
	
	@Override
	public int getPinsCount()
	{
		/*
		 * 0-7 data bus
		 * 8-11 address bus
		 * 12 read
		 * 13 write
		 * 14 chip select
		 */
		return 15;
	}

	@Override
	public void setBusConnection(Pins pins)
	{
		this.pins = pins;
	}
	
	@Override
	public void update()
	{
		setFlag(Flag.MOVING, positionChanged);
		positionChanged = false;
		
		noise = (byte)(random.nextInt() % 256);
		
		if (!pins.readPin(Pin.CHIP_SELECT))
		{
			pins.setAllPins(false);
			return;
		}
		
		boolean write = pins.readPin(Pin.WRITE);
		boolean read = pins.readPin(Pin.READ);
		
		byte value = 0;
		int address = readAddressBus();
		
		if (write)
		{
			value |= readDataBus();
		}
		
		if (read)
		{
			value |= readRegister(address);
		}
		
		if (write)
		{
			writeRegister(address, value);
		}
		
		if (read)
		{
			writeToDataBus(value);
		}
		else
		{
			pins.setAllPins(false);
		}
	}
	
	public boolean isSet(int flag)
	{
		return (flags & flag) != 0;
	}
	
	public void setFlag(int flag, boolean value)
	{
		if (value)
		{
			flags |= flag;
		}
		else
		{
			flags &= ~flag;
		}
	}
	
	private byte readRegister(int address)
	{
		switch (address)
		{
			case Address.POS_X:
				return posX;
			case Address.POS_Y:
				return posY;
			case Address.MOVE_TARGET_X:
				return targetX;
			case Address.MOVE_TARGET_Y:
				return targetY;
			case Address.FLAGS:
				return flags;
			case Address.NOISE:
				return noise;
			case Address.ENEMY_X:
				return enemyX;
			case Address.ENEMY_Y:
				return enemyY;
			default:
				return 0;
		}
	}
	
	private void writeRegister(int address, byte value)
	{
		switch (address)
		{
			case Address.MOVE_TARGET_X:
				targetX = value;
				break;
			case Address.MOVE_TARGET_Y:
				targetY = value;
				break;
			case Address.FLAGS:
				flags = value;
				break;
			case Address.SET_FLAGS:
				flags |= value;
				break;
			case Address.UNSET_FLAGS:
				flags &= ~value;
				break;
			case Address.ENEMY_X:
				enemyX = value;
			case Address.ENEMY_Y:
				enemyY = value;
		}
	}
	
	private void writeToDataBus(byte value)
	{
		PinUtil.codeValue(value, dataBits);
		pins.setPins(dataBits, Pin.DATA_START, Pin.DATA_LEN);
	}
	
	private byte readDataBus()
	{
		pins.readPins(dataBits, Pin.DATA_START, Pin.DATA_LEN);
		return (byte)PinUtil.decodeValue(dataBits);
	}
	
	private int readAddressBus()
	{
		pins.readPins(addressBits, Pin.ADDRESS_START, Pin.ADDRESS_LEN);
		return (int)PinUtil.decodeValue(addressBits);
	}
}
