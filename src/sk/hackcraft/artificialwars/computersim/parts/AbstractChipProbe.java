package sk.hackcraft.artificialwars.computersim.parts;

public interface AbstractChipProbe<R extends AbstractChipProbe.Register>
{
	public interface Register
	{
		String getName();

		int getBytesSize();
	}

	byte getByteValue(R register);

	short getShortValue(R register);

	void setValue(R register, int value);
}
