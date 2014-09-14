package sk.hackcraft.artificialwars.computersim;

public interface Part
{
	default String getName() { return "Unknown"; }
	
	default void update() {}
}
