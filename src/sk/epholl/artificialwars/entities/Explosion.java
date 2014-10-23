package sk.epholl.artificialwars.entities;

import java.awt.Color;

import sk.epholl.artificialwars.logic.Simulation;
import sk.epholl.artificialwars.logic.Vector2D;

public class Explosion extends Doodad
{
	public static final Explosion create(Simulation game, Vector2D position)
	{
		return create(game, position, Vector2D.ORIGIN, 0);
	}
	
	private final int cascade;
	
	public static final Explosion create(Simulation game, Vector2D position, Vector2D direction, double moveSpeed)
	{
		Explosion explosion = new Explosion(game, 1);
		explosion.setMoveSpeed(moveSpeed);
		explosion.setCenterPosition(position);
		explosion.setDirection(direction);
		
		return explosion;
	}
	
	public Explosion(Simulation game, int cascade)
	{
		super(game, new Color(255, 0, 0, 100), 3 * cascade);
		
		this.cascade = cascade;
	}
	
	@Override
	protected void destroyed()
	{
		super.destroyed();
		
		if (cascade < 4)
		{
			Explosion explosion = new Explosion(game, cascade + 1);
			explosion.setCenterPosition(getCenterPosition());
			explosion.setDirection(getDirection());
			explosion.setMoveSpeed(getMoveSpeed() / 2);
			game.addEntity(explosion);
		}
	}
}
