package sk.epholl.artificialwars.logic;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import sk.epholl.artificialwars.entities.Area;
import sk.epholl.artificialwars.entities.Obstacle;
import sk.epholl.artificialwars.entities.Spawn;
import sk.epholl.artificialwars.entities.robots.Robot;
import sk.epholl.artificialwars.logic.objectives.CaptureLocationObjective;
import sk.epholl.artificialwars.logic.objectives.CommandAtLeastUnitsObjective;
import sk.epholl.artificialwars.logic.objectives.DestroyEnemyObjective;
import sk.epholl.artificialwars.logic.objectives.Objective;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProgramException;

public class LevelLoader
{
	private Simulation simulation;
	
	private final RobotFactory robotFactory = new RobotFactory();

	public LevelLoader(Simulation logic)
	{
		this.simulation = logic;
	}

	public void loadLevel(String fileName) throws IOException, IllegalArgumentException
	{
		try (BufferedReader inputReader = new BufferedReader(new FileReader("levels/" + fileName)))
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
						case "spawn":
							parseSpawn(scanner);
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
		
		String robotName = scanner.next();

		try
		{
			Robot robot = robotFactory.loadRobot(simulation, robotName + ".rbt");
			
			robot.setColor(color);
			robot.setPlayer(player);
			
			robot.setCenterPosition(x, y);
			
			simulation.addEntity(robot);
		}
		catch (ProgramException e)
		{
			throw new IOException(String.format("Can't load robot %s: %s", robotName, e.getMessage()));
		}
	}

	private void parseObstacle(Scanner scanner)
	{
		int x = scanner.nextInt();
		int y = scanner.nextInt();

		int width = scanner.nextInt();
		int height = scanner.nextInt();
		
		Obstacle obstacle = new Obstacle(x, y, width, height, simulation);
		
		simulation.addEntity(obstacle);
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
				
				Area captureArea = new Area(simulation, w, h);
				captureArea.setCenterPosition(x, y);
				simulation.addEntity(captureArea);
				
				CaptureLocationObjective objective = new CaptureLocationObjective(simulation, player, unitsAmount, captureArea);
				simulation.addObjective(objective);
				
				break;
			}
			case "destroy":
			{
				int player = scanner.nextInt();
				
				Objective objective = new DestroyEnemyObjective(player);
				simulation.addObjective(objective);
				
				break;
			}
			case "command":
			{
				int player = scanner.nextInt();
				int unitsCount = scanner.nextInt();
				
				CommandAtLeastUnitsObjective objective = new CommandAtLeastUnitsObjective(player, unitsCount);
				simulation.addObjective(objective);
				
				break;
			}
			default:
				throw new IllegalArgumentException("Invalid objective type: " + type);
		}
	}
	
	private void parseSpawn(Scanner scanner)
	{
		int id, x, y;
		
		id = scanner.nextInt();
		x = scanner.nextInt();
		y = scanner.nextInt();
		
		Spawn spawn = new Spawn(simulation, id);
		spawn.setCenterPosition(x, y);
		simulation.addSpawn(spawn);
	}
}
