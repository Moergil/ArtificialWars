package sk.epholl.artificialwars.entities.instructionsets;

import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet;

public class EPH32InstructionSet extends InstructionSet
{
	public static final int WORD_BYTES_SIZE = 4;
	
	private static final EPH32InstructionSet INSTANCE = new EPH32InstructionSet();
	
	public static EPH32InstructionSet getInstance()
	{
		return INSTANCE;
	}
	
	private OpcodeCompiler compiler = (opcode, operands, output) -> {
		output.writeInt(opcode.toInt());
		
		if (operands.length == 0)
		{
			output.writeInt(0);
		}
		else
		{
			output.write(operands);
		}
	};
	
	private EPH32InstructionSet()
	{
		super(WORD_BYTES_SIZE);
		
		add(0, "wait", EPH32MemoryAddressing.IMMEDIATE);
		add(1, "add", EPH32MemoryAddressing.IMPLIED);
		add(2, "sub", EPH32MemoryAddressing.IMPLIED);
		add(3, "inc", EPH32MemoryAddressing.IMPLIED);
		add(4, "dec", EPH32MemoryAddressing.IMPLIED);
		add(5, "swp", EPH32MemoryAddressing.IMPLIED);
		add(6, "setab", EPH32MemoryAddressing.IMPLIED);
		add(8, "seta", EPH32MemoryAddressing.IMMEDIATE);
		add(9, "setb", EPH32MemoryAddressing.IMMEDIATE);
		add(10, "fire", EPH32MemoryAddressing.IMPLIED);
		add(15, "scan", EPH32MemoryAddressing.IMPLIED);
		add(16, "lock", EPH32MemoryAddressing.IMPLIED);
		add(20, "rnd", EPH32MemoryAddressing.IMMEDIATE);
		add(21, "rndb", EPH32MemoryAddressing.IMPLIED);
		add(30, "posx", EPH32MemoryAddressing.IMPLIED);
		add(31, "posy", EPH32MemoryAddressing.IMPLIED);
		add(32, "move", EPH32MemoryAddressing.IMPLIED);
		add(40, "setmp", EPH32MemoryAddressing.IMPLIED);
		add(41, "incmp", EPH32MemoryAddressing.IMPLIED);
		add(42, "decmp", EPH32MemoryAddressing.IMPLIED);
		add(45, "memsave", EPH32MemoryAddressing.IMPLIED);
		add(46, "memload", EPH32MemoryAddressing.IMPLIED);
		add(50, "jmp", EPH32MemoryAddressing.IMMEDIATE);
		add(51, "jmpz", EPH32MemoryAddressing.IMMEDIATE);
		add(52, "jmpc", EPH32MemoryAddressing.IMMEDIATE);
		add(53, "jmpm", EPH32MemoryAddressing.IMMEDIATE);
		add(54, "jmpl", EPH32MemoryAddressing.IMMEDIATE);
	}
	
	private void add(int code, String name, EPH32MemoryAddressing ma)
	{
		add(code, name, ma, compiler);
	}

	@Override
	protected int calculateWordsSize(int code, MemoryAddressing memoryAddressing)
	{
		return 1;
	}
	
	public enum EPH32MemoryAddressing implements InstructionSet.MemoryAddressing
	{
		IMPLIED(0, "imp"),
		IMMEDIATE(1, "imm");

		private final int operandsWordsSize;
		private final String shortName;
		
		private EPH32MemoryAddressing(int operandsWordsSize, String shortName)
		{
			this.operandsWordsSize = operandsWordsSize;
			this.shortName = shortName;
		}
		
		public int getOperandsWordsSize()
		{
			return operandsWordsSize;
		}
		
		@Override
		public String getShortName()
		{
			return shortName;
		}
	}
}
