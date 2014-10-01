package sk.epholl.artificialwars.entities.objectives;

import sk.epholl.artificialwars.logic.GameLogic;

public class DestroyEnemyObjective extends Objective
{
	private final int enemyPlayerId;
	
	public DestroyEnemyObjective(GameLogic game, int playerId)
	{
		super(game);
		
		this.enemyPlayerId = playerId;
	}

	@Override
	public String getDescription()
	{
		return String.format("Kill all enemy units.");
	}
	
	@Override
	public boolean isPhysical()
	{
		return false;
	}

	@Override
	protected State evaluate(GameLogic game)
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
