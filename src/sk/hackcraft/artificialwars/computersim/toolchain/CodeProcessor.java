package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public abstract class CodeProcessor<S extends CodeProcessorState>
{
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
	}
}
