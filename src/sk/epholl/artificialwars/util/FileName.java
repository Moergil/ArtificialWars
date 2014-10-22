package sk.epholl.artificialwars.util;

public class FileName
{
	public static String removeExtension(String name)
	{
		String tokens[] = name.split("\\.");
		
		int size = tokens[tokens.length - 1].length();
		
		return name.substring(0, name.length() - (size + 1));
	}
	
	public static boolean isLevelFile(String name)
	{
		return "lvl".equals(getExtension(name));
	}
	
	public static boolean isLevelType(String name, String type)
	{
		String typeInName = name.substring(0, type.length());
		
		return typeInName.equals(type);
	}
	
	public static boolean isRobotFile(String name)
	{
		return "rbt".equals(getExtension(name));
	}
	
	private static String getExtension(String name)
	{
		String tokens[] = name.split("\\.");
		
		if (tokens.length < 2)
		{
			return null;
		}
		else
		{
			return tokens[tokens.length - 1];
		}
	}
}
