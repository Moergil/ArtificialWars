package sk.hackcraft.artificialwars.computersim.parts;

import java.util.EnumMap;
import java.util.Map;

import sk.hackcraft.artificialwars.computersim.Util;
import sk.hackcraft.artificialwars.computersim.debug.CommonValueFormatter;
import sk.hackcraft.artificialwars.computersim.debug.CommonValueFormatter.IntFormatter;

public class ProbeProcessorTEK1608
{
	public interface Register
	{
		String getName();
		int getBytesSize();
	}
	
	public enum RegisterTEK1608 implements Register
	{
		A(1),
		X(1),
		Y(1),
		SP(1),
		PC(2),
		SR(1);
		
		private final int bytesSize;
		
		private RegisterTEK1608(int bytesSize)
		{
			this.bytesSize = bytesSize;
		}
		
		@Override
		public String getName()
		{
			return toString();
		}
		
		@Override
		public int getBytesSize()
		{
			return bytesSize;
		}
	}
	
	private final ProcessorTEK1608 processor;
	
	private final Map<RegisterTEK1608, IntFormatter> formatters = new EnumMap<>(RegisterTEK1608.class);
	
	public ProbeProcessorTEK1608(ProcessorTEK1608 processor)
	{
		this.processor = processor;
		
		formatters.put(RegisterTEK1608.A, CommonValueFormatter::toHexa8);
		formatters.put(RegisterTEK1608.X, CommonValueFormatter::toHexa8);
		formatters.put(RegisterTEK1608.Y, CommonValueFormatter::toHexa8);
		formatters.put(RegisterTEK1608.SP, CommonValueFormatter::toHexa8);
		formatters.put(RegisterTEK1608.PC, CommonValueFormatter::toHexa16);
		formatters.put(RegisterTEK1608.SR, CommonValueFormatter::toBinary8);
	}
	
	public RegisterTEK1608[] getRegisters()
	{
		return RegisterTEK1608.values();
	}
	
	private short get(RegisterTEK1608 register)
	{
		switch (register)
		{
			case A:
				return processor.a;
			case X:
				return processor.x;
			case Y:
				return processor.y;
			case SP:
				return processor.sp;
			case PC:
				return processor.pc;
			case SR:
				return processor.sr;
			default:
				throw new IllegalArgumentException("Getting register " + register + " value not supported.");
		}
	}
	
	public byte getByteValue(RegisterTEK1608 register)
	{
		return (byte)get(register);
	}
	
	public short getShortValue(RegisterTEK1608 register)
	{
		return get(register);
	}
	
	public void setValue(RegisterTEK1608 register, int value)
	{
		switch (register)
		{
			case A:
				processor.a = (byte)value;
				break;
			case X:
				processor.x = (byte)value;
				break;
			case Y:
				processor.y = (byte)value;
				break;
			case SP:
				processor.sp = (byte)value;
				break;
			case PC:
				processor.pc = (short)value;
				break;
			case SR:
				processor.sr = (byte)value;
				break;
			default:
				throw new IllegalArgumentException("Setting register " + register + " value not supported.");
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		
		for (RegisterTEK1608 register : RegisterTEK1608.values())
		{
			b
			.append(register.getName()).append(":")
			.append(formatters.get(register).format(getShortValue(register)))
			.append(" ");
		}
		
		return b.toString();
	}
}
