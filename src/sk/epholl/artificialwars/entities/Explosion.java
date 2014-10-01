package sk.epholl.artificialwars.entities;

import java.awt.Color;

import sk.epholl.artificialwars.logic.GameLogic;
import sk.epholl.artificialwars.logic.Vector2D;

public class Explosion extends Doodad
{
	public static final Explosion create(GameLogic game, Vector2D position)
	{
		return create(game, position, Vector2D.ORIGIN, 0);
	}
	
	public static final Explosion create(GameLogic game, Vector2D position, Vector2D direction, double moveSpeed)
	{
		Explosion explosion = new Explosion(game);
		explosion.setMoveSpeed(moveSpeed);
		explosion.setPosition(position);
		explosion.setDirection(direction);
		
		return explosion;
	}
	
	public Explosion(GameLogic game)
	{
		super(game, new Color(255, 0, 0, 100), 3);
	}
}
