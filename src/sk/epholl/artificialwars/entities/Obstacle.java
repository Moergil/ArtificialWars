package sk.epholl.artificialwars.entities;

import java.awt.Color;

import sk.epholl.artificialwars.logic.GameLogic;

/**
 * @author epholl
 */
public class Obstacle extends Entity
{
	public static final Color DEFAULT_OBSTACLE_COLOR = new Color(200, 150, 150);

	private boolean aboutToRemove = false;

	public Obstacle(int posX, int posY, GameLogic game)
	{
		super(posX, posY, game);

		this.sizeX = 10;
		this.sizeY = 10;

		this.color = DEFAULT_OBSTACLE_COLOR;
	}

	public Obstacle(int leftBorder, int rightBorder, int upperBorder, int lowerBorder, GameLogic game)
	{
		super(((leftBorder + rightBorder) / 2), ((upperBorder + lowerBorder) / 2), game);

		this.sizeX = Math.abs(rightBorder - leftBorder);
		this.sizeY = Math.abs(upperBorder - lowerBorder);

		this.color = DEFAULT_OBSTACLE_COLOR;
	}

	@Override
	public boolean aboutToRemove()
	{
		return aboutToRemove;
	}

	@Override
	public boolean movesInfinitely()
	{
		return true;
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
	public boolean isTimed()
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

	@Override
	public void destroy()
	{
		destroyed = true;
		aboutToRemove = true;
	}
}
