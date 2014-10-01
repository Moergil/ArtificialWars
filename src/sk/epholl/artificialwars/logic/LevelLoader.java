package sk.epholl.artificialwars.logic;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Obstacle;
import sk.epholl.artificialwars.entities.objectives.Objective;
import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.entities.robots.FirmwareLoader;
import sk.epholl.artificialwars.entities.robots.RobotExterminator;

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

	public boolean loadLevel()
	{
		String line;
		String currentWord;
		StringTokenizer parser;
		try
		{
			while ((line = inputReader.readLine()) != null)
			{
				if (line.equals("") || line.startsWith("//"))
					continue;
				parser = new StringTokenizer(line);

				currentWord = parser.nextToken();

				if (currentWord.equals("objective_units"))
				{
					Objective o;

					int leftBorder = Integer.parseInt(parser.nextToken());
					int rightBorder = Integer.parseInt(parser.nextToken());
					int upperBorder = Integer.parseInt(parser.nextToken());
					int lowerBorder = Integer.parseInt(parser.nextToken());

					final int ownerPlayer = Integer.parseInt(parser.nextToken());
					final int requiredUnits = Integer.parseInt(parser.nextToken());

					String objectiveMessage = String.format("Player %d must bring %d robots to designated location.", ownerPlayer, requiredUnits);

					o = new Objective(leftBorder, rightBorder, upperBorder, lowerBorder, logic, objectiveMessage)
					{
						@Override
						protected boolean evaluate(GameLogic game)
						{
							int ownedUnits = 0;
							int unitsInArea = 0;

							for (Entity e : game.getEntities())
							{
								if (e.getPlayer() != ownerPlayer)
								{
									continue;
								}
								
								ownedUnits++;
								
								if (this.isCollidingWith(e))
								{
									unitsInArea++;
								}
							}
							
							if (ownedUnits == 0)
							{
								setSuccess(false);
								return true;
							}
							
							if (unitsInArea >= requiredUnits)
							{
								setSuccess(true);
								return true;
							}
							
							return false;
						}
					};

					inputReader.readLine();

					logic.addEntity(o);
				}
				else if (currentWord.equals("obstacle"))
				{
					Obstacle o;

					int leftBorder = Integer.parseInt(parser.nextToken());
					int rightBorder = Integer.parseInt(parser.nextToken());

					if (parser.hasMoreTokens())
					{
						int upperBorder = Integer.parseInt(parser.nextToken());
						int lowerBorder = Integer.parseInt(parser.nextToken());

						o = new Obstacle(leftBorder, rightBorder, upperBorder, lowerBorder, logic);
					}
					else
					{
						o = new Obstacle(leftBorder, rightBorder, logic);
					}

					logic.addEntity(o);
				}
				else if (currentWord.equals("robot"))
				{
					Eph32BasicRobot r;

					int red = Integer.parseInt(parser.nextToken());
					int green = Integer.parseInt(parser.nextToken());
					int blue = Integer.parseInt(parser.nextToken());
					int player = Integer.parseInt(parser.nextToken());
					int posX = Integer.parseInt(parser.nextToken());
					int posY = Integer.parseInt(parser.nextToken());

					String instructionFile = parser.nextToken();

					r = new Eph32BasicRobot(new Color(red, green, blue), player, logic);
					r.setPosition(posX, posY);
					
					try
					{
						FirmwareLoader.loadFirmwareRobot(instructionFile, r);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					logic.addEntity(r);
				}
				else if (currentWord.equals("exterminator"))
				{
					RobotExterminator r;
					
					int red = Integer.parseInt(parser.nextToken());
					int green = Integer.parseInt(parser.nextToken());
					int blue = Integer.parseInt(parser.nextToken());
					int player = Integer.parseInt(parser.nextToken());
					int posX = Integer.parseInt(parser.nextToken());
					int posY = Integer.parseInt(parser.nextToken());

					String instructionFile = parser.nextToken();
					
					r = new RobotExterminator(new Color(red, green, blue), player, posX, posY, logic, 0);
					
					try
					{
						FirmwareLoader.loadFirmwareExterminator(instructionFile, r);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					logic.addEntity(r);
				}
				else
					throw new IllegalArgumentException("Unknown level entity in file: " + line);
			}
		}
		catch (IOException e)
		{
			return false;
		}
		catch (NoSuchElementException e)
		{
			return false;
		}
		catch (NullPointerException e)
		{
			return false;
		}

		return true;
	}

	public boolean isDone()
	{
		return isDone;
	}
}
