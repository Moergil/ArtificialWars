package sk.epholl.artificialwars.entities;

import java.awt.Color;

import sk.epholl.artificialwars.logic.GameLogic;

/**
 * @author epholl
 */
public class Doodad extends Entity
{
	public static final int DEFAULT_DOODAD_DURATION = 15;

	private int doodadDuration;
	private int cascadeCount;

	public Doodad(int posX, int posY, GameLogic game, Color color, int cascadeCount, int size)
	{
		super(posX, posY, game);
		this.color = color;
		doodadDuration = DEFAULT_DOODAD_DURATION;

		this.cascadeCount = cascadeCount - 1;
		this.sizeX = size;
		this.sizeY = size;
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
	public boolean isTimed()
	{
		return true;
	}

	@Override
	public boolean isCollidable()
	{
		return false;
	}

	@Override
	public boolean movesInfinitely()
	{
		return true;
	}

	@Override
	public void destroy()
	{
		destroyed = true;
		if (cascadeCount > 0)
		{
			Doodad explosion = new Doodad((int) getPosX(), (int) getPosY(), game, color, cascadeCount, sizeX * 2);
			explosion.setMoveSpeed(moveSpeed);
			explosion.setVectorX(vectorX);
			explosion.setVectorY(vectorY);

			game.addEntity(explosion);
		}
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public boolean aboutToRemove()
	{
		doodadDuration--;
		if (doodadDuration < 0)
		{
			destroy();
			return true;
		}
		return false;
	}

	@Override
	public boolean collidesWith(Entity e)
	{
		return false;
	}
}
