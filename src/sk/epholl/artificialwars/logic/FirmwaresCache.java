package sk.epholl.artificialwars.logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProgramException;

public class FirmwaresCache
{
	private final Map<String, byte[]> cache = new HashMap<>();
	
	@FunctionalInterface
	public interface Resolver
	{
		byte[] resolve(String architecture, String firmwareName) throws IOException, ProgramException;
	}
	
	private Resolver resolver = (arch, firmware) -> null;
	
	public void setResolver(Resolver resolver)
	{
		this.resolver = resolver;
	}
	
	private String assemblyKey(String architecture, String firmwareName)
	{
		return String.format("%s:%s", architecture, firmwareName);
	}
	
	public byte[] get(String architecture, String firmwareName) throws IOException, ProgramException
	{
		String key = assemblyKey(architecture, firmwareName);
		byte firmware[] = cache.get(key);
		
		if (firmware == null)
		{
			firmware = resolver.resolve(architecture, firmwareName);
			
			if (firmware != null)
			{
				cache.put(key, firmware);
			}
		}
		
		return firmware;
	}
	
	public void clear()
	{
		cache.clear();
	}
}
