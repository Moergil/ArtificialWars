package sk.epholl.artificialwars.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.entities.robots.Robot;
import sk.epholl.artificialwars.graphics.GameButton;
import sk.epholl.artificialwars.graphics.GamePanel;

public class GamePanelInput
{
	private ArrayList<GameButton> buttons = new ArrayList<GameButton>();
	
	private SimulationRunner runner;

	public GamePanelInput(MainLogic mainLogic, SimulationRunner runner)
	{
		GameButton button;
		
		this.runner = runner;

		button = new GameButton(10, 90, 545, 565, "Menu");
		button.setActivationListener(() -> {runner.dispose(); mainLogic.showMenu();});
		buttons.add(button);
		
		button = new GameButton(110, 190, 545, 565, "Reset");
		button.setActivationListener(() -> {
			try
			{
				runner.restart();
			}
			catch (Exception e)
			{
				mainLogic.showMenu();
			}
		});
		buttons.add(button);
		
		button = new GameButton(210, 290, 545, 565, "Step");
		button.setActivationListener(() -> runner.step());
		buttons.add(button);

		button = new GameButton(310, 390, 545, 565, "Slow");
		button.setActivationListener(() -> runner.run(100));
		buttons.add(button);
		
		button = new GameButton(410, 490, 545, 565, "Fast");
		button.setActivationListener(() -> runner.run(1));
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
		Set<Entity> entities = runner.getSimulation().getEntities();
		
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
