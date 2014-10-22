package sk.epholl.artificialwars.logic;

import sk.epholl.artificialwars.entities.robots.RobotTWM1608;

public class TWM1608RobotCreator extends AbstractRobotCreator<RobotTWM1608>
{
	public TWM1608RobotCreator(Simulation simulation, byte firmware[])
	{
		super(simulation, firmware);
	}

	@Override
	protected RobotTWM1608 createRobot(Simulation simulation)
	{
		return new RobotTWM1608(simulation, simulation.getSeed());
	}
}
