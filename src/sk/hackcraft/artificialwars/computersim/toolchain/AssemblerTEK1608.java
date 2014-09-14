package sk.hackcraft.artificialwars.computersim.toolchain;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

import sk.hackcraft.artificialwars.computersim.parts.ProcessorTEK1608;
import sk.hackcraft.artificialwars.computersim.parts.ProcessorTEK1608.MemoryAddressing;

public class AssemblerTEK1608 extends Assembler<MemoryAddressing>
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
		
		addRegex(MemoryAddressing.ACCUMULATOR, "A");
		addRegex(MemoryAddressing.ABSOLUTE, "\\$([0-9A-F]{2})([0-9A-F]{2})");
		addRegex(MemoryAddressing.ABSOLUTE_INDEXED_X, "\\$([0-9A-F]{2})([0-9A-F]{2}),X");
		addRegex(MemoryAddressing.ABSOLUTE_INDEXED_Y, "\\$([0-9A-F]{2})([0-9A-F]{2}),Y");
		addRegex(MemoryAddressing.IMMEDIATE, "#\\$([0-9A-F]{2})");
		addRegex(MemoryAddressing.IMPLIED, "");
		addRegex(MemoryAddressing.INDIRECT, "\\(\\$([0-9A-F]{2})([0-9A-F]{2})\\)");
		addRegex(MemoryAddressing.X_INDEXED_INDIRECT, "\\(\\$([0-9A-F]{2}),X\\)");
		addRegex(MemoryAddressing.INDIRECT_Y_INDEXED, "\\(\\$([0-9A-F]{2})\\),Y");
		addRegex(MemoryAddressing.RELATIVE, "\\$([0-9A-F]{2})");
		addRegex(MemoryAddressing.ZEROPAGE, "\\$([0-9A-F]{2})");
		addRegex(MemoryAddressing.ZEROPAGE_X_INDEXED, "\\$([0-9A-F]{2}),X");
		addRegex(MemoryAddressing.ZEROPAGE_Y_INDEXED, "\\$([0-9A-F]{2}),Y");
	}
	
	@Override
	protected Map<MemoryAddressing, Pattern> createMemoryAddressingPatternsMap()
	{
		return new EnumMap<>(MemoryAddressing.class);
	}
}
