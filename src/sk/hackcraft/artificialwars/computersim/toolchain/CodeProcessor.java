package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CodeProcessor<S extends CodeProcessorState>
{
	protected PrintStream verboseOut = new PrintStream(new OutputStream()
	{
		@Override
		public void write(int b) throws IOException
		{
		}
	});
	
	public byte[] process(byte input[]) throws ProcessException, IOException
	{
		ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		process(inputStream, outputStream);
		
		return outputStream.toByteArray();
	}
	
	public abstract void process(InputStream input, OutputStream output) throws ProcessException, IOException;

	protected abstract S started();
	
	protected void finished(S stateObject)
	{
	}
	
	protected List<String> readLines(InputStream input) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(input));

		List<String> lines = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null)
		{
			lines.add(line);
		}

		return lines;
	}
	
	protected List<String> splitLine(String line)
	{
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
		
		ArrayList<String> list = new ArrayList<>();
		while (m.find())
		{
			list.add(m.group(1).trim());
		}
		
		return list;
	}
	
	public static class Line
	{
		private static final String DEFAULT_DELIMITER = " ";

		public static Line createFromNumberedLine(String line)
		{
			int lineNumberEnd = line.indexOf(DEFAULT_DELIMITER);
			
			int number = Integer.parseInt(line.substring(0, lineNumberEnd));
			String content = line.substring(lineNumberEnd + 1);
			
			return new Line(number, content);
		}
		
		private final int number;
		private final String content;
		
		public Line(int number, String content)
		{
			this.number = number;
			this.content = content;
		}
		
		public int getNumber()
		{
			return number;
		}
		
		public String getContent()
		{
			return content;
		}
		
		public Line modify(Function<String, String> modifier)
		{
			String modifiedContent = modifier.apply(content);
			return new Line(number, modifiedContent);
		}
		
		@Override
		public String toString()
		{
			return String.format("%d %s", number, content);
		}
	}
	
	public static class ProcessException extends Exception
	{
		public ProcessException(String message)
		{
			super(message);
		}
	}
	
	public static class CodeSyntaxException extends ProcessException
	{
		private final Line line;
		
		public CodeSyntaxException(Line line, String message)
		{
			super(message);
			
			this.line = line;
		}
		
		public Line getLine()
		{
			return line;
		}
		
		@Override
		public String getMessage()
		{
			return String.format("Error on line %d: %s", line.getNumber(), super.getMessage());
		}
	}
	
	public static class LinkingException extends ProcessException
	{
		public LinkingException(String message)
		{
			super(message);
		}
	}
}
