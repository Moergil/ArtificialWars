package sk.epholl.artificialwars.logic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Timer;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Obstacle;
import sk.epholl.artificialwars.entities.objectives.Objective;
import sk.epholl.artificialwars.graphics.GamePanel;

public class Simulation
{
	private final long seed;

	private int cycleCount;

	private final Set<Entity> entities;
	private final List<Objective> objectives;

	public Simulation(long seed)
	{
		this.seed = seed;
		
		entities = new HashSet<>();
		objectives = new ArrayList<>();

		cycleCount = 0;
		
		initialise();
	}
	
	public long getSeed()
	{
		return seed;
	}
	
	public void step()
	{
		logicCycle();
		cycleCount++;
	}
	
	public void addObjective(Objective objective)
	{
		objectives.add(objective);
	}

	@Deprecated
	private void initialise()
	{
		//borders
		
		int thickness = 10;
		int uiPanel = 100;
		
		int width = 795;
		int height = 570;
		
		entities.add(new Obstacle(0, 0, width, thickness, this));
		entities.add(new Obstacle(0, 0, thickness, height, this));
		entities.add(new Obstacle(width - thickness, 0, thickness, height, this));
		entities.add(new Obstacle(0, height - 100, width, uiPanel, this));
	}

	public Set<Entity> getEntities()
	{
		return entities;
	}
	
	public List<Objective> getObjectives()
	{
		return objectives;
	}

	public void addEntity(Entity e)
	{		
		entities.add(e);
	}

	public void removeEntity(Entity e)
	{
		entities.remove(e);
	}

	public int getCycleCount()
	{
		return cycleCount;
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
			entity.act();
			
			if (!entity.isExists())
			{
				entities.remove(entity);
			}
		}
	}
}
