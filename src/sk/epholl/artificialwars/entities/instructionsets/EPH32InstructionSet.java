package sk.epholl.artificialwars.entities.instructionsets;

import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet;

public class EPH32InstructionSet extends InstructionSet
{
	public EPH32InstructionSet()
	{
		InstructionCompiler compiler = (ins, ma, operandBytes, output) -> {
			output.writeInt(ins.getCode(ma));
			output.write(operandBytes);
		};

		add("wait", 0, EPH32MemoryAddressing.IMPLIED, compiler);
		add("add", 1, EPH32MemoryAddressing.IMPLIED, compiler);
		add("sub", 2, EPH32MemoryAddressing.IMPLIED, compiler);
		add("inc", 3, EPH32MemoryAddressing.IMPLIED, compiler);
		add("dec", 4, EPH32MemoryAddressing.IMPLIED, compiler);
		add("swp", 5, EPH32MemoryAddressing.IMPLIED, compiler);
		add("setab", 6, EPH32MemoryAddressing.IMPLIED, compiler);
		add("seta", 8, EPH32MemoryAddressing.IMMEDIATE, compiler);
		add("setb", 9, EPH32MemoryAddressing.IMMEDIATE, compiler);
		add("fire", 10, EPH32MemoryAddressing.IMPLIED, compiler);
		add("scan", 15, EPH32MemoryAddressing.IMPLIED, compiler);
		add("lock", 16, EPH32MemoryAddressing.IMPLIED, compiler);
		add("rnd", 20, EPH32MemoryAddressing.IMMEDIATE, compiler);
		add("rndb", 21, EPH32MemoryAddressing.IMPLIED, compiler);
		add("posx", 30, EPH32MemoryAddressing.IMPLIED, compiler);
		add("posy", 31, EPH32MemoryAddressing.IMPLIED, compiler);
		add("move", 32, EPH32MemoryAddressing.IMPLIED, compiler);
		add("setmp", 40, EPH32MemoryAddressing.IMPLIED, compiler);
		add("incmp", 41, EPH32MemoryAddressing.IMPLIED, compiler);
		add("decmp", 42, EPH32MemoryAddressing.IMPLIED, compiler);
		add("memsave", 45, EPH32MemoryAddressing.IMPLIED, compiler);
		add("memload", 46, EPH32MemoryAddressing.IMPLIED, compiler);
		add("jmp", 50, EPH32MemoryAddressing.IMMEDIATE, compiler);
		add("jmpz", 51, EPH32MemoryAddressing.IMMEDIATE, compiler);
		add("jmpc", 52, EPH32MemoryAddressing.IMMEDIATE, compiler);
		add("jmpm", 53, EPH32MemoryAddressing.IMMEDIATE, compiler);
		add("jmpl", 54, EPH32MemoryAddressing.IMMEDIATE, compiler);
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
