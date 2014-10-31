package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import sk.hackcraft.artificialwars.computersim.Endianness;
import sk.hackcraft.artificialwars.computersim.parts.TEK1608InstructionSet;
import sk.hackcraft.artificialwars.computersim.parts.TEK1608InstructionSet.TEK1608MemoryAddressing;

public class AssemblerTEK1608 extends AbstractAssembler
{	
	public AssemblerTEK1608()
	{
		super(TEK1608InstructionSet.getInstance(), Endianness.LITTLE, ".SEG", "(.+):");
		
		addSegmentIdentifier(Segment.PROGRAM, "PRG");
		addSegmentIdentifier(Segment.DATA, "DAT");

		// TODO sometimes absolute is used instead of zeropage, because absolute
		// addressing mode can match the value before zeropage
		// not acute but should be fixed
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
		
		// zero
		addValueParser((value) -> {
			if (!value.equals("0"))
			{
				throw new NumberFormatException("Not zero.");
			}
			
			return 0;
		});
		
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
	protected void export(AssemblerState state, OutputStream output) throws IOException, LinkingException
	{
		// NMI = 0,
		// RES = 1,
		// IRQ = 2;
		
		int addresses[] = {0xFFFA, 0xFFFC, 0xFFFE};
		String labelsNames[] = {"_NMI", "_RES", "_IRQ"};
		
		Map<String, Integer> labels = state.getLabels();

		for (int i = 0; i < 3; i++)
		{
			String labelName = labelsNames[i];
			Integer labelAddress = labels.get(labelName);
			
			if (labelAddress == null)
			{
				throw new LinkingException("Subroutine for handling " + labelName + " interrupt is missing.");
			}
			
			int address = addresses[i];
			byte value[] = endianness.valueToBytes(address, 2);
			
			output.write(value);
		}

		int programStartAddress = state.getSegmentStartAddress(Segment.PROGRAM);
		byte programStart[] = endianness.valueToBytes(programStartAddress, 2);
		byte program[] = state.getSegmentBytes(Segment.PROGRAM);
		byte programLength[] = endianness.valueToBytes(program.length, 2);
		
		int dataStartAddress = state.getSegmentStartAddress(Segment.DATA);
		byte dataStart[] = endianness.valueToBytes(dataStartAddress, 2);
		byte data[] = state.getSegmentBytes(Segment.DATA);
		byte dataLength[] = endianness.valueToBytes(data.length, 2);

		int programSegmentEnd = programStartAddress + program.length;
		if (programSegmentEnd > dataStartAddress)
		{
			throw new LinkingException("Segments collision.");
		}
		
		DataOutputStream dataOutput = new DataOutputStream(output);
		dataOutput.writeByte(2);
		
		output.write(programStart);
		output.write(programLength);
		output.write(program);
		
		output.write(dataStart);
		output.write(dataLength);
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
