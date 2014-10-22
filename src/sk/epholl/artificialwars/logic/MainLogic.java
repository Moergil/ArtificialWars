package sk.epholl.artificialwars.logic;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;

import sk.epholl.artificialwars.entities.Spawn;
import sk.epholl.artificialwars.entities.robots.FirmwareCompiler;
import sk.epholl.artificialwars.entities.robots.Robot;
import sk.epholl.artificialwars.entities.robots.StockRobotsId;
import sk.epholl.artificialwars.graphics.ArenaPropertiesPanel;
import sk.epholl.artificialwars.graphics.Eph32BasicRobotDebug;
import sk.epholl.artificialwars.graphics.ErrorWindow;
import sk.epholl.artificialwars.graphics.GamePanel;
import sk.epholl.artificialwars.graphics.MenuPanel;
import sk.epholl.artificialwars.graphics.TWM1608RobotDebug;
import sk.epholl.artificialwars.logic.RobotCreator.AbstractRobot;
import sk.epholl.artificialwars.logic.objectives.ArenaObjective;
import sk.epholl.artificialwars.logic.objectives.CommandAtLeastUnitsObjective;
import sk.epholl.artificialwars.logic.objectives.DestroyEnemyObjective;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProgramException;

/**
 * @author epholl
 */
public class MainLogic implements Runnable
{
	private JFrame gameWindow;

	private WindowListener windowListener;
	
	private long masterSeed = 1;
	
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
		long seed = masterSeed;
		GamePanel panel = new GamePanel(this, levelName, seed);
		
		setGamePanel(panel);
	}
	
	public void createArenaGame(String levelName, String robot1Name, String robot2Name)
	{
		String fullLevelName = "dm_" + levelName + ".lvl";

		long seed = masterSeed;
		GamePanel panel = new GamePanel(this, fullLevelName, seed);
		
		panel.setSimulationCreatedListener((simulation) -> prepareArena(simulation, robot1Name, robot2Name));
		
		setGamePanel(panel);
	}
	
	private void setGamePanel(GamePanel panel)
	{
		try
		{
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
		catch (Exception e)
		{
			showMenu();
		}
	}
	
	private void prepareArena(Simulation simulation, String robot1Name, String robot2Name) throws IOException, ProgramException
	{
		FirmwaresCache firmwaresCache = new FirmwaresCache();
		firmwaresCache.setResolver((arch, firmware) -> RobotFactory.loadFirmware(arch, firmware));
		
		Map<Integer, Spawn> spawns = simulation.getSpawns();
		
		if (!spawns.containsKey(1) || !spawns.containsKey(2))
		{
			throw new IllegalStateException("Missing spawn points.");
		}
		
		String robotsNames[] = {robot1Name, robot2Name};
		Color robotsColors[] = {Color.RED, Color.GREEN};
		
		for (int i = 0; i < robotsNames.length; i++)
		{
			Spawn spawn = spawns.get(i + 1);
			
			String robotName = robotsNames[i];
			Color robotColor = robotsColors[i];
			int robotPlayer = spawn.getId();

			spawn.setCreator((s) -> {
				AbstractRobot abstractRobot = RobotFactory.loadAbstractRobot(robotName);
				byte firmware[] = firmwaresCache.get(abstractRobot.getArchitecture(), abstractRobot.getCodeFileName());
				
				Robot robot = RobotFactory.loadStandardRobot(simulation, abstractRobot, firmware);

				robot.setColor(robotColor);
				robot.setCenterPosition(s.getCenterPosition());
				robot.setPlayer(robotPlayer);

				simulation.addEntity(robot);
			});
			
			spawn.setCreationFailedListener((r) -> {
				System.out.printf("Creating robot %s failed: %s%n", robotName, r);
			});
		}
		
		simulation.addObjective(new ArenaObjective(1));
		
		simulation.step();
	}

	public void setScreenComponent(Container component)
	{
		Container container = gameWindow.getContentPane();
		container.removeAll();

		container.add(component);

		gameWindow.validate();
		gameWindow.repaint();
	}

	public void showArenaProperties()
	{
		ArenaPropertiesPanel panel = new ArenaPropertiesPanel();
		
		try
		{
			panel.loadDefaultValues();
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
		
		panel.setBackListener((e) -> showMenu());
		panel.setStartListener((e) -> {
			try
			{
				panel.saveDefaultValues();
			}
			catch (IOException exc)
			{
				System.out.println(exc.getMessage());
			}
			
			String levelName = panel.getLevelName();
			String robot1Name = panel.getRobotName(1);
			String robot2Name = panel.getRobotName(2);
			
			createArenaGame(levelName, robot1Name, robot2Name);
		});
		
		setScreenComponent(panel);
	}
}
