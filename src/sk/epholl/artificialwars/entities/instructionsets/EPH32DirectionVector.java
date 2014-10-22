package sk.epholl.artificialwars.entities.instructionsets;

import java.util.HashMap;

import sk.epholl.artificialwars.logic.Vector2D;

public enum EPH32DirectionVector 
{
	ONE(	 5.0, -8.7), 
	TWO(	 8.7, -5.0), 
	THREE(	 1.0,  0.0), 
	FOUR(	 8.7,  5.0), 
	FIVE(	 5.0,  8.7), 
	SIX(	 0.0,  1.0), 
	SEVEN(	-5.0,  8.7), 
	EIGHT(	-8.7,  5.0), 
	NINE(	-1.0,  0.0), 
	TEN(	-8.7, -5.0), 
	ELEVEN(	-5.0, -8.7), 
	TWELVE(	 0.0, -1.0);
	
	private static final HashMap<Integer, EPH32DirectionVector> vectors = new HashMap<>();
	
	static
	{
		vectors.put( 1, ONE);
		vectors.put( 2, TWO);
		vectors.put( 3, THREE);
		vectors.put( 4, FOUR);
		vectors.put( 5, FIVE);
		vectors.put( 6, SIX);
		vectors.put( 7, SEVEN);
		vectors.put( 8, EIGHT);
		vectors.put( 9, NINE);
		vectors.put(10, TEN);
		vectors.put(11, ELEVEN);
		vectors.put(12, TWELVE);
	}
	
	public static EPH32DirectionVector getClockDirection(int index)
	{
		return vectors.get(index);
	}

	private Vector2D vector;
	
	private EPH32DirectionVector(double x, double y)
	{
		this.vector = new Vector2D(x, y).normalise();
	}
	
	public Vector2D getVector()
	{
		return vector;
	}
}
