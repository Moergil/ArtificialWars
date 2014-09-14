package sk.hackcraft.artificialwars.computersim.parts;

import sk.hackcraft.artificialwars.computersim.Device;
import sk.hackcraft.artificialwars.computersim.Pins;

public class TextDisplay implements Device
{	
	private final byte buffer[];
	
	public TextDisplay()
	{
		buffer = new byte[80 * 25];
	}

	@Override
	public void update()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPinsCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setBusConnection(Pins pins)
	{
		// TODO Auto-generated method stub
		
	}
}
