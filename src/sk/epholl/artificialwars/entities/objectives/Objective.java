package sk.epholl.artificialwars.entities.objectives;

import java.awt.Color;
import java.util.Set;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.logic.GameLogic;
import sk.epholl.artificialwars.logic.Vector2D;

public abstract class Objective extends Entity
{
	public enum State
	{
		IN_PROGRESS,
		SUCCESS,
		FAIL;
	}

	public static final Color DEFAULT_OBJECTIVE_COLOR = new Color(128, 200, 128, 32);

	private State state;

	public Objective(GameLogic game)
	{
		super(game);

		this.color = DEFAULT_OBJECTIVE_COLOR;
		
		state = State.IN_PROGRESS;
	}
	
	public abstract String getDescription();
	
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
	
	@Deprecated
	/*
	 * Rework so objectives will not be entities, but they will use special entities for collisions and such
	 */
	public abstract boolean isPhysical();
	
	@Override
	public void update(Set<Entity> nearbyEntities)
	{
		super.update(nearbyEntities);
		
		state = evaluate(game);
		if (state != State.IN_PROGRESS)
		{
			game.objectiveReached(this);
		}
	}
	
	public State getState()
	{
		return state;
	}
	
	protected abstract State evaluate(GameLogic game);

	@Override
	public String toString()
	{
		return getDescription();
	}
}
