package sk.hackcraft.artificialwars.computersim;

public interface Pins
{
	public static final int UNCONNECTED = -1;
	
	void setPin(int index, boolean value);
	boolean readPin(int index);
	
	void setAllPins(boolean value);
	
	default void setPins(int indexes[], boolean values[])
	{
		if (indexes.length != values.length)
		{
			throw new IllegalArgumentException("Arrays have to be of equals length.");
		}
		
		for (int i = 0; i < indexes.length; i++)
		{
			int pinIndex = indexes[i];
			boolean value = values[i];
			setPin(pinIndex, value);
		}
	}
	
	default void readPins(int indexes[], boolean values[])
	{
		if (indexes.length != values.length)
		{
			throw new IllegalArgumentException("Arrays have to be of equals length.");
		}
		
		for (int i = 0; i < indexes.length; i++)
		{
			int pinIndex = indexes[i];
			values[i] = readPin(pinIndex);
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
