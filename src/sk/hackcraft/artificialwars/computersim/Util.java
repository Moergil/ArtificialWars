package sk.hackcraft.artificialwars.computersim;

public class Util
{
	public static final byte UNSIGNED_BYTE_MAX_VALUE_BITS = (byte)0xFF;
	public static final int UNSIGNED_BYTE_MAX_VALUE = 0x000000FF;
	
	public static boolean isBetween(int fromInc, int toExc, int value)
	{
		return value >= fromInc && value < toExc;
	}
	
	public static void valueToLittleEndianBytes(int value, byte array[])
	{
		int to = Math.min(array.length, Integer.BYTES);
		for (int i = 0; i < to; i++)
		{
			array[i] = (byte)value;
			value >>>= 8;
		}
	}
	
	public static void valueToBigEndianBytes(int value, byte array[])
	{
		int from = Math.min(array.length, Integer.BYTES);
		for (int i = from - 1; i >= 0; i--)
		{
			array[i] = (byte)value;
			value >>>= 8;
		}
	}
	
	public static String byteArrayToHexaString(byte array[])
	{
		String format = "%02X ";
		StringBuilder builder = new StringBuilder();
		
		for (byte b : array)
		{
			builder.append(String.format(format, b));
		}
		
		return builder.toString();
	}
}
