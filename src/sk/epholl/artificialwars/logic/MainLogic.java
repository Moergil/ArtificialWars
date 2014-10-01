package sk.epholl.artificialwars.logic;

import java.awt.Container;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;

import sk.epholl.artificialwars.graphics.GamePanel;
import sk.epholl.artificialwars.graphics.MenuPanel;

/**
 * @author epholl
 */
public class MainLogic implements Runnable
{
	private JFrame gameWindow;

	public MainLogic()
	{
		gameWindow = createGameWindow();

		showMenu();
	}

	private JFrame createGameWindow()
	{
		JFrame frame = new JFrame("Artificial Wars");

		frame.setSize(800, 600);
		frame.setFocusable(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		return frame;
	}

	@Override
	public void run()
	{
		gameWindow.setVisible(true);
	}

	public void showMenu()
	{
		MenuPanel menu = new MenuPanel(this);

		setScreenComponent(menu);
	}

	public void loadLevel(String name)
	{
		GameLogic logic = new GameLogic(new Random().nextLong());

		LevelLoader parser = new LevelLoader(logic, name);
		if (!parser.loadLevel())
		{
			throw new RuntimeException("Error loading level: " + name);
		}

		createGame(logic);
	}

	public void createGame(GameLogic logic)
	{
		GamePanel panel = new GamePanel();
		panel.setGameLogic(logic, this);

		this.setScreenComponent(panel);

		logic.startGame();
	}

	public void setScreenComponent(JComponent struct)
	{
		Container container = gameWindow.getContentPane();
		container.removeAll();

		container.add(struct);

		gameWindow.validate();
		gameWindow.repaint();
	}
}
