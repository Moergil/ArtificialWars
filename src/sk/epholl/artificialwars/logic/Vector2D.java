package sk.epholl.artificialwars.logic;

/**
 * @author epholl
 */
public class Vector2D
{
	private double x;
	private double y;

	private double length;

	public Vector2D(double vectorX, double vectorY)
	{
		this.x = vectorX;
		this.y = vectorY;

		recountLength();
	}

	public void setX(double x)
	{
		this.x = x;
	}

	public void setY(double y)
	{
		this.y = y;
	}

	public void recountLength()
	{
		this.length = Math.sqrt(x * x + y * y);
	}

	public double getNewX()
	{
		return ((x / length));
	}

	public double getNewY()
	{
		return ((y / length));
	}

	public double getLength()
	{
		return length;
	}
}
