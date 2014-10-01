package sk.hackcraft.artificialwars.computersim;

import java.util.ArrayList;
import java.util.List;

public class Computer
{
	private final Bus bus;
	private List<ComputerPart> parts = new ArrayList<>();
	
	public Computer(int busPinsCount)
	{
		this.bus = new Bus(busPinsCount);
	}
	
	protected Bus getBus()
	{
		return bus;
	}
	
	public void addPart(ComputerPart part)
	{
		parts.add(part);		
	}
	
	public void tick()
	{
		for (ComputerPart part : parts)
		{
			part.update();
		}
		
		bus.update();
	}
}
