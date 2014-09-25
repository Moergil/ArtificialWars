package sk.hackcraft.artificialwars.computersim.toolchain;

import sk.epholl.artificialwars.entities.instructionsets.EPH32InstructionSet.EPH32MemoryAddressing;
import sk.hackcraft.artificialwars.computersim.Endianness;

public class AssemblerEPH32 extends Assembler
{
	public AssemblerEPH32(InstructionSet instructionSet)
	{
		super(instructionSet, Endianness.BIG);
		
		addMemoryAddressingFormat(EPH32MemoryAddressing.IMPLIED, "");
		addMemoryAddressingFormat(EPH32MemoryAddressing.IMMEDIATE, "%");
		
		LabelType labelType = new LabelType()
		{
			@Override
			public int getOperandsBytesSize()
			{
				return 4;
			}
			
			@Override
			public int getOperandValue(int labelAddress, int programCounterAddress)
			{
				return labelAddress;
			}
		};
		
		enableLabels("jmp", labelType);
		
		addValueParser((value) -> {
			return Integer.decode(value);
		});
	}
}
