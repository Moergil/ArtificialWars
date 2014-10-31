package sk.epholl.artificialwars;

import java.util.List;

import sk.epholl.artificialwars.logic.LaunchParams;
import sk.epholl.artificialwars.logic.MainLogic;

public class Launcher
{
	public static void main(String args[])
	{
		Arguments a = new Arguments(args);
		
		if (checkHelp(a))
		{
			showHelp();
		}
		else
		{
			LaunchParams p = processLaunchParams(a);	
			new MainLogic(p).run();
		}
	}
	
	private static boolean checkHelp(Arguments a)
	{
		
		return a.contains("help", (v) -> {});
	}
	
	private static void showHelp()
	{
		String lines[] = {
				"Artificial Wars game created by Epholl & Moergil",
				"",
				"Help guide for command line parameters:",
				"",
				"-help \t\t shows this help, without running the game.",
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
	
	private static LaunchParams processLaunchParams(Arguments a)
	{
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
