package sk.epholl.artificialwars.entities.objectives;

import sk.epholl.artificialwars.logic.Simulation;

public class DestroyEnemyObjective extends Objective
{
	private final int enemyPlayerId;
	
	public DestroyEnemyObjective(int playerId)
	{
		super("Destroy all enemy robots.");
		
		this.enemyPlayerId = playerId;
	}

	@Override
	protected Result evaluate(Simulation game)
	{
		long count = game.getEntities()
				.stream()
				.filter((entity) -> entity.hasPlayer() && entity.getPlayer() == enemyPlayerId)
				.count();
		
		if (count == 0)
		{
			return Result.SUCCESS;
		}
		else
		{
			return null;
		}
	}
}
