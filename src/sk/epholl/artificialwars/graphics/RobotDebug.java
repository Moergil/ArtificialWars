package sk.epholl.artificialwars.graphics;

import java.awt.Graphics2D;

import sk.epholl.artificialwars.entities.robots.Robot;

public interface RobotDebug
{
	// TODO implement some type of type cast safety
	void draw(Graphics2D g2d, Robot robot);
}
