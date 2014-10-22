package sk.epholl.artificialwars.logic;

import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;

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
