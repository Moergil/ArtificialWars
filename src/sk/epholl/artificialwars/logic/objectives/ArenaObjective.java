package sk.epholl.artificialwars.logic.objectives;

import java.util.Set;
import java.util.stream.Collectors;

import sk.epholl.artificialwars.entities.robots.Robot;
import sk.epholl.artificialwars.logic.Simulation;

public class ArenaObjective extends Objective
{
	private boolean unitsFound;
	
	private final int player;
	
	public ArenaObjective(int player)
	{
		super("Destroy all enemy units.");
		
		this.player = player;
	}
	
	@Override
	protected Result evaluate(Simulation simulation)
	{
		Set<Robot> robots = simulation.getEntities().stream()
				.filter((e) -> e instanceof Robot)
				.map((e) -> (Robot)e)
				.collect(Collectors.toSet());
		
		if (!unitsFound && robots.size() > 0)
		{
			unitsFound = true;
		}
		
		if (unitsFound)
		{
			long playerUnitsCount = robots.stream()
			.filter((r) -> r.getPlayer() == player)
			.count();
			
			if (playerUnitsCount == robots.size())
			{
				return Objective.Result.SUCCESS;
			}
			else if (playerUnitsCount == 0)
			{
				return Objective.Result.FAIL;
			}
		}
		
		return null;
	}
}
