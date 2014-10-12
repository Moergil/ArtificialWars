package sk.epholl.artificialwars.entities.instructionsets;

import sk.epholl.artificialwars.logic.Vector2D;

public enum EPH32DirectionVector 
{
	ONE(new 	Vector2D(5d, 	-8.7d)), 
	TWO(new 	Vector2D(8.7d, 	-5d)), 
	THREE(new 	Vector2D(1d, 	0d)), 
	FOUR(new 	Vector2D(8.7d,	5d)), 
	FIVE(new 	Vector2D(5d, 	8.7d)), 
	SIX(new 	Vector2D(0d, 	1d)), 
	SEVEN(new 	Vector2D(-5d, 	8.7d)), 
	EIGHT(new 	Vector2D(-8.7d,	5d)), 
	NINE(new 	Vector2D(-1d, 	0d)), 
	TEN(new 	Vector2D(-8.7d,	-5d)), 
	ELEVEN(new 	Vector2D(-5d,	-8.7d)), 
	TWELVE(new 	Vector2D(0,		-1d));
	
	private Vector2D vector;
	
	private EPH32DirectionVector(Vector2D vector)
	{
		this.vector = vector;
	}
	
	public Vector2D getVector()
	{
		return vector;
	}
}
