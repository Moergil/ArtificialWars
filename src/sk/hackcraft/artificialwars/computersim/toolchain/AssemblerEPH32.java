package sk.hackcraft.artificialwars.computersim.toolchain;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

import sk.epholl.artificialwars.entities.instructionsets.EPH32InstructionSet.EPH32MemoryAddressing;
import sk.hackcraft.artificialwars.computersim.Endianness;

public class AssemblerEPH32 extends Assembler
{
	public AssemblerEPH32(InstructionSet instructionSet)
	{
		super(instructionSet, Endianness.LITTLE);
		
		addMemoryAddressingFormat(EPH32MemoryAddressing.IMPLIED, "");
		addMemoryAddressingFormat(EPH32MemoryAddressing.IMMEDIATE, "%");
	}
}
