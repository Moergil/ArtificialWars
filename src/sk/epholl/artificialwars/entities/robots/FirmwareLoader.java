package sk.epholl.artificialwars.entities.robots;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import sk.epholl.artificialwars.entities.instructionsets.EPH32InstructionSet;
import sk.hackcraft.artificialwars.computersim.toolchain.AssemblerEPH32;
import sk.hackcraft.artificialwars.computersim.toolchain.AssemblerTEK1608;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.CodeProcessException;
import sk.hackcraft.artificialwars.computersim.toolchain.Preprocessor;

public class FirmwareLoader
{
	public static void loadFirmwareRobot(String fileName, Robot robot) throws IOException, ProgrammingException
	{
		EPH32InstructionSet instructionSet = new EPH32InstructionSet();
		Preprocessor preprocessor = new Preprocessor(";");
		AssemblerEPH32 assembler = new AssemblerEPH32(instructionSet);
		
		try (InputStream input = new FileInputStream(fileName);)
		{
			ByteArrayOutputStream preprocessorOutput = new ByteArrayOutputStream();
			preprocessor.process(input, preprocessorOutput);
			
			ByteArrayInputStream assemblerInput = new ByteArrayInputStream(preprocessorOutput.toByteArray());
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			assembler.process(assemblerInput, output);
			
			byte objectCode[] = output.toByteArray();
			robot.loadFirmware(objectCode, 10);
		}
		catch (IOException | CodeProcessException e)
		{
			throw new ProgrammingException(e.getMessage());
		}
	}
	
	public static void loadFirmwareExterminator(String firmwareFile, Exterminator exterminator) throws IOException, ProgrammingException
	{
		Preprocessor preprocessor = new Preprocessor(";");
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
			throw new ProgrammingException(e.getMessage());
		}
	}
	
	public static class ProgrammingException extends Exception
	{
		public ProgrammingException(String message)
		{
			super(message);
		}
	}
}
