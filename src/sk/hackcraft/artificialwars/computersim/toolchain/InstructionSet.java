package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;

public class InstructionSet
{
	private final Map<String, InstructionRecord> instructionsByName = new HashMap<>();
	private final Map<Integer, String> instructionNames = new HashMap<>();
	
	public void add(String name, final int code, MemoryAddressing memoryAddressing, InstructionCompiler compiler)
	{
		if (instructionNames.containsKey(code))
		{
			throw new IllegalArgumentException(String.format("Code %d already used."));
		}
		
		instructionNames.put(code, name);
		
		if (!instructionsByName.containsKey(name))
		{
			instructionsByName.put(name, new InstructionRecord(name, compiler));
		}
		
		InstructionRecord instructionRecord = instructionsByName.get(name);
		
		if (instructionRecord.hasMemoryAddressing(memoryAddressing))
		{
			String addressing = memoryAddressing.toString();
			int storedCode = instructionRecord.getCode(memoryAddressing);
			String message = String.format("Memory addressing %s for instruction %s (%d) already defined for code %d.", addressing, name, code, storedCode);
			throw new IllegalArgumentException(message);
		}
		
		instructionRecord.addCode(memoryAddressing, code);
	}

	public Instruction get(String name)
	{
		Instruction instruction = instructionsByName.get(name);
		
		if (instruction == null)
		{
			throw new NoSuchElementException("Instruction " + name + " is not available.");
		}
		
		return instruction;
	}
	
	public String getName(int code)
	{
		return instructionNames.get(code);
	}
	
	public interface Instruction
	{
		int getCode(MemoryAddressing MemoryAddressing);
		int getBytesSize(MemoryAddressing MemoryAddressing);
		String getName();
		Set<MemoryAddressing> getMemoryAddressingModes();
		boolean hasMemoryAddressing(MemoryAddressing MemoryAddressing);
		InstructionCompiler getParser();
	}
	
	public interface MemoryAddressing
	{
		int getOperandsBytesSize();
	}
	
	@FunctionalInterface
	public interface InstructionCompiler
	{
		void parse(Instruction ins, MemoryAddressing ma, Matcher m, DataOutput o) throws IOException;
	}
	
	private class InstructionRecord implements Instruction
	{
		private final Map<MemoryAddressing, Integer> codes = new HashMap<>();
		private final String name;
		private final InstructionCompiler parser;
		
		public InstructionRecord(String name, InstructionCompiler parser)
		{
			this.name = name;
			this.parser = parser;
		}

		@Override
		public String getName()
		{
			return name;
		}
		
		@Override
		public Set<MemoryAddressing> getMemoryAddressingModes()
		{
			return codes.keySet();
		}
		
		@Override
		public int getCode(MemoryAddressing MemoryAddressing)
		{
			return codes.get(MemoryAddressing);
		}
		
		@Override
		public int getBytesSize(MemoryAddressing MemoryAddressing)
		{
			return MemoryAddressing.getOperandsBytesSize() + 1;
		}
		
		@Override
		public boolean hasMemoryAddressing(MemoryAddressing MemoryAddressing)
		{
			return codes.containsKey(MemoryAddressing);
		}
		
		public void addCode(MemoryAddressing MemoryAddressing, int code)
		{
			codes.put(MemoryAddressing, code);
		}
		
		@Override
		public InstructionCompiler getParser()
		{
			return parser;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
