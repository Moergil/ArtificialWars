package sk.epholl.artificialwars.logic;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Obstacle;
import sk.epholl.artificialwars.entities.objectives.CaptureLocationObjective;
import sk.epholl.artificialwars.entities.objectives.DestroyEnemyObjective;
import sk.epholl.artificialwars.entities.objectives.Objective;
import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.entities.robots.FirmwareLoader;
import sk.epholl.artificialwars.entities.robots.RobotTWM1608;
import sk.epholl.artificialwars.entities.robots.FirmwareLoader.ProgrammingException;

public class LevelLoader
{
	private GameLogic logic;
	private BufferedReader inputReader;

	private boolean isDone;

	public LevelLoader(GameLogic logic, String fileName)
	{
		this.logic = logic;
		isDone = !setLevelFile(fileName);
	}

	public final boolean setLevelFile(String fileName)
	{
		try
		{
			inputReader = new BufferedReader(new FileReader(fileName));
		}
		catch (FileNotFoundException e)
		{
			return false;
		}

		return true;
	}

	public void loadLevel() throws IOException, IllegalArgumentException
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

	private void parseRobot(Scanner scanner)
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
					
					FirmwareLoader.loadFirmwareRobot(firmwareFileName, robot);
					logic.addEntity(robot);
					break;
				}
				case "twm1608":
				{
					RobotTWM1608 robot = new RobotTWM1608(color, player, logic, logic.getSeed());
					robot.setCenterPosition(x, y);
					
					FirmwareLoader.loadFirmwareExterminator(firmwareFileName, robot);
					logic.addEntity(robot);
					break;
				}
				default:
					throw new IllegalArgumentException("Invalid robot type: " + type);
			}
		}
		catch (ProgrammingException e)
		{
			e.printStackTrace();
		}
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
				
				Objective objective = new CaptureLocationObjective(logic, player, unitsAmount);
				objective.setCenterPosition(x, y);
				objective.setWidth(w);
				objective.setHeight(h);
				logic.addObjective(objective);
				
				break;
			}
			case "destroy":
			{
				int player = scanner.nextInt();
				
				Objective objective = new DestroyEnemyObjective(logic, player);
				logic.addObjective(objective);
				
				break;
			}
			default:
				throw new IllegalArgumentException("Invalid objective type: " + type);
		}
	}

	public boolean isDone()
	{
		return isDone;
	}
}
