package sk.epholl.artificialwars.entities;

import java.awt.Color;

import sk.epholl.artificialwars.logic.Simulation;
import sk.epholl.artificialwars.logic.RobotCreator.AbstractRobot;

public class Spawn extends Entity
{
	@FunctionalInterface
	public interface Creator
	{
		void create(Spawn spawn) throws Exception;
	}
	
	@FunctionalInterface
	public interface CreationFailedListener
	{
		void failed(String reason);
	}
	
	private final int id;
	
	private Creator creator = (s) -> {};
	private CreationFailedListener creationFailedListener = (r) -> {};
	
	private boolean used;
	
	public Spawn(Simulation game, int id)
	{
		super(game);
		
		this.id = id;
		
		setWidth(20);
		setHeight(20);
		
		color = new Color(0.0f, 0.0f, 0.3f, 0.2f);
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setCreator(Creator creator)
	{
		this.creator = creator;
	}
	
	public void setCreationFailedListener(CreationFailedListener creationFailedListener)
	{
		this.creationFailedListener = creationFailedListener;
	}
	
	@Override
	public void act()
	{
		super.act();
		
		if (!used)
		{
			try
			{
				creator.create(this);
			}
			catch (Exception e)
			{
				creationFailedListener.failed(e.getMessage());
			}

			used = true;
		}
	}

	@Override
	public boolean isDestructible()
	{
		return false;
	}

	@Override
	public boolean hasPlayer()
	{
		return false;
	}

	@Override
	public boolean isCollidable()
	{
		return false;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}
}
