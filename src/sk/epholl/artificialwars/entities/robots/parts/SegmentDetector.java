package sk.epholl.artificialwars.entities.robots.parts;

import java.util.Arrays;


public class SegmentDetector implements RobotPart
{
	private final SpotsProvider provider;
	
	private final double segment[];
	private final double threshold;

	public SegmentDetector(SpotsProvider provider, int segmentsCount, double threshold)
	{
		this.provider = provider;
		
		this.segment = new double[segmentsCount];
		this.threshold = threshold;
	}
	
	@Override
	public String getName()
	{
		return segment.length + " Segment Detector";
	}
	
	@Override
	public void update()
	{
		Arrays.fill(segment, 0);
		
		for (DetectorSpot spot : provider.getDetectorSpots())
		{
			double angle = spot.getSignedRelativeAngle();
			double distance = spot.getDistance();
			double width = spot.getWidth();
			
			setValue(angle, distance, width);
		}
	}
	
	public int getSegmentsCount()
	{
		return segment.length;
	}
	
	public boolean isActivated(int index)
	{
		return segment[index] > threshold;
	}
	
	private void setValue(double angle, double distance, double width)
	{
		int index = getIndex(angle);
		
		double value = width / distance;

		segment[index] += value;
	}
	
	private int getIndex(double angle)
	{
		double anglePerSegment = Math.PI / (segment.length / 2);
	
		int index = (int)(angle / anglePerSegment);
		
		if (angle > 0)
		{
			return index;
		}
		else
		{
			return segment.length + index - 1;
		}
	}
}
