package sk.epholl.artificialwars.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.logic.GameLogic;
import sk.epholl.artificialwars.logic.GamePanelInput;
import sk.epholl.artificialwars.logic.MainLogic;

public class GamePanel extends JPanel
{
	private static final long serialVersionUID = 5460028729487426583L;

	private GameLogic logic;
	private GamePanelInput input;

	public GamePanel()
	{
		this.setFocusable(true);
		this.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent event)
			{
				if (event.getButton() == MouseEvent.BUTTON1 && input != null)
					input.mouseClicked(event.getX(), event.getY());
			}
		});
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
				g2d.setColor(e.getColor());
				g2d.fillRect(e.getLeftmostPosX(), e.getDownmostPosY(), e.getSizeX(), e.getSizeY());
			}

			g2d.setColor(Color.yellow);

			g2d.drawString("Time: " + logic.getCycleCount(), 20, 537);
			g2d.drawString(logic.getOutputString(), 120, 537);

			for (GameButton button : input.getButtons())
			{
				button.paint(g2d);
			}
		}

		Toolkit.getDefaultToolkit().sync();
		g.dispose();
	}
}
