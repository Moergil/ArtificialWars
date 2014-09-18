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

import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.CodeProcessException;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.Instruction;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.MemoryAddressing;

public abstract class Assembler extends CodeProcessor<Assembler.AssemblerState>
{
	private final InstructionSet instructionSet;
	
	private final Pattern instructionNamePattern;
	private final Map<MemoryAddressing, Pattern> instructionMemoryAddressingRegexes = new HashMap<>();
	private final Pattern labelPattern;
	
	private final Map<String, Integer> variableTypes = new HashMap<>();
	
	private final Map<String, Integer> labels = new HashMap<>();
	private final Map<String, String> constants = new HashMap<>();
	private final Map<String, Integer> variables = new HashMap<>();
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
			processRecord(r, state);
		}
		
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

	private void scanLine(String line, AssemblerState state) throws CodeProcessException, IOException
	{
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
		
		ArrayList<String> list = new ArrayList<>();
		while (m.find())
		{
			list.add(m.group(1));
		}

		String chunks[] = new String[list.size()];
		list.toArray(chunks);

		for (int i = 0; i < chunks.length; i++)
		{
			chunks[i] = chunks[i].trim();
		}
		
		if (chunks.length == 0)
		{
			return;
		}
		
		// pragmas
		if (chunks[0].charAt(0) == '.')
		{
			if (chunks[0].equals(".SEG") && chunks[1].equals("PRG"))
			{
				int offset = Integer.decode("0x" + chunks[2].substring(1));
				state.setProgramStartAddress(offset);
			}
			
			if (chunks[0].equals(".SEG") && chunks[1].equals("DAT"))
			{
				int offset = Integer.decode("0x" + chunks[2].substring(1));
				state.setDataStartAddress(offset);
			}
		}
		
		// variables
		if (variableTypes.containsKey(chunks[0]))
		{
			int size = variableTypes.get(chunks[0]);
			byte binaryData[] = new byte[size];
			
			String name = chunks[1];
			String data = (chunks.length > 2) ? chunks[2] : null;
			
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
			
			variables.put(name, state.getDataAddress());
			
			state.addToDataAddress(binaryData.length);
			return;
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
			int address = state.getProgramAddress();
			
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
			
			// variables
			// only absolute addressing is supported
			if (variables.containsKey(param))
			{
				param = String.format("$%04X", variables.get(param));
			}
			
			Instruction ins = instructionSet.get(name);
			
			MemoryAddressing ma = detectMemoryAddressing(param, ins);

			String maName = (ma != null) ? ma.getShortName() : "LBL";
			System.out.printf("I: %s %s%n", name, maName);
			
			InstructionRecord record = new InstructionRecord(state.getLineNumber(), state.getProgramAddress(), name, param, ma);
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

			state.addToProgramAddress(offset);
		}
	}
	
	private void processRecord(InstructionRecord record, AssemblerState state) throws CodeProcessException, IOException
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
		
		DataOutput output = state.getProgramOutput();
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
		public void reset()
		{
			programAddress = 0;
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
