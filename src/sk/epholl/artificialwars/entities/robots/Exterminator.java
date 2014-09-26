package sk.epholl.artificialwars.entities.robots;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import sk.epholl.artificialwars.entities.Doodad;
import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Projectile;
import sk.epholl.artificialwars.logic.GameLogic;
import sk.hackcraft.artificialwars.computersim.Bus;
import sk.hackcraft.artificialwars.computersim.Computer;
import sk.hackcraft.artificialwars.computersim.TEK1608InstructionSet;
import sk.hackcraft.artificialwars.computersim.parts.BusProbe;
import sk.hackcraft.artificialwars.computersim.parts.MEXTIOChip;
import sk.hackcraft.artificialwars.computersim.parts.MemChip1024;
import sk.hackcraft.artificialwars.computersim.parts.ProbeProcessorTEK1608;
import sk.hackcraft.artificialwars.computersim.parts.ProcessorTEK1608;
import sk.hackcraft.artificialwars.computersim.parts.SegmentDisplay4b8;
import sk.hackcraft.artificialwars.computersim.parts.ProbeProcessorTEK1608.RegisterTEK1608;

public class Exterminator extends Entity
{
	public static void main(String[] args) throws Exception
	{
		Exterminator e = new Exterminator(Color.BLACK, 0, 10, 15, null, 0);
		
		FirmwareLoader.loadFirmwareExterminator("fibonacci.asm", e);
		
		for (int i = 0; i < 1000; i++)
		{
			e.turn();
		}
		
		System.out.println("end.");
	}
	
	private final Random random;
	private int player;
	private int hitpoints = 5;
	private final int GUN_COOLDOWN_TIME = 30;
	private int actualGunCooldown = 0;
	
	private int computerFrequency = 1;
	
	private Computer computer;
	private MemChip1024 programMemchip;
	private MEXTIOChip ioChip;
	private SegmentDisplay4b8 display;
	private BusProbe busProbe;
	private ProbeProcessorTEK1608 procProbe;
	
	private final int PROGRAM_OFFSET = 0x0200;
	
	public Exterminator(Color color, int player, int posX, int posY, GameLogic game, long seed)
	{
		super(posX, posY, game);
		
		this.color = color;
		this.player = player;
		
		this.random = new Random(seed);
		
		int
			DATA_RANGE = 8,
			ADDRESS_RANGE = 16,
			CONTROL_RANGE = 1,
			CHIP_SELECT_RANGE = 3;

		int
		D0 = 0,
		D1 = 1,
		D2 = 2,
		D3 = 3,
		D4 = 4,
		D5 = 5,
		D6 = 6,
		D7 = 7,
		A0 = 8,
		A1 = 9,
		A2 = 10,
		A3 = 11,
		A4 = 12,
		A5 = 13,
		A6 = 14,
		A7 = 15,
		A8 = 16,
		A9 = 17,
		A10 = 18,
		A11 = 19,
		A12 = 20,
		A13 = 21,
		A14 = 22,
		A15 = 23,
		RW = 24,
		CS0 = 25,
		CS1 = 26,
		CS2 = 27;

		int busPinsCount = DATA_RANGE + ADDRESS_RANGE + CONTROL_RANGE;
		int allPinsCount = busPinsCount + CHIP_SELECT_RANGE;
		
		Bus bus = new Bus(busPinsCount);
		bus.addCircuit((b) -> !b.readBusPin(A10), CS0);
		bus.addCircuit((b) -> b.readBusPin(A10) && !b.readBusPin(A4), CS1);
		bus.addCircuit((b) -> b.readBusPin(A10) && b.readBusPin(A4), CS2);
		
		BusProbe probe = new BusProbe(allPinsCount, (builder, bits) -> {
			for (int i = bits.length - 1; i >= 0; i--)
			{
				if (i == D7)
				{
					builder.append(" D");
				}
				else if (i == A15)
				{
					builder.append(" A");
				}
				else if (i == RW)
				{
					builder.append(" RW");
				}
				else if (i == CS2)
				{
					builder.append(" CS");
				}

				builder.append(bits[i] ? "1" : "0");
			}
		});

		// data(0-7), address(0-15), readwrite, chipSelect(0-2)
		bus.connectDevice(probe, new int[]{D0, D1, D2, D3, D4, D5, D6, D7, A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, RW, CS0, CS1, CS2});
		
		ProcessorTEK1608 processor = new ProcessorTEK1608();
		
		// TODO debug
		processor.setInstructionListener((pc, opcode) -> System.out.printf("INS: %d %d %s%n", pc, opcode, TEK1608InstructionSet.getInstance().getOpcode(opcode).getInstructionName()));
		
		ProbeProcessorTEK1608 processorProbe = new ProbeProcessorTEK1608(processor);
		
		// readwrite, address(0-15), data(0-7)
		bus.connectDevice(processor, new int[]{RW, A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, D0, D1, D2, D3, D4, D5, D6, D7});
		
		MemChip1024 memory = new MemChip1024();
		
		// readwrite, chipSelect, address(0-9), data(0-7)
		bus.connectDevice(memory, new int[]{RW, CS0, A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, D0, D1, D2, D3, D4, D5, D6, D7});
		
		MEXTIOChip io = new MEXTIOChip(new Random().nextLong());
		
		// data(0-7), address(8-11), readwrite, chipSelect
		bus.connectDevice(io, new int[]{D0, D1, D2, D3, D4, D5, D6, D7, A0, A1, A2, A3, RW, CS1});
		
		SegmentDisplay4b8 display = new SegmentDisplay4b8();
		
		// readwrite, address, data(0-7), chipSelect
		bus.connectDevice(display, new int[]{RW, A0, D0, D1, D2, D3, D4, D5, D6, D7, CS2});
		
		Computer computer = new Computer(bus);
		
		computer.addPart(processor);
		computer.addPart(memory);
		computer.addPart(io);
		
		computer.addPart(display);
		computer.addPart(probe);
		
		this.computer = computer;
		this.programMemchip = memory;
		this.ioChip = io;
		this.display = display;
		this.busProbe = probe;
		this.procProbe = processorProbe;
		
		processorProbe.setValue(RegisterTEK1608.PC, PROGRAM_OFFSET);
	}
	
	public void loadFirmware(byte firmware[])
	{
		programMemchip.writeData(firmware, 0);
	}
	
	private static void printMemory(byte data[], int offset, int len)
	{
		for (int i = 0; i < len; i++)
		{
			System.out.print((int)(data[i + offset] & 0xff) + " ");
		}
		System.out.println();
	}
	
	@Override
	public void turn()
	{
		for (int i = 0; i < computerFrequency; i++)
		{
			System.out.println(busProbe);
			System.out.println(procProbe);
			printMemory(programMemchip.getMemory(), 0, 10);
			computer.tick();
		}

		// TODO
		/*ioChip.setPosX((byte)(getPosX() / 32));
		ioChip.setPosY((byte)(getPosY() / 32));
		
		if (ioChip.areSet(MEXTIOChip.Flag.MOVE))
		{
			setDestination(ioChip.getTargetX() * 32, ioChip.getTargetY() * 32);
		}
		
		if (ioChip.areSet(MEXTIOChip.Flag.LOCK))
		{
			Entity target = findTarget();
			
			ioChip.setEnemyX((byte)(target.getPosX() / 32));
			ioChip.setEnemyY((byte)(target.getPosY() / 32));
			
			ioChip.setFlags(MEXTIOChip.Flag.LOCK, false);
		}

		ioChip.setFlags(MEXTIOChip.Flag.GUN_READY, isGunReady());
		
		if (ioChip.areSet(MEXTIOChip.Flag.SHOT) && isGunReady())
		{
			int x = ioChip.getEnemyX() * 32;
			int y = ioChip.getEnemyY() * 32;
			fire(x, y);
			ioChip.setFlags(MEXTIOChip.Flag.SHOT, false);
			actualGunCooldown = GUN_COOLDOWN_TIME;
		}*/
		
		if (actualGunCooldown > 0)
		{
			actualGunCooldown--;
		}
		
		System.out.println(display);
	}
	
	private boolean isGunReady()
	{
		return actualGunCooldown == 0;
	}
	
	private void fire(int x, int y)
	{
		Projectile shot = new Projectile(getPosX(), getPosY(), x, y, game);
		game.addEntity(shot);
	}
	
	private Entity findTarget()
	{
		ArrayList<Entity> targets = new ArrayList<Entity>();

		for (Entity e : game.getEntities())
		{
			if (e == this)
			{
				continue;
			}
			
			if (e.getPlayer() == getPlayer())
			{
				continue;
			}
			
			if (e.isDestructible())
			{
				targets.add(e);
			}
		}
		
		int index = random.nextInt(targets.size());
		return targets.get(index);
	}

	@Override
	public boolean movesInfinitely()
	{
		return false;
	}

	@Override
	public boolean isDestructible()
	{
		return true;
	}

	@Override
	public boolean hasPlayer()
	{
		return true;
	}

	@Override
	public boolean isTimed()
	{
		return false;
	}

	@Override
	public boolean isCollidable()
	{
		return true;
	}

	@Override
	public boolean isSolid()
	{
		return true;
	}
	
	@Override
	public boolean aboutToRemove()
	{
		if (hitpoints >= 0)
		{
			return false;
		}
		else
		{
			destroy();
			return true;
		}
	}

	@Override
	public void destroy()
	{
		destroyed = true;
		Color doodadColor = new Color(0, 0, 0, 100);

		Doodad explosion = new Doodad((int) getPosX(), (int) getPosY(), game, doodadColor, 4, 5);
		explosion.setMoveSpeed(moveSpeed);
		explosion.setVectorX(vectorX);
		explosion.setVectorY(vectorY);
		game.addEntity(explosion);
	}
	
	@Override
	public void beHit(Projectile shot)
	{
		this.hitpoints -= shot.getDamage();
		shot.destroy();

		Color doodadColor = new Color(0, 0, 0, 100);
		Doodad explosion = new Doodad((int) getPosX(), (int) getPosY(), game, doodadColor, 3, 3);
		game.addEntity(explosion);
	}
	
	@Override
	public int getPlayer()
	{
		return player;
	}
}
