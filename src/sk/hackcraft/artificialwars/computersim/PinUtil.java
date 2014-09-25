package sk.hackcraft.artificialwars.computersim;

import java.util.Arrays;

public class PinUtil
{
	public static int[] createSequenceIndexes(int offset, int len)
	{
		int indexes[] = new int[len];
		
		for (int i = 0; i < indexes.length; i++)
		{
			indexes[i] = i + offset;
		}
		
		return indexes;
	}
	
	public static String bitsToString(boolean[] bits)
	{
		StringBuilder builder = new StringBuilder(bits.length);
		
		for (int i = bits.length - 1; i >= 0; i--)
		{
			builder.append(bits[i] ? '1' : '0');
		}
		
		return builder.toString();
	}
	
	public static void codeValue(long value, boolean bits[])
	{
		for (int i = 0; i < bits.length; i++)
		{
			bits[i] = (value & 1) != 0;
			value >>>= 1;
		}
	}
	
	public static long decodeValue(boolean[] bits)
	{
		long value = 0;
		
		for (int i = bits.length - 1; i >= 0; i--)
		{
			int pinValue = bits[i] ? 1 : 0;
			
			value |= pinValue;
			
			if (i > 0)
			{
				value <<= 1;
			}
		}
		
		return value;
	}
	
	public static void shiftLeft(boolean[] bits, int shift)
	{
		if (shift <= 0)
		{
			return;
		}
		
		if (shift > bits.length)
		{
			Arrays.fill(bits, false);
			return;
		}
		
		for (int i = bits.length - 1; i >= 0; i--)
		{
			boolean value = bits[i];
			
			int newI = i + shift;
			if (newI < bits.length)
			{
				bits[newI] = value;
			}
			
			bits[i] = false;
		}
	}
	
	public static void shiftRight(boolean[] bits, int shift)
	{
		if (shift <= 0)
		{
			return;
		}
		
		if (shift > bits.length)
		{
			Arrays.fill(bits, false);
			return;
		}
		
		for (int i = 0; i < bits.length; i++)
		{
			boolean value = bits[i];
			
			int newI = i - shift;
			if (newI >= 0)
			{
				bits[newI] = value;
			}
			
			bits[i] = false;
		}
	}
	
	public static void copy(boolean[] bitsFrom, boolean[] bitsTo, int[] pinout)
	{
		for (int i = 0; i < bitsFrom.length; i++)
		{
			if (pinout[i] == Pins.UNCONNECTED)
			{
				continue;
			}
			
			int ti = pinout[i];
			
			bitsTo[ti] = bitsFrom[i];
		}
	}
	
	public static boolean checkMask(boolean[] bits, boolean[] mask)
	{
		for (int i = 0; i < bits.length; i++)
		{
			if (mask[i] && !bits[i])
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static void combine(boolean[] bitsFrom, boolean[] bitsTo)
	{
		for (int i = 0; i < bitsFrom.length; i++)
		{
			if (bitsFrom[i])
			{
				bitsTo[i] = true;
			}
		}
	}
	
	public static void clear(boolean[] bits)
	{
		Arrays.fill(bits, false);
	}
}
