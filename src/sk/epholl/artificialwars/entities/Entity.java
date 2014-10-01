package sk.epholl.artificialwars.entities;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import sk.epholl.artificialwars.logic.GameLogic;
import sk.epholl.artificialwars.logic.Vector2D;

public abstract class Entity
{
	private Vector2D position, direction;

	private double moveSpeed, rotateSpeed;

	protected final GameLogic game;

	protected Color color = Color.BLACK;
	private int width = 10;
	private int height = 10;

	private boolean colliding = false;
	private boolean destroyed = false;

	public Entity(GameLogic game)
	{
		this.position = Vector2D.ORIGIN;
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
		
		Vector2D newPosition = calcNewPosition();

		if (isCollidable())
		{
			Set<Entity> collidingEntities = getCollisions(newPosition, nearbyEntities);
			
			if (collidingEntities.isEmpty())
			{
				setCenterPosition(newPosition);
			}
			else
			{
				colliding = true;
				collided(collidingEntities);
			}
		}
		else
		{
			setCenterPosition(newPosition);
		}
	}
	
	protected void collided(Set<Entity> entities)
	{
	}
	
	protected void destroyed()
	{
	}

	public void turn()
	{
	}
	
	private Vector2D calcNewPosition()
	{
		return position.add(direction.scale(moveSpeed));
	}
	
	private Set<Entity> getCollisions(Vector2D position, Set<Entity> entities)
	{
		Set<Entity> collidingEntities = new HashSet<>();
		
		for (Entity collisionEntity : entities)
		{
			if (collisionEntity != this && this.isCollidingWith(collisionEntity))
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
		this.position = position;
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
	
	public Vector2D getPosition()
	{
		return position;
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

	public GameLogic getGame()
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

	public boolean isCollidingWith(Entity e)
	{
		if (!e.isCollidable())
		{
			return false;
		}
		
		double halfWidth = width / 2;
		double halfHeight = height / 2;
		
		double x1, x2, y1, y2;
		x1 = position.getX() - halfWidth;
		x2 = position.getX() + halfWidth;
		y1 = position.getY() - halfHeight;
		y2 = position.getY() + halfHeight;
		
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
		ex1 = position.getX() - halfWidth;
		ex2 = position.getX() + halfWidth;
		ey1 = position.getY() - halfHeight;
		ey2 = position.getY() + halfHeight;
		
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
		return new Vector2D(getPosition(), e.getPosition()).getLength();
	}

	public void destroy()
	{
		destroyed = true;
		destroyed();
	}
}
