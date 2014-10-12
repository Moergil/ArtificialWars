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

import javax.swing.JPanel;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Explosion;
import sk.epholl.artificialwars.entities.objectives.Objective;
import sk.epholl.artificialwars.entities.robots.Eph32BasicRobot;
import sk.epholl.artificialwars.entities.robots.Robot;
import sk.epholl.artificialwars.entities.robots.RobotTWM1608;
import sk.epholl.artificialwars.logic.GameLogic;
import sk.epholl.artificialwars.logic.GamePanelInput;
import sk.epholl.artificialwars.logic.MainLogic;
import sk.epholl.artificialwars.logic.Vector2D;

public class GamePanel extends JPanel
{
	private static final long serialVersionUID = 5460028729487426583L;

	private GameLogic logic;
	private GamePanelInput input;
	
	private Point mousePointer;
	private Robot selectedRobot;
	
	private final Font monospace = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	private final Map<Integer, RobotDebug> robotDebugs = new HashMap<>();

	public GamePanel()
	{
		this.setFocusable(true);
		MouseAdapter mouseAdapter = createMouseListener();
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
	}

	public void setGameLogic(GameLogic logic, MainLogic mainLogic)
	{
		this.logic = logic;
		input = new GamePanelInput(logic, mainLogic);
		logic.setGamePanel(this);
	}

	@Override
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, 800, 600);

		if (logic != null)
		{
			for (Entity e : logic.getEntities())
			{
				if (e instanceof Objective)
				{
					Objective o = (Objective)e;
					
					if (!o.isPhysical())
					{
						continue;
					}
				}

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

			g2d.drawString("Time: " + logic.getCycleCount(), 20, 537);
			g2d.drawString(logic.getOutputString(), 120, 537);
			
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
						repaint();
					}
				}
			}
			
			@Override
			public void mouseMoved(MouseEvent event)
			{
				mousePointer = new Point(event.getX(), event.getY());
				repaint();
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
