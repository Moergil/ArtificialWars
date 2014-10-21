package sk.epholl.artificialwars.logic;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import sk.epholl.artificialwars.entities.Area;
import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Obstacle;
import sk.epholl.artificialwars.entities.objectives.CaptureLocationObjective;
import sk.epholl.artificialwars.entities.objectives.DestroyEnemyObjective;
import sk.epholl.artificialwars.entities.objectives.Objective;
import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.entities.robots.FirmwareCompiler;
import sk.epholl.artificialwars.entities.robots.RobotTWM1608;
import sk.epholl.artificialwars.graphics.CompilationErrorWindow;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProgramException;

public class LevelLoader
{
	private Simulation logic;
	
	private final Map<String, byte[]> firmwares = new HashMap<>();

	public LevelLoader(Simulation logic)
	{
		this.logic = logic;
	}

	public void loadLevel(String fileName) throws IOException, IllegalArgumentException
	{
		try (BufferedReader inputReader = new BufferedReader(new FileReader(fileName)))
		{
			String line;
	
			while ((line = inputReader.readLine()) != null)
			{
				if (line.equals("") || line.startsWith("//"))
				{
					continue;
				}
				
				try (Scanner scanner = new Scanner(line))
				{
					String key = scanner.next();
	
					switch (key)
					{
						case "objective":
							parseObjective(scanner);
							break;
						case "obstacle":
							parseObstacle(scanner);
							break;
						case "robot":
							parseRobot(scanner);
							break;
						default:
							throw new IllegalArgumentException("Unknown entity: " + key);
					}
				}
			}
		}
	}

	private void parseRobot(Scanner scanner) throws IOException
	{		
		int red = scanner.nextInt();
		int green = scanner.nextInt();
		int blue = scanner.nextInt();
		
		int player = scanner.nextInt();
		
		int x = scanner.nextInt();
		int y = scanner.nextInt();

		Color color = new Color(red, green, blue);
		
		String type = scanner.next();
		String firmwareFileName = scanner.next();
		
		try
		{
			switch (type)
			{
				case "eph32":
				{
					Eph32BasicRobot robot = new Eph32BasicRobot(color, player, logic);
					robot.setCenterPosition(x, y);
					
					robot.setFirmware(getFirmware(type, firmwareFileName));
					
					logic.addEntity(robot);
					break;
				}
				case "twm1608":
				{
					RobotTWM1608 robot = new RobotTWM1608(color, player, logic, logic.getSeed());
					robot.setCenterPosition(x, y);
					
					robot.setFirmware(getFirmware(type, firmwareFileName));
					
					logic.addEntity(robot);
					break;
				}
				default:
					throw new IllegalArgumentException("Invalid robot type: " + type);
			}
		}
		catch (ProgramException e)
		{
			new CompilationErrorWindow().show(e);
			
			throw new IOException("Can't load robot: " + type + " " + firmwareFileName);
		}
	}
	
	private byte[] getFirmware(String robotType, String firmwareName) throws ProgramException
	{
		String key = robotType + ":" + firmwareName;
		
		byte firmware[] = firmwares.get(key);
		
		if (firmware == null)
		{
			try
			{
				firmware = FirmwareCompiler.compileFirmware(robotType, firmwareName);
			}
			catch (IOException e)
			{
				throw new ProgramException(e.getMessage());
			}
			
			firmwares.put(key, firmware);
		}
		
		return firmware;
	}

	private void parseObstacle(Scanner scanner)
	{
		int x = scanner.nextInt();
		int y = scanner.nextInt();

		int width = scanner.nextInt();
		int height = scanner.nextInt();
		
		Obstacle obstacle = new Obstacle(x, y, width, height, logic);
		
		logic.addEntity(obstacle);
	}

	private void parseObjective(Scanner scanner)
	{
		String type = scanner.next();
		
		switch (type)
		{
			case "capture":
			{
				int x = scanner.nextInt();
				int y = scanner.nextInt();
				int w = scanner.nextInt();
				int h = scanner.nextInt();
				
				int player = scanner.nextInt();
				int unitsAmount = scanner.nextInt();
				
				Area captureArea = new Area(logic, w, h);
				captureArea.setCenterPosition(x, y);
				logic.addEntity(captureArea);
				
				CaptureLocationObjective objective = new CaptureLocationObjective(logic, player, unitsAmount, captureArea);
				logic.addObjective(objective);
				
				break;
			}
			case "destroy":
			{
				int player = scanner.nextInt();
				
				Objective objective = new DestroyEnemyObjective(player);
				logic.addObjective(objective);
				
				break;
			}
			default:
				throw new IllegalArgumentException("Invalid objective type: " + type);
		}
	}
}
