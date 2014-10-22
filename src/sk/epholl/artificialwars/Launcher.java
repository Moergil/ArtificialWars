package sk.epholl.artificialwars;

import sk.epholl.artificialwars.logic.LaunchParams;
import sk.epholl.artificialwars.logic.MainLogic;

public class Launcher
{
	public static void main(String[] args)
	{
		LaunchParams p = new LaunchParams();
		
		p.getRobotsNames().add("draken.rbt");
		p.getRobotsNames().add("maus.rbt");
		
		p.setArena(true);
		p.setLevelName("dm_arena_1.lvl");
		
		new MainLogic(p).run();
	}
}
