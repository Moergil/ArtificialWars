package sk.epholl.artificialwars.entities.instructionsets;

import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet;

public class EPH32InstructionSet extends InstructionSet
{
	public EPH32InstructionSet()
	{
		add(0, "wait", EPH32MemoryAddressing.IMPLIED);
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

	@Override
	protected int calculateBytesSize(int code, MemoryAddressing memoryAddressing)
	{
		return 8;
	}
	
	public enum EPH32MemoryAddressing implements InstructionSet.MemoryAddressing
	{
		IMPLIED(0, "IMP"),
		IMMEDIATE(4, "IMM");
		
		private final int operandsBytesSize;
		private final String shortName;
		
		private EPH32MemoryAddressing(int operandsBytesSize, String shortName)
		{
			this.operandsBytesSize = operandsBytesSize;
			this.shortName = shortName;
		}
		
		public int getOperandsBytesSize()
		{
			return operandsBytesSize;
		}
		
		@Override
		public String getShortName()
		{
			return shortName;
		}
	}
}
