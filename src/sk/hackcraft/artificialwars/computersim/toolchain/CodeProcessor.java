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
import java.util.Scanner;
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
	
	public byte[] process(String name, byte input[]) throws ProgramException, IOException
	{
		ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		process(name, inputStream, outputStream);
		
		return outputStream.toByteArray();
	}
	
	public abstract void process(String name, InputStream input, OutputStream output) throws ProgramException, IOException;

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
			
			Scanner scanner = new Scanner(line);
			
			String fileName = scanner.next();
			int number = scanner.nextInt();
			String content = scanner.nextLine();
			
			return new Line(fileName, number, content);
		}
		
		private final String fileName;
		private final int number;
		private final String content;
		
		public Line(String fileName, int number, String content)
		{
			this.fileName = fileName;
			this.number = number;
			this.content = content;
		}
		
		public String getFileName()
		{
			return fileName;
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
			return new Line(fileName, number, modifiedContent);
		}
		
		@Override
		public String toString()
		{
			return String.format("%s %d %s", fileName, number, content);
		}
	}
	
	public static class ProgramException extends Exception
	{
		public ProgramException(String message)
		{
			super(message);
		}
	}
	
	public static class CodeSyntaxException extends ProgramException
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
			return String.format("In %s:%d, %s", line.getFileName(), line.getNumber(), super.getMessage());
		}
	}
	
	public static class LinkingException extends ProgramException
	{
		public LinkingException(String message)
		{
			super(message);
		}
	}
}
