package sk.epholl.artificialwars;

import java.util.ArrayList;
import java.util.List;

import sk.epholl.artificialwars.logic.LaunchParams;
import sk.epholl.artificialwars.logic.MainLogic;
import sk.epholl.artificialwars.util.FileName;

public class Launcher
{
	public static void main(String args[])
	{
		LaunchParams p = createLaunchParams(args);
		
		new MainLogic(p).run();
	}
	
	private static LaunchParams createLaunchParams(String args[])
	{
		Arguments a = new Arguments(args);
		
		LaunchParams p = new LaunchParams();

		a
		.start()
		.value("level", (v) -> p.setLevelName(v));
		
		a
		.start()
		.contains("arena", (v) -> p.setArena(v));
		
		a.start()
		.values("robots", (v) -> {
			List<String> robots = p.getRobotsNames();
			
			for (String value : v)
			{
				robots.add(value);
			}
		});
		
		a.start()
		.contains("autostart", (v) -> p.setAutostart(v));
		
		a.start()
		.contains("autorestart", (v) -> p.setRestart(v));
		
		a.start()
		.valueDouble("speed", (v) -> p.setSpeed(v));
		
		a.start()
		.valueLong("seed", (v) -> p.setSeed(v));
		
		return p;
	}
}
