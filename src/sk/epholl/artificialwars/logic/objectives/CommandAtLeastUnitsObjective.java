package sk.epholl.artificialwars.logic.objectives;

import sk.epholl.artificialwars.entities.robots.Robot;
import sk.epholl.artificialwars.logic.Simulation;

public class CommandAtLeastUnitsObjective extends Objective
{
	private final int player;
	private final int unitsCount;
	
	public CommandAtLeastUnitsObjective(int player, int unitsCount)
	{
		super(String.format("Command at least %d units.", unitsCount));
		
		this.player = player;
		this.unitsCount = unitsCount;
	}
	
	@Override
	protected Result evaluate(Simulation simulation)
	{
		long commandingUnitsCount = simulation.getEntities().stream()
		.filter((e) -> e instanceof Robot)
		.map((e) -> (Robot)e)
		.filter((r) -> r.getPlayer() == player && !r.isDestroyed())
		.count();
		
		return (commandingUnitsCount > unitsCount) ? Result.SUCCESS : null;
	}
}
