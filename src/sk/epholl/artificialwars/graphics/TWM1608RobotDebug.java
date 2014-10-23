package sk.epholl.artificialwars.graphics;

import java.awt.Graphics2D;
import java.util.List;

import sk.epholl.artificialwars.entities.robots.Robot;
import sk.epholl.artificialwars.entities.robots.RobotTWM1608;
import sk.hackcraft.artificialwars.computersim.debug.CommonValueFormatter;
import sk.hackcraft.artificialwars.computersim.parts.ComputerTWM1000;
import sk.hackcraft.artificialwars.computersim.parts.ProbeProcessorTEK1608.RegisterTEK1608;
import sk.hackcraft.artificialwars.computersim.parts.TEK1608InstructionSet;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet;

public class TWM1608RobotDebug implements RobotDebug
{
	@Override
	public void draw(Graphics2D g2d, Robot robot)
	{
		ComputerTWM1000 computer = ((RobotTWM1608)robot).getComputer();
		
		String processor = computer.getProcessorProbe().toString();
		g2d.drawString(processor, 0, 0);
		
		String io = computer.getIoProbe().toString();
		g2d.drawString(io, 0, 15);
		
		String bus = computer.getBusProbe().toString();
		g2d.drawString(bus, 500, 0);
		
		int actualPC = computer.getProcessorProbe().getUnsignedShortValue(RegisterTEK1608.PC);
		int actualInstructionCode = computer.getProcessorProbe().getUnsignedByteValue(RegisterTEK1608.IR);
		InstructionSet.Opcode opcode = TEK1608InstructionSet.getInstance().getOpcode(actualInstructionCode);
		
		String actualInstruction;
		if (opcode != null)
		{
			StringBuilder b = new StringBuilder();
			
			b
			.append(String.format("%04X", actualPC - opcode.getBytesSize()))
			.append(" ")
			.append(opcode.getInstructionName())
			.append(" ")
			.append(opcode.getMemoryAddressing().getShortName());
			
			int operandsBytesSize = opcode.getBytesSize() - 1;
			for (int i = 0; i < operandsBytesSize; i++)
			{
				int address = actualPC - operandsBytesSize + i;
				
				byte value = computer.getMemoryValue(address);
				b.append(" ").append(String.format("%02X", value));
			}

			actualInstruction = b.toString();
		}
		else
		{
			actualInstruction = "N/A";
		}

		g2d.drawString(actualInstruction, 500, 15);
		
		List<String> memory = computer.getMemoryProbe().getMemory(0, 16, 128);
		g2d.drawString(memory.get(0), 0, 30);
		
		int sp = computer.getProcessorProbe().getUnsignedByteValue(RegisterTEK1608.SP);
		int spOffset = 256 + sp + 1;
		int spLen = 512 - spOffset;
		
		if (spLen > 8)
		{
			spLen = 8;
		}
		
		List<String> stackMemory = computer.getMemoryProbe().getMemory(spOffset, spLen, 128);
		
		g2d.drawString("SP " + CommonValueFormatter.toHexa2(sp) + ":" + stackMemory.get(0), 500, 30);
	}
}
