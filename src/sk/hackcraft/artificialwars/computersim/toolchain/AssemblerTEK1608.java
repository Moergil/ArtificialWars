package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.IOException;
import java.io.OutputStream;

import sk.hackcraft.artificialwars.computersim.Endianness;
import sk.hackcraft.artificialwars.computersim.TEK1608InstructionSet;
import sk.hackcraft.artificialwars.computersim.TEK1608InstructionSet.TEK1608MemoryAddressing;

public class AssemblerTEK1608 extends AbstractAssembler
{	
	public AssemblerTEK1608()
	{
		super(TEK1608InstructionSet.getInstance(), Endianness.LITTLE, ".SEG", "(.+):");
		
		addSegmentIdentifier(Segment.PROGRAM, "PRG");
		addSegmentIdentifier(Segment.DATA, "DAT");

		addMemoryAddressingFormat(TEK1608MemoryAddressing.ACCUMULATOR, "A");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.ABSOLUTE, "%");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X, "%,X");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y, "%,Y");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.IMMEDIATE, "#%");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.IMPLIED, "");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.INDIRECT, "(%)");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.X_INDEXED_INDIRECT, "(%,X)");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.INDIRECT_Y_INDEXED, "(%),Y");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.RELATIVE, "%");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.ZEROPAGE, "%");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED, "%,X");
		addMemoryAddressingFormat(TEK1608MemoryAddressing.ZEROPAGE_Y_INDEXED, "%,Y");

		enableLabels("BCC", TEK1608LabelType.RELATIVE);
		enableLabels("BCS", TEK1608LabelType.RELATIVE);
		enableLabels("BEQ", TEK1608LabelType.RELATIVE);
		enableLabels("BMI", TEK1608LabelType.RELATIVE);
		enableLabels("BNE", TEK1608LabelType.RELATIVE);
		enableLabels("BPL", TEK1608LabelType.RELATIVE);
		enableLabels("BVC", TEK1608LabelType.RELATIVE);
		enableLabels("BVS", TEK1608LabelType.RELATIVE);
		enableLabels("JMP", TEK1608LabelType.ABSOLUTE);
		enableLabels("JSR", TEK1608LabelType.ABSOLUTE);
		
		addVariableType("BYTE", Byte.BYTES);
		addVariableType("WORD", Short.BYTES);
		
		// hexa
		addValueParser((value) -> {
			if (value.charAt(0) != '$')
			{
				throw new NumberFormatException("$ missing.");
			}
			
			value = value.substring(1);
			
			return Integer.parseInt(value, 16);
		});
		
		// binary
		addValueParser((value) -> {
			if (value.charAt(0) != '%')
			{
				throw new NumberFormatException("% missing.");
			}
			
			value = value.substring(1);
			
			return Integer.parseInt(value, 2);
		});
		
		// octal
		addValueParser((value) -> {
			if (value.charAt(0) != '0')
			{
				throw new NumberFormatException("0 missing.");
			}
			
			value = value.substring(1);

			return Integer.parseInt(value, 8);
		});
		
		// decimal
		addValueParser((value) -> {
			if (value.charAt(0) == '0')
			{
				throw new NumberFormatException("Can't start with 0.");
			}
			
			return Integer.parseInt(value);
		});
	}
	
	@Override
	protected void linkTogether(AssemblerState state, OutputStream output) throws IOException, CodeProcessException
	{
		byte offset[];
		
		int programStartAddress = state.getSegmentStartAddress(Segment.PROGRAM);
		byte program[] = state.getSegmentBytes(Segment.PROGRAM);
		
		int dataStartAddress = state.getSegmentStartAddress(Segment.DATA);
		byte data[] = state.getSegmentBytes(Segment.DATA);
		
		offset = new byte[programStartAddress];
		
		output.write(offset);
		output.write(program);

		int programSegmentEnd = offset.length + program.length;
		if (programSegmentEnd > dataStartAddress)
		{
			throw new CodeProcessException(-1, "Segments collision.");
		}
		
		int gap = dataStartAddress - programSegmentEnd;
		
		offset = new byte[gap];
		
		output.write(offset);
		output.write(data);
	}
	
	private enum TEK1608LabelType implements LabelType
	{
		RELATIVE(TEK1608InstructionSet.WORD_BYTES_SIZE)
		{
			@Override
			public int getOperandValue(int labelAddress, int actualPCAddress)
			{
				int offset = labelAddress - actualPCAddress;
				
				if (offset < Byte.MIN_VALUE || offset > Byte.MAX_VALUE)
				{
					throw new IllegalArgumentException("Offset size is bigger than <-128, 128> limit.");
				}
				
				return offset;
			}
		},
		ABSOLUTE(TEK1608InstructionSet.WORD_BYTES_SIZE * 2)
		{
			@Override
			public int getOperandValue(int labelAddress, int actualPCAddress)
			{
				if (labelAddress < 0 || labelAddress > 0xFFFF)
				{
					throw new IllegalArgumentException("Out of address space: " + labelAddress);
				}
				return labelAddress;
			}
		};
		
		private final int operandsBitsSize;
		
		private TEK1608LabelType(int operandsBitsSize)
		{
			this.operandsBitsSize = operandsBitsSize;
		}

		@Override
		public int getOperandsBitsSize()
		{
			return operandsBitsSize;
		}
	}
}
