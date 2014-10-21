package sk.epholl.artificialwars.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Explosion;
import sk.epholl.artificialwars.entities.objectives.Objective;
import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.entities.robots.Robot;
import sk.epholl.artificialwars.entities.robots.RobotTWM1608;
import sk.epholl.artificialwars.logic.LevelLoader;
import sk.epholl.artificialwars.logic.Simulation;
import sk.epholl.artificialwars.logic.GamePanelInput;
import sk.epholl.artificialwars.logic.MainLogic;
import sk.epholl.artificialwars.logic.SimulationRunner;
import sk.epholl.artificialwars.logic.Vector2D;

public class GamePanel extends JPanel implements SimulationRunner
{
	private static final long serialVersionUID = 5460028729487426583L;

	private final MainLogic mainLogic;
	
	private final long seed;
	private final String levelName;
	
	private Simulation simulation;
	
	private GamePanelInput input;
	
	private final Timer logicTimer, graphicsTimer;
	
	private Point mousePointer;
	private Robot selectedRobot;
	
	private final Font monospace = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	private final Map<Integer, RobotDebug> robotDebugs = new HashMap<>();

	public GamePanel(MainLogic mainLogic, String levelName, long seed)
	{
		this.mainLogic = mainLogic;
		
		this.seed = seed;
		this.levelName = levelName;
		
		this.setFocusable(true);
		
		MouseAdapter mouseAdapter = createMouseListener();
		
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
		
		input = new GamePanelInput(this);
		
		logicTimer = new Timer(0, (e) -> {
			simulation.step();
		});
		
		graphicsTimer = new Timer(0, (e) -> {
			repaint();
		});
		
		graphicsTimer.setDelay(16);
		graphicsTimer.start();
	}
	
	@Override
	public void pause()
	{
		logicTimer.stop();
	}
	
	@Override
	public void run(long millisDelay)
	{
		logicTimer.setDelay((int)millisDelay);
		logicTimer.start();
	}
	
	@Override
	public void restart()
	{
		pause();
		
		simulation = new Simulation(seed);
		
		LevelLoader parser = new LevelLoader(simulation);
		
		try
		{
			parser.loadLevel(levelName);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error loading level: ", e);
		}
	}
	
	@Override
	public void dispose()
	{
		pause();
		simulation = null;
		
		graphicsTimer.stop();
		
		mainLogic.showMenu();
	}
	
	@Override
	public Simulation getSimulation()
	{
		return simulation;
	}
	
	@Override
	public void step()
	{
		pause();
		
		simulation.step();
	}

	@Override
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, 800, 600);

		for (Entity e : simulation.getEntities())
		{
			g2d.setColor(e.getColor());

			int width = e.getWidth();
			int height = e.getHeight();
			
			Vector2D cornerPosition = e.getCornerPosition();
			int x = (int)cornerPosition.getX();
			int y = (int)cornerPosition.getY();
			
			g2d.fillRect(x, y, width, height);
			
			Vector2D centerPosition = e.getCenterPosition();
			int x1 = (int)centerPosition.getX();
			int y1 = (int)centerPosition.getY();

			if (e instanceof Robot)
			{
				Vector2D direction = e.getDirection();
				int x2 = (int)(x1 + direction.getX() * 10);
				int y2 = (int)(y1 + direction.getY() * 10);
				
				g2d.drawLine(x1, y1, x2, y2);
			}
		}

		g2d.setColor(Color.yellow);

		g2d.drawString("Time: " + simulation.getCycleCount(), 20, 537);
		// TODO
		g2d.drawString("TODO rozbite", 120, 537);
		
		g2d.drawString(formatMousePointer(), 520, 560);
		
		Robot robot = getSelectedRobot();
		
		Graphics2D g2d2 = (Graphics2D)g2d.create();
		g2d2.translate(10, 490);
		g2d2.setFont(monospace);
		
		if (robot == null)
		{
			g2d2.drawString("Click any robot to select it.", 520, 537);
		}
		else
		{
			drawRobotDebug(g2d2, robot);
		}
		
		g2d2.dispose();	
		
		for (GameButton button : input.getButtons())
		{
			button.paint(g2d);
		}

		Toolkit.getDefaultToolkit().sync();
		g.dispose();
	}
	
	public void addRobotDebug(int identifier, RobotDebug robotDebug)
	{
		robotDebugs.put(identifier, robotDebug);
	}
	
	private void drawRobotDebug(Graphics2D g2d, Robot robot)
	{
		RobotDebug robotDebug = robotDebugs.get(robot.getRobotTypeId());
		
		if (robotDebug != null)
		{
			robotDebug.draw(g2d, robot);
		}
	}
	
	private MouseAdapter createMouseListener()
	{
		return new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent event)
			{
				if (event.getButton() == MouseEvent.BUTTON1 && input != null)
				{
					if (!input.gameButtonClicked(event.getX(), event.getY()))
					{
						selectedRobot = input.checkRobotClicked(event.getX(), event.getY());
					}
				}
			}
			
			@Override
			public void mouseMoved(MouseEvent event)
			{
				mousePointer = new Point(event.getX(), event.getY());
			}
		};
	}
	
	private String formatMousePointer()
	{
		if (mousePointer != null)
		{
			return String.format("Current mouse pos: [%d, %d]", mousePointer.x, mousePointer.y);
		}
		else
		{
			return "";
		}
	}
	
	private Robot getSelectedRobot()
	{
		return selectedRobot;
	}
}
