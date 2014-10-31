package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sk.hackcraft.artificialwars.computersim.Endianness;
import sk.hackcraft.artificialwars.computersim.parts.EPH32InstructionSet;
import sk.hackcraft.artificialwars.computersim.parts.EPH32InstructionSet.EPH32MemoryAddressing;

public class AssemblerEPH32 extends AbstractAssembler
{
	private final int programSegmentStart, dataSegmentStart;
	private final int programMemorySize, dataMemorySize;
	
	public AssemblerEPH32(int programSegmentStart, int dataSegmentStart, int programMemorySize, int dataMemorySize)
	{
		super(EPH32InstructionSet.getInstance(), Endianness.BIG, ".segment", "(.+):");
		
		this.programSegmentStart = programSegmentStart;
		this.dataSegmentStart = dataSegmentStart;
		
		this.programMemorySize = programMemorySize;
		this.dataMemorySize = dataMemorySize;
		
		// TODO vyhodit ak to nebude potrebne
		addSegmentIdentifier(Segment.PROGRAM, "program");
		addSegmentIdentifier(Segment.DATA, "data");
		
		addMemoryAddressingFormat(EPH32MemoryAddressing.IMPLIED, "");
		addMemoryAddressingFormat(EPH32MemoryAddressing.IMMEDIATE, "%");
		
		LabelType labelType = new LabelType()
		{
			@Override
			public int getOperandsBitsSize()
			{
				return EPH32InstructionSet.WORD_BYTES_SIZE;
			}
			
			@Override
			public int getOperandValue(int labelAddress, int programCounterAddress)
			{
				return labelAddress;
			}
		};
		
		addLabelType("jmp", labelType);
		addLabelType("jmpz", labelType);
		addLabelType("jmpc", labelType);
		addLabelType("jmpm", labelType);
		addLabelType("jmpl", labelType);
		
		addValueParser((value) -> {
			return Integer.decode(value);
		});
		
		addVariableType("int", Integer.BYTES);
	}
	
	@Override
	protected AssemblerState started()
	{
		AssemblerState state = super.started();
		
		state.setSegmentStartAddress(Segment.PROGRAM, programSegmentStart);
		state.setSegmentStartAddress(Segment.DATA, dataSegmentStart);
		
		return state;
	}
	
	@Override
	protected void linkTogether(AssemblerState state, OutputStream output) throws LinkingException, IOException
	{
		byte program[] = state.getSegmentBytes(Segment.PROGRAM);
		byte data[] = state.getSegmentBytes(Segment.DATA);
		
		// TODO
		int programWordLength = program.length / EPH32InstructionSet.WORD_BYTES_SIZE;
		if (program.length / Integer.BYTES / 2 > programMemorySize)
		{
			throw new LinkingException("Program size is bigger than available program memory.");
		}
		
		if (data.length / Integer.BYTES > programMemorySize)
		{
			throw new LinkingException("Data size is bigger than available data memory.");
		}
		
		DataOutputStream dataOutput = new DataOutputStream(output);
		
		dataOutput.writeInt(program.length);
		dataOutput.writeInt(data.length);
		dataOutput.write(program);
		dataOutput.write(data);
	}
}
