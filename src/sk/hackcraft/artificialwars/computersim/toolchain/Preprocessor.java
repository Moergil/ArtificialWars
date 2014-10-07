package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class Preprocessor extends CodeProcessor<CodeProcessorState>
{
	private String lineComment;
	private String macroStart, macroEnd;

	private final Map<String, Macro> macros = new HashMap<>();
	
	public Preprocessor(String lineComment, String macroStart, String macroEnd)
	{	
		this.lineComment = lineComment;
		this.macroStart = macroStart;
		this.macroEnd = macroEnd;
	}
	
	@Override
	protected PreprocessorState started()
	{
		PreprocessorState state = new PreprocessorState();
		
		if (state.isVerbose())
		{
			verboseOut = System.out;
		}
		
		verboseOut.println("Preprocessor started.");
		
		return state;
	}
	
	@Override
	public void process(InputStream input, OutputStream output) throws CodeSyntaxException, IOException
	{
		PreprocessorState state = started();
		
		List<String> lines = readLines(input);
		List<Line> codeLines = new ArrayList<>();
		
		// remove mess (comments, empty lines) and find macros
		verboseOut.println("*** pass 1 ***");
		int lineNumber = 0;
		for (String lineString : lines)
		{
			lineNumber++;
			
			String cleanedLine = cleanLine(lineString);
			if (cleanedLine.isEmpty())
			{
				continue;
			}

			Line line = new Line(lineNumber, cleanedLine);
			
			if (state.isParsingMacro())
			{
				parseMacro(line, state);
			}
			else if (!scanMacros(cleanedLine, state))
			{
				codeLines.add(line);
			}
		}

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
		
		// unroll macros
		verboseOut.println("*** pass 2 ***");
		for (Line line : codeLines)
		{
			preprocessCode(line, state, writer);
		}

		writer.close();

		finished(state);
	}
	
	private String cleanLine(String line)
	{
		int commentIndex = line.indexOf(lineComment);
		
		if (commentIndex != -1)
		{
			line = line.substring(0, commentIndex);
		}
		
		return line.trim();
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

	private void parseMacro(Line line, PreprocessorState state)
	{
		List<String> parts = splitLine(line.getContent());
		
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
	
	private void preprocessCode(Line line, PreprocessorState state, BufferedWriter writer) throws CodeSyntaxException, IOException
	{
		String parts[] = line.getContent().split(" ");
		
		Macro macro = macros.get(parts[0]);
		
		if (macro == null)
		{
			verboseOut.println(line);
			
			writer.write(line.toString());
			writer.newLine();
			return;
		}
		else
		{
			String operandValues[] = Arrays.copyOfRange(parts, 1, parts.length);

			List<Line> macroLines = macro.process(line, operandValues);
			
			for (Line macroLine : macroLines)
			{
				String rawMacroLine = macroLine.toString();
				
				verboseOut.println(rawMacroLine);
				
				writer.write(rawMacroLine);
				writer.newLine();
			}
		}
	}
	
	@Override
	protected void finished(CodeProcessorState stateObject)
	{
		verboseOut.println("Preprocessing finished.");
	}
	
	private class PreprocessorState extends CodeProcessorState
	{
		private boolean parsingMacro;
		private final MacroBuilder macroBuilder = new MacroBuilder();

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

		private List<Line> macroAssembly = new ArrayList<>();
		
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
		
		public void addAssemblyLine(Line line)
		{
			macroAssembly.add(line);
		}
		
		public void reset()
		{
			name = null;
			operandNames = null;
			macroAssembly.clear();
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
		
		private List<Line> macroAssembly = new ArrayList<>();
		
		public Macro(String name, String operandNames[], List<Line> macroAssembly)
		{
			this.name = name;
			this.operandNames = operandNames;
			this.macroAssembly.addAll(macroAssembly);
		}
		
		public String getName()
		{
			return name;
		}
		
		public List<Line> process(Line line, String operandValues[]) throws CodeSyntaxException
		{
			if (operandNames.length != operandValues.length)
			{
				throw new CodeSyntaxException(line, "Macro operands count mismatch.");
			}

			List<Line> processedLines = new ArrayList<>(macroAssembly.size());
			
			for (Line macroLine : macroAssembly)
			{
				for (int i = 0; i < operandValues.length; i++)
				{
					String operandName = operandNames[i];
					String operandValue = Matcher.quoteReplacement(operandValues[i]);

					Line processedLine = macroLine.modify((content) -> content.replaceAll(operandName, operandValue));
					processedLines.add(processedLine);
				}
			}
			
			return processedLines;
		}
	}
}
