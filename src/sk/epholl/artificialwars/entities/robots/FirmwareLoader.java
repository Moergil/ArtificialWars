package sk.epholl.artificialwars.entities.robots;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import sk.hackcraft.artificialwars.computersim.toolchain.AssemblerEPH32;
import sk.hackcraft.artificialwars.computersim.toolchain.AssemblerTEK1608;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.CodeProcessException;
import sk.hackcraft.artificialwars.computersim.toolchain.Preprocessor;

public class FirmwareLoader
{
	public static void loadFirmwareRobot(String fileName, Robot robot) throws IOException, ProgrammingException
	{
		Preprocessor preprocessor = new Preprocessor(";", "macro", "/macro");
		
		int programSegmentStartAddress = 0;
		int dataSegmentStartAddress = 0;
		
		AssemblerEPH32 assembler = new AssemblerEPH32(programSegmentStartAddress, dataSegmentStartAddress);
		
		try (InputStream input = new FileInputStream(fileName);)
		{
			ByteArrayOutputStream preprocessorOutput = new ByteArrayOutputStream();
			preprocessor.process(input, preprocessorOutput);
			
			ByteArrayInputStream assemblerInput = new ByteArrayInputStream(preprocessorOutput.toByteArray());
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			assembler.process(assemblerInput, output);
			
			byte objectCode[] = output.toByteArray();
			robot.loadFirmware(objectCode, 0);
		}
		catch (IOException | CodeProcessException e)
		{
			throw new ProgrammingException("Can't load firmware:", e);
		}
	}
	
	public static void loadFirmwareExterminator(String firmwareFile, Exterminator exterminator) throws IOException, ProgrammingException
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
		catch (IOException | CodeProcessException e)
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
