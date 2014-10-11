package sk.hackcraft.artificialwars.computersim.parts;

import java.util.ArrayList;
import java.util.List;

public class MemoryProbe
{
	private final MemoryChip memory;
	
	public MemoryProbe(MemoryChip memory)
	{
		this.memory = memory;
	}
	
	public List<String> getMemory(int offset, int len, int lineCount)
	{
		List<String> output = new ArrayList<String>();
		
		StringBuilder b = new StringBuilder();
		
		// TODO
		for (int i = 0; i < len; i++)
		{
			b.append(memory.readFromChip(i + offset) + " ");
			
			if (i != 0 && i % lineCount == 0)
			{
				output.add(b.toString());
				b.setLength(0);
			}
		}
		
		return output;
	}
}
