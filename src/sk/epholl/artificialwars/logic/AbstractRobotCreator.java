package sk.epholl.artificialwars.logic;

import java.io.IOException;
import java.util.Map;

import sk.epholl.artificialwars.entities.robots.FirmwareCompiler;
import sk.epholl.artificialwars.entities.robots.Robot;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProgramException;

public abstract class AbstractRobotCreator<R extends Robot> implements RobotCreator<R>
{
	private final Simulation simulation;
	
	private final byte firmware[];
	
	public AbstractRobotCreator(Simulation simulation, byte firmware[])
	{
		this.simulation = simulation;
		
		this.firmware = firmware;
	}

	@Override
	public R load() throws IOException
	{
		R robot = createRobot(simulation);
		
		robot.setFirmware(firmware);
		
		return robot;
	}
	
	protected abstract R createRobot(Simulation simulation);
}
