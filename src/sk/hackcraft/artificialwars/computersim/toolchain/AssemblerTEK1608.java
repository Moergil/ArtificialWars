package sk.hackcraft.artificialwars.computersim.toolchain;

import sk.hackcraft.artificialwars.computersim.Endianness;
import sk.hackcraft.artificialwars.computersim.TEK1608InstructionSet;
import sk.hackcraft.artificialwars.computersim.parts.ProcessorTEK1608.TEK1608MemoryAddressing;

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
		
		addVariableType("BYTE", 1);
		addVariableType("WORD", 2);
		
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
	
	private enum TEK1608LabelType implements LabelType
	{
		RELATIVE(1)
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
		ABSOLUTE(2)
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
		
		private final int operandsBytesSize;
		
		private TEK1608LabelType(int operandsBytesSize)
		{
			this.operandsBytesSize = operandsBytesSize;
		}

		@Override
		public int getOperandsBytesSize()
		{
			return operandsBytesSize;
		}
	}
}
