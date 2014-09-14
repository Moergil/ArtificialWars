package sk.epholl.artificialwars.entities;

import java.awt.Color;

import sk.epholl.artificialwars.logic.GameLogic;
import sk.epholl.artificialwars.logic.Vector2D;

/**
 * @author epholl
 */
public abstract class Entity
{
	protected double posX;
	protected double posY;
	protected double vectorX = 0;
	protected double vectorY = 0;
	protected double moveSpeed = 0.2;
	protected int moveDuration = -1;

	protected GameLogic game;

	protected Color color = Color.BLACK;
	protected int sizeX = 10;
	protected int sizeY = 10;

	protected boolean colliding = false;
	protected boolean destroyed = false;

	public Entity(int posX, int posY, GameLogic game)
	{
		this.posX = posX;
		this.posY = posY;

		this.game = game;
	}

	public void setDestination(int destinationX, int destinationY)
	{
		Vector2D vector = new Vector2D(destinationX - posX, destinationY - posY);

		vectorX = vector.getNewX();
		vectorY = vector.getNewY();

		if (!movesInfinitely())
			moveDuration = (int) (vector.getLength() / moveSpeed);
		else
			moveDuration = -1;
	}

	public void turn()
	{

	}

	public void moveToNextPosition()
	{
		colliding = false;

		if (moveDuration == 0)
		{
			vectorX = 0;
			vectorY = 0;
			return;
		}

		posX += getVectorX();
		posY += getVectorY();

		if (!movesInfinitely())
			moveDuration--;
	}

	public void returnToPreviousPosition()
	{
		colliding = true;

		posX -= getVectorX();
		posY -= getVectorY();
		if (!movesInfinitely())
			moveDuration++;
	}

	public int getPosX()
	{
		return (int) posX;
	}

	public int getLeftmostPosX()
	{
		return (int) posX - (sizeX / 2);
	}

	public int getRightmostPosX()
	{
		return (int) posX + (sizeX / 2);
	}

	public int getPosY()
	{
		return (int) posY;
	}

	public int getUppermostPosY()
	{
		return (int) posY + (sizeY / 2);
	}

	public int getDownmostPosY()
	{
		return (int) posY - (sizeY / 2);
	}

	public double getVectorX()
	{
		return vectorX * moveSpeed;
	}

	public double getVectorY()
	{
		return vectorY * moveSpeed;
	}

	public int getSizeX()
	{
		return sizeX;
	}

	public int getSizeY()
	{
		return sizeY;
	}

	public Color getColor()
	{
		return color;
	}

	public GameLogic getGame()
	{
		return game;
	}

	public double getMoveSpeed()
	{
		return moveSpeed;
	}

	public void setVectorX(double vectorX)
	{
		this.vectorX = vectorX;
	}

	public void setVectorY(double vectorY)
	{
		this.vectorY = vectorY;
	}

	public void setMoveSpeed(double moveSpeed)
	{
		this.moveSpeed = moveSpeed;
	}

	public boolean isCollided()
	{
		return colliding;
	}

	public boolean isEnded()
	{
		return destroyed;
	}

	public boolean collidesWith(Entity e)
	{
		if (e.isCollidable() && Math.abs(e.getPosX() - getPosX()) < ((getSizeX() + e.getSizeX()) / 2) && Math.abs(e.getPosY() - getPosY()) < ((getSizeY() + e.getSizeY()) / 2))
			return true;
		return false;
	}

	public boolean aboutToRemove()
	{
		return destroyed;
	}

	public boolean isMoving()
	{
		if (isCollided())
			return false;
		return (vectorX != 0 || vectorY != 0);
	}

	public void beHit(Projectile shot)
	{
		if (isSolid())
			shot.destroy();
	}

	public int getPlayer()
	{
		return -1;
	}

	public double getDistance(Entity e)
	{
		double differenceX = Math.abs(posX - e.posX);
		double differenceY = Math.abs(posY - e.posY);

		return Math.sqrt(differenceX * differenceX + differenceY * differenceY);
	}

	public abstract boolean movesInfinitely();

	public abstract boolean isDestructible();

	public abstract boolean hasPlayer();

	public abstract boolean isTimed();

	public abstract boolean isCollidable();

	public abstract boolean isSolid();

	public abstract void destroy();
}
