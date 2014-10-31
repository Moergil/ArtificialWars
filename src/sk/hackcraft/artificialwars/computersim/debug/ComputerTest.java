package sk.hackcraft.artificialwars.computersim.debug;

import sk.epholl.artificialwars.entities.robots.FirmwareCompiler;
import sk.hackcraft.artificialwars.computersim.parts.ComputerTWM1000;
import sk.hackcraft.artificialwars.computersim.parts.ProbeProcessorTEK1608.RegisterTEK1608;
import sk.hackcraft.artificialwars.computersim.toolchain.Preprocessor;

public class ComputerTest
{
	public static void main(String[] args) throws Exception
	{
		ComputerTWM1000 c = new ComputerTWM1000();
		
		byte firmware[] = FirmwareCompiler.compileFirmware("twm1608", "irq_test.asm");
		
		c.loadFirmware(firmware);
		
		while (c.getProcessorProbe().getUnsignedByteValue(RegisterTEK1608.Y) != 1)
		{
			c.tick();
			int v = c.getProcessorProbe().getUnsignedByteValue(RegisterTEK1608.A);
			
			System.out.println(c.getBusProbe());
			System.out.println(v);
		}
		
		System.out.println("end.");
	}
}
