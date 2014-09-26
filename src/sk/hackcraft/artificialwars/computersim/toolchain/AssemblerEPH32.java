package sk.hackcraft.artificialwars.computersim.toolchain;

import sk.epholl.artificialwars.entities.instructionsets.EPH32InstructionSet;
import sk.epholl.artificialwars.entities.instructionsets.EPH32InstructionSet.EPH32MemoryAddressing;
import sk.hackcraft.artificialwars.computersim.Endianness;

public class AssemblerEPH32 extends AbstractAssembler
{
	private final int programSegmentStart, dataSegmentStart;
	
	public AssemblerEPH32(int programSegmentStart, int dataSegmentStart)
	{
		super(EPH32InstructionSet.getInstance(), Endianness.BIG, ".segment", "(.+):");
		
		this.programSegmentStart = programSegmentStart;
		this.dataSegmentStart = dataSegmentStart;
		
		// TODO vyhodit ak to nebude potrebne
		addSegmentIdentifier(Segment.PROGRAM, "program");
		addSegmentIdentifier(Segment.DATA, "data");
		
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
	
	@Override
	protected AssemblerState started()
	{
		AssemblerState state = super.started();
		
		state.setSegmentStartAddress(Segment.PROGRAM, programSegmentStart);
		state.setSegmentStartAddress(Segment.DATA, dataSegmentStart);
		
		return super.started();
	}
}
