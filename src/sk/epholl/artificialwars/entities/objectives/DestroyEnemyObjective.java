package sk.epholl.artificialwars.entities.objectives;

import sk.epholl.artificialwars.logic.Simulation;

public class DestroyEnemyObjective extends Objective
{
	private final int enemyPlayerId;
	
	public DestroyEnemyObjective(Simulation game, int playerId)
	{
		this.enemyPlayerId = playerId;
		
		setDescription("Destroy all enemy robots.");
		
		setEvaluator(() -> evaluate(game));
	}

	protected State evaluate(Simulation game)
	{
		long count = game.getEntities()
				.stream()
				.filter((entity) -> entity.hasPlayer() && entity.getPlayer() == enemyPlayerId)
				.count();
		
		if (count == 0)
		{
			return State.SUCCESS;
		}
		else
		{
			return State.IN_PROGRESS;
		}
	}
}
