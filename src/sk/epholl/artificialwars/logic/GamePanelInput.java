package sk.epholl.artificialwars.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.entities.robots.Robot;
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
		
		button = new GameButton(110, 190, 545, 565, "Reset");
		button.setActivationListener(() -> mainLogic.loadLevel(game.getLevelName()));
		buttons.add(button);
		
		button = new GameButton(210, 290, 545, 565, "Step");
		button.setActivationListener(() -> {game.pauseGame(); game.singleStep();});
		buttons.add(button);
		
		button = new GameButton(310, 390, 545, 565, "Slow");
		button.setActivationListener(() -> {game.setTimerDelay(40); game.continueGame();});
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
	
	public Robot checkRobotClicked(int x, int y)
	{
		Set<Entity> entities = gameLogic.getEntities();
		
		Vector2D pointer = new Vector2D(x, y);
		
		return entities
				.stream()
				.filter((entity) -> entity instanceof Robot)
				.filter((entity) -> entity.isCollidingWith(pointer))
				.map((entity) -> (Robot)entity)
				.findAny()
				.orElse(null);
	}
}
