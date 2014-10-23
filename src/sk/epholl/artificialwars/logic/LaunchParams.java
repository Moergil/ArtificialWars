package sk.epholl.artificialwars.logic;

import java.util.ArrayList;
import java.util.List;

public class LaunchParams
{
	private String levelName;
	private boolean arena;
	private boolean autostart, restart;

	private Long seed;
	
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
	
	public void setAutostart(boolean autostart)
	{
		this.autostart = autostart;
	}
	
	public boolean isAutostart()
	{
		return autostart;
	}
	
	public void setRestart(boolean restart)
	{
		this.restart = restart;
	}
	
	public boolean isRestart()
	{
		return restart;
	}

	public void setSeed(long seed)
	{
		this.seed = seed;
	}
	
	public boolean hasSeed()
	{
		return seed != null;
	}
	
	public long getSeed()
	{
		return seed;
	}
}
