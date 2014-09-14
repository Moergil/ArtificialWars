package sk.epholl.artificialwars.entities.instructionsets.exceptions;

public class IllegalInstructionException extends Exception
{
	private String message = "Unknown instruction.";

	public IllegalInstructionException(String text)
	{
		this.message = "Unknown instruction used: " + text;
	}

	@Override
	public String getMessage()
	{
		return message;
	}
}
