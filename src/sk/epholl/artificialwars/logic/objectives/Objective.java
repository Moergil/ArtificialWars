package sk.epholl.artificialwars.logic.objectives;

import sk.epholl.artificialwars.logic.Simulation;

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
