package sk.epholl.artificialwars.logic;

/**
 * @author epholl
 */
public class Vector2D
{
	public static final Vector2D ORIGIN = new Vector2D(0, 0);
	public static final Vector2D NORTH = new Vector2D(0, 1);
	public static final Vector2D EAST = new Vector2D(1, 0);
	
	private final double x, y;

	private double length = Double.NaN;

	public Vector2D(double vectorX, double vectorY)
	{
		this.x = vectorX;
		this.y = vectorY;
	}
	
	public Vector2D(Vector2D from, Vector2D to)
	{
		this.x = to.x - from.x;
		this.y = to.y - from.y;
	}

	public double getLength()
	{
		if (Double.isNaN(length))
		{
			if (x == 0 && y == 0)
			{
				length = 0;
			}

			length = Math.sqrt(x * x + y * y);
		}
		
		return length;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}
	
	public Vector2D add(Vector2D vector)
	{
		return new Vector2D(x + vector.x, y + vector.y);
	}
	
	public Vector2D sub(Vector2D vector)
	{
		return new Vector2D(x - vector.x, y - vector.y);
	}
	
	public Vector2D scale(double scalar)
	{
		return new Vector2D(x * scalar, y * scalar);
	}
	
	public Vector2D rotate(double angle)
	{
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		
		double newX = cos * x + -sin * y;
		double newY = sin * x +  cos * y;
		
		return new Vector2D(newX, newY);
	}
	
	public Vector2D normalise()
	{
		double length = getLength();
		
		if (isZero())
		{
			return this;
		}
		
		Vector2D vector = new Vector2D(x / length, y / length);
		vector.length = 1;
		
		return vector;
	}
	
	public boolean isUnit()
	{
		return getLength() == 1;
	}
	
	public boolean isZero()
	{
		return getLength() == 0;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Vector2D))
		{
			return false;
		}
		
		Vector2D vector = (Vector2D)obj;
		
		return vector.x == x && vector.y == y;
	}
	
	@Override
	public int hashCode()
	{
		// TODO
		return (int)Double.doubleToLongBits(x * y);
	}
	
	@Override
	public String toString()
	{
		return String.format("[%.2f,%.2f]", x, y);
	}
}
