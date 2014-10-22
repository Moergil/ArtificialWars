package sk.epholl.artificialwars.logic;

import java.util.ArrayList;
import java.util.List;

public class LaunchParams
{
	private String levelName;
	private boolean arena;
	
	private final List<String> robotsNames = new ArrayList<>();
	
	public void setLevelName(String levelName)
	{
		this.levelName = levelName;
	}
	
	public String getLevelName()
	{
		return levelName;
	}
	
	public void setArena(boolean arena)
	{
		this.arena = arena;
	}
	
	public boolean isArena()
	{
		return arena;
	}
	
	public List<String> getRobotsNames()
	{
		return robotsNames;
	}
}
