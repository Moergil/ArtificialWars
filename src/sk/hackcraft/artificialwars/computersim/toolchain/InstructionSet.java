package sk.hackcraft.artificialwars.computersim.toolchain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class InstructionSet
{
	private final Map<Integer, Opcode> opcodes = new HashMap<>();
	private final Map<String, InstructionRecord> instructions = new HashMap<>();

	public void add(int code, Object name, MemoryAddressing memoryAddressing)
	{
		add(code, name.toString(), memoryAddressing);
	}
	
	public void add(int code, String name, MemoryAddressing memoryAddressing)
	{
		if (opcodes.containsKey(code))
		{
			throw new IllegalArgumentException(String.format("Code %d already used."));
		}
		
		int bytesSize = calculateBytesSize(code, memoryAddressing);
		
		Opcode opcode = new OpcodeRecord(code, name.toString(), memoryAddressing, bytesSize);
		
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
		
		String getInstructionName();
		MemoryAddressing getMemoryAddressing();
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
		
		private final int bytesSize;
		
		public OpcodeRecord(int opcode, String name, MemoryAddressing memoryAddressing, int bytesSize)
		{
			this.opcode = opcode;
			this.name = name;
			this.memoryAddressing = memoryAddressing;
			
			this.bytesSize = bytesSize;
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
		public int getBytesSize()
		{
			return bytesSize;
		}
		
		@Override
		public String toString()
		{
			return String.format("%s %s (%d)", name, memoryAddressing, opcode); 
		}
	}
}
