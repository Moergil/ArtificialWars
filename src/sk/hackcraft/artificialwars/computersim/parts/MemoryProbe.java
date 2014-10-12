package sk.hackcraft.artificialwars.computersim.parts;

import java.util.ArrayList;
import java.util.List;

import sk.hackcraft.artificialwars.computersim.debug.CommonValueFormatter;

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
		
		if (len == 0)
		{
			output.add("");
			return output;
		}
		
		StringBuilder b = new StringBuilder();

		for (int i = 0; i < len; i++)
		{
			b
			.append(CommonValueFormatter.toHexa2(memory.readFromChip(i + offset)))
			.append(" ");
			
			if (i != 0 && i % lineCount == 0)
			{
				output.add(b.toString());
				b.setLength(0);
			}
		}
		
		if (b.length() > 0)
		{
			output.add(b.toString());
		}
		
		return output;
	}
}
