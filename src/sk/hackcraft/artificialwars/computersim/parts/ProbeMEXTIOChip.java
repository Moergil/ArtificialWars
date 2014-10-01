package sk.hackcraft.artificialwars.computersim.parts;

import java.util.EnumMap;
import java.util.Map;

import sk.hackcraft.artificialwars.computersim.debug.CommonValueFormatter;
import sk.hackcraft.artificialwars.computersim.debug.CommonValueFormatter.IntFormatter;

public class ProbeMEXTIOChip implements AbstractProcessorProbe<ProbeMEXTIOChip.RegisterMEXTIO>
{	
	public enum RegisterMEXTIO implements AbstractProcessorProbe.Register
	{
		RH(1),
		RL(1),
		ROH(1),
		ROL(1),
		MOV(1),
		F(1),
		N(1),
		DS(1),
		DG(1);

		private final int bytesSize;
		
		private RegisterMEXTIO(int bytesSize)
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
	
	private final MEXTIOChip chip;
	
	private final Map<RegisterMEXTIO, IntFormatter> formatters = new EnumMap<>(RegisterMEXTIO.class);
	
	public ProbeMEXTIOChip(MEXTIOChip chip)
	{
		this.chip = chip;
		
		formatters.put(RegisterMEXTIO.RH, CommonValueFormatter::toHexa2);
		formatters.put(RegisterMEXTIO.RL, CommonValueFormatter::toHexa2);
		formatters.put(RegisterMEXTIO.ROH, CommonValueFormatter::toHexa2);
		formatters.put(RegisterMEXTIO.ROL, CommonValueFormatter::toHexa2);
		formatters.put(RegisterMEXTIO.MOV, CommonValueFormatter::toHexa2);
		formatters.put(RegisterMEXTIO.F, CommonValueFormatter::toBinary8);
		formatters.put(RegisterMEXTIO.N, CommonValueFormatter::toHexa2);
		formatters.put(RegisterMEXTIO.DS, CommonValueFormatter::toBinary8);
		formatters.put(RegisterMEXTIO.DG, CommonValueFormatter::toHexa2);
	}
	
	public RegisterMEXTIO[] getRegisters()
	{
		return RegisterMEXTIO.values();
	}
	
	private byte get(RegisterMEXTIO register)
	{
		switch (register)
		{
			case RH:
				return chip.absoluteRotationHibyte;
			case RL:
				return chip.absoluteRotationLobyte;
			case ROH:
				return chip.rotationOrderHibyte;
			case ROL:
				return chip.rotationOrderLobyte;
			case MOV:
				return chip.moveOrderValue;
			case F:
				return chip.flags;
			case N:
				return chip.noise;
			case DS:
				return chip.detectionSegment;
			case DG:
				return chip.detectionGradient;
			default:
				throw new IllegalArgumentException("Getting register " + register + " value not supported.");
		}
	}
	
	@Override
	public byte getByteValue(RegisterMEXTIO register)
	{
		return (byte)get(register);
	}
	
	@Override
	public short getShortValue(RegisterMEXTIO register)
	{
		return get(register);
	}
	
	@Override
	public void setValue(RegisterMEXTIO register, int value)
	{
		switch (register)
		{
			case RH:
				chip.absoluteRotationHibyte = (byte)value;
			case RL:
				chip.absoluteRotationLobyte = (byte)value;
			case ROH:
				chip.rotationOrderHibyte = (byte)value;
			case ROL:
				chip.rotationOrderLobyte = (byte)value;
			case MOV:
				chip.moveOrderValue = (byte)value;
			case F:
				chip.flags = (byte)value;
			case N:
				chip.noise = (byte)value;
			case DS:
				chip.detectionSegment = (byte)value;
			case DG:
				chip.detectionGradient = (byte)value;
			default:
				throw new IllegalArgumentException("Setting register " + register + " value not supported.");
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		
		for (RegisterMEXTIO register : RegisterMEXTIO.values())
		{
			b
			.append(register.getName()).append(":")
			.append(formatters.get(register).format(getShortValue(register)))
			.append(" ");
		}
		
		return b.toString();
	}
}
