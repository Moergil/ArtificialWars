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
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.Instruction;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.MemoryAddressing;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.Opcode;

public abstract class AbstractAssembler extends CodeProcessor<AbstractAssembler.AssemblerState>
{
	private final InstructionSet instructionSet;
	private final Endianness endianness;
	
	private final Map<MemoryAddressing, Pattern> memoryAddressingFormats = new HashMap<>();
	private final Pattern labelCatchPattern;
	
	private final Map<String, Integer> variableTypes = new HashMap<>();
	
	private final Map<String, LabelType> relativeLabelAddressing = new HashMap<>();
	
	private final Set<ValueParser> valueParsers = new HashSet<>();
	
	private final String segmentPragma;
	private final Map<String, Segment> segmentIdentifiers = new HashMap<>();
	
	public AbstractAssembler(InstructionSet instructionSet, Endianness endianness, String segmentPragma, String labelRegex)
	{
		this.instructionSet = instructionSet;
		this.endianness = endianness;

		this.labelCatchPattern = Pattern.compile("^" + labelRegex + "$");
		this.segmentPragma = segmentPragma;
	}

	protected void addMemoryAddressingFormat(MemoryAddressing ma, String memoryAddressingFormat)
	{
		String quoted = Pattern.quote(memoryAddressingFormat);
		
		// TODO assembler specific
		String patternText = quoted.replaceAll("([%])", "\\\\E([\\$A-Za-z0-9_]+)\\\\Q");
		patternText = patternText.replaceAll("\\\\Q\\\\E", "");

		Pattern pattern = Pattern.compile("^" + patternText + "$");

		memoryAddressingFormats.put(ma, pattern);
	}
	
	protected void addSegmentIdentifier(Segment segment, String identifier)
	{
		segmentIdentifiers.put(identifier, segment);
	}
	
	protected void enableLabels(String instructionName, LabelType type)
	{
		relativeLabelAddressing.put(instructionName, type);
	}
	
	protected void addVariableType(String name, int bytesSize)
	{
		variableTypes.put(name, bytesSize);
	}
	
	protected void addValueParser(ValueParser numberParser)
	{
		valueParsers.add(numberParser);
	}

	@Override
	protected AssemblerState started()
	{
		System.out.println("Assembling initiated...");
		
		return new AssemblerState();
	}
	
	private int parseValue(String value)
	{
		for (ValueParser parser : valueParsers)
		{
			if (parser.validate(value))
			{
				return parser.parse(value);
			}
		}
		
		throw new NumberFormatException("Can't parse " + value);
	}
	
	private void decodeValue(String value, byte output[])
	{
		int binaryValue = parseValue(value);
		endianness.valueToBytes(binaryValue, output);
	}
	
	private boolean validateValue(String value, int bytesCount)
	{
		for (ValueParser parser : valueParsers)
		{
			if (parser.validate(value))
			{
				int numericValue = parser.parse(value);
				
				String testValue = Integer.toBinaryString(numericValue);
				if (testValue.length() <= bytesCount * 8)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void process(InputStream input, OutputStream output) throws CodeProcessException, IOException
	{
		// TODO add checks for errors when reusing constants, variables or labels
		AssemblerState state = started();

		List<String> lines = readLines(input);
		List<String> codeLines = new ArrayList<>();

		// 1 pass - find segments, labels, variables and constants
		// also writes data to data segments
		System.out.println("=== Pass 1 ===");
		List<String> parts;
		for (String line : lines)
		{
			state.incrementLineNumber();
			
			parts = splitLine(line);
			if (!scanIdentifiers(line, parts, state))
			{
				codeLines.add(line);
			}
		}

		// 2 pass - find, preprocess and record instructions
		// puts instructions to records with memory addressing and such
		System.out.println("=== Pass 2 ===");
		for (String line : codeLines)
		{
			state.incrementLineNumber();
			
			parts = splitLine(line);
			scanCode(line, parts, state);
		}

		// 3 pass - insert values, calculate labels and write instructions to output
		// writes instructions to output, with resolving jump adresses
		System.out.println("=== Pass 3 ===");
		state.rewind();
		for (InstructionRecord r : state.getInstructions())
		{
			processRecord(r, state);
		}
		
		// write result
		byte offset[];
		
		int programStartAddress = state.getSegmentStartAddress(Segment.PROGRAM);
		byte program[] = state.getSegmentBytes(Segment.PROGRAM);
		
		int dataStartAddress = state.getSegmentStartAddress(Segment.DATA);
		byte data[] = state.getSegmentBytes(Segment.DATA);
		
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

	protected boolean scanIdentifiers(String line, List<String> parts, AssemblerState state) throws CodeProcessException, IOException
	{
		// pragmas
		String firstPart = parts.get(0);
		if (firstPart.charAt(0) == '.')
		{
			if (firstPart.equals(segmentPragma))
			{
				// TODO fix fixed bytes size
				String readSegmentIdentifier = parts.get(1);
				int offset = parseValue(parts.get(2));

				Segment segment = segmentIdentifiers.get(readSegmentIdentifier);

				if (segment != null)
				{
					switch (segment)
					{
						case PROGRAM:
							System.out.printf("S: PRG -> %04X%n", offset);
							state.setSegmentStartAddress(Segment.PROGRAM, offset);
							return true;
						case DATA:
							System.out.printf("S: DAT -> %04X%n", offset);
							state.setSegmentStartAddress(Segment.DATA, offset);
							return true;
						default:
							return false;
					}
				}
			}
			
			return false;
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

					byte chunkBuffer[] = new byte[size];
					ByteBuffer buf = ByteBuffer.allocate(size * dataChunks.length);

					for (String dataChunk : dataChunks)
					{
						decodeValue(dataChunk, chunkBuffer);
						buf.put(chunkBuffer);
					}
					
					binaryData = buf.array();
				}
			}
			
			state.getSegmentOutput(Segment.DATA).write(binaryData);
			
			System.out.printf("V: %s -> %s%n", name, data);
			state.getVariables().put(name, state.getSegmentActualAddress(Segment.DATA));
			
			state.addToSegmentActualAddress(Segment.DATA, binaryData.length);
			return true;
		}
		
		// constants definitions
		if (parts.size() == 3 && parts.get(1).equals("="))
		{
			String name = parts.get(0);
			String value = parts.get(2);
			
			System.out.printf("C: %s -> %s%n", name, value);
			state.getConstants().put(name, value);
			return true;
		}
		
		// labels
		Matcher labelMatcher = labelCatchPattern.matcher(firstPart);
		if (labelMatcher.find())
		{
			String name = labelMatcher.group(1);
			
			state.getLabels().put(name, null);
			
			System.out.printf("L: %s%n", name);
			return false;
		}
		
		return false;
	}
	
	protected void scanCode(String line, List<String> parts, AssemblerState state) throws CodeProcessException, IOException
	{
		String name = parts.get(0);
		
		// updating label with address
		Matcher labelMatcher = labelCatchPattern.matcher(parts.get(0));
		if (labelMatcher.find())
		{
			String labelName = labelMatcher.group(1);
			int address = state.getSegmentActualAddress(Segment.PROGRAM);
			state.getLabels().put(labelName, address);
			return;
		}

		Instruction instruction = instructionSet.getInstruction(name);
		
		if (instruction == null)
		{
			throw new CodeProcessException(state.getLineNumber(), "Can't parse line: " + line);
		}

		String param = (parts.size() > 1) ? parts.get(1) : "";

		boolean found = false;
		for (MemoryAddressing ma : instruction.getMemoryAddressings())
		{
			Pattern p = memoryAddressingFormats.get(ma);
			
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
				
				String constant = state.getConstants().get(operandValue);
				if (constant != null)
				{
					operandValue = constant;
					finalized = true;
				}
				else
				{
					Integer variable = state.getVariables().get(operandValue);
					if (variable != null)
					{
						operandValue = Integer.toString(variable);
						finalized = true;
					}
					else
					{
						if (state.getLabels().containsKey(operandValue) && relativeLabelAddressing.containsKey(name))
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
					
					if (!validateValue(operandValue, bytesCount))
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
			int address = state.getSegmentActualAddress(Segment.PROGRAM);
			
			Opcode opcode = instruction.getOpcode(ma);
			InstructionRecord record = new InstructionRecord(lineNumber, address, opcode, operandValue);
			state.getInstructions().add(record);

			found = true;
			
			int offset = 1 + ma.getOperandsBytesSize();
			state.addToSegmentActualAddress(Segment.PROGRAM, offset);

			System.out.printf("I: %s %s %s%n", name, ma.getShortName(), operandValue);
			
			break;
		}
		
		if (!found)
		{
			throw new CodeProcessException(state.getLineNumber(), "Can't process instruction; no suitable memory addressing found for given param: " + param);
		}
	}
	
	protected void processRecord(InstructionRecord record, AssemblerState state) throws CodeProcessException, IOException
	{		
		Opcode opcode = record.getOpcode();
				
		String name = opcode.getInstructionName();
		String parameter = record.getOperandRawValue();
		
		MemoryAddressing ma = opcode.getMemoryAddressing();
		
		byte operandValue[] = new byte[ma.getOperandsBytesSize()];

		if (parameter != null)
		{
			if (state.getConstants().containsKey(parameter))
			{
				parameter = state.getConstants().get(parameter);
			}
			
			if (state.getVariables().containsKey(parameter))
			{
				endianness.valueToBytes(state.getVariables().get(parameter), operandValue);
			}
			else if (state.getLabels().containsKey(parameter) && relativeLabelAddressing.containsKey(name))
			{
				Integer labelValue = state.getLabels().get(parameter);
				if (labelValue == null)
				{
					throw new CodeProcessException(record.getLine(), "Label " + parameter + " doesn't exists.");
				}
				
				LabelType labelType = relativeLabelAddressing.get(name);
				
				int labelAddress = labelValue.intValue();
				int programCounterAddress = state.getSegmentActualAddress(Segment.PROGRAM) + opcode.getBytesSize();
				
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
			else if (validateValue(parameter, ma.getOperandsBytesSize()))
			{
				decodeValue(parameter, operandValue);
			}
			else
			{
				throw new CodeProcessException(state.getLineNumber(), "Invalid operand value: " + parameter);
			}
		}
		
		int offset = 1 + ma.getOperandsBytesSize();
		state.addToSegmentActualAddress(Segment.PROGRAM, offset);
		
		DataOutput output = state.getSegmentOutput(Segment.PROGRAM);
		
		opcode.compile(operandValue, output);
		
		System.out.printf("%04X %s -> %02X %s%n", record.getAddress(), opcode.getInstructionName(), opcode.toInt(), Util.byteArrayToHexaString(operandValue));
	}
	
	@Override
	protected void finished(AssemblerState stateObject)
	{
		System.out.println("Finished!");
		System.out.println("Processed " + stateObject.getLineNumber() + " lines.");
	}
	
	public enum Segment
	{
		PROGRAM,
		DATA;
	}
	
	protected static class AssemblerState extends CodeProcessorState
	{
		private static final int SIZE = Segment.values().length;
		
		private int segmentStartAddress[] = new int[SIZE];
		private int segmentActualAddress[] = new int[SIZE];
		
		private ByteArrayOutputStream segmentOutput[] = new ByteArrayOutputStream[SIZE];
		private DataOutput segmentDataOutput[] = new DataOutput[SIZE];
		
		private final Map<String, Integer> labels = new HashMap<>();
		private final Map<String, String> constants = new HashMap<>();
		private final Map<String, Integer> variables = new HashMap<>();
		private final List<InstructionRecord> instructions = new LinkedList<>();
		
		public AssemblerState()
		{
			super(2);
			
			for (Segment segment : Segment.values())
			{
				int index = segment.ordinal();
				
				segmentOutput[index] = new ByteArrayOutputStream();
				segmentDataOutput[index] = new DataOutputStream(segmentOutput[index]);
			}
		}
		
		public Map<String, Integer> getLabels()
		{
			return labels;
		}
		
		public Map<String, String> getConstants()
		{
			return constants;
		}
		
		public Map<String, Integer> getVariables()
		{
			return variables;
		}
		
		public List<InstructionRecord> getInstructions()
		{
			return instructions;
		}
		
		public void setSegmentStartAddress(Segment segment, int address)
		{
			segmentStartAddress[segment.ordinal()] = address;
			segmentActualAddress[segment.ordinal()] = address;
		}
		
		public int getSegmentStartAddress(Segment segment)
		{
			return segmentStartAddress[segment.ordinal()];
		}
		
		public void addToSegmentActualAddress(Segment segment, int value)
		{
			segmentActualAddress[segment.ordinal()] += value;
		}
		
		public void setSegmentActualAddress(Segment segment, int address)
		{
			segmentActualAddress[segment.ordinal()] = address;
		}
		
		public int getSegmentActualAddress(Segment segment)
		{
			return segmentActualAddress[segment.ordinal()];
		}
		
		public DataOutput getSegmentOutput(Segment segment)
		{
			return segmentDataOutput[segment.ordinal()];
		}
		
		public byte[] getSegmentBytes(Segment segment)
		{
			return segmentOutput[segment.ordinal()].toByteArray();
		}
		
		@Override
		public void rewind()
		{
			for (Segment segment : Segment.values())
			{
				setSegmentActualAddress(segment, getSegmentStartAddress(segment));
			}

			super.rewind();
		}
	}
	
	private static class InstructionRecord
	{
		private final int line;
		private final int address;
		private final Opcode opcode;
		private final String operandRawValue;

		public InstructionRecord(int line, int address, Opcode opcode, String operandRawValue)
		{
			this.line = line;
			this.address = address;
			this.opcode = opcode;
			this.operandRawValue = operandRawValue;
		}
		
		public int getLine()
		{
			return line;
		}
		
		public int getAddress()
		{
			return address;
		}
		
		public Opcode getOpcode()
		{
			return opcode;
		}
		
		public String getOperandRawValue()
		{
			return operandRawValue;
		}
		
		@Override
		public String toString()
		{
			String name = opcode.getInstructionName();
			String memoryAddressingShortName = opcode.getMemoryAddressing().getShortName();
			return String.format("%04X %s %s %s", address, name, operandRawValue, memoryAddressingShortName);
		}
	}
	
	protected interface LabelType
	{
		int getOperandsBytesSize();
		int getOperandValue(int labelAddress, int programCounterAddress);
	}

	@FunctionalInterface
	protected interface ValueParser
	{
		default boolean validate(String value)
		{
			try
			{
				parse(value);
				return true;
			}
			catch (NumberFormatException e)
			{
				return false;
			}
		}
		
		int parse(String value);
	}
}
