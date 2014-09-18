package sk.hackcraft.artificialwars.computersim.toolchain;

public class CodeProcessorState
{
	private final int passes;
	private int pass;
	private int lineNumber;
	
	public CodeProcessorState(int passes)
	{
		this.passes = passes;
		this.pass = 0;
	}
	
	public void reset()
	{
		lineNumber = 0;
	}
	
	public void setLineNumber(int lineNumber)
	{
		this.lineNumber = lineNumber;
	}
	
	public int getLineNumber()
	{
		return lineNumber;
	}
	
	public void incrementLineNumber()
	{
		lineNumber++;
	}
	
	public int getPasses()
	{
		return passes;
	}
	
	public int getPass()
	{
		return pass;
	}
	
	public void incrementPass()
	{
		pass++;
	}
}