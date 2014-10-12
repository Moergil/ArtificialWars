package sk.epholl.artificialwars.entities.instructionsets;

import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet;

public class EPH32InstructionSet extends InstructionSet
{
	public static final int SELF_DESTRUCT = -1;
	public static final int WAIT = 0;
	public static final int ADD = 1;
	public static final int SUB = 2;
	public static final int INC = 3;
	public static final int DEC = 4;
	public static final int SWP = 5;
	public static final int SETAB = 6;
	public static final int SETA = 8;
	public static final int SETB = 9;
	public static final int FIRE = 10;
	public static final int SCAN = 15;
	public static final int LOCK = 16;
	public static final int RND = 20;
	public static final int RNDB = 21;
	public static final int POSX = 30;
	public static final int POSY = 31;
	public static final int MOVE = 32;
	public static final int ROT = 33;
	public static final int SETMP = 40;
	public static final int INCMP = 41;
	public static final int DECMP = 42;
	public static final int MEMSAVE = 45;
	public static final int MEMLOAD = 46;
	public static final int JMP = 50;
	public static final int JMPZ = 51;
	public static final int JMPC = 52;
	public static final int JMPM = 53;
	public static final int JMPL = 54;
	
	
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
		
		add(WAIT, "wait", EPH32MemoryAddressing.IMMEDIATE);
		add(ADD, "add", EPH32MemoryAddressing.IMPLIED);
		add(SUB, "sub", EPH32MemoryAddressing.IMPLIED);
		add(INC, "inc", EPH32MemoryAddressing.IMPLIED);
		add(DEC, "dec", EPH32MemoryAddressing.IMPLIED);
		add(SWP, "swp", EPH32MemoryAddressing.IMPLIED);
		add(SETAB, "setab", EPH32MemoryAddressing.IMPLIED);
		add(SETA, "seta", EPH32MemoryAddressing.IMMEDIATE);
		add(SETB, "setb", EPH32MemoryAddressing.IMMEDIATE);
		add(FIRE, "fire", EPH32MemoryAddressing.IMPLIED);
		add(SCAN, "scan", EPH32MemoryAddressing.IMPLIED);
		add(LOCK, "lock", EPH32MemoryAddressing.IMPLIED);
		add(RND, "rnd", EPH32MemoryAddressing.IMMEDIATE);
		add(RNDB, "rndb", EPH32MemoryAddressing.IMPLIED);
		add(POSX, "posx", EPH32MemoryAddressing.IMPLIED);
		add(POSY, "posy", EPH32MemoryAddressing.IMPLIED);
		add(MOVE, "move", EPH32MemoryAddressing.IMMEDIATE);
		add(ROT, "rot", EPH32MemoryAddressing.IMPLIED);
		add(SETMP, "setmp", EPH32MemoryAddressing.IMPLIED);
		add(INCMP, "incmp", EPH32MemoryAddressing.IMPLIED);
		add(DECMP, "decmp", EPH32MemoryAddressing.IMPLIED);
		add(MEMSAVE, "memsave", EPH32MemoryAddressing.IMPLIED);
		add(MEMLOAD, "memload", EPH32MemoryAddressing.IMPLIED);
		add(JMP, "jmp", EPH32MemoryAddressing.IMMEDIATE);
		add(JMPZ, "jmpz", EPH32MemoryAddressing.IMMEDIATE);
		add(JMPC, "jmpc", EPH32MemoryAddressing.IMMEDIATE);
		add(JMPM, "jmpm", EPH32MemoryAddressing.IMMEDIATE);
		add(JMPL, "jmpl", EPH32MemoryAddressing.IMMEDIATE);
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
