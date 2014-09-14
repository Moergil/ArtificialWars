package sk.epholl.artificialwars.entities;

import java.awt.Color;

import sk.epholl.artificialwars.logic.GameLogic;

/**
 * @author epholl
 */
public class Projectile extends Entity
{
	public static final double DEFAULT_SHOT_SPEED = 2.5;
	public static final int DEFAULT_SHOT_DURATION = 200;
	public static final int DEFAULT_ARM_TIME = 3;

	private int shotDuration;
	private boolean armed;
	private int armCooldown;

	public Projectile(int posX, int posY, int destinationX, int destinationY, GameLogic game)
	{
		super(posX, posY, game);

		this.sizeX = 2;
		this.sizeY = 2;

		this.shotDuration = DEFAULT_SHOT_DURATION;
		this.moveSpeed = DEFAULT_SHOT_SPEED;
		this.armed = false;
		this.armCooldown = DEFAULT_ARM_TIME;

		if (posX != destinationX || posY != destinationY)
		{
			setDestination(destinationX, destinationY);
		}
	}

	@Override
	public void moveToNextPosition()
	{
		super.moveToNextPosition();

		shotDuration--;
		if (armCooldown > 0)
			armCooldown--;
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
	public boolean aboutToRemove()
	{
		if (shotDuration < 0)
		{
			destroy();
			return true;
		}
		return false;
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
	public boolean collidesWith(Entity e)
	{
		if (e.isCollidable() && e.getUppermostPosY() >= posY && e.getDownmostPosY() <= posY && e.getLeftmostPosX() <= posX && e.getRightmostPosX() >= posX)
		{
			if (armed)
			{
				e.beHit(this);
			}
			return false;
		}
		if (armCooldown == 0)
			armed = true;
		return false;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public void destroy()
	{
		destroyed = true;
		shotDuration = -1;

		Color doodadColor = new Color(255, 0, 0, 100);
		Doodad explosion = new Doodad((int) getPosX(), (int) getPosY(), game, doodadColor, 3, 3);
		explosion.setMoveSpeed(moveSpeed / 3);
		explosion.setVectorX(vectorX);
		explosion.setVectorY(vectorY);

		game.addEntity(explosion);
	}
}
