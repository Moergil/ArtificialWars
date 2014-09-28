package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class InstructionSet
{
	protected final int wordBytesSize;
	
	private final Map<Integer, Opcode> opcodes = new HashMap<>();
	private final Map<String, InstructionRecord> instructions = new HashMap<>();
	
	public InstructionSet(int wordBytesSize)
	{
		this.wordBytesSize = wordBytesSize;
	}
	
	public int getWordBytesSize()
	{
		return wordBytesSize;
	}

	public void add(int code, Object name, MemoryAddressing memoryAddressing, OpcodeCompiler compiler)
	{
		add(code, name.toString(), memoryAddressing, compiler);
	}
	
	public void add(int code, String name, MemoryAddressing memoryAddressing, OpcodeCompiler compiler)
	{
		if (opcodes.containsKey(code))
		{
			throw new IllegalArgumentException(String.format("Code %d already used."));
		}
		
		int bytesSize = calculateBytesSize(code, memoryAddressing);
		
		Opcode opcode = new OpcodeRecord(code, name.toString(), memoryAddressing, bytesSize, compiler);
		
		InstructionRecord instruction = instructions.get(name);
		if (instruction == null)
		{
			instruction = new InstructionRecord(name);
			instructions.put(name, instruction);
		}
		else if (instruction.hasMemoryAddressing(memoryAddressing))
		{
			throw new IllegalArgumentException(String.format("Memory addressing %s already defined for instruction %s", memoryAddressing, name));
		}
		
		opcodes.put(code, opcode);
		instruction.addOpcode(memoryAddressing, opcode);
	}
	
	protected abstract int calculateBytesSize(int code, MemoryAddressing memoryAddressing);

	public Set<Instruction> getAllInstructions()
	{
		Set<Instruction> set = new TreeSet<>((Instruction ins1, Instruction ins2) -> ins1.getName().compareTo(ins2.getName()));
		
		set.addAll(instructions.values());
		
		return set;
	}
	
	public Instruction getInstruction(String name)
	{
		return instructions.get(name);
	}
	
	public Opcode getOpcode(int code)
	{
		return opcodes.get(code);
	}
	
	public interface Opcode
	{
		int toInt();		
		int getBytesSize();
		int getWordsSize();
		
		String getInstructionName();
		MemoryAddressing getMemoryAddressing();
		
		void compile(byte operands[], DataOutput output) throws IOException;
	}
	
	@FunctionalInterface
	public interface OpcodeCompiler
	{
		void compile(Opcode opcode, byte data[], DataOutput output) throws IOException;
	}
	
	public interface Instruction
	{
		Opcode getOpcode(MemoryAddressing memoryAddressing);
		String getName();
		
		boolean hasMemoryAddressing(MemoryAddressing memoryAddressing);
		
		Set<MemoryAddressing> getMemoryAddressings();
	}
	
	public interface MemoryAddressing
	{
		int getOperandsBytesSize();
		String getShortName();
	}
	
	private static class InstructionRecord implements Instruction
	{
		private final String name;
		
		private final Map<MemoryAddressing, Opcode> opcodes = new HashMap<>();
		
		public InstructionRecord(String name)
		{
			this.name = name;
		}
		
		public void addOpcode(MemoryAddressing memoryAddressing, Opcode opcode)
		{
			this.opcodes.put(memoryAddressing, opcode);
		}
		
		@Override
		public Opcode getOpcode(MemoryAddressing memoryAddressing)
		{
			return opcodes.get(memoryAddressing);
		}

		@Override
		public String getName()
		{
			return name;
		}
		
		@Override
		public boolean hasMemoryAddressing(MemoryAddressing memoryAddressing)
		{
			return opcodes.containsKey(memoryAddressing);
		}

		@Override
		public Set<MemoryAddressing> getMemoryAddressings()
		{
			return opcodes.keySet();
		}
	}
	
	private class OpcodeRecord implements Opcode
	{
		private final int opcode;
		private final String name;
		private final MemoryAddressing memoryAddressing;
		
		private final int wordsSize;
		private final OpcodeCompiler compiler;
		
		public OpcodeRecord(int opcode, String name, MemoryAddressing memoryAddressing, int wordsSize, OpcodeCompiler compiler)
		{
			this.opcode = opcode;
			this.name = name;
			this.memoryAddressing = memoryAddressing;
			
			this.wordsSize = wordsSize;
			this.compiler = compiler;
		}

		@Override
		public int toInt()
		{
			return opcode;
		}

		@Override
		public String getInstructionName()
		{
			return name;
		}

		@Override
		public MemoryAddressing getMemoryAddressing()
		{
			return memoryAddressing;
		}

		@Override
		public int getWordsSize()
		{
			return wordsSize;
		}
		
		@Override
		public int getBytesSize()
		{
			return wordsSize / getWordsSize();
		}
		
		@Override
		public void compile(byte data[], DataOutput output) throws IOException
		{
			compiler.compile(this, data, output);
		}
		
		@Override
		public String toString()
		{
			return String.format("%s %s (%d)", name, memoryAddressing, opcode); 
		}
	}
}
