package sk.hackcraft.artificialwars.computersim.toolchain;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

import sk.epholl.artificialwars.entities.instructionsets.EPH32InstructionSet.EPH32MemoryAddressing;

public class AssemblerEPH32 extends Assembler
{
	public AssemblerEPH32(InstructionSet instructionSet)
	{
		super(instructionSet, "^([A-Za-z]{3,})$");
		
		addRegex(EPH32MemoryAddressing.IMPLIED, "");
		addRegex(EPH32MemoryAddressing.IMMEDIATE, "([0-9]+)");
	}
}
