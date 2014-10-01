package sk.epholl.artificialwars.entities;

import java.awt.Color;

import sk.epholl.artificialwars.logic.GameLogic;
import sk.epholl.artificialwars.logic.Vector2D;

public class Obstacle extends Entity
{
	public static final Color DEFAULT_OBSTACLE_COLOR = new Color(200, 150, 150);

	public Obstacle(int posX, int posY, GameLogic game)
	{
		super(game);
		
		setPosition(new Vector2D(posX, posY));

		setWidth(10);
		setHeight(10);

		this.color = DEFAULT_OBSTACLE_COLOR;
	}

	public Obstacle(int leftBorder, int rightBorder, int upperBorder, int lowerBorder, GameLogic game)
	{
		super(game);

		setPosition(new Vector2D((leftBorder + rightBorder) / 2, (upperBorder + lowerBorder) / 2));
		
		setWidth(Math.abs(rightBorder - leftBorder));
		setHeight(Math.abs(upperBorder - lowerBorder));

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
