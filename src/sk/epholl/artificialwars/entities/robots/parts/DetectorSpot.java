package sk.epholl.artificialwars.entities.robots.parts;


public class DetectorSpot
{
	private final double relativeRotation;
	private final double distance;
	private final double width;
	
	public DetectorSpot(double relativeRotation, double distance, double width)
	{
		this.relativeRotation = relativeRotation;
		this.distance = distance;
		this.width = width;
	}
	
	public double getSignedRelativeAngle()
	{
		return relativeRotation;
	}
	
	public double getDistance()
	{
		return distance;
	}
	
	public double getWidth()
	{
		return width;
	}
}