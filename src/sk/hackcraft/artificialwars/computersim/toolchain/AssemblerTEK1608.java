package sk.hackcraft.artificialwars.computersim.toolchain;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

import sk.hackcraft.artificialwars.computersim.parts.ProcessorTEK1608;
import sk.hackcraft.artificialwars.computersim.parts.ProcessorTEK1608.TEK1608MemoryAddressing;

public class AssemblerTEK1608 extends Assembler
{
	/*
	
A			....	Accumulator	 			OPC A	 		operand is AC
abs			....	absolute	 			OPC $HHLL	 	operand is address $HHLL
abs,X		....	absolute, X-indexed	 	OPC $HHLL,X	 	operand is address incremented by X with carry
abs,Y		....	absolute, Y-indexed	 	OPC $HHLL,Y	 	operand is address incremented by Y with carry
#			....	immediate	 			OPC #$BB	 	operand is byte (BB)
impl		....	implied	 				OPC	 			operand implied
ind			....	indirect	 			OPC ($HHLL)	 	operand is effective address; effective address is value of address
X,ind		....	X-indexed, indirect	 	OPC ($BB,X)	 	operand is effective zeropage address; effective address is byte (BB) incremented by X without carry
ind,Y		....	indirect, Y-indexed	 	OPC ($LL),Y	 	operand is effective address incremented by Y with carry; effective address is word at zeropage address
rel			....	relative	 			OPC $BB	 		branch target is PC + offset (BB), bit 7 signifies negative offset
zpg			....	zeropage	 			OPC $LL	 		operand is of address; address hibyte = zero ($00xx)
zpg,X		....	zeropage, X-indexed	 	OPC $LL,X	 	operand is address incremented by X; address hibyte = zero ($00xx); no page transition
zpg,Y		....	zeropage, Y-indexed	 	OPC $LL,Y	 	operand is address incremented by Y; address hibyte = zero ($00xx); no page transition
    */
	// TODO
	public static String exampleAssembly1 = ""
			+ "ASL A\n"
			+ "ADC $0010\n"
			+ "ADC $001A,X\n"
			+ "LDX $001B,Y\n"
			+ "ADC #$05\n"
			+ "TYA\n"
			+ "JMP ($001C)\n"
			+ "ADC ($11,X)\n"
			+ "ADC ($12),Y\n"
			+ "BCS $03\n"
			+ "ADC $05\n"
			+ "ADC $05,X\n"
			+ "LDX $05,Y";
	
	public AssemblerTEK1608()
	{
		super(new ProcessorTEK1608().getInstructionSet(), "^([A-Z]{3})$");
		
		addRegex(TEK1608MemoryAddressing.ACCUMULATOR, "A");
		addRegex(TEK1608MemoryAddressing.ABSOLUTE, "\\$([0-9A-F]{2})([0-9A-F]{2})");
		addRegex(TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X, "\\$([0-9A-F]{2})([0-9A-F]{2}),X");
		addRegex(TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y, "\\$([0-9A-F]{2})([0-9A-F]{2}),Y");
		addRegex(TEK1608MemoryAddressing.IMMEDIATE, "#\\$([0-9A-F]{2})");
		addRegex(TEK1608MemoryAddressing.IMPLIED, "");
		addRegex(TEK1608MemoryAddressing.INDIRECT, "\\(\\$([0-9A-F]{2})([0-9A-F]{2})\\)");
		addRegex(TEK1608MemoryAddressing.X_INDEXED_INDIRECT, "\\(\\$([0-9A-F]{2}),X\\)");
		addRegex(TEK1608MemoryAddressing.INDIRECT_Y_INDEXED, "\\(\\$([0-9A-F]{2})\\),Y");
		addRegex(TEK1608MemoryAddressing.RELATIVE, "\\$([0-9A-F]{2})");
		addRegex(TEK1608MemoryAddressing.ZEROPAGE, "\\$([0-9A-F]{2})");
		addRegex(TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED, "\\$([0-9A-F]{2}),X");
		addRegex(TEK1608MemoryAddressing.ZEROPAGE_Y_INDEXED, "\\$([0-9A-F]{2}),Y");

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
	}
	
	private enum TEK1608LabelType implements LabelType
	{
		RELATIVE(1)
		{
			@Override
			public String getOperandValue(int labelAddress, int actualPCAddress)
			{
				int offset = labelAddress - actualPCAddress;
				
				if (offset < Byte.MIN_VALUE || offset > Byte.MAX_VALUE)
				{
					throw new IllegalArgumentException("Offset size is bigger than <-128, 128> limit.");
				}
				
				return String.format("$%02X", (byte)offset);
			}
		},
		ABSOLUTE(2)
		{
			@Override
			public String getOperandValue(int labelAddress, int actualPCAddress)
			{
				return String.format("$%04X", labelAddress);
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
