package sk.hackcraft.artificialwars.computersim;

import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet;

public class TEK1608InstructionSet extends InstructionSet
{
	public static final int WORD_BYTES_SIZE = 1;
	
	private static final TEK1608InstructionSet INSTANCE = new TEK1608InstructionSet();
	
	public static TEK1608InstructionSet getInstance()
	{
		return INSTANCE;
	}

	private TEK1608InstructionSet()
	{
		super(WORD_BYTES_SIZE);
		
		// ADC add with carry
		add(0x69, Name.ADC, TEK1608MemoryAddressing.IMMEDIATE);
		add(0x65, Name.ADC, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x75, Name.ADC, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x6D, Name.ADC, TEK1608MemoryAddressing.ABSOLUTE);
		add(0x7D, Name.ADC, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		add(0x79, Name.ADC, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y);
		add(0x61, Name.ADC, TEK1608MemoryAddressing.X_INDEXED_INDIRECT);
		add(0x71, Name.ADC, TEK1608MemoryAddressing.INDIRECT_Y_INDEXED);
		
		// AND logical and
		add(0x29, Name.AND, TEK1608MemoryAddressing.IMMEDIATE);
		add(0x25, Name.AND, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x35, Name.AND, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x2D, Name.AND, TEK1608MemoryAddressing.ABSOLUTE);
		add(0x3D, Name.AND, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		add(0x39, Name.AND, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y);
		add(0x21, Name.AND, TEK1608MemoryAddressing.X_INDEXED_INDIRECT);
		add(0x31, Name.AND, TEK1608MemoryAddressing.INDIRECT_Y_INDEXED);
		
		// ASL shift left one bit
		add(0x0A, Name.ASL, TEK1608MemoryAddressing.ACCUMULATOR);
		add(0x06, Name.ASL, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x16, Name.ASL, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x0E, Name.ASL, TEK1608MemoryAddressing.ABSOLUTE);
		add(0x1E, Name.ASL, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		
		// BCC branch on carry clear
		add(0x90, Name.BCC, TEK1608MemoryAddressing.RELATIVE);
		
		// BCS branch on carry set
		add(0xB0, Name.BCS, TEK1608MemoryAddressing.RELATIVE);
		
		// BEQ branch on result zero
		add(0xF0, Name.BEQ, TEK1608MemoryAddressing.RELATIVE);
		
		// BIT test bits in memory with accumulator TODO
		add(0x24, Name.BIT, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x2C, Name.BIT, TEK1608MemoryAddressing.ABSOLUTE);
		
		// BMI branch on result minus
		add(0x30, Name.BMI, TEK1608MemoryAddressing.RELATIVE);
		
		// BNE branch on result not zero
		add(0xD0, Name.BNE, TEK1608MemoryAddressing.RELATIVE);
		
		// BPL branch on result plus
		add(0x10, Name.BPL, TEK1608MemoryAddressing.RELATIVE);
		
		// BRK force break
		// TODO,
		
		// BVC branch on overflow clear
		add(0x50, Name.BVC, TEK1608MemoryAddressing.RELATIVE);
		
		// BVS branch on overflow set
		add(0x70, Name.BVS, TEK1608MemoryAddressing.RELATIVE);
		
		// CLC clear carry flag
		add(0x18, Name.CLC, TEK1608MemoryAddressing.IMPLIED);
		
		// CLD clear decimal mode
		// UNSUPPORTED,
		
		// CLI clear interrupt disable bit
		// TODO,
		
		// CLV clear overflow flag
		add(0xB8, Name.CLV, TEK1608MemoryAddressing.IMPLIED);
		
		// CMP compare memory with accumulator
		add(0xC9, Name.CMP, TEK1608MemoryAddressing.IMMEDIATE);
		add(0xC5, Name.CMP, TEK1608MemoryAddressing.ZEROPAGE);
		add(0xD5, Name.CMP, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0xCD, Name.CMP, TEK1608MemoryAddressing.ABSOLUTE);
		add(0xDD, Name.CMP, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		add(0xD9, Name.CMP, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y);
		add(0xC1, Name.CMP, TEK1608MemoryAddressing.X_INDEXED_INDIRECT);
		add(0xD1, Name.CMP, TEK1608MemoryAddressing.INDIRECT_Y_INDEXED);
		
		// CPX compare memory and index X
		add(0xE0, Name.CPX, TEK1608MemoryAddressing.IMMEDIATE);
		add(0xE4, Name.CPX, TEK1608MemoryAddressing.ZEROPAGE);
		add(0xEC, Name.CPX, TEK1608MemoryAddressing.ABSOLUTE);
		
		// CPY compare memory and index Y
		add(0xC0, Name.CPY, TEK1608MemoryAddressing.IMMEDIATE);
		add(0xC4, Name.CPY, TEK1608MemoryAddressing.ZEROPAGE);
		add(0xCC, Name.CPY, TEK1608MemoryAddressing.ABSOLUTE);
		
		// DEC decrement memory by one
		add(0xC6, Name.DEC, TEK1608MemoryAddressing.ZEROPAGE);
		add(0xD6, Name.DEC, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0xCE, Name.DEC, TEK1608MemoryAddressing.ABSOLUTE);
		add(0xDE, Name.DEC, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		
		// DEX decrement index X by one
		add(0xCA, Name.DEX, TEK1608MemoryAddressing.IMPLIED);
		
		// DEY decrement index Y by one
		add(0x88, Name.DEY, TEK1608MemoryAddressing.IMPLIED);
		
		// EOR xor memory with accumulator
		add(0x49, Name.EOR, TEK1608MemoryAddressing.IMMEDIATE);
		add(0x45, Name.EOR, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x55, Name.EOR, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x4D, Name.EOR, TEK1608MemoryAddressing.ABSOLUTE);
		add(0x5D, Name.EOR, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		add(0x59, Name.EOR, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y);
		add(0x41, Name.EOR, TEK1608MemoryAddressing.X_INDEXED_INDIRECT);
		add(0x51, Name.EOR, TEK1608MemoryAddressing.INDIRECT_Y_INDEXED);
		
		// INC increment memory by one
		add(0xE6, Name.INC, TEK1608MemoryAddressing.ZEROPAGE);
		add(0xF6, Name.INC, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0xEE, Name.INC, TEK1608MemoryAddressing.ABSOLUTE);
		add(0xFE, Name.INC, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		
		// INX increment index X by one
		add(0xE8, Name.INX, TEK1608MemoryAddressing.IMPLIED);
		
		// INY increment index Y by one
		add(0xC8, Name.INY, TEK1608MemoryAddressing.IMPLIED);
		
		// JMP jump to setInstruction(0x, new location
		add(0x4C, Name.JMP, TEK1608MemoryAddressing.ABSOLUTE);
		add(0x6C, Name.JMP, TEK1608MemoryAddressing.INDIRECT);
		
		// JSR jump to setInstruction(0x, new location saving return address
		add(0x20, Name.JSR, TEK1608MemoryAddressing.IMPLIED);
		
		// LDA load accumulator with memory
		add(0xA9, Name.LDA, TEK1608MemoryAddressing.IMMEDIATE);
		add(0xA5, Name.LDA, TEK1608MemoryAddressing.ZEROPAGE);
		add(0xB5, Name.LDA, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0xAD, Name.LDA, TEK1608MemoryAddressing.ABSOLUTE);
		add(0xBD, Name.LDA, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		add(0xB9, Name.LDA, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y);
		add(0xA1, Name.LDA, TEK1608MemoryAddressing.X_INDEXED_INDIRECT);
		add(0xB1, Name.LDA, TEK1608MemoryAddressing.INDIRECT_Y_INDEXED);
		
		// LDX load index X with memory
		add(0xA2, Name.LDX, TEK1608MemoryAddressing.IMMEDIATE);
		add(0xA6, Name.LDX, TEK1608MemoryAddressing.ZEROPAGE);
		add(0xB6, Name.LDX, TEK1608MemoryAddressing.ZEROPAGE_Y_INDEXED);
		add(0xAE, Name.LDX, TEK1608MemoryAddressing.ABSOLUTE);
		add(0xBE, Name.LDX, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y);
		
		// LDY load index Y with memory
		add(0xA0, Name.LDY, TEK1608MemoryAddressing.IMMEDIATE);
		add(0xA4, Name.LDY, TEK1608MemoryAddressing.ZEROPAGE);
		add(0xB4, Name.LDY, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0xAC, Name.LDY, TEK1608MemoryAddressing.ABSOLUTE);
		add(0xBC, Name.LDY, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		
		// LSR shift one bit right
		add(0x4A, Name.LSR, TEK1608MemoryAddressing.ACCUMULATOR);
		add(0x46, Name.LSR, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x56, Name.LSR, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x4E, Name.LSR, TEK1608MemoryAddressing.ABSOLUTE);
		add(0x5E, Name.LSR, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		
		// NOP
		add(0xEA, Name.NOP, TEK1608MemoryAddressing.IMPLIED);
		
		// ORA or memory with accumulator
		add(0x09, Name.ORA, TEK1608MemoryAddressing.IMMEDIATE);
		add(0x05, Name.ORA, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x15, Name.ORA, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x0D, Name.ORA, TEK1608MemoryAddressing.ABSOLUTE);
		add(0x1D, Name.ORA, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		add(0x19, Name.ORA, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y);
		add(0x01, Name.ORA, TEK1608MemoryAddressing.X_INDEXED_INDIRECT);
		add(0x11, Name.ORA, TEK1608MemoryAddressing.INDIRECT_Y_INDEXED);
		
		// PHA push accumulator on stack
		add(0x48, Name.PHA, TEK1608MemoryAddressing.IMPLIED);
		
		// PHP push processor status on stack
		add(0x08, Name.PHP, TEK1608MemoryAddressing.IMPLIED);
		
		// PLA pull accumulator from stack
		add(0x68, Name.PLA, TEK1608MemoryAddressing.IMPLIED);
		
		// PLP pull processor status from stack
		add(0x28, Name.PLP, TEK1608MemoryAddressing.IMPLIED);
		
		// ROL rotate one bit left
		add(0x2A, Name.ROL, TEK1608MemoryAddressing.ACCUMULATOR);
		add(0x26, Name.ROL, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x36, Name.ROL, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x2E, Name.ROL, TEK1608MemoryAddressing.ABSOLUTE);
		add(0x3E, Name.ROL, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		
		// ROR rotate one bit right
		add(0x6A, Name.ROR, TEK1608MemoryAddressing.ACCUMULATOR);
		add(0x66, Name.ROR, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x76, Name.ROR, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x6E, Name.ROR, TEK1608MemoryAddressing.ABSOLUTE);
		add(0x7E, Name.ROR, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		
		// RTI return from interrupt
		// TODO
				
		// RTS return from subroutine
		add(0x60, Name.RTS, TEK1608MemoryAddressing.IMPLIED);
		
		// SBC subtract memory from accumulator with borrow
		add(0xE9, Name.SBC, TEK1608MemoryAddressing.IMMEDIATE);
		add(0xE5, Name.SBC, TEK1608MemoryAddressing.ZEROPAGE);
		add(0xF5, Name.SBC, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0xED, Name.SBC, TEK1608MemoryAddressing.ABSOLUTE);
		add(0xFD, Name.SBC, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		add(0xFA, Name.SBC, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y);
		add(0xE1, Name.SBC, TEK1608MemoryAddressing.X_INDEXED_INDIRECT);
		add(0xF1, Name.SBC, TEK1608MemoryAddressing.INDIRECT_Y_INDEXED);
		
		// SEC set carry flag
		add(0x38, Name.SEC, TEK1608MemoryAddressing.IMPLIED);
		
		// SED set decimal flag 
		// UNSUPORTED
		
		// SEI set interrupt disable status
		// TODO,
		
		// STA store accumulator in memory
		add(0x85, Name.STA, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x95, Name.STA, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x8D, Name.STA, TEK1608MemoryAddressing.ABSOLUTE);
		add(0x9D, Name.STA, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X);
		add(0x99, Name.STA, TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y);
		add(0x81, Name.STA, TEK1608MemoryAddressing.X_INDEXED_INDIRECT);
		add(0x91, Name.STA, TEK1608MemoryAddressing.INDIRECT_Y_INDEXED);
		
		// STX store index X in memory
		add(0x86, Name.STX, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x96, Name.STX, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x8E, Name.STX, TEK1608MemoryAddressing.ABSOLUTE);
		
		// STY store index Y in memory
		add(0x84, Name.STY, TEK1608MemoryAddressing.ZEROPAGE);
		add(0x94, Name.STY, TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED);
		add(0x8C, Name.STY, TEK1608MemoryAddressing.ABSOLUTE);
		
		// TAX transfer accumulator to index X
		add(0xAA, Name.TAX, TEK1608MemoryAddressing.IMPLIED);
		
		// TAY transfer accumulator to index Y
		add(0xA8, Name.TAY, TEK1608MemoryAddressing.IMPLIED);
		
		// TSX transfer stack register to index X
		add(0xBA, Name.TSX, TEK1608MemoryAddressing.IMPLIED);
		
		// TXA transfer index X to accumulator
		add(0x8A, Name.TXA, TEK1608MemoryAddressing.IMPLIED);
		
		// TXS transfer index X to stack register
		add(0x9A, Name.TXS, TEK1608MemoryAddressing.IMPLIED);
		
		// TYA transfer index Y to accumulator
		add(0x98, Name.TYA, TEK1608MemoryAddressing.IMPLIED);
	}
	
	private void add(int code, Name name, TEK1608MemoryAddressing ma)
	{
		OpcodeCompiler compiler = (opcode, data, output) -> {
			output.writeByte((byte)opcode.toInt());
			
			if (data.length != opcode.getMemoryAddressing().getOperandsBytesSize())
			{
				throw new IllegalArgumentException("Illegal data length.");
			}
			
			output.write(data);
		};
		
		add(code, name, ma, compiler);
	}
	
	@Override
	protected int calculateBytesSize(int code, MemoryAddressing memoryAddressing)
	{
		return getWordBytesSize() + memoryAddressing.getOperandsBytesSize();
	}
	
	public enum TEK1608MemoryAddressing implements InstructionSet.MemoryAddressing
	{
		/**
		 * Operand is AC.
		 */
		ACCUMULATOR(0, "ACC"),
		/**
		 * Operand is address $HHLL.
		 */
		ABSOLUTE(WORD_BYTES_SIZE * 2, "ABS"),
		/**
		 * Operand is address incremented by X with carry.
		 */
		ABSOLUTE_INDEXED_X(WORD_BYTES_SIZE * 2, "ABX"),
		/**
		 * Operand is address incremented by Y with carry.
		 */
		ABSOLUTE_INDEXED_Y(WORD_BYTES_SIZE * 2, "ABY"),
		/**
		 * Operand is byte (BB).
		 */
		IMMEDIATE(WORD_BYTES_SIZE, "IMM"),
		/**
		 * Operand implied.
		 */
		IMPLIED(0, "IMP"),
		/**
		 * Operand is effective address; effective address is value of address.
		 */
		INDIRECT(WORD_BYTES_SIZE * 2, "IND"),
		/**
		 * Operand is effective zeropage address; effective address is byte (BB) incremented by X without carry.
		 */
		X_INDEXED_INDIRECT(WORD_BYTES_SIZE, "INX"),
		/**
		 * Operand is effective address incremented by Y with carry; effective address is word at zeropage address.
		 */
		INDIRECT_Y_INDEXED(WORD_BYTES_SIZE, "INY"),
		/**
		 * Branch target is PC + offset (BB), bit 7 signifies negative offset.
		 */
		RELATIVE(WORD_BYTES_SIZE, "REL"),
		/**
		 * Operand is of address; address hibyte = zero ($00xx).
		 */
		ZEROPAGE(WORD_BYTES_SIZE, "ZPG"),
		/**
		 * Operand is address incremented by X; address hibyte = zero ($00xx); no page transition.
		 */
		ZEROPAGE_X_INDEXED(WORD_BYTES_SIZE, "ZPX"),
		/**
		 * Operand is address incremented by Y; address hibyte = zero ($00xx); no page transition.
		 */
		ZEROPAGE_Y_INDEXED(WORD_BYTES_SIZE, "ZPY");
		
		private final int operandsBytesSize;
		private final String shortName;
		
		private TEK1608MemoryAddressing(int operandsBytesSize, String shortName)
		{
			this.operandsBytesSize = operandsBytesSize;
			this.shortName = shortName;
		}
		
		@Override
		public int getOperandsBytesSize()
		{
			return operandsBytesSize;
		}
		
		public String getShortName()
		{
			return shortName;
		}
	}
	
	public enum Name
	{
		ADC,
		AND,
		ASL,
		BCC,
		BCS,
		BEQ,
		BIT,
		BMI,
		BNE,
		BPL,
		BRK,
		BVC,
		BVS,
		CLC,
		CLD,
		CLI,
		CLV,
		CMP,
		CPX,
		CPY,
		DEC,
		DEX,
		DEY,
		EOR,
		INC,
		INX,
		INY,
		JMP,
		JSR,
		LDA,
		LDX,
		LDY,
		LSR,
		NOP,
		ORA,
		PHA,
		PHP,
		PLA,
		PLP,
		ROL,
		ROR,
		RTI,
		RTS,
		SBC,
		SEC,
		SED,
		SEI,
		STA,
		STX,
		STY,
		TAX,
		TAY,
		TSX,
		TXA,
		TXS,
		TYA,
	}
}
