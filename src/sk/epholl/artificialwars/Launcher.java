package sk.epholl.artificialwars;

import sk.epholl.artificialwars.logic.MainLogic;

public class Launcher
{
	public static void main(String[] args)
	{
		new MainLogic("Level 1.txt").run();
	}
}
