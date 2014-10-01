package sk.epholl.artificialwars.logic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	
	private GamePanel panel;
	private boolean gameRunning = true;
	private Timer timer;
	private int cycleCount;
	private int objectiveCount;

	private String outputString;
	private int outputStringValidity;

	private Set<Entity> entities;

	public GameLogic(long seed)
	{
		this.seed = seed;
		
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
		
		entities.add(new Obstacle(-20, panel.getWidth() + 30, -20, 0, this));
		entities.add(new Obstacle(-20, panel.getWidth() + 30, panel.getHeight() - 80, panel.getHeight() + 20, this));
		entities.add(new Obstacle(-20, 0, 0, panel.getHeight(), this));
		entities.add(new Obstacle(panel.getWidth() , panel.getWidth() + 20, 0, panel.getHeight(), this));
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
		if (e instanceof Objective)
		{
			setOutputString(e.toString(), 1);
			objectiveCount++;
		}
		
		entities.add(e);
	}

	public void objectiveReached(Objective o)
	{
		if (o.isSuccess())
		{
			objectiveCount--;
			setOutputString("Objective reached: " + o.toString(), 400);
			o.destroy();
			if (objectiveCount == 0)
				endGame(true);
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

	private void logicCycle()
	{
		// update entities internal state
		for (Entity entity : entities)
		{
			entity.update(new HashSet<>(entities));
		}
		
		// allow entities to interact with world
		Set<Entity> entitiesToRemove = new HashSet<>();
		for (Entity entity : entities)
		{
			entity.turn();
			
			if (!entity.isExists())
			{
				entitiesToRemove.add(entity);
			}
		}
		
		// remove destroyed entities
		for (Entity entity : entitiesToRemove)
		{
			entities.remove(entity);
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
