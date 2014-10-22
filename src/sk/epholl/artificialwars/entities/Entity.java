package sk.epholl.artificialwars.entities;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import sk.epholl.artificialwars.logic.Simulation;
import sk.epholl.artificialwars.logic.Vector2D;

// TODO passive and active entities, like movement and collision checkings
public abstract class Entity
{
	private Vector2D centerPosition, direction;

	private double moveSpeed, rotateSpeed;

	protected final Simulation game;

	protected Color color = Color.BLACK;
	private int width = 10;
	private int height = 10;

	private boolean colliding = false;
	private boolean destroyed = false;

	public Entity(Simulation game)
	{
		this.centerPosition = Vector2D.ORIGIN;
		this.direction = Vector2D.NORTH;

		this.game = game;
	}
	
	/**
	 * Checks if entity is destroyable.
	 * @return <code>true</code> if entity is destroyable, <code>false</code> otherwise
	 */
	public abstract boolean isDestructible();
	
	/**
	 * Checks if entity has player.
	 * @return <code>true</code> if entity has player, <code>false</code> otherwise
	 */
	public abstract boolean hasPlayer();
	
	/**
	 * Checks if entity is collidable.
	 * @return <code>true</code> if entity is collidable, <code>false</code> otherwise
	 */
	public abstract boolean isCollidable();
	
	/**
	 * Checks if entity is solid.
	 * @return <code>true</code> if entity is solid, <code>false</code> otherwise
	 */
	public abstract boolean isSolid();
	
	public void update(Set<Entity> nearbyEntities)
	{
		colliding = false;
		
		Vector2D newDirection = this.direction.rotate(rotateSpeed);
		Vector2D newCenterPosition = calcNewPosition(newDirection);

		setDirection(newDirection);
		
		if (isCollidable())
		{
			Set<Entity> collidingEntities = getCollisions(newCenterPosition, nearbyEntities);
			
			if (collidingEntities.isEmpty())
			{
				Vector2D offset = new Vector2D(getCenterPosition(), newCenterPosition);
				setCenterPosition(newCenterPosition);
				moved(offset, rotateSpeed);
			}
			else
			{
				colliding = true;
				collided(collidingEntities);
			}
		}
		else
		{
			setCenterPosition(newCenterPosition);
		}
	}
	
	protected void moved(Vector2D offset, double angle)
	{
	}
	
	protected void collided(Set<Entity> entities)
	{
	}
	
	protected void destroyed()
	{
	}

	public void act()
	{
	}
	
	private Vector2D calcNewPosition(Vector2D direction)
	{
		Vector2D newDirection = direction.scale(moveSpeed);
		return centerPosition.add(newDirection);
	}
	
	private Set<Entity> getCollisions(Vector2D centerPosition, Set<Entity> entities)
	{
		Set<Entity> collidingEntities = new HashSet<>();
		
		if (!isCollidable())
		{
			return collidingEntities;
		}
		
		for (Entity collisionEntity : entities)
		{
			if (!collisionEntity.isCollidable())
			{
				continue;
			}

			if (collisionEntity != this && this.isCollidingWith(collisionEntity, centerPosition))
			{
				collidingEntities.add(collisionEntity);
			}
		}
		
		return collidingEntities;
	}
	
	public void setCenterPosition(double x, double y)
	{
		setCenterPosition(new Vector2D(x, y));
	}
	
	public void setCornerPosition(double x, double y)
	{
		setCornerPosition(new Vector2D(x, y));
	}
	
	public void setCenterPosition(Vector2D position)
	{
		this.centerPosition = position;
	}
	
	public void setCornerPosition(Vector2D position)
	{
		double x = position.getX() + width / 2;
		double y = position.getY() + height / 2;

		setCenterPosition(x, y);
	}
	
	public void setDirection(Vector2D direction)
	{
		this.direction = direction.normalise();
	}
	
	public Vector2D getCenterPosition()
	{
		return centerPosition;
	}
	
	public Vector2D getCornerPosition()
	{
		double x = centerPosition.getX() - width / 2;
		double y = centerPosition.getY() - height / 2;
		
		return new Vector2D(x, y);
	}
	
	public Vector2D getDirection()
	{
		return direction;
	}
	
	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getWidth()
	{
		return width;
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}
	
	public int getHeight()
	{
		return height;
	}

	public Color getColor()
	{
		return color;
	}

	public Simulation getGame()
	{
		return game;
	}

	public double getMoveSpeed()
	{
		return moveSpeed;
	}

	public void setMoveSpeed(double moveSpeed)
	{
		this.moveSpeed = moveSpeed;
	}
	
	public double getRotateSpeed()
	{
		return rotateSpeed;
	}
	
	public void setRotateSpeed(double rotateSpeed)
	{
		this.rotateSpeed = rotateSpeed;
	}

	public boolean isColliding()
	{
		return colliding;
	}

	public boolean isCollidingWith(Entity e, Vector2D centerPosition)
	{		
		double halfWidth = width / 2;
		double halfHeight = height / 2;
		
		double x1, x2, y1, y2;
		x1 = centerPosition.getX() - halfWidth;
		x2 = centerPosition.getX() + halfWidth;
		y1 = centerPosition.getY() - halfHeight;
		y2 = centerPosition.getY() + halfHeight;
		
		return e.isColliding(x1, x2, y1, y2);
	}
	
	public boolean isCollidingWith(Vector2D vector)
	{
		double x = vector.getX();
		double y = vector.getY();
		
		return isColliding(x, x, y, y);
	}
	
	private boolean isColliding(double x1, double x2, double y1, double y2)
	{
		double halfWidth = width / 2;
		double halfHeight = height / 2;
		
		double ex1, ex2, ey1, ey2;
		ex1 = centerPosition.getX() - halfWidth;
		ex2 = centerPosition.getX() + halfWidth;
		ey1 = centerPosition.getY() - halfHeight;
		ey2 = centerPosition.getY() + halfHeight;
		
		boolean collideX = false, collideY = false;
		
		if (x1 > ex1 && x1 < ex2 || x2 > ex1 && x2 < ex2)
		{
			collideX = true;
		}
		
		if (y1 > ey1 && y1 < ey2 || y2 > ey1 && y2 < ey2)
		{
			collideY = true;
		}

		return collideX && collideY;
	}
	
	public boolean isDestroyed()
	{
		return destroyed;
	}

	public boolean isExists()
	{
		return !destroyed;
	}

	public boolean isMoving()
	{
		return moveSpeed != 0;
	}
	
	public boolean isRotating()
	{
		return rotateSpeed != 0;
	}

	public void beHit(Projectile shot)
	{
		if (isSolid())
		{
			shot.destroy();
		}
	}

	public int getPlayer()
	{
		return -1;
	}

	public double getDistance(Entity e)
	{
		return new Vector2D(getCenterPosition(), e.getCenterPosition()).getLength();
	}

	public void destroy()
	{
		destroyed = true;
		destroyed();
	}
}
