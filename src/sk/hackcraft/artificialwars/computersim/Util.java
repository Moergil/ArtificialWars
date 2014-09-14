package sk.hackcraft.artificialwars.computersim;

public class Util
{
	public static boolean isBetween(int fromInc, int toExc, int value)
	{
		return value >= fromInc && value < toExc;
	}
	
	public static int byteToUnsignedInt(int b)
	{
		return b & 0xff;
	}
}
