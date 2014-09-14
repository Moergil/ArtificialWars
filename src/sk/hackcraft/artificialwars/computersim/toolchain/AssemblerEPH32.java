package sk.hackcraft.artificialwars.computersim.toolchain;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

import sk.epholl.artificialwars.entities.instructionsets.EPH32InstructionSet.MemoryAddressing;

public class AssemblerEPH32 extends Assembler<MemoryAddressing>
{
	public AssemblerEPH32(InstructionSet instructionSet)
	{
		super(instructionSet, "^([A-Za-z]{3,})$");
		
		addRegex(MemoryAddressing.IMPLIED, "");
		addRegex(MemoryAddressing.IMMEDIATE, "([0-9]+)");
	}
	
	@Override
	protected Map<MemoryAddressing, Pattern> createMemoryAddressingPatternsMap()
	{
		return new EnumMap<>(MemoryAddressing.class);
	}
}
