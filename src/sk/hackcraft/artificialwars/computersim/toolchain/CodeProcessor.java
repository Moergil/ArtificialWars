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

public abstract class CodeProcessor<SO extends CodeProcessorStateObject>
{
	public byte[] process(byte input[]) throws CodeProcessException, IOException
	{
		ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		process(inputStream, outputStream);
		
		return outputStream.toByteArray();
	}
	
	public void process(InputStream input, OutputStream output) throws CodeProcessException, IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(input));

		DataOutputStream dataOutput = new DataOutputStream(output);

		SO stateObject = createStateObject();
		
		started();
		
		int lineNumber = 0;
		String line;
		while ((line = br.readLine()) != null)
		{
			stateObject.setLineNumber(lineNumber++);
			process(dataOutput, line, stateObject);
		}
		
		finished(stateObject);
	}
	
	protected abstract SO createStateObject();
	protected abstract void process(DataOutput output, String line, SO stateObject) throws CodeProcessException, IOException;
	
	protected void started()
	{	
	}
	
	protected void finished(SO stateObject)
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
