package sk.epholl.artificialwars.entities.objectives;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.logic.GameLogic;

/**
 * @author epholl
 */
public class NumberOfUnitsObjective extends Objective
{
	private int numberOfUnits;
	private int player;

	public NumberOfUnitsObjective(int leftBorder, int rightBorder, int upperBorder, int lowerBorder, GameLogic game, int player, int numberOfUnits, boolean visible)
	{
		super(leftBorder, rightBorder, upperBorder, lowerBorder, game, visible);

		this.numberOfUnits = numberOfUnits;
		this.player = player;
	}

	@Override
	public void turn()
	{
		int unitsFoundCount = 0;

		for (Entity e : game.getEntities())
		{
			if (e instanceof Eph32BasicRobot)
			{
				Eph32BasicRobot r = (Eph32BasicRobot) e;
				if (e.getPlayer() == player && this.collidesWith(e))
					unitsFoundCount++;
			}
		}
		if (unitsFoundCount == numberOfUnits)
			game.objectiveReached(this);
	}

	@Override
	public String toString()
	{
		if (message == null)
			return "Player " + player + " must bring " + numberOfUnits + " robots to " + "designated location.";
		else
			return message;
	}
}
