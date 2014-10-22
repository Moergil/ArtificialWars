package sk.epholl.artificialwars.logic;

public class SystemNanoGameTime implements GameTime
{
	@Override
	public long getTicks()
	{
		return System.nanoTime();
	}
}
