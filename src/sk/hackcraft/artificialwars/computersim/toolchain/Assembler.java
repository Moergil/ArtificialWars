package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.CodeProcessException;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.Instruction;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.MemoryAddressing;

public abstract class Assembler extends CodeProcessor<Assembler.AssemblerState>
{
	private final InstructionSet instructionSet;
	
	private final Pattern instructionNamePattern;
	private final Map<MemoryAddressing, Pattern> instructionMemoryAddressingRegexes = new HashMap<>();
	private final Pattern labelPattern;
	
	private final Map<String, Integer> labels = new HashMap<>();
	private final Map<String, String> constants = new HashMap<>();
	private final List<InstructionRecord> instructions = new LinkedList<>();
	
	private final Map<String, LabelType> relativeLabelAddressing = new HashMap<>();
	
	public Assembler(InstructionSet instructionSet, String instructionNamePattern)
	{
		this.instructionSet = instructionSet;
		
		this.instructionNamePattern = Pattern.compile(instructionNamePattern);
		this.labelPattern = Pattern.compile("^([A-Z0-9]+):");
	}

	protected void addRegex(MemoryAddressing ma, String regex)
	{
		instructionMemoryAddressingRegexes.put(ma, Pattern.compile("^" + regex + "$"));
	}
	
	protected void enableLabels(String instructionName, LabelType type)
	{
		relativeLabelAddressing.put(instructionName, type);
	}

	@Override
	protected AssemblerState started()
	{
		System.out.println("Assembling initiated...");
		
		return new AssemblerState();
	}
	
	@Override
	public void process(InputStream input, OutputStream output) throws CodeProcessException, IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(input));

		AssemblerState state = started();
	
		// 1 pass - check labels, constants and decode instructions
		int lineNumber = 0;
		String line;
		while ((line = br.readLine()) != null)
		{
			state.setLineNumber(lineNumber++);
			scanLine(line, state);
		}

		// 2 pass - insert values, calculate labels and write instructions to output
		DataOutput dataOutput = new DataOutputStream(output);
		for (InstructionRecord r : instructions)
		{
			processRecord(r, dataOutput, state);
		}
		
		finished(state);
	}

	private void scanLine(String line, AssemblerState state) throws CodeProcessException
	{
		String chunks[] = line.split(" ");

		for (int i = 0; i < chunks.length; i++)
		{
			chunks[i] = chunks[i].trim();
		}
		
		// pragmas
		if (chunks[0].charAt(0) == '.')
		{
			if (chunks[0].equals(".SEG") && chunks[1].equals("PRG"))
			{
				int offset = Integer.decode("0x" + chunks[2].substring(1));
				state.setAddress(offset);
			}
		}
		
		// constants definitions
		if (chunks.length == 3)
		{
			if (chunks[1].equals("="))
			{
				System.out.printf("C: %s -> %s%n", chunks[0], chunks[2]);
				constants.put(chunks[0], chunks[2]);
				return;
			}
		}
		
		// labels
		Matcher labelMatcher = labelPattern.matcher(chunks[0]);
		if (labelMatcher.find())
		{
			String name = labelMatcher.group(1);
			int address = state.getAddress();
			
			labels.put(name, address);
			
			System.out.printf("L: %04X %s%n", address, name);
			return;
		}
		
		// instructions
		Matcher instructionMatcher = instructionNamePattern.matcher(chunks[0]);
		if (instructionMatcher.find())
		{
			String name = instructionMatcher.group(1);
			
			int operandIndex = 1;
			String param = (chunks.length > operandIndex) ? chunks[operandIndex] : "";
			
			// constants are used as soon as possible
			// that also means, that constant has to be defined before first use
			if (constants.containsKey(param))
			{
				param = constants.get(param);
			}
			
			Instruction ins = instructionSet.get(name);
			
			MemoryAddressing ma = detectMemoryAddressing(param, ins);

			String maName = (ma != null) ? ma.getShortName() : "LBL";
			System.out.printf("I: %s %s%n", name, maName);
			
			InstructionRecord record = new InstructionRecord(state.getLineNumber(), state.getAddress(), name, param, ma);
			instructions.add(record);
			
			int offset = 1;
			if (ma != null)
			{
				offset += ma.getOperandsBytesSize();
			}
			else if (relativeLabelAddressing.containsKey(name))
			{
				offset += relativeLabelAddressing.get(name).getOperandsBytesSize();
			}
			else
			{
				throw new CodeProcessException(state.getLineNumber(), "Can't determine instruction length: " + name);
			}

			state.addToAddress(offset);
		}
	}
	
	private void processRecord(InstructionRecord record, DataOutput output, AssemblerState state) throws CodeProcessException, IOException
	{
		String name = record.getName();
		
		Matcher nameMatcher = instructionNamePattern.matcher(name);
		if (!nameMatcher.matches())
		{
			throw new CodeProcessException(record.getLine(), "Invalid instruction name: " + name);
		}
		
		String param = record.getParam();
		
		Instruction ins = instructionSet.get(name);
		
		MemoryAddressing ma = record.getMemoryAddressing();
		String debugParamValue = param;
		if (ma == null && relativeLabelAddressing.containsKey(name))
		{
			if (!labels.containsKey(param))
			{
				throw new CodeProcessException(record.getLine(), "Trying to use non-existing label: " + param);
			}
			
			String labelName = param;
			int address = labels.get(param);
			
			LabelType labelType = relativeLabelAddressing.get(name);
			param = labelType.getOperandValue(address, record.getAddress() + labelType.getOperandsBytesSize() + 1);

			ma = detectMemoryAddressing(param, ins);

			debugParamValue = param + " (" + labelName + ")";
		}
		
		Matcher m = instructionMemoryAddressingRegexes.get(ma).matcher(param);
		
		if (!m.find())
		{
			throw new CodeProcessException(record.getLine(), "Can't process parameter: " + record.getName() + " " + param + " " + record.getMemoryAddressing());
		}
		
		ins.getParser().parse(ins, ma, m, output);
		
		System.out.printf("%04X %s --> %02X %s%n", record.getAddress(), record.getName(), ins.getCode(ma), debugParamValue);
	}
	
	private MemoryAddressing detectMemoryAddressing(String param, Instruction ins)
	{
		MemoryAddressing matchedMemoryAddressing = null;
		for (MemoryAddressing ma : ins.getMemoryAddressingModes())
		{
			Pattern p = instructionMemoryAddressingRegexes.get(ma);
			
			Matcher m = p.matcher(param);
			if (m.find())
			{
				matchedMemoryAddressing = ma;
				break;
			}
		}
		
		return matchedMemoryAddressing;
	}
	
	@Override
	protected void finished(AssemblerState stateObject)
	{
		System.out.println("Finished!");
		System.out.println("Processed " + stateObject.getLineNumber() + " lines.");
	}
	
	protected static class AssemblerState extends CodeProcessorState
	{
		private int address;
		
		public AssemblerState()
		{
			super(2);
		}
		
		public void addToAddress(int value)
		{
			this.address += value;
		}
		
		public int getAddress()
		{
			return address;
		}
		
		public void setAddress(int address)
		{
			this.address = address;
		}
		
		@Override
		public void reset()
		{
			address = 0;
			super.reset();
		}
	}
	
	private static class InstructionRecord
	{
		private final int line;
		private final int address;
		private final String name;
		private final String param;
		private final MemoryAddressing memoryAddressing;

		public InstructionRecord(int line, int address, String name, String param, MemoryAddressing memoryAddressing)
		{
			this.line = line;
			this.address = address;
			this.name = name;
			this.param = param;
			this.memoryAddressing = memoryAddressing;
		}
		
		public int getLine()
		{
			return line;
		}
		
		public int getAddress()
		{
			return address;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getParam()
		{
			return param;
		}
		
		public MemoryAddressing getMemoryAddressing()
		{
			return memoryAddressing;
		}
	}
	
	protected interface LabelType
	{
		int getOperandsBytesSize();
		String getOperandValue(int labelAddress, int actualPCAddress);
	}
}
