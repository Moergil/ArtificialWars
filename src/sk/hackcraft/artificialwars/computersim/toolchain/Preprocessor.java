package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessor extends CodeProcessor<CodeProcessorState>
{
	private Pattern lineCommentPattern;
	private String macroStart, macroEnd;

	private final Map<String, Macro> macros = new HashMap<>();
	
	public Preprocessor(String lineCommentStart, String macroStart, String macroEnd)
	{	
		this.lineCommentPattern = Pattern.compile(String.format("^([^%s]*)%s", lineCommentStart, lineCommentStart));
		this.macroStart = macroStart;
		this.macroEnd = macroEnd;
	}
	
	@Override
	protected PreprocessorState started()
	{
		verboseOut.println("Preprocessor started.");
		
		return new PreprocessorState();
	}
	
	@Override
	public void process(InputStream input, OutputStream output) throws CodeProcessException, IOException
	{
		PreprocessorState state = started();
		
		List<String> lines = readLines(input);
		List<String> codeLines = new ArrayList<>();
		
		// remove mess (comments, empty lines) and find macros
		verboseOut.println("*** pass 1 ***");
		for (String line : lines)
		{
			state.incrementLineNumber();
			
			if (isMess(line))
			{
				continue;
			}
			
			if (state.isParsingMacro())
			{
				parseMacro(line, state);
			}
			else if (!scanMacros(line, state))
			{
				codeLines.add(line);
			}
		}
		
		state.rewind();
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
		
		// unroll macros
		verboseOut.println("*** pass 2 ***");
		for (String line : codeLines)
		{
			state.incrementLineNumber();
			preprocessCode(line, state, writer);
		}

		writer.close();

		finished(state);
	}
	
	private boolean isMess(String line)
	{
		Matcher commentMatcher = lineCommentPattern.matcher(line);
		
		line = (commentMatcher.find()) ? commentMatcher.group(1) : line;
		line = line.trim();
		
		return line.isEmpty() ? true : false;
	}
	
	private boolean scanMacros(String line, PreprocessorState state)
	{
		List<String> parts = splitLine(line);
		
		if (parts.size() < 2)
		{
			return false;
		}
		
		String lineMacroDeclaration, name;
		
		lineMacroDeclaration = parts.get(0);
		
		if (!lineMacroDeclaration.equals(macroStart))
		{
			return false;
		}
		
		name = parts.get(1);
		
		ArrayList<String> operands = new ArrayList<>();
		for (int i = 2; i < parts.size(); i++)
		{
			operands.add(parts.get(i));
		}
		
		verboseOut.printf("M: %s %s%n", name, operands.toString());
		
		state.setParsingMacro(true);
		
		state.getMacroBuilder()
		.setName(name)
		.setOperandNames(operands.toArray(new String[operands.size()]));
		
		return true;
	}

	private void parseMacro(String line, PreprocessorState state)
	{
		List<String> parts = splitLine(line);
		
		MacroBuilder b = state.getMacroBuilder();
		
		if (parts.get(0).equals(macroEnd))
		{
			Macro macro = b.create();
			macros.put(macro.getName(), macro);
			
			state.setParsingMacro(false);
			b.reset();
		}
		else
		{
			b.addAssemblyLine(line);
		}
	}
	
	private void preprocessCode(String line, PreprocessorState state, BufferedWriter writer) throws CodeProcessException, IOException
	{
		String parts[] = line.split(" ");
		
		Macro macro = macros.get(parts[0]);
		
		if (macro == null)
		{
			verboseOut.println(line);
			
			writer.write(line);
			writer.newLine();
			return;
		}
		else
		{
			String operandValues[] = Arrays.copyOfRange(parts, 1, parts.length);

			String assembly = macro.process(operandValues, state.getLineNumber());
			
			verboseOut.println(assembly);
			
			writer.write(assembly);
		}
	}
	
	@Override
	protected void finished(CodeProcessorState stateObject)
	{
		verboseOut.println("Preprocessing finished.");
		verboseOut.println("Processed " + stateObject.getLineNumber() + " lines.");
	}
	
	private class PreprocessorState extends CodeProcessorState
	{
		private boolean parsingMacro;
		private final MacroBuilder macroBuilder = new MacroBuilder();
		
		public PreprocessorState()
		{
			super(2);
		}

		public boolean isParsingMacro()
		{
			return parsingMacro;
		}
		
		public void setParsingMacro(boolean parsingMacro)
		{
			this.parsingMacro = parsingMacro;
		}
		
		public MacroBuilder getMacroBuilder()
		{
			return macroBuilder;
		}
	}
	
	private static class MacroBuilder
	{
		private String name;
		private String operandNames[];
		
		private String macroAssembly = "";
		
		public MacroBuilder setName(String name)
		{
			this.name = name;
			return this;
		}
		
		public MacroBuilder setOperandNames(String[] operandNames)
		{
			this.operandNames = operandNames;
			return this;
		}
		
		public void addAssemblyLine(String line)
		{
			macroAssembly += line + System.lineSeparator();
		}
		
		public void reset()
		{
			name = null;
			operandNames = null;
			macroAssembly = "";
		}
		
		public Macro create()
		{
			return new Macro(name, operandNames, macroAssembly);
		}
	}
	
	private static class Macro
	{
		private final String name;
		private final String operandNames[];
		
		private String macroAssembly;
		
		public Macro(String name, String operandNames[], String macroAssembly)
		{
			this.name = name;
			this.operandNames = operandNames;
			this.macroAssembly = macroAssembly;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String[] getOperands()
		{
			return operandNames;
		}
		
		public String process(String operandValues[], int lineNumber) throws CodeProcessException
		{
			if (operandNames.length != operandValues.length)
			{
				throw new CodeProcessException(lineNumber, "Macro operands count mismatch.");
			}
			
			String assembly = macroAssembly;
			
			for (int i = 0; i < operandValues.length; i++)
			{
				String operandName = operandNames[i];
				String operandValue = Matcher.quoteReplacement(operandValues[i]);
				
				assembly = assembly.replaceAll(operandName, operandValue);
			}
			
			return assembly;
		}
	}
}
