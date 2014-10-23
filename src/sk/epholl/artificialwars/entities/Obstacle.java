package sk.epholl.artificialwars.entities;

import java.awt.Color;

import sk.epholl.artificialwars.logic.Simulation;

public class Obstacle extends Entity
{
	public static final Color DEFAULT_OBSTACLE_COLOR = new Color(200, 150, 150);

	public Obstacle(int x, int y, int width, int height, Simulation game)
	{
		super(game);

		setWidth(width);
		setHeight(height);
		
		setCornerPosition(x, y);

		this.color = DEFAULT_OBSTACLE_COLOR;
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
		return true;
	}

	@Override
	public boolean isSolid()
	{
		return true;
	}
}
