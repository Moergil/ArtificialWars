package sk.epholl.artificialwars.entities.robots.parts;

import java.util.Arrays;
import java.util.HashSet;


public class SegmentDetector implements RobotPart
{
	private final SpotsProvider provider;
	
	private final boolean segment[];
	private final double threshold;

	public SegmentDetector(SpotsProvider provider, int segmentsCount, double threshold)
	{
		this.provider = provider;
		
		this.segment = new boolean[segmentsCount];
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
		Arrays.fill(segment, false);
		
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
		return segment[index];
	}
	
	private void setValue(double angle, double distance, double width)
	{
		int index = getIndex(angle);
		
		double value = width / distance;

		segment[index] = value > threshold;
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
