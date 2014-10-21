package sk.epholl.artificialwars.entities.objectives;

import java.util.Set;
import java.util.stream.Collectors;

import sk.epholl.artificialwars.entities.Area;
import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.logic.Simulation;

public class CaptureLocationObjective extends Objective
{
	private final int playerId, amount;
	private final Area area;
	
	public CaptureLocationObjective(Simulation game, int playerId, int amount, Area area)
	{
		this.playerId = playerId;
		this.amount = amount;
		this.area = area;
		
		setDescription(String.format("Command at least %d units in target location.", amount));
		
		setEvaluator(() -> evaluate(game));
	}

	protected State evaluate(Simulation game)
	{
		int count = (int)game.getEntities()
				.stream()
				.filter((entity) -> entity.hasPlayer() && entity.getPlayer() == playerId)
				.filter((entity) -> area.isCollidingWith(entity, area.getCenterPosition()))
				.count();
		
		if (count >= amount)
		{
			return State.SUCCESS;
		}
		else
		{
			return State.IN_PROGRESS;
		}
	}
}
