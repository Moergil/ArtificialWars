package sk.epholl.artificialwars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import sk.epholl.artificialwars.logic.LaunchParams;

public class Arguments
{
	private final Map<String, Integer> indexes = new HashMap<>();
	private final Map<String, Integer> paramsCounts = new HashMap<>();
	private final List<String> data;
	
	public Arguments(String args[])
	{
		data = new ArrayList<>(args.length);
		
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			
			if (isName(arg))
			{
				String name = toName((arg));
				
				indexes.put(name, i);
				
				int paramsCount = 0;
				
				for (int j = i + 1; j < args.length; j++)
				{
					if (isValue(args[j]))
					{
						paramsCount++;
					}
					else
					{
						break;
					}
				}
				
				paramsCounts.put(name, paramsCount);
			}

			data.add(arg);
		}
	}
	
	private boolean isName(String value)
	{
		return value.charAt(0) == '-';
	}
	
	private boolean isValue(String value)
	{
		return value.charAt(0) != '-';
	}
	
	private String toName(String value)
	{
		return value.substring(1);
	}
	
	public Fetcher start()
	{
		return new Fetcher();
	}
	
	public class Fetcher
	{		
		public Fetcher values(String argument, Getter<String[]> mapper)
		{
			if (indexes.containsKey(argument))
			{
				int index = indexes.get(argument);
				
				int paramsCount = paramsCounts.get(argument);
				
				List<String> subData = data.subList(index + 1, index + 1 + paramsCount);
				
				mapper.get(subData.toArray(new String[subData.size()]));
			}
			
			return this;
		}
		
		public Fetcher values(String argument, int paramsCount, Getter<String[]> mapper)
		{
			if (indexes.containsKey(argument))
			{
				int index = indexes.get(argument);
				
				int nameParamsCount = paramsCounts.get(argument);
				paramsCount = Math.min(paramsCount, nameParamsCount);
				
				List<String> subData = data.subList(index + 1, index + 1 + paramsCount);
				
				mapper.get(subData.toArray(new String[subData.size()]));
			}
			
			return this;
		}
		
		public <V> Fetcher value(String argument, Mapper<V> mapper, Getter<V> getter)
		{
			if (indexes.containsKey(argument) && paramsCounts.get(argument) > 0)
			{
				int index = indexes.get(argument) + 1;

				try
				{
					V value = mapper.map(data.get(index));
					getter.get(value);
				}
				catch (Exception e)
				{
					System.err.println("Can't process argument: " + argument);
				}
			}
			
			return this;
		}
		
		public <V> Fetcher value(String argument, Getter<String> getter)
		{
			return value(argument, (s) -> s, getter);
		}
		
		public <V> Fetcher valueLong(String argument, Getter<Long> getter)
		{
			return value(argument, (s) -> Long.decode(s), getter);
		}
		
		public <V> Fetcher valueInt(String argument, Getter<Integer> getter)
		{
			return value(argument, (s) -> Integer.decode(s), getter);
		}
		
		public Fetcher contains(String argument, Getter<Boolean> mapper)
		{
			mapper.get(indexes.containsKey(argument));
			
			return this;
		}
	}
	
	@FunctionalInterface
	public interface Getter<V>
	{
		void get(V v);
	}
	
	@FunctionalInterface
	public interface Mapper<V>
	{
		V map(String v);
	}
}
