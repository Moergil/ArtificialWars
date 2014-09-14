package sk.hackcraft.artificialwars.computersim;

public interface Device extends Part
{
	default String getPinName(int index)
	{
		return "unknown";
	}
	
	int getPinsCount();
	void setBusConnection(Pins pins);
}
