package sk.epholl.artificialwars.logic;

import java.util.ArrayList;
import java.util.List;

import sk.epholl.artificialwars.graphics.GameButton;

public class GamePanelInput
{
	private ArrayList<GameButton> buttons = new ArrayList<GameButton>();

	public GamePanelInput(GameLogic game, MainLogic mainLogic)
	{
		GameButton button;

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

	public void mouseClicked(int x, int y)
	{
		for (GameButton button : buttons)
		{
			if (button.checkClicked(x, y))
			{
				button.action();
				return;
			}
		}
	}

	public List<GameButton> getButtons()
	{
		return buttons;
	}
}
