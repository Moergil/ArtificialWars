package sk.hackcraft.artificialwars.computersim;

public interface Device extends ComputerPart
{
	default String getPinName(int index)
	{
		return "P" + index;
	}
	
	int getPinsCount();
	void setBusConnection(Pins pins);
}
