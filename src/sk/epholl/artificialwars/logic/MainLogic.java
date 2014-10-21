package sk.epholl.artificialwars.logic;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;

import sk.epholl.artificialwars.entities.robots.StockRobotsId;
import sk.epholl.artificialwars.graphics.Eph32BasicRobotDebug;
import sk.epholl.artificialwars.graphics.GamePanel;
import sk.epholl.artificialwars.graphics.MenuPanel;
import sk.epholl.artificialwars.graphics.TWM1608RobotDebug;

/**
 * @author epholl
 */
public class MainLogic implements Runnable
{
	private JFrame gameWindow;

	private WindowListener windowListener;
	
	public MainLogic(String levelName)
	{
		gameWindow = createGameWindow();

		if (levelName == null)
		{
			showMenu();
		}
		else
		{
			createGame(levelName);
		}
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
	
	private void setWindowListener(WindowListener listener)
	{
		gameWindow.removeWindowListener(windowListener);
		
		this.windowListener = listener;
		gameWindow.addWindowListener(listener);
	}

	@Override
	public void run()
	{
		gameWindow.setVisible(true);
	}

	public void showMenu()
	{
		setWindowListener(null);
		
		MenuPanel menu = new MenuPanel(this);

		setScreenComponent(menu);
	}

	public void createGame(String levelName)
	{
		// TODO
		long seed = 1;
		GamePanel panel = new GamePanel(this, levelName, seed);
		panel.restart();
		
		panel.addRobotDebug(StockRobotsId.Eph32BasicRobot, new Eph32BasicRobotDebug());
		panel.addRobotDebug(StockRobotsId.RobotWTM1608, new TWM1608RobotDebug());

		this.setScreenComponent(panel);
	
		setWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				panel.dispose();
			}
		});
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
