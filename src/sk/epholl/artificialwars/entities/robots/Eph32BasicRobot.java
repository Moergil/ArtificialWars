package sk.epholl.artificialwars.entities.robots;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Explosion;
import sk.epholl.artificialwars.entities.Projectile;
import sk.epholl.artificialwars.entities.instructionsets.EPH32DirectionVector;
import sk.epholl.artificialwars.logic.Simulation;
import sk.epholl.artificialwars.logic.Vector2D;
import sk.hackcraft.artificialwars.computersim.debug.CommonValueFormatter;
import sk.hackcraft.artificialwars.computersim.parts.EPH32InstructionSet;

/**
 * @author epholl
 */
//TODO treba abstraktnu triedu Robot, ktora by reprezentovala entitu typu robot
public class Eph32BasicRobot extends Robot
{
	public static final int DEFAULT_HITPOINTS = 5;
	public static final int DEFAULT_INSTRUCTION_COOLDOWN_MULTIPLICATOR = 2;
	
	private final Random random;

	private int hitpoints;

	private int regA, regB;
	
	private final int LOCK_OFFSET, LOCK_ACCURACY_TIME;
	private int lockX, lockY, lockTime;
	private boolean lockSuccess;

	private int[] instructionMemory;
	private int[] parameterMemory;
	private int[] memory;
	private int memoryPointer;
	private int instructionPointer;
	private int instructionCooldown;
	
	private final double MOVE_SPEED = 1, ROTATION_SPEED = Math.PI / 64;

	/*
	 * TODO
	 * Takze, potrebujeme:
	 * Move instrukcia, ktora zapne a vypne pohyb.
	 * 
	 * Rotate instrukcia: nejako nastavit rotaciu. Smer, bud instantne, alebo postupne.
	 */
	
	public Eph32BasicRobot(Simulation game)
	{
		super(game);
		
		this.random = new Random(game.getSeed());

		setWidth(6);
		setHeight(6);

		hitpoints = DEFAULT_HITPOINTS;

		instructionMemory = new int[64];
		parameterMemory = new int[64];

		memory = new int[4];

		LOCK_OFFSET = 30;
		LOCK_ACCURACY_TIME = 250;
		lockTime = 0;
	}
	
	@Override
	public int getRobotTypeId()
	{
		return StockRobotsId.Eph32BasicRobot;
	}
	
	@Override
	public void update(Set<Entity> nearbyEntities)
	{
		super.update(nearbyEntities);
		
		if (hitpoints <= 0)
		{
			destroy();
		}
	}

	@Override
	public void act()
	{
		if (!instructionCooldownDone())
		{
			return;
		}
		
		if (instructionPointer >= instructionMemory.length)
		{
			return;
		}

		try
		{
			instructionCycleSwitch();
		}
		catch (NullPointerException e)
		{
			selfDestruct();
		}
		catch (IndexOutOfBoundsException e)
		{
			selfDestruct();
		}

		incrementInstructionPointer();

		nextInstructionCooldownSwitch();
	}

	private boolean instructionCooldownDone()
	{
		if (instructionCooldown > 0)
		{
			instructionCooldown--;
			return false;
		}
		else
		{
			return true;
		}
	}

	private void instructionCycleSwitch()
	{
		int instruction = instructionMemory[instructionPointer];
		int parameter = parameterMemory[instructionPointer];

		switch (instruction)
		{
			case EPH32InstructionSet.SELF_DESTRUCT:
			{
				// self destruct
				selfDestruct();
				break;
			}
			case EPH32InstructionSet.WAIT: // 10*P wait
				break;
				
			case EPH32InstructionSet.ADD:
			{
				// A = A + B
				regA += regB;
				break;
			}
			case EPH32InstructionSet.SUB:
			{
				// A = A - B
				regA -= regB;
				break;
			}
			case EPH32InstructionSet.INC:
			{
				// A = A++
				regA++;
				break;
			}
			case EPH32InstructionSet.DEC:
			{
				// A = A--
				regA--;
				break;
			}

			case EPH32InstructionSet.SWP:
			{
				// A = B, B = A
				int temp = regA;
				regA = regB;
				regB = temp;
				break;
			}
			case EPH32InstructionSet.SETAB:
			{
				// A = B
				regA = regB;
				break;
			}
			case EPH32InstructionSet.SETA:
			{
				// A = P
				regA = parameter;
				break;
			}
			case EPH32InstructionSet.SETB:
			{
				// B = P
				regB = parameter;
				break;
			}

			case EPH32InstructionSet.FIRE:
			{
				// fire a shot to [A, B]
				fire();
				break;
			}
			case EPH32InstructionSet.SCAN:
			{
				// find a robot to lock (if none found, null returned)
				setLock();
				break;
			}
			case EPH32InstructionSet.LOCK:
			{
				// set A and B to actual accuracy lock coordinates
				setLockToRegisters();
				break;
			}

			case EPH32InstructionSet.RND:
			{
				// A = random int in <-P, P>
				regA = random.nextInt((parameter * 2) + 1) - parameter;
				break;
			}
			case EPH32InstructionSet.RNDB:
			{
				// A = random int in <-B, B>
				regA = random.nextInt((regB * 2) + 1) - regB;
				break;
			}

			case EPH32InstructionSet.POSX:
			{
				// A = current pos X
				regA = (int)getCenterPosition().getX();
				break;
			}
			case EPH32InstructionSet.POSY:
			{
				// A = current pos Y
				regA = (int)getCenterPosition().getY();
				break;
			}
			case EPH32InstructionSet.MOVE:
			{
				if (parameter < -1 || parameter > 2)
					selfDestruct();
				else
					setMoveSpeed(parameter / 2.0);
				break;
			}
			case EPH32InstructionSet.ROT:
			{
				if (regA < 1 || regA > 12)
					selfDestruct();
				else
					setRotation(regA);
				break;
			}
			case EPH32InstructionSet.SETMP:
			{
				// set A to MP
				memoryPointer = regA;
				break;
			}
			case EPH32InstructionSet.INCMP:
			{
				// MP++
				memoryPointer++;
				break;
			}
			case EPH32InstructionSet.DECMP:
			{
				// MP--
				memoryPointer--;
				break;
			}
			case EPH32InstructionSet.MEMSAVE:
			{
				// [MP] = A
				memory[memoryPointer] = regA;
				break;
			}
			case EPH32InstructionSet.MEMLOAD:
			{
				// A = [MP]
				regA = memory[memoryPointer];
				break;
			}

			case EPH32InstructionSet.JMP:
			{
				// IP = P
				instructionPointer = parameter;
				decrementInstructionPointer();
				break;
			}
			case EPH32InstructionSet.JMPZ:
			{
				// if (A == 0) IP = P
				if (regA == 0)
				{
					instructionPointer = parameter;
					decrementInstructionPointer();
				}
				break;
			}
			case EPH32InstructionSet.JMPC:
			{
				// if (isCollided()) IP = P
				if (isColliding())
				{
					instructionPointer = parameter;
					decrementInstructionPointer();
				}
				break;
			}
			case EPH32InstructionSet.JMPM:
			{
				// if (isMoving()) IP = P
				if (isMoving())
				{
					instructionPointer = parameter;
					decrementInstructionPointer();
				}
				break;
			}
			case EPH32InstructionSet.JMPL:
			{
				// if something was locked IP = P
				if (lockSuccess)
				{
					instructionPointer = parameter;
					decrementInstructionPointer();
				}
				break;
			}
			default:
			{
				// default instruction operations
			}
		}
	}

	private void nextInstructionCooldownSwitch()
	{
		if (instructionPointer >= instructionMemory.length)
		{
			selfDestruct();
			return;
		}
		
		switch (instructionMemory[instructionPointer])
		{
			case EPH32InstructionSet.SELF_DESTRUCT:
			{
				instructionCooldown = 10;
				break;
			}
			case EPH32InstructionSet.WAIT:
			{
				instructionCooldown = parameterMemory[instructionPointer];
				break;
			}
			case EPH32InstructionSet.FIRE:
			{
				instructionCooldown = 5;
				break;
			}
			case EPH32InstructionSet.SCAN:
			{
				instructionCooldown = 10;
				break;
			}
			case EPH32InstructionSet.LOCK:
			{
				instructionCooldown = 5;
				break;
			}
			case EPH32InstructionSet.MOVE:
			{
				instructionCooldown = 7;
				break;
			}
			default:
			{
				instructionCooldown = 0;
			}
		}

		instructionCooldown *= DEFAULT_INSTRUCTION_COOLDOWN_MULTIPLICATOR;
		if (isMoving())
			instructionCooldown *= 4;
	}

	public boolean setInstructionPointer(int newVal)
	{
		if (newVal >= 0 && newVal < instructionMemory.length)
		{
			instructionPointer = newVal;
			return true;
		}
		return false;
	}

	public void setInstruction(int instruction, int parameter)
	{
		instructionMemory[instructionPointer] = instruction;
		parameterMemory[instructionPointer] = parameter;
	}

	public void programInstruction(int pointer, int instruction, int parameter)
	{
		instructionMemory[pointer] = instruction;
		parameterMemory[pointer] = parameter;
	}
	
	@Override
	public void setFirmware(byte[] firmware) throws IOException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(firmware);
		DataInput input = new DataInputStream(bais);
		
		int programSize = input.readInt() / Integer.BYTES / 2;
		int dataSize = input.readInt() / Integer.BYTES;
		
		for (int i = 0; i < programSize; i++)
		{
			int instruction = input.readInt();
			int parameter = input.readInt();
			
			instructionMemory[i] = instruction;
			parameterMemory[i] = parameter;
		}
		
		for (int i = 0; i < dataSize; i++)
		{
			memory[i] = input.readInt();
		}
	}
	
	private void setRotation(int directionOClock)
	{
		Vector2D clockVector = EPH32DirectionVector.getClockDirection(directionOClock).getVector();
		setDirection(clockVector);
	}

	@Override
	public void beHit(Projectile shot)
	{
		super.beHit(shot);
		this.hitpoints -= shot.getDamage();
		
		Explosion explosion = Explosion.create(game, getCenterPosition());
		game.addEntity(explosion);
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
	public void destroy()
	{
		super.destroy();		

		Explosion explosion = Explosion.create(game, getCenterPosition());
		game.addEntity(explosion);
	}

	@Override
	public int getPlayer()
	{
		return player;
	}

	public void fire()
	{
		Projectile shot = new Projectile(game, this);
		
		shot.setCenterPosition(getCenterPosition());
		shot.setDirection(new Vector2D(regA, regB).sub(getCenterPosition()));
		
		game.addEntity(shot);
	}

	public void selfDestruct()
	{
		hitpoints = -1;
	}
	
	public String getRegistersString()
	{
		String a, b, mp, ip, ic;
		
		a = CommonValueFormatter.toDecimal5(regA);
		b = CommonValueFormatter.toDecimal5(regB);
		mp = CommonValueFormatter.toDecimal5(memoryPointer);
		ip = CommonValueFormatter.toDecimal5(instructionPointer);
		ic = CommonValueFormatter.toDecimal5(instructionCooldown);
		
		return String.format("A:%s B:%s MP:%s IP:%s IC:%s", a, b, mp, ip, ic);
	}

	public String getActualLine()
	{
		String line = CommonValueFormatter.toDecimal5(instructionPointer);
		String instructionName = EPH32InstructionSet.getInstance().getOpcode(instructionMemory[instructionPointer]).getInstructionName();
		String param = CommonValueFormatter.toDecimal5(parameterMemory[instructionPointer]);
		
		return String.format("Line %s %5s %s", line, instructionName, param);
	}

	private void incrementInstructionPointer()
	{
		if (++instructionPointer >= instructionMemory.length)
			instructionPointer--;
	}

	private void decrementInstructionPointer()
	{
		instructionPointer--;
		if (instructionPointer < 0)
		{
			instructionPointer = -1;
		}
	}

	private void setLock()
	{
		List<Entity> targets = new ArrayList<>();

		for (Entity e : game.getEntities())
		{
			if (e == this || e.getPlayer() == getPlayer())
			{
				continue;
			}
			
			if (e.isDestructible())
			{
				targets.add(e);
			}
		}

		if (targets.isEmpty())
		{
			lockSuccess = false;
			lockX = regA;
			lockY = regB;
		}
		else
		{
			lockSuccess = true;
			lockTime = game.getCycleCount();
			
			Entity target = targets.get(random.nextInt(targets.size()));
			Vector2D position = target.getCenterPosition();
			
			lockX = (int)position.getX();
			lockY = (int)position.getY();
		}
	}
	
	private void setLockToRegisters()
	{
		int elapsedCycles = game.getCycleCount() - lockTime;

		int offsetX = 0, offsetY = 0;
		if (elapsedCycles < LOCK_ACCURACY_TIME)
		{
			double multiplier = 1 - (1.0 / LOCK_ACCURACY_TIME) * elapsedCycles;

			int offset = (int)(LOCK_OFFSET * multiplier);
			
			offsetX = random.nextInt(offset * 2) - offset;
			offsetY = random.nextInt(offset * 2) - offset;
		}
		
		regA = lockX + offsetX;
		regB = lockY + offsetY;
	}
}
