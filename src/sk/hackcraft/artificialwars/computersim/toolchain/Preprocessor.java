package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO support for constants and macros
public class Preprocessor extends CodeProcessor<CodeProcessorStateObject>
{
	public static String exampleMacros1 = ""
			+ "DEF MACRO test op1 op2"
			+ "ADC $op1op2\n";
	
	private Pattern lineCommentPattern;
	private Pattern constantDefinitionPattern;
	
	private final Map<String, String> constants = new HashMap<>();
	private final Map<String, Macro> macros = new HashMap<>();
	
	public Preprocessor(String lineCommentStart)
	{	
		this.lineCommentPattern = Pattern.compile(String.format("^([^%s]*)%s", lineCommentStart, lineCommentStart));
		
		// TODO
		String constantDefinitionTemplate = "";
		this.constantDefinitionPattern = Pattern.compile(constantDefinitionTemplate);
		
		macros.put("TEST", new Macro("TEST", "ADC $op1op2\n", new String[]{"op1", "op2"}));
	}
	
	@Override
	protected CodeProcessorStateObject createStateObject()
	{
		return new CodeProcessorStateObject();
	}
	
	@Override
	protected void started()
	{
		System.out.println("Preprocessor started.");
	}
	
	@Override
	protected void process(DataOutput output, String line, CodeProcessorStateObject stateObject) throws CodeProcessException, IOException
	{
		Matcher commentMatcher = lineCommentPattern.matcher(line);
		
		line = (commentMatcher.find()) ? commentMatcher.group(1) : line;
		line.trim();
		
		if (line.isEmpty())
		{
			return;
		}
		
		// TODO
		/*Matcher variableMatcher = constantDefinitionPattern.matcher(line);
		if (variableMatcher.find())
		{
			String name = variableMatcher.group(1);
			String value = variableMatcher.group(2);
			
			constants.put(name, value);
		}

		String parts[] = line.split(" ");
		
		Macro macro = macros.get(parts[0]);
		
		if (macro == null)
		{*/
			output.write((line + "\n").getBytes());
		/*}
		else
		{
			String operands[] = Arrays.copyOfRange(parts, 1, parts.length);
			String macroAssembly = macro.process(operands);
			
			output.write(macroAssembly.getBytes());
		}*/
	}
	
	@Override
	protected void finished(CodeProcessorStateObject stateObject)
	{
		System.out.println("Preprocessing finished.");
		System.out.println("Processed " + stateObject.getLineNumber() + " lines.");
	}
	
	private static class Macro
	{
		private String name;
		private String macroAssembly;
		private String operandNames[];
		
		public Macro(String name, String macroAssembly, String operandNames[])
		{
			this.name = name;
			this.macroAssembly = macroAssembly;
			this.operandNames = operandNames;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String[] getOperands()
		{
			return operandNames;
		}
		
		public String process(String operands[])
		{
			String assembly = macroAssembly;
			
			for (int i = 0; i < operands.length; i++)
			{
				String operand = operands[i];
				String operandName = operandNames[i];
				
				assembly = assembly.replaceAll(operandName, operand);
			}
			
			return assembly;
		}
	}
}
