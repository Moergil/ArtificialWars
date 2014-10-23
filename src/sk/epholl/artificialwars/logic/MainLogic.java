package sk.epholl.artificialwars.logic;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.List;
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
import sk.epholl.artificialwars.graphics.GamePanel.SimulationCreatedListener;
import sk.epholl.artificialwars.logic.RobotCreator.AbstractRobot;
import sk.epholl.artificialwars.logic.objectives.ArenaObjective;
import sk.epholl.artificialwars.logic.objectives.CommandAtLeastUnitsObjective;
import sk.epholl.artificialwars.logic.objectives.DestroyEnemyObjective;
import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProgramException;

public class MainLogic implements Runnable
{
	private JFrame gameWindow;

	private WindowListener windowListener;
	
	private final long masterSeed;
	
	public MainLogic(LaunchParams p)
	{
		gameWindow = createGameWindow();

		Long seed = p.getSeed();
		masterSeed = (seed != null) ? seed : new Random().nextLong();
		
		if (p.getLevelName() == null)
		{
			showMenu();
			return;
		}
		
		if (p.isArena())
		{
			if (hasAllArenaParameters(p))
			{
				createArenaGame(p);
			}
			else
			{
				showArenaProperties();
			}
		}
		else
		{
			createMissionGame(p);
		}
	}
	
	private boolean hasAllArenaParameters(LaunchParams p)
	{
		return p.getLevelName() != null && p.getRobotsNames().size() >= 2;
	}
	
	public void createArenaGame(LaunchParams p)
	{
		String levelName = p.getLevelName() + ".lvl";
		
		List<String> robotsNames = p.getRobotsNames();
		
		String robot1Name = robotsNames.get(0) + ".rbt";
		String robot2Name = robotsNames.get(1) + ".rbt";
		
		SimulationCreatedListener listener = (simulation) -> prepareArena(simulation, robot1Name, robot2Name);
		GamePanel gamePanel = createGame(levelName, listener);
		
		configureSimulationRunner(gamePanel, p);
	}
	
	public GamePanel createMissionGame(String levelName)
	{
		SimulationCreatedListener listener = (simulation) -> {};
		return createGame(levelName, listener);
	}
	
	public void createMissionGame(LaunchParams p)
	{
		String levelName = p.getLevelName() + ".lvl";
		GamePanel gamePanel = createMissionGame(levelName);
		
		configureSimulationRunner(gamePanel, p);
	}
	
	private void configureSimulationRunner(SimulationRunner runner, LaunchParams p)
	{
		boolean autoStart = p.isAutoStart();
		
		runner.setAutoStart(autoStart);
		
		runner.setAutoRestart(p.isAutoRestart());

		Double speed = p.getSpeed();
		if (speed != null)
		{
			runner.setSpeed(speed);
		}
		
		if (autoStart)
		{
			runner.run();
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

	public GamePanel createGame(String levelName, SimulationCreatedListener listener)
	{
		long seed = masterSeed;
		GamePanel panel = new GamePanel(this, levelName, seed);
		
		panel.setSimulationCreatedListener(listener);
		
		setGamePanel(panel);
		
		return panel;
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
				new ErrorWindow("Error", String.format("Creating robot %s failed: %s%n", robotName, r)).show();
			});
			
			spawn.act();
		}
		
		simulation.addObjective(new ArenaObjective(1));
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
			panel.loadAvailableFiles();
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
				System.out.println("Can't save arena properties: " + exc.getMessage());
			}
			
			String levelFileName = panel.getLevel().getFileName();
			String robot1FileName = panel.getRobot(1).getFileName();
			String robot2FileName = panel.getRobot(2).getFileName();
			
			SimulationCreatedListener listener = (simulation) -> prepareArena(simulation, robot1FileName, robot2FileName);
			createGame(levelFileName, listener);
		});
		
		setScreenComponent(panel);
	}
}
