package sk.epholl.artificialwars.logic.objectives;

import java.awt.Color;
import java.util.Set;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.logic.Simulation;
import sk.epholl.artificialwars.logic.Updateable;
import sk.epholl.artificialwars.logic.Vector2D;

public abstract class Objective
{
	public enum Result
	{
		SUCCESS,
		FAIL;
	}

	private Result state;
	
	private final String description;
	
	public Objective(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return description;
	}

	public void update(Simulation simulation)
	{
		state = evaluate(simulation);
	}
	
	public Result getState()
	{
		return state;
	}
	
	protected abstract Result evaluate(Simulation simulation);

	@Override
	public String toString()
	{
		return "Objective: " + description;
	}
}
