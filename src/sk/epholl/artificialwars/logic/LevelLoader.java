package sk.epholl.artificialwars.logic;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import sk.epholl.artificialwars.entities.Obstacle;
import sk.epholl.artificialwars.entities.objectives.NumberOfUnitsObjective;
import sk.epholl.artificialwars.entities.robots.Exterminator;
import sk.epholl.artificialwars.entities.robots.FirmwareLoader;
import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;

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
					NumberOfUnitsObjective o;

					int leftBorder = Integer.parseInt(parser.nextToken());
					int rightBorder = Integer.parseInt(parser.nextToken());
					int upperBorder = Integer.parseInt(parser.nextToken());
					int lowerBorder = Integer.parseInt(parser.nextToken());

					int ownerPlayer = Integer.parseInt(parser.nextToken());
					int numberOfUnits = Integer.parseInt(parser.nextToken());

					boolean visible;
					if (parser.hasMoreTokens() && parser.nextToken().equals("invisible"))
						visible = false;
					else
						visible = true;

					o = new NumberOfUnitsObjective(leftBorder, rightBorder, upperBorder, lowerBorder, logic, ownerPlayer, numberOfUnits, visible);

					o.setMessage(inputReader.readLine());

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

					r = new Eph32BasicRobot(new Color(red, green, blue), player, posX, posY, logic);
					
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
					Exterminator r;
					
					int red = Integer.parseInt(parser.nextToken());
					int green = Integer.parseInt(parser.nextToken());
					int blue = Integer.parseInt(parser.nextToken());
					int player = Integer.parseInt(parser.nextToken());
					int posX = Integer.parseInt(parser.nextToken());
					int posY = Integer.parseInt(parser.nextToken());

					String instructionFile = parser.nextToken();
					
					r = new Exterminator(new Color(red, green, blue), player, posX, posY, logic, 0);
					
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
