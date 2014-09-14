package sk.hackcraft.artificialwars.computersim.toolchain;

public class CodeProcessorStateObject
{
	private int lineNumber;
	
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
}