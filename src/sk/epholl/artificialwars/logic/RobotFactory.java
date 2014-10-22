package sk.epholl.artificialwars.logic;

import java.io.IOException;
import java.util.Map;

import sk.epholl.artificialwars.entities.robots.FirmwareCompiler;
import sk.epholl.artificialwars.entities.robots.Robot;
import sk.epholl.artificialwars.logic.RobotCreator.AbstractRobot;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProgramException;

public class RobotFactory
{
	private static final String ROBOTS_PATH = "robots/%s.rbt";
	
	private final FirmwaresCache firmwaresCache;
	
	public static AbstractRobot loadAbstractRobot(String robotName) throws IOException
	{
		String robotPath = String.format(ROBOTS_PATH, robotName);
		return AbstractRobot.loadFromFile(robotPath);
	}
	
	public static byte[] loadFirmware(String architecture, String codeFileName) throws IOException, ProgramException
	{
		return FirmwareCompiler.compileFirmware(architecture, codeFileName);
	}
	
	public static Robot loadStandardRobot(Simulation simulation, AbstractRobot abstractRobot, byte firmware[]) throws IOException
	{
		switch (abstractRobot.getArchitecture())
		{
			case "eph32":
				return new Eph32RobotCreator(simulation, firmware).load();
			case "twm1608":
				return new TWM1608RobotCreator(simulation, firmware).load();
			default:
				return null;
		}
	}
	
	public RobotFactory()
	{
		firmwaresCache = new FirmwaresCache();
		
		firmwaresCache.setResolver(RobotFactory::loadFirmware);
	}
	
	public Robot loadRobot(Simulation simulation, String robotName) throws IOException, ProgramException
	{
		AbstractRobot abstractRobot = RobotFactory.loadAbstractRobot(robotName);
		byte firmware[] = firmwaresCache.get(abstractRobot.getArchitecture(), abstractRobot.getCodeFileName());

		return RobotFactory.loadStandardRobot(simulation, abstractRobot, firmware);
	}
}
