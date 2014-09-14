package sk.epholl.artificialwars.entities.objectives;

import java.awt.Color;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.logic.GameLogic;

/**
 * @author epholl
 */
public abstract class Objective extends Entity
{
	public static final Color DEFAULT_OBJECTIVE_COLOR = new Color(128, 200, 128, 32);

	protected String message;

	public Objective(int leftBorder, int rightBorder, int upperBorder, int lowerBorder, GameLogic game, boolean visible)
	{
		super(((leftBorder + rightBorder) / 2), ((upperBorder + lowerBorder) / 2), game);

		this.sizeX = Math.abs(rightBorder - leftBorder);
		this.sizeY = Math.abs(upperBorder - lowerBorder);

		if (!visible)
			this.color = new Color(0, 0, 0, 0);
		else
			this.color = DEFAULT_OBJECTIVE_COLOR;
	}

	@Override
	public void turn()
	{

	}

	public void setMessage(String message)
	{
		this.message = message;
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
		return false;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public void destroy()
	{
		this.destroyed = true;
	}

	@Override
	public String toString()
	{
		if (message == null)
			return "Objective";
		else
			return message;
	}
}
