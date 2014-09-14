package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.Instruction;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.MemoryAddressing;


public abstract class Assembler<MA> extends CodeProcessor<CodeProcessorStateObject>
{
	private final InstructionSet instructionSet;
	
	private final Pattern instructionNamePattern;
	private final Map<MA, Pattern> instructionMemoryAddressingRegexes;
	
	public Assembler(InstructionSet instructionSet, String instructionNamePattern)
	{
		this.instructionSet = instructionSet;
		
		this.instructionNamePattern = Pattern.compile(instructionNamePattern);
		
		this.instructionMemoryAddressingRegexes = createMemoryAddressingPatternsMap();
	}
	
	protected abstract Map<MA, Pattern> createMemoryAddressingPatternsMap();
	
	protected void addRegex(MA ma, String regex)
	{
		instructionMemoryAddressingRegexes.put(ma, Pattern.compile("^" + regex + "$"));
	}
	
	@Override
	protected CodeProcessorStateObject createStateObject()
	{
		return new CodeProcessorStateObject();
	}
	
	@Override
	protected void started()
	{
		System.out.println("Assembling initiated...");
	}
	
	protected void process(DataOutput output, String line, CodeProcessorStateObject stateObject) throws CodeProcessException, IOException
	{
		System.out.printf("%04d %-10s -->", stateObject.getLineNumber(), line);

		String parts[] = line.split(" ");

		String name = parts[0].trim();
		
		Matcher nameMatcher = instructionNamePattern.matcher(name);
		if (!nameMatcher.matches())
		{
			throw new CodeProcessException(stateObject.getLineNumber(), "Invalid instruction name: " + name);
		}
		
		String param = (parts.length > 1) ? parts[1].trim() : "";
		
		processLine(stateObject.getLineNumber(), output, name, param);
		
		stateObject.incrementLineNumber();
	}
	
	@Override
	protected void finished(CodeProcessorStateObject stateObject)
	{
		System.out.println("Finished!");
		System.out.println("Processed " + stateObject.getLineNumber() + " lines.");
	}

	private void processLine(int lineNumber, DataOutput baos, String name, String param) throws CodeProcessException, IOException
	{
		Instruction ins = instructionSet.get(name);
		
		boolean matched = false;
		for (MemoryAddressing ma : ins.getMemoryAddressingModes())
		{
			Pattern p = instructionMemoryAddressingRegexes.get(ma);
			
			Matcher m = p.matcher(param);
			if (m.find())
			{
				matched = true;
	
				ins.getParser().parse(ins, ma, m, baos);
				
				int code = ins.getCode(ma);				
				System.out.printf(" %02X  ", code);
				
				for (int i = m.groupCount(); i > 0; i--)
				{
					String rawOperand = m.group(i);
					int operand = Integer.decode("0x" + rawOperand);
					System.out.printf(" %02X", operand);
				}
				
				System.out.println();
				break;
			}
		}
		
		if (!matched)
		{
			System.out.println();
			throw new CodeProcessException(lineNumber, "Can't parse " + name + " " + param + " instruction; invalid memory addressing mode.");
		}
	}
}
