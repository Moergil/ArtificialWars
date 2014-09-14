package sk.hackcraft.artificialwars.computersim;

public interface Pins
{
	public static final int UNCONNECTED = -1;
	
	void setPin(int index, boolean value);
	boolean readPin(int index);
	
	void setAllPins(boolean value);
	
	default void setPins(boolean[] values)
	{
		setPins(values, 0, values.length);
	}
	
	default void readPins(boolean[] readValues)
	{
		readPins(readValues, 0, readValues.length);
	}
	
	default void setPins(boolean[] values, int offset, int len)
	{
		for (int i = 0; i < len; i++)
		{
			setPin(i + offset, values[i]);
		}
	}
	
	default void readPins(boolean[] readValues, int offset, int len)
	{
		for (int i = 0; i < len; i++)
		{
			readValues[i] = readPin(i + offset);
		}
	}
	
	public static final Pins DUMMY = new Pins()
	{
		@Override
		public void setPin(int index, boolean value)
		{
		}
		
		@Override
		public boolean readPin(int index)
		{
			return false;
		}
		
		public void setAllPins(boolean value)
		{
		}
	};
}
