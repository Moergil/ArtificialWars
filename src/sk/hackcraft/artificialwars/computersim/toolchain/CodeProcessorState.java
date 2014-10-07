package sk.hackcraft.artificialwars.computersim.toolchain;

public class CodeProcessorState
{
	private boolean verbose = true;
	
	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}
	
	public boolean isVerbose()
	{
		return verbose;
	}
}