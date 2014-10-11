package sk.epholl.artificialwars.logic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Timer;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Obstacle;
import sk.epholl.artificialwars.entities.objectives.Objective;
import sk.epholl.artificialwars.graphics.GamePanel;

/**
 * @author epholl
 */
public class GameLogic
{
	private final long seed;
	private final String levelName;
	
	private GamePanel panel;
	private boolean gameRunning = true;
	private Timer timer;
	private int cycleCount;
	private int objectiveCount;

	private String outputString;
	private int outputStringValidity;

	private Set<Entity> entities;

	public GameLogic(long seed, String levelName)
	{
		this.seed = seed;
		this.levelName = levelName;
		
		entities = new HashSet<>();

		outputString = "";
		outputStringValidity = 0;

		cycleCount = 0;

		timer = new Timer(10, new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent ae)
			{
				singleStep();
			}
		});
	}
	
	public long getSeed()
	{
		return seed;
	}
	
	public void singleStep()
	{
		logicCycle();
		panel.repaint();
		cycleCount++;
	}

	private void initialise()
	{
		//borders
		
		int thickness = 10;
		int uiPanel = 100;
		
		entities.add(new Obstacle(0, 0, panel.getWidth(), thickness, this));
		entities.add(new Obstacle(0, 0, thickness, panel.getHeight(), this));
		entities.add(new Obstacle(panel.getWidth() - thickness, 0, thickness, panel.getHeight(), this));
		entities.add(new Obstacle(0, panel.getHeight() - 100, panel.getWidth(), uiPanel, this));
	}

	public void startGame()
	{
		initialise();
		panel.repaint();
	}

	public void pauseGame()
	{
		timer.stop();
	}

	public void continueGame()
	{
		if (gameRunning)
			timer.start();
	}

	public void setTimerDelay(int delay)
	{
		timer.setDelay(delay);
	}

	public void setGamePanel(GamePanel panel)
	{
		this.panel = panel;
	}

	public Set<Entity> getEntities()
	{
		return entities;
	}

	public boolean checkCollision(int x, int y)
	{
		return false;
	}

	public void addEntity(Entity e)
	{		
		entities.add(e);
	}
	
	public void addObjective(Objective objective)
	{
		setOutputString(objective.getDescription(), 1);
		objectiveCount++;
		
		addEntity(objective);
	}

	public void objectiveReached(Objective o)
	{
		if (o.getState() == Objective.State.SUCCESS)
		{
			objectiveCount--;
			setOutputString("Objective reached: " + o.toString(), 400);
			o.destroy();
			if (objectiveCount == 0)
			{
				endGame(true);
			}
		}
		else
		{
			setOutputString(o.toString(), 100);
			endGame(false);
			o.destroy();
		}
	}

	public void removeEntity(Entity e)
	{
		entities.remove(e);
	}

	public int getCycleCount()
	{
		return cycleCount;
	}

	public void setOutputString(String output, int cycleCount)
	{
		outputString = output;
		outputStringValidity = getCycleCount() + cycleCount;
	}

	public String getOutputString()
	{
		return outputString;
	}
	
	public String getLevelName()
	{
		return levelName;
	}

	private void logicCycle()
	{
		// snapshotting current entities
		Set<Entity> entitiesCopy = Collections.unmodifiableSet(new HashSet<>(entities));
		
		// update entities internal state
		for (Entity entity : entitiesCopy)
		{
			entity.update(entitiesCopy);
		}
		
		// allow entities to interact with world
		for (Entity entity : entitiesCopy)
		{
			entity.turn();
			
			if (!entity.isExists())
			{
				entities.remove(entity);
			}
		}

		if (outputStringValidity < getCycleCount())
		{
			outputString = "";
		}
	}

	private void endGame(boolean victory)
	{
		pauseGame();
		gameRunning = false;

		if (victory)
		{
			setOutputString("Victory!", 1);
		}
		else
		{
			setOutputString("Defeat!", 1);
		}

		panel.repaint();
	}

	private void endGame(String message)
	{
		pauseGame();
		gameRunning = false;

		setOutputString(message, 1);

		panel.repaint();
	}
}
