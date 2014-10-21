package sk.epholl.artificialwars.entities;

import java.awt.Color;

import sk.epholl.artificialwars.logic.Simulation;

public class Area extends Entity
{
	public Area(Simulation game, int width, int height)
	{
		super(game);

		setWidth(width);
		setHeight(height);
		
		color = new Color(0, 0.7f, 0.3f, 0.1f);
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
	
	public boolean isInside(Entity entity)
	{
		return isCollidingWith(entity, getCenterPosition());
	}
}
