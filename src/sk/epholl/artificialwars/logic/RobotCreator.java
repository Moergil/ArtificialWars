package sk.epholl.artificialwars.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import sk.epholl.artificialwars.entities.robots.Robot;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProgramException;

public interface RobotCreator<R extends Robot>
{
	R load() throws IOException, ProgramException;
	
	public class AbstractRobot
	{		
		private static final String
			NAME = "name",
			ARCHITECTURE = "arch",
			CODE_FILE = "code";

		public static AbstractRobot loadFromFile(String path) throws IOException
		{
			Properties properties = new Properties();

			File robotFile = new File(path);
			
			try (InputStream input = new FileInputStream(robotFile))
			{
				properties.load(input);
				
				String robotName = properties.getProperty(NAME);
				String architecture = properties.getProperty(ARCHITECTURE);
				String codeFile = properties.getProperty(CODE_FILE);
				
				return new AbstractRobot(robotName, architecture, codeFile);
			}
		}
		
		private final String name;
		private final String architecture;
		private final String codeFile;
		
		public AbstractRobot(String name, String architecture, String codeFile)
		{
			this.name = name;
			this.architecture = architecture;
			this.codeFile = codeFile;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getArchitecture()
		{
			return architecture;
		}
		
		public String getCodeFileName()
		{
			return codeFile;
		}
	}
}
