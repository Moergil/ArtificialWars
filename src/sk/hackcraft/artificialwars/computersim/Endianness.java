package sk.hackcraft.artificialwars.computersim;

public enum Endianness
{
	LITTLE
	{
		@Override
		public byte[] valueToBytes(int value, byte[] array)
		{
			return Util.valueToLittleEndianBytes(value, array);
		}
	},
	BIG
	{
		@Override
		public byte[] valueToBytes(int value, byte[] array)
		{
			return Util.valueToBigEndianBytes(value, array);
		}
	};
	
	public abstract byte[] valueToBytes(int value, byte array[]);
}
