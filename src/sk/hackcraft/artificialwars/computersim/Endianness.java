package sk.hackcraft.artificialwars.computersim;

public enum Endianness
{
	LITTLE
	{
		@Override
		public void valueToBytes(int value, byte[] array)
		{
			Util.valueToLittleEndianBytes(value, array);
		}
	},
	BIG
	{
		@Override
		public void valueToBytes(int value, byte[] array)
		{
			Util.valueToBigEndianBytes(value, array);
		}
	};
	
	public abstract void valueToBytes(int value, byte array[]);
	public byte[] valueToBytes(int value, int bytes)
	{
		byte array[] = new byte[bytes];
		
		valueToBytes(value, array);
		
		return array;
	}
}
