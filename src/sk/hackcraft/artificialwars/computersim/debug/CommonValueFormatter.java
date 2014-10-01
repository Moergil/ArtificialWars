package sk.hackcraft.artificialwars.computersim.debug;


public class CommonValueFormatter
{
	@FunctionalInterface
	public interface IntFormatter
	{
		String format(int value);
	}
	
	private static String format(String zeros, String raw)
	{
		raw = zeros + raw;
		
		int from = raw.length() - zeros.length();
		int to = raw.length();
		return raw.substring(from, to);
	}
	
	public static String toBinary8(int value)
	{
		return format("00000000", Integer.toBinaryString(value));
	}
	
	public static String toHexa2(int value)
	{
		return format("00", Integer.toHexString(value).toUpperCase());
	}
	
	public static String toHexa4(int value)
	{
		return format("0000", Integer.toHexString(value).toUpperCase());
	}
	
	public static String toDecimal5(int value)
	{
		return format("    ", Integer.toString(value));
	}
}