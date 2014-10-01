package sk.epholl.artificialwars.entities.objectives;

import java.util.Set;
import java.util.stream.Collectors;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.logic.GameLogic;

public class CaptureLocationObjective extends Objective
{
	private final int playerId, amount;
	
	public CaptureLocationObjective(GameLogic game, int playerId, int amount)
	{
		super(game);

		this.playerId = playerId;
		this.amount = amount;
	}
	
	@Override
	public String getDescription()
	{
		return String.format("Command at least %d units in target location.", amount);
	}
	
	@Override
	public boolean isPhysical()
	{
		return true;
	}
	
	@Override
	protected State evaluate(GameLogic game)
	{
		int count = (int)game.getEntities()
				.stream()
				.filter((entity) -> entity.hasPlayer() && entity.getPlayer() == playerId)
				.filter((entity) -> this.isCollidingWith(entity))
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
