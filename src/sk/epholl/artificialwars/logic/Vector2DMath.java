package sk.epholl.artificialwars.logic;

public class Vector2DMath
{
	public static void main(String[] args)
	{
		Vector2D v1 = new Vector2D(1, 0);
		Vector2D v2 = new Vector2D(0, 1);
		
		System.out.println(getRelativeSignedAngle(v1, v2));
	}
	
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
