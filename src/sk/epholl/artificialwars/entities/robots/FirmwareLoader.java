package sk.epholl.artificialwars.entities.robots;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import sk.hackcraft.artificialwars.computersim.toolchain.AssemblerEPH32;
import sk.hackcraft.artificialwars.computersim.toolchain.AssemblerTEK1608;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.CodeSyntaxException;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProcessException;
import sk.hackcraft.artificialwars.computersim.toolchain.Preprocessor;

public class FirmwareLoader
{
	public static void loadFirmwareRobot(String fileName, Eph32BasicRobot robot) throws ProgrammingException
	{
		Preprocessor preprocessor = new Preprocessor(";", "macro", "/macro");
		
		int programSegmentStartAddress = 0;
		int programMemorySize = 64 * Integer.BYTES * 2;
		
		int dataSegmentStartAddress = 0;
		int dataMemorySize = 4 * Integer.BYTES;
		
		AssemblerEPH32 assembler = new AssemblerEPH32(programSegmentStartAddress, dataSegmentStartAddress, programMemorySize, dataMemorySize);
		
		try (InputStream input = new FileInputStream(fileName);)
		{
			ByteArrayOutputStream preprocessorOutput = new ByteArrayOutputStream();
			preprocessor.process(input, preprocessorOutput);
			
			ByteArrayInputStream assemblerInput = new ByteArrayInputStream(preprocessorOutput.toByteArray());
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			assembler.process(assemblerInput, output);
			
			byte firmware[] = output.toByteArray();
			robot.loadFirmware(firmware, 0);
		}
		catch (IOException | ProcessException e)
		{
			throw new ProgrammingException("Can't load firmware:", e);
		}
	}
	
	public static void loadFirmwareExterminator(String firmwareFile, RobotTWM1608 exterminator) throws ProgrammingException
	{
		Preprocessor preprocessor = new Preprocessor(";", "MACRO", "/MACRO");
		AssemblerTEK1608 assembler = new AssemblerTEK1608();
		
		try (InputStream input = new FileInputStream(firmwareFile);)
		{
			ByteArrayOutputStream preprocessorOutput = new ByteArrayOutputStream();
			preprocessor.process(input, preprocessorOutput);
			
			ByteArrayInputStream assemblerInput = new ByteArrayInputStream(preprocessorOutput.toByteArray());
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			assembler.process(assemblerInput, output);
			
			byte objectCode[] = output.toByteArray();
			exterminator.loadFirmware(objectCode);
		}
		catch (IOException | ProcessException e)
		{
			throw new ProgrammingException("Can't load firmware:", e);
		}
	}
	
	public static class ProgrammingException extends Exception
	{
		public ProgrammingException(String message)
		{
			super(message);
		}
		
		public ProgrammingException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
}
