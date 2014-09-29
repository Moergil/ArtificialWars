package sk.epholl.artificialwars.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.graphics.GameButton;

public class GamePanelInput
{
	private ArrayList<GameButton> buttons = new ArrayList<GameButton>();
	
	private GameLogic gameLogic;

	public GamePanelInput(GameLogic game, MainLogic mainLogic)
	{
		GameButton button;
		
		this.gameLogic = game;

		button = new GameButton(10, 90, 545, 565, "Menu");
		button.setActivationListener(() -> {game.pauseGame(); mainLogic.showMenu();});
		buttons.add(button);
		
		button = new GameButton(110, 190, 545, 565, "Pause");
		button.setActivationListener(() -> game.pauseGame());
		buttons.add(button);
		
		button = new GameButton(210, 290, 545, 565, "Slow");
		button.setActivationListener(() -> {game.setTimerDelay(80); game.continueGame();});
		buttons.add(button);
		
		button = new GameButton(310, 390, 545, 565, "Normal");
		button.setActivationListener(() -> {game.setTimerDelay(20); game.continueGame();});
		buttons.add(button);
		
		button = new GameButton(410, 490, 545, 565, "Fast");
		button.setActivationListener(() -> {game.setTimerDelay(5); game.continueGame();});
		buttons.add(button);
	}

	public boolean gameButtonClicked(int x, int y)
	{
		for (GameButton button : buttons)
		{
			if (button.checkClicked(x, y))
			{
				button.action();
				return true;
			}
		}
		return false;
	}

	public List<GameButton> getButtons()
	{
		return buttons;
	}
	
	public Eph32BasicRobot checkRobotClicked(int x, int y)
	{
		List<Entity> entities = gameLogic.getEntities();
		
		Set<Entity> robots = entities
				.stream()
				.filter((entity) -> entity instanceof Eph32BasicRobot)
				.collect(Collectors.toSet());

		for (Entity robot: robots)
		{
			if (checkRobotClicked(x, y, robot))
				return (Eph32BasicRobot) robot;
		}
		
		return null;
	}
	
	private boolean checkRobotClicked(int x, int y, Entity entity)
	{
		Eph32BasicRobot robot = (Eph32BasicRobot) entity;
		
		return (x >= robot.getLeftmostPosX() && x <= robot.getRightmostPosX() && y >= robot.getDownmostPosY() && y <= robot.getUppermostPosY());
	}
}
