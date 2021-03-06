package sk.epholl.artificialwars.entities.robots;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import sk.epholl.artificialwars.entities.Doodad;
import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Projectile;
import sk.epholl.artificialwars.entities.instructionsets.EPH32InstructionSet;
import sk.epholl.artificialwars.logic.GameLogic;
import sk.hackcraft.artificialwars.computersim.debug.CommonValueFormatter;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet;

/**
 * @author epholl
 */
//TODO treba abstraktnu triedu Robot, ktora by reprezentovala entitu typu robot
public class Eph32BasicRobot extends Entity
{
	public static final int DEFAULT_HITPOINTS = 5;
	public static final int DEFAULT_INSTRUCTION_COOLDOWN_MULTIPLICATOR = 2;
	public static final Random random = new Random();

	private int player;

	private int hitpoints;

	private int regA;
	private int regB;

	private int[] instructionMemory;
	private int[] parameterMemory;
	private int[] memory;
	private int memoryPointer;
	private int instructionPointer;
	private int instructionCooldown;

	private InstructionSet instructionSet;

	private Entity aimLock;

	public Eph32BasicRobot(Color color, int player, int posX, int posY, GameLogic game)
	{
		super(posX, posY, game);
		this.color = color;
		this.player = player;

		this.sizeX = 6;
		this.sizeY = 6;

		hitpoints = DEFAULT_HITPOINTS;

		instructionMemory = new int[64];
		parameterMemory = new int[64];

		memory = new int[4];

		instructionSet = EPH32InstructionSet.getInstance();
		aimLock = null;
	}

	@Override
	public void turn()
	{
		refreshLocksState();

		if (!instructionCooldownDone())
			return;
		
		if (instructionPointer >= instructionMemory.length)
			return;

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

		incrementInsturcionPointer();

		nextInstructionCooldownSwitch();
	}

	public InstructionSet getInstructionSet()
	{
		return instructionSet;
	}

	private void refreshLocksState()
	{
		if (aimLock != null && aimLock.isEnded())
			aimLock = null;
	}

	private boolean instructionCooldownDone()
	{
		if (instructionCooldown != 0)
		{
			instructionCooldown--;
			return false;
		}
		return true;
	}

	private void instructionCycleSwitch()
	{
		int instruction = instructionMemory[instructionPointer];
		int parameter = parameterMemory[instructionPointer];

		switch (instruction)
		{
			case -1:
			{ // self destruct
				selfDestruct();
				break;
			}
			case 0: // 10*P wait
				break;
			case 1:
			{ // A = A + B
				regA += regB;
				break;
			}
			case 2:
			{ // A = A - B
				regA -= regB;
				break;
			}
			case 3:
			{ // A = A++
				regA++;
				break;
			}
			case 4:
			{ // A = A--
				regA--;
				break;
			}

			case 5:
			{ // A = B, B = A
				int temp = regA;
				regA = regB;
				regB = temp;
				break;
			}
			case 6:
			{ // A = B
				regA = regB;
				break;
			}
			case 8:
			{ // A = P
				regA = parameter;
				break;
			}
			case 9:
			{ // B = P
				regB = parameter;
				break;
			}

			case 10:
			{ // fire a shot to [A, B]
				fire();
				break;
			}
			case 15:
			{ // find a robot to lock (if none found, null returned)
				findEnemyRobot();
				break;
			}
			case 16:
			{ // set A and B to coords of aimLock (posX and posY if null)
				if (aimLock == null)
				{
					regA = getPosX();
					regB = getPosY();
				}
				else
				{
					regA = aimLock.getPosX();
					regB = aimLock.getPosY();
				}
				break;
			}

			case 20:
			{ // A = random int in <-P, P>
				regA = random.nextInt((parameter * 2) + 1) - parameter;
				break;
			}
			case 21:
			{ // A = random int in <-B, B>
				regA = random.nextInt((regB * 2) + 1) - regB;
				break;
			}

			case 30:
			{ // A = current pos X
				regA = getPosX();
				break;
			}
			case 31:
			{ // A = current pos Y
				regA = getPosY();
				break;
			}
			case 32:
			{ // set movement to [A, B]
				setDestination(regA, regB);
				break;
			}

			case 40:
			{ // set A to MP
				memoryPointer = regA;
				break;
			}
			case 41:
			{ // MP++
				memoryPointer++;
				break;
			}
			case 42:
			{ // MP--
				memoryPointer--;
				break;
			}
			case 45:
			{ // [MP] = A
				memory[memoryPointer] = regA;
				break;
			}
			case 46:
			{ // A = [MP]
				regA = memory[memoryPointer];
				break;
			}

			case 50:
			{ // IP = P
				instructionPointer = parameter;
				decrementInstructionPointer();
				break;
			}
			case 51:
			{ // if (A == 0) IP = P
				if (regA == 0)
				{
					instructionPointer = parameter;
					decrementInstructionPointer();
				}
				break;
			}
			case 52:
			{ // if (isCollided()) IP = P
				if (isCollided())
				{
					instructionPointer = parameter;
					decrementInstructionPointer();
				}
				break;
			}
			case 53:
			{ // if (isMoving()) IP = P
				if (isMoving())
				{
					instructionPointer = parameter;
					decrementInstructionPointer();
				}
				break;
			}
			case 54:
			{ // if (aimLock != null) IP = P
				if (aimLock != null)
				{
					instructionPointer = parameter;
					decrementInstructionPointer();
				}
				break;
			}
			default:
			{ // default instruction operation

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
			case -1:
			{
				instructionCooldown = 10;
				break;
			}
			case 0:
			{
				instructionCooldown = 2 + parameterMemory[instructionPointer] * 10;
				break;
			}
			case 10:
			{
				instructionCooldown = 5;
				break;
			}
			case 15:
			{
				instructionCooldown = 10;
				break;
			}
			case 16:
			{
				instructionCooldown = 5;
				break;
			}
			case 32:
			{
				instructionCooldown = 7;
				break;
			}
			default:
			{
				instructionCooldown = 1;
			}
		}

		instructionCooldown *= DEFAULT_INSTRUCTION_COOLDOWN_MULTIPLICATOR;
		if (isMoving())
			instructionCooldown *= 9;
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
	
	public void loadFirmware(byte[] firmware, int offset) throws IOException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(firmware);
		DataInput input = new DataInputStream(bais);
		
		int programSize = input.readInt() / Integer.BYTES / 2;
		int dataSize = input.readInt() / Integer.BYTES;
		
		for (int i = 0; i < programSize; i++)
		{
			int instruction = input.readInt();
			int parameter = input.readInt();
			
			instructionMemory[i + offset] = instruction;
			parameterMemory[i + offset] = parameter;
		}
		
		for (int i = 0; i < dataSize; i++)
		{
			memory[i] = input.readInt();
		}
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
	public boolean movesInfinitely()
	{
		return false;
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
	public boolean isSolid()
	{
		return true;
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
	public int getPlayer()
	{
		return player;
	}

	public void fire()
	{
		Projectile shot = new Projectile(getPosX(), getPosY(), regA, regB, game);
		game.addEntity(shot);
	}

	public void selfDestruct()
	{
		for (int i = 0; i < 15; i++)
		{
			regA = getPosX() + random.nextInt(11) - 5;
			regB = getPosY() + random.nextInt(11) - 5;
			fire();
		}
		hitpoints = -1;
	}
	
	public String getRegistersString()
	{
		String a, b, mp, ip, ic;
		
		a = CommonValueFormatter.toDecimal4(regA);
		b = CommonValueFormatter.toDecimal4(regB);
		mp = CommonValueFormatter.toDecimal4(memoryPointer);
		ip = CommonValueFormatter.toDecimal4(instructionPointer);
		ic = CommonValueFormatter.toDecimal4(instructionCooldown);
		
		return String.format("A:%s B:%s MP:%s IP:%s IC:%s", a, b, mp, ip, ic);
	}

	public String getActualLine()
	{
		String line = CommonValueFormatter.toDecimal4(instructionPointer);
		String instructionName = EPH32InstructionSet.getInstance().getOpcode(instructionMemory[instructionPointer]).getInstructionName();
		String param = CommonValueFormatter.toDecimal4(parameterMemory[instructionPointer]);
		
		return String.format("Line %s %5s %s", line, instructionName, param);
	}

	private void incrementInsturcionPointer()
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

	private void findEnemyRobot()
	{
		ArrayList<Eph32BasicRobot> targets = new ArrayList<Eph32BasicRobot>();

		for (Entity e : game.getEntities())
		{
			if (e instanceof Eph32BasicRobot)
			{
				Eph32BasicRobot r = (Eph32BasicRobot) e;
				if (r.getPlayer() != getPlayer())
				{
					targets.add(r);
				}
			}
		}

		if (targets.isEmpty())
		{
			aimLock = null;
		}
		else
		{
			aimLock = targets.get(random.nextInt(targets.size()));
		}
	}
}
