package sk.epholl.artificialwars.logic;

public class Vector2DMath
{
	public static double getRelativeSignedAngle(Vector2D from, Vector2D to)
	{
		double x = from.getX() * to.getY() - from.getY() * to.getX();
		double y = from.getX() * to.getX() + from.getY() * to.getY();
		
		return Math.atan2(x, y);
	}
	
	public static Vector2D unitCircleAngleToVector(double angle)
	{
		double y = Math.sin(angle);
		double x = Math.cos(angle);
		
		return new Vector2D(x, y);
	}
}
