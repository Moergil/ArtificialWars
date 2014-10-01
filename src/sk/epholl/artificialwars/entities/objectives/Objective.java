package sk.epholl.artificialwars.entities.objectives;

import java.awt.Color;
import java.util.Set;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.logic.GameLogic;
import sk.epholl.artificialwars.logic.Vector2D;

public abstract class Objective extends Entity
{
	public static final Color DEFAULT_OBJECTIVE_COLOR = new Color(128, 200, 128, 32);

	private final String message;
	private boolean completed, success;

	public Objective(int leftBorder, int rightBorder, int upperBorder, int lowerBorder, GameLogic game, String message)
	{
		super(game);
		
		this.message = message;

		setPosition(new Vector2D((leftBorder + rightBorder) / 2, (upperBorder + lowerBorder) / 2));
		
		setWidth(Math.abs(rightBorder - leftBorder));
		setHeight(Math.abs(upperBorder - lowerBorder));

		this.color = DEFAULT_OBJECTIVE_COLOR;
	}
	
	@Override
	public boolean isSolid()
	{
		return false;
	}
	
	@Override
	public boolean isDestructible()
	{
		return false;
	}
	
	@Override
	public boolean isCollidable()
	{
		return false;
	}
	
	@Override
	public boolean hasPlayer()
	{
		return false;
	}
	
	@Override
	public void update(Set<Entity> nearbyEntities)
	{
		super.update(nearbyEntities);
		
		if (evaluate(game))
		{
			completed = true;
			game.objectiveReached(this);
		}
	}
	
	public boolean isCompleted()
	{
		return completed;
	}
	
	public boolean isSuccess()
	{
		return success;
	}
	
	protected void setSuccess(boolean success)
	{
		this.success = success;
	}
	
	protected abstract boolean evaluate(GameLogic game);

	@Override
	public String toString()
	{
		if (message == null)
			return "Objective";
		else
			return message;
	}
}
