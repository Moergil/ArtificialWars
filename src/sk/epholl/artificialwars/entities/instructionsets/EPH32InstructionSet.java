package sk.epholl.artificialwars.entities.instructionsets;

import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet;

public class EPH32InstructionSet extends InstructionSet
{
	public EPH32InstructionSet()
	{
		InstructionCompiler compiler = (ins, ma, m, o) -> {
			o.writeInt(ins.getCode(ma));
			
			int parameter = (m.groupCount() > 0) ? Integer.decode(m.group(1)) : 0;
			o.writeInt(parameter);
		};

		add("wait", 0, MemoryAddressing.IMPLIED, compiler);
		add("add", 1, MemoryAddressing.IMPLIED, compiler);
		add("sub", 2, MemoryAddressing.IMPLIED, compiler);
		add("inc", 3, MemoryAddressing.IMPLIED, compiler);
		add("dec", 4, MemoryAddressing.IMPLIED, compiler);
		add("swp", 5, MemoryAddressing.IMPLIED, compiler);
		add("setab", 6, MemoryAddressing.IMPLIED, compiler);
		add("seta", 8, MemoryAddressing.IMMEDIATE, compiler);
		add("setb", 9, MemoryAddressing.IMMEDIATE, compiler);
		add("fire", 10, MemoryAddressing.IMPLIED, compiler);
		add("scan", 15, MemoryAddressing.IMPLIED, compiler);
		add("lock", 16, MemoryAddressing.IMPLIED, compiler);
		add("rnd", 20, MemoryAddressing.IMMEDIATE, compiler);
		add("rndb", 21, MemoryAddressing.IMPLIED, compiler);
		add("posx", 30, MemoryAddressing.IMPLIED, compiler);
		add("posy", 31, MemoryAddressing.IMPLIED, compiler);
		add("move", 32, MemoryAddressing.IMPLIED, compiler);
		add("setmp", 40, MemoryAddressing.IMPLIED, compiler);
		add("incmp", 41, MemoryAddressing.IMPLIED, compiler);
		add("decmp", 42, MemoryAddressing.IMPLIED, compiler);
		add("memsave", 45, MemoryAddressing.IMPLIED, compiler);
		add("memload", 46, MemoryAddressing.IMPLIED, compiler);
		add("jmp", 50, MemoryAddressing.IMMEDIATE, compiler);
		add("jmpz", 51, MemoryAddressing.IMMEDIATE, compiler);
		add("jmpc", 52, MemoryAddressing.IMMEDIATE, compiler);
		add("jmpm", 53, MemoryAddressing.IMMEDIATE, compiler);
		add("jmpl", 54, MemoryAddressing.IMMEDIATE, compiler);
	}
	
	public enum MemoryAddressing implements InstructionSet.MemoryAddressing
	{
		IMPLIED(0),
		IMMEDIATE(4);
		
		private int operandsBytesSize;
		
		private MemoryAddressing(int operandsBytesSize)
		{
			this.operandsBytesSize = operandsBytesSize;
		}
		
		public int getOperandsBytesSize()
		{
			return operandsBytesSize;
		}
	}
}
