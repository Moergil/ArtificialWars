package sk.epholl.artificialwars.entities.robots;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import sk.hackcraft.artificialwars.computersim.toolchain.AssemblerEPH32;
import sk.hackcraft.artificialwars.computersim.toolchain.AssemblerTEK1608;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.CodeSyntaxException;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProgramException;
import sk.hackcraft.artificialwars.computersim.toolchain.Preprocessor;

public class FirmwareCompiler
{
	public static byte[] compileFirmware(String robotType, String firmwareFileName) throws ProgramException, IOException
	{
		switch (robotType)
		{
			case "eph32":
				return compileEph32(firmwareFileName);
			case "twm1608":
				return compileTWM1608(firmwareFileName);
			default:
				throw new ProgramException("No compiler available for robot type " + robotType);
		}
	}
	
	private static byte[] compileEph32(String firmwareFileName) throws ProgramException, IOException
	{
		System.out.println("Loading " + firmwareFileName);
		
		Preprocessor preprocessor = new Preprocessor(";", "macro", "/macro");
		
		int programSegmentStartAddress = 0;
		int programMemorySize = 64 * Integer.BYTES * 2;
		
		int dataSegmentStartAddress = 0;
		int dataMemorySize = 4 * Integer.BYTES;
		
		AssemblerEPH32 assembler = new AssemblerEPH32(programSegmentStartAddress, dataSegmentStartAddress, programMemorySize, dataMemorySize);
		
		try (InputStream input = new FileInputStream(firmwareFileName);)
		{
			ByteArrayOutputStream preprocessorOutput = new ByteArrayOutputStream();
			preprocessor.process(firmwareFileName, input, preprocessorOutput);
			
			ByteArrayInputStream assemblerInput = new ByteArrayInputStream(preprocessorOutput.toByteArray());
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			assembler.process(firmwareFileName, assemblerInput, output);
			
			return output.toByteArray();
		}
	}
	
	private static byte[] compileTWM1608(String firmwareFileName) throws ProgramException, IOException
	{
		System.out.println("Loading " + firmwareFileName);
		
		Preprocessor preprocessor = new Preprocessor(";", "MACRO", "/MACRO");
		AssemblerTEK1608 assembler = new AssemblerTEK1608();
		
		try (InputStream input = new FileInputStream(firmwareFileName);)
		{
			ByteArrayOutputStream preprocessorOutput = new ByteArrayOutputStream();
			preprocessor.process(firmwareFileName, input, preprocessorOutput);
			
			ByteArrayInputStream assemblerInput = new ByteArrayInputStream(preprocessorOutput.toByteArray());
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			assembler.process(firmwareFileName, assemblerInput, output);
			
			return output.toByteArray();
		}
	}
}
