package sk.epholl.artificialwars.logic;

public class Vector2DMath
{
	public static double getRelativeSignedAngle(Vector2D from, Vector2D to)
	{
		return Math.atan2(to.getY(), to.getX()) - Math.atan2(from.getY(), from.getX());
	}
	
	public static Vector2D unitCircleAngleToVector(double angle)
	{
		double y = Math.sin(angle);
		double x = Math.cos(angle);
		
		return new Vector2D(x, y);
	}
}
