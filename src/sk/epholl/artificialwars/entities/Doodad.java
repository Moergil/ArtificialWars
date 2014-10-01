package sk.epholl.artificialwars.entities;

import java.awt.Color;
import java.util.Set;

import sk.epholl.artificialwars.logic.GameLogic;

/**
 * @author epholl
 */
public class Doodad extends Entity
{
	public static final int DEFAULT_DOODAD_DURATION = 15;

	private int doodadDuration;

	public Doodad(GameLogic game, Color color, int size)
	{
		super(game);
		this.color = color;
		doodadDuration = DEFAULT_DOODAD_DURATION;
		
		setWidth(size);
		setHeight(size);
	}
	
	@Override
	public void update(Set<Entity> nearbyEntities)
	{
		super.update(nearbyEntities);
		
		if (doodadDuration < 0)
		{
			destroy();
			return;
		}
		
		doodadDuration--;
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
		return false;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public boolean isCollidingWith(Entity e)
	{
		return false;
	}
}
