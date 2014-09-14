package sk.hackcraft.artificialwars.computersim;

import java.util.ArrayList;
import java.util.List;

public class Computer
{
	private final Bus bus;
	private List<Part> parts = new ArrayList<>();
	
	public Computer(Bus bus)
	{
		this.bus = bus;
	}
	
	public void addPart(Part part)
	{
		parts.add(part);		
	}
	
	public void tick()
	{
		for (Part part : parts)
		{
			part.update();
		}
		
		bus.update();
	}
}
