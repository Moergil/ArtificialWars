package sk.epholl.artificialwars.entities.robots.parts;



public class GradientDetector implements RobotPart
{
	private final SpotsProvider provider;

	private final double angleRange;
	
	private double excitation;

	public GradientDetector(SpotsProvider provider, double angleRange)
	{
		this.provider = provider;
		
		this.angleRange = angleRange / 2;
	}
	
	@Override
	public String getName()
	{
		return (int)angleRange + "° Gradient Detector";
	}
	
	@Override
	public void update()
	{
		this.excitation = 0;
		
		for (DetectorSpot spot : provider.getDetectorSpots())
		{
			double signedAngle = spot.getSignedRelativeAngle();
			
			if (Math.abs(signedAngle) > angleRange)
			{
				continue;
			}
			
			double distance = spot.getDistance();
			double width = spot.getWidth();
			
			double spotIntensity = calcSpotIntensity(distance, width);
			
			excitation += calcDetectorExcitation(signedAngle, spotIntensity);
		}
	}
	
	public double getExcitation()
	{
		return excitation;
	}
	
	private double calcSpotIntensity(double distance, double width)
	{
		return width / distance;
	}
	
	private double calcDetectorExcitation(double signedAngle, double spotIntensity)
	{
		double offsetAngle = Math.abs(signedAngle);
		
		double percentage = 1 / angleRange;
		double invertedAngle = angleRange - offsetAngle;
		double multiplier = percentage * invertedAngle;
		
		return spotIntensity * multiplier;
	}
}
