package sk.epholl.artificialwars.graphics;

import java.awt.Graphics2D;

import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.entities.robots.Robot;

public class Eph32BasicRobotDebug implements RobotDebug
{
	@Override
	public void draw(Graphics2D g2d, Robot robot)
	{
		Eph32BasicRobot eph32basicRobot = (Eph32BasicRobot)robot;
		
		String registers = eph32basicRobot.getRegistersString();
		g2d.drawString(registers, 5, 5);
		
		String line = eph32basicRobot.getActualLine();
		g2d.drawString(line, 5, 20);
	}
}
