package sk.epholl.artificialwars.entities.robots;

import java.awt.Color;
import java.io.IOError;
import java.io.IOException;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.logic.Simulation;

public abstract class Robot extends Entity
{
	protected int player;
	
	public Robot(Simulation game)
	{
		super(game);
	}
	
	public void setColor(Color color)
	{
		this.color = color;
	}
	
	public void setPlayer(int player)
	{
		this.player = player;
	}

	abstract public int getRobotTypeId();
	
	abstract public void setFirmware(byte firmware[]) throws IOException;
}
