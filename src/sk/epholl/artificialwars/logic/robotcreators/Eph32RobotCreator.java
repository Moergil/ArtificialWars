package sk.epholl.artificialwars.logic.robotcreators;

import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.logic.AbstractRobotCreator;
import sk.epholl.artificialwars.logic.Simulation;

public class Eph32RobotCreator extends AbstractRobotCreator<Eph32BasicRobot>
{
	public Eph32RobotCreator(Simulation simulation, byte firmware[])
	{
		super(simulation, firmware);
	}

	@Override
	protected Eph32BasicRobot createRobot(Simulation simulation)
	{
		return new Eph32BasicRobot(simulation);
	}
}
