package sk.epholl.artificialwars.entities.objectives;

import java.awt.Color;
import java.util.Set;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.logic.Simulation;
import sk.epholl.artificialwars.logic.Updateable;
import sk.epholl.artificialwars.logic.Vector2D;

public abstract class Objective
{
	public enum State
	{
		IN_PROGRESS,
		SUCCESS,
		FAIL;
	}
	
	@FunctionalInterface
	public interface StateChangedListener
	{
		void stateChanged(State newState);
	}
	
	public interface Evaluator
	{
		State evaluate();
	}

	private State state = State.IN_PROGRESS;
	
	private String description = "";
	
	private Evaluator evaluator;
	private StateChangedListener stateChangedListener;
	
	public void setStateChangedListener(StateChangedListener stateChangedListener)
	{
		this.stateChangedListener = stateChangedListener;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setEvaluator(Evaluator evaluator)
	{
		this.evaluator = evaluator;
	}

	public void check()
	{
		if (evaluator == null)
		{
			return;
		}

		State newState = evaluator.evaluate();
		
		if (newState != state)
		{
			state = newState;
			stateChangedListener.stateChanged(newState);
		}
	}
	
	public State getState()
	{
		return state;
	}

	@Override
	public String toString()
	{
		return "Objective: " + description;
	}
}
