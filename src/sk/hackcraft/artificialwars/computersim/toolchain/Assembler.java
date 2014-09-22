package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sk.hackcraft.artificialwars.computersim.Endianness;
import sk.hackcraft.artificialwars.computersim.Util;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.CodeProcessException;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.Instruction;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.MemoryAddressing;

public abstract class Assembler extends CodeProcessor<Assembler.AssemblerState>
{
	private final InstructionSet instructionSet;
	private final Endianness endianness;
	
	private final Map<MemoryAddressing, Pattern> instructionMemoryAddressingRegexes = new HashMap<>();
	private final Pattern labelCatchPattern;
	
	private final Map<String, Integer> variableTypes = new HashMap<>();
	
	private final Map<String, Integer> labels = new HashMap<>();
	private final Map<String, String> constants = new HashMap<>();
	private final Map<String, Integer> variables = new HashMap<>();
	private final List<InstructionRecord> instructions = new LinkedList<>();
	
	private final Map<String, LabelType> relativeLabelAddressing = new HashMap<>();
	
	public Assembler(InstructionSet instructionSet, Endianness endianness)
	{
		this.instructionSet = instructionSet;
		this.endianness = endianness;

		this.labelCatchPattern = Pattern.compile("^(.+):");
	}

	protected void addMemoryAddressingFormat(MemoryAddressing ma, String memoryAddressingFormat)
	{
		String quoted = Pattern.quote(memoryAddressingFormat);
		
		String patternText = quoted.replaceAll("([%])", "\\\\E([\\$A-Z][A-Z0-9_]*)\\\\Q");
		patternText = patternText.replaceAll("\\\\Q\\\\E", "");

		Pattern pattern = Pattern.compile("^" + patternText + "$");

		instructionMemoryAddressingRegexes.put(ma, pattern);
	}
	
	protected void enableLabels(String instructionName, LabelType type)
	{
		relativeLabelAddressing.put(instructionName, type);
	}
	
	protected void addVariableType(String name, int bytesSize)
	{
		variableTypes.put(name, bytesSize);
	}

	@Override
	protected AssemblerState started()
	{
		System.out.println("Assembling initiated...");
		
		return new AssemblerState();
	}
	
	private int decodeHexValue(String value)
	{
		return Integer.decode("0x" + value.substring(1));
	}
	
	private void decodeHexValue(String value, byte output[])
	{
		int binaryValue = decodeHexValue(value);
		endianness.valueToBytes(binaryValue, output);
	}
	
	private boolean validateHexaValue(String value, int bytesCount)
	{
		value = value.substring(1);
		
		if (value.length() != bytesCount * 2)
		{
			return false;
		}
		
		try
		{
			Integer.decode("0x" + value);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
	
	@Override
	public void process(InputStream input, OutputStream output) throws CodeProcessException, IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(input));

		AssemblerState state = started();

		List<String> lines = readLines(br);

		// 1 pass - find segments, labels, variables and constants
		// also writes data to data segments
		System.out.println("=== Pass 1 ===");
		List<String> parts;
		for (String line : lines)
		{
			state.incrementLineNumber();
			
			parts = splitLine(line);
			scanIdentifiers(line, parts, state);
		}

		// 2 pass - find, preprocess and record instructions
		// puts instructions to records with memory addressing and such
		System.out.println("=== Pass 2 ===");
		for (String line : lines)
		{
			state.incrementLineNumber();
			
			parts = splitLine(line);
			scanCode(line, parts, state);
		}

		// 3 pass - insert values, calculate labels and write instructions to output
		// writes instructions to output, with resolving jump adresses
		System.out.println("=== Pass 3 ===");
		state.rewind();
		for (InstructionRecord r : instructions)
		{
			processRecord(r, state);
		}
		
		// write result
		byte offset[];
		
		int programStartAddress = state.getProgramStartAddress();
		byte program[] = state.getProgramBytes();
		
		int dataStartAddress = state.getDataStartAddress();
		byte data[] = state.getDataBytes();
		
		offset = new byte[programStartAddress];
		
		output.write(offset);
		output.write(program);

		int programSegmentEnd = offset.length + program.length;
		if (programSegmentEnd > dataStartAddress)
		{
			throw new CodeProcessException(-1, "Segments collision.");
		}
		
		int gap = dataStartAddress - programSegmentEnd;
		
		offset = new byte[gap];
		
		output.write(offset);
		output.write(data);
		
		finished(state);
	}

	private List<String> readLines(BufferedReader br) throws IOException
	{
		List<String> lines = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null)
		{
			lines.add(line);
		}

		return lines;
	}
	
	private List<String> splitLine(String line)
	{
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
		
		ArrayList<String> list = new ArrayList<>();
		while (m.find())
		{
			list.add(m.group(1).trim());
		}
		
		return list;
	}

	private void scanIdentifiers(String line, List<String> parts, AssemblerState state) throws CodeProcessException, IOException
	{
		// pragmas
		String firstPart = parts.get(0);
		if (firstPart.charAt(0) == '.')
		{
			if (firstPart.equals(".SEG"))
			{
				String segment = parts.get(1);
				int offset = decodeHexValue(parts.get(2));
				
				switch (segment)
				{
					case "PRG":
						System.out.printf("S: PRG -> %04X%n", offset);
						state.setProgramStartAddress(offset);
						break;
					case "DAT":
						System.out.printf("S: DAT -> %04X%n", offset);
						state.setDataStartAddress(offset);
						break;
				}
			}
			
			return;
		}
		
		// variables
		if (variableTypes.containsKey(firstPart))
		{
			int size = variableTypes.get(firstPart);
			byte binaryData[] = new byte[size];
			
			String name = parts.get(1);
			String data = (parts.size() > 2) ? parts.get(2) : null;
			
			if (data != null)
			{
				int begin = 0;
				int end = data.length() - 1;
				if (data.charAt(begin) == '\"' && data.charAt(end) == '\"')
				{
					String string = data.substring(begin + 1, end);
					binaryData = string.getBytes(StandardCharsets.US_ASCII);
				}
				else
				{
					String dataChunks[] = data.split(",");
					
					for (int i = 0; i < dataChunks.length; i++)
					{
						dataChunks[i] = dataChunks[i].trim();
					}

					ByteBuffer buf = ByteBuffer.allocate(size * dataChunks.length);
					
					for (String dataChunk : dataChunks)
					{
						int value = Integer.decode("0x" + dataChunk.substring(1));
		
						switch (size)
						{
							case 1:
								buf.put((byte)value);
								break;
							case 2:
								buf.putShort((short)value);
								break;
							default:
								throw new CodeProcessException(state.getLineNumber(), "Incorrect bytes size while writing number value: " + size);
						}
					}
					
					binaryData = buf.array();
				}
			}
			
			state.getDataOutput().write(binaryData);
			
			System.out.printf("V: %s -> %s%n", name, data);
			variables.put(name, state.getDataAddress());
			
			state.addToDataAddress(binaryData.length);
			return;
		}
		
		// constants definitions
		if (parts.size() == 3 && parts.get(1).equals("="))
		{
			String name = parts.get(0);
			String value = parts.get(2);
			
			System.out.printf("C: %s -> %s%n", name, value);
			constants.put(name, value);
			return;
		}
		
		// labels
		Matcher labelMatcher = labelCatchPattern.matcher(firstPart);
		if (labelMatcher.find())
		{
			String name = labelMatcher.group(1);
			
			labels.put(name, null);
			
			System.out.printf("L: %s%n", name);
			return;
		}
	}
	
	private void scanCode(String line, List<String> parts, AssemblerState state) throws CodeProcessException, IOException
	{
		String name = parts.get(0);
		
		// updating label with address
		Matcher labelMatcher = labelCatchPattern.matcher(parts.get(0));
		if (labelMatcher.find())
		{
			String labelName = labelMatcher.group(1);
			int address = state.getProgramAddress();
			labels.put(labelName, address);
			return;
		}

		Instruction instruction = instructionSet.get(name);
		
		if (instruction == null)
		{
			return;
		}

		String param = (parts.size() > 1) ? parts.get(1) : "";

		boolean found = false;
		for (MemoryAddressing ma : instruction.getMemoryAddressingModes())
		{
			Pattern p = instructionMemoryAddressingRegexes.get(ma);
			
			Matcher mam = p.matcher(param);
			
			if (!mam.find())
			{
				continue;
			}
			
			boolean implicitValue = param.isEmpty();

			String operandValue;
			if (!implicitValue)
			{
				operandValue = mam.group(1);
				
				boolean finalized = false;
				
				String constant = constants.get(operandValue);
				if (constant != null)
				{
					operandValue = constant;
					finalized = true;
				}
				else
				{
					Integer variable = variables.get(operandValue);
					if (variable != null)
					{
						operandValue = String.format("$%04X", variable);
						finalized = true;
					}
					else
					{
						if (labels.containsKey(operandValue) && relativeLabelAddressing.containsKey(name))
						{
							finalized = false;
						}
						else
						{
							finalized = true;
						}
					}
				}
				
				if (finalized)
				{
					int bytesCount = ma.getOperandsBytesSize();
					
					if (!validateHexaValue(operandValue, bytesCount))
					{
						continue;
					}
				}
			}
			else
			{
				operandValue = null;
			}
			
			int lineNumber = state.getLineNumber();
			int address = state.getProgramAddress();
			
			InstructionRecord record = new InstructionRecord(lineNumber, address, instruction, operandValue, ma);
			instructions.add(record);

			found = true;
			
			int offset = 1 + ma.getOperandsBytesSize();
			state.addToProgramAddress(offset);

			System.out.printf("I: %s %s %s%n", name, ma.getShortName(), operandValue);
			
			break;
		}
		
		if (!found)
		{
			throw new CodeProcessException(state.getLineNumber(), "Can't process instruction; no suitable memory addressing found for given param: " + param);
		}
	}
	
	private void processRecord(InstructionRecord record, AssemblerState state) throws CodeProcessException, IOException
	{		
		Instruction ins = record.getInstruction();
				
		String name = ins.getName();
		String parameter = record.getOperandRawValue();
		
		MemoryAddressing ma = record.getMemoryAddressing();
		
		byte operandValue[] = new byte[ma.getOperandsBytesSize()];

		if (parameter != null)
		{
			if (constants.containsKey(parameter))
			{
				parameter = constants.get(parameter);
			}
			
			if (variables.containsKey(parameter))
			{
				endianness.valueToBytes(variables.get(parameter), operandValue);
			}
			else if (labels.containsKey(parameter) && relativeLabelAddressing.containsKey(name))
			{
				Integer labelValue = labels.get(parameter);
				if (labelValue == null)
				{
					throw new CodeProcessException(record.getLine(), "Label " + parameter + " doesn't exists.");
				}
				
				LabelType labelType = relativeLabelAddressing.get(name);
				
				int labelAddress = labelValue.intValue();
				int programCounterAddress = state.getProgramAddress() + ins.getBytesSize(ma);
				
				try
				{
					int address = labelType.getOperandValue(labelAddress, programCounterAddress);
					endianness.valueToBytes(address, operandValue);
				}
				catch (IllegalArgumentException e)
				{
					throw new CodeProcessException(record.getLine(), e.getMessage());
				}
			}
			else if (parameter.charAt(0) == '$' && validateHexaValue(parameter, ma.getOperandsBytesSize()))
			{
				decodeHexValue(parameter.substring(1), operandValue);
			}
			else
			{
				throw new CodeProcessException(state.getLineNumber(), "Invalid operand value: " + parameter);
			}
		}
		
		int offset = 1 + ma.getOperandsBytesSize();
		state.addToProgramAddress(offset);
		
		DataOutput output = state.getProgramOutput();
		
		byte opcode = (byte)ins.getCode(ma);
		output.write(opcode);
		output.write(operandValue);
		
		System.out.printf("%04X %s -> %02X %s%n", record.getAddress(), ins.getName(), opcode, Util.byteArrayToString(operandValue));
	}
	
	@Override
	protected void finished(AssemblerState stateObject)
	{
		System.out.println("Finished!");
		System.out.println("Processed " + stateObject.getLineNumber() + " lines.");
	}
	
	protected static class AssemblerState extends CodeProcessorState
	{
		private int programStartAddress, dataStartAddress;
		private int programAddress, dataAddress;
		
		private ByteArrayOutputStream programOutput, dataOutput;
		private DataOutputStream programDataOutput, dataDataOutput;
		
		public AssemblerState()
		{
			super(2);
			
			programOutput = new ByteArrayOutputStream();
			dataOutput = new ByteArrayOutputStream();
			
			programDataOutput = new DataOutputStream(programOutput);
			dataDataOutput = new DataOutputStream(dataOutput);
		}
		
		public void setProgramStartAddress(int address)
		{
			programStartAddress = address;
			programAddress = address;
		}
		
		public int getProgramStartAddress()
		{
			return programStartAddress;
		}
		
		public void setDataStartAddress(int address)
		{
			dataStartAddress = address;
			dataAddress = address;
		}
		
		public int getDataStartAddress()
		{
			return dataStartAddress;
		}
		
		public void addToProgramAddress(int value)
		{
			this.programAddress += value;
		}
		
		public int getProgramAddress()
		{
			return programAddress;
		}
		
		public void setProgramAddress(int address)
		{
			this.programAddress = address;
		}
		
		public void addToDataAddress(int value)
		{
			this.dataAddress += value;
		}
		
		public int getDataAddress()
		{
			return dataAddress;
		}
		
		public void setDataAddress(int dataAddress)
		{
			this.dataAddress = dataAddress;
		}
		
		public DataOutput getProgramOutput()
		{
			return programDataOutput;
		}
		
		public DataOutput getDataOutput()
		{
			return dataDataOutput;
		}
		
		public byte[] getProgramBytes()
		{
			return programOutput.toByteArray();
		}
		
		public byte[] getDataBytes()
		{
			return dataOutput.toByteArray();
		}
		
		@Override
		public void rewind()
		{
			programAddress = programStartAddress;
			dataAddress = dataStartAddress;
			super.rewind();
		}
	}
	
	private static class InstructionRecord
	{
		private final int line;
		private final int address;
		private final Instruction instruction;
		private final String operandRawValue;
		private final MemoryAddressing memoryAddressing;

		public InstructionRecord(int line, int address, Instruction instruction, String operandRawValue, MemoryAddressing memoryAddressing)
		{
			this.line = line;
			this.address = address;
			this.instruction = instruction;
			this.operandRawValue = operandRawValue;
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
		
		public Instruction getInstruction()
		{
			return instruction;
		}
		
		public String getOperandRawValue()
		{
			return operandRawValue;
		}
		
		public MemoryAddressing getMemoryAddressing()
		{
			return memoryAddressing;
		}
		
		@Override
		public String toString()
		{
			String name = instruction.getName();
			return String.format("%04X %s %s %s", address, name, operandRawValue, memoryAddressing.getShortName());
		}
	}
	
	protected interface LabelType
	{
		int getOperandsBytesSize();
		int getOperandValue(int labelAddress, int programCounterAddress);
	}
}
