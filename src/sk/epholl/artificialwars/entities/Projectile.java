package sk.epholl.artificialwars.entities;

import java.awt.Color;
import java.util.Set;

import sk.epholl.artificialwars.logic.GameLogic;

/**
 * @author epholl
 */
public class Projectile extends Entity
{
	public static final double DEFAULT_SHOT_SPEED = 2.5;
	public static final int DEFAULT_SHOT_DURATION = 200;

	private final Entity source;
	
	private int shotDuration;

	public Projectile(GameLogic game, Entity source)
	{
		super(game);
		
		this.source = source;

		setWidth(2);
		setHeight(2);

		this.shotDuration = DEFAULT_SHOT_DURATION;
		setMoveSpeed(DEFAULT_SHOT_SPEED);
	}

	public int getDamage()
	{
		return 2;
	}

	@Override
	public Color getColor()
	{
		return Color.red;
	}
	
	@Override
	public void update(Set<Entity> nearbyEntities)
	{
		if (shotDuration < 0)
		{
			destroy();
		}
		
		shotDuration--;
		
		super.update(nearbyEntities);
	}
	
	@Override
	protected void collided(Set<Entity> entities)
	{
		for (Entity e : entities)
		{
			if (e.isDestructible())
			{
				e.beHit(this);
			}
		}
		
		destroy();
	}
	
	@Override
	public boolean isCollidingWith(Entity e)
	{
		if (e == source)
		{
			return false;
		}
		else
		{
			return super.isCollidingWith(e);
		}		
	}

	@Override
	public boolean isDestructible()
	{
		return false;
	}

	@Override
	public boolean hasPlayer()
	{
		return false;
	}

	@Override
	public boolean isCollidable()
	{
		return true;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public void destroy()
	{
		super.destroy();

		Explosion explosion = Explosion.create(game, getPosition(), getDirection(), getMoveSpeed() / 2);
		game.addEntity(explosion);
	}
}
