package sk.epholl.artificialwars;

import java.util.List;

import sk.epholl.artificialwars.logic.LaunchParams;
import sk.epholl.artificialwars.logic.MainLogic;

public class Launcher
{
	public static void main(String args[])
	{
		if (checkHelp(args))
		{
			return;
		}
		
		LaunchParams p = processLaunchParams(args);
		
		new MainLogic(p).run();
	}
	
	private static boolean checkHelp(String args[])
	{
		Arguments a = new Arguments(args);
		
		Boolean b;
		
		return a.contains("help", (v) -> {if (v) showHelp();});
	}
	
	private static void showHelp()
	{
		String lines[] = {
				"Artificial Wars game created by Epholl & Moergil",
				"",
				"Help guide for command line parameters:",
				"",
				"-level \t\t name of level, without .lvl extension.",
				"-robots \t list of robots names, without .rbt extension. Usable only in arena mode.",
				"-arena \t\t arena mode.",
				"-autostart \t automatic start of level.",
				"-autorestart \t automatic restart of level.",
				"-speed \t\t speed of game in ticks per second.",
				"-seed \t\t seed value for random number generatos. Range is in java long type."
		};
		
		for (String line : lines)
		{
			System.out.println(line);
		}
	}
	
	private static LaunchParams processLaunchParams(String args[])
	{
		Arguments a = new Arguments(args);

		LaunchParams p = new LaunchParams();

		a.value("level", (v) -> p.setLevelName(v));
		
		a.contains("arena", (v) -> p.setArena(v));
		
		a.values("robots", (v) -> {
			List<String> robots = p.getRobotsNames();
			
			for (String value : v)
			{
				robots.add(value);
			}
		});
		
		a.contains("autostart", (v) -> p.setAutostart(v));
		
		a.contains("autorestart", (v) -> p.setRestart(v));
		
		a.valueDouble("speed", (v) -> p.setSpeed(v));
		
		a.valueLong("seed", (v) -> p.setSeed(v));
		
		return p;
	}
}
