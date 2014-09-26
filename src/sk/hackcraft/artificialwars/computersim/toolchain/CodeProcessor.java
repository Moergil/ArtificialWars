package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CodeProcessor<S extends CodeProcessorState>
{
	protected PrintWriter verboseOut = new PrintWriter(new Writer()
	{
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException
		{
		}
		
		@Override
		public void flush() throws IOException
		{
		}
		
		@Override
		public void close() throws IOException
		{
		}
	});
	
	public byte[] process(byte input[]) throws CodeProcessException, IOException
	{
		ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		process(inputStream, outputStream);
		
		return outputStream.toByteArray();
	}
	
	public abstract void process(InputStream input, OutputStream output) throws CodeProcessException, IOException;

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
	
	public static class CodeProcessException extends Exception
	{
		private final int lineNumber;
		
		public CodeProcessException(int lineNumber, String message)
		{
			super(message);
			
			this.lineNumber = lineNumber;
		}
		
		public int getLineNumber()
		{
			return lineNumber;
		}
		
		@Override
		public String getMessage()
		{
			return "Line " + lineNumber + ": " + super.getMessage();
		}
	}
}
