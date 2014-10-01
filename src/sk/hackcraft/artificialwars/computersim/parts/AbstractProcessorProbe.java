package sk.hackcraft.artificialwars.computersim.parts;

public interface AbstractProcessorProbe<R extends AbstractProcessorProbe.Register>
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
