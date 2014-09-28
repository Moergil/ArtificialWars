package sk.epholl.artificialwars.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JPanel;

import sk.epholl.artificialwars.entities.instructionsets.EPH32InstructionSet;
import sk.epholl.artificialwars.logic.MainLogic;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.Instruction;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.MemoryAddressing;

public class MenuPanel extends JPanel
{
	private static final long serialVersionUID = 3449302831153620662L;

	private final ArrayList<MenuButton> buttons = new ArrayList<MenuButton>();

	public MenuPanel(MainLogic mainLogic)
	{
		int left = 60;
		int right = 140;
		int y = 100;
		int height = 30;
		int spacing = 50;
		for (int i = 0; i < 5; i++)
		{
			int levelIndex = i + 1;
			String text = "Level " + levelIndex;
			
			int up = y;
			int down = up + height;

			MenuButton button = new MenuButton(left, right, up, down, text);
			button.setActivationListener(() -> mainLogic.loadLevel(text + ".txt"));
			buttons.add(button);
			
			y = down + spacing;
		}
		
		MenuButton exitButton = new MenuButton(60, 140, 500, 530, "Exit");
		exitButton.setActivationListener(() -> System.exit(0));
		buttons.add(exitButton);

		this.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent event)
			{
				if (event.getButton() == MouseEvent.BUTTON1)
					checkMouseClicked(event);
			}
		});
	}

	private void checkMouseClicked(MouseEvent event)
	{
		int x = event.getX();
		int y = event.getY();

		for (MenuButton button : buttons)
		{
			if (button.checkClicked(x, y))
			{
				button.action();
				return;
			}
		}
	}

	@Override
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, 800, 600);

		g2d.setColor(new Color(200, 150, 150));
		g2d.fillRect(300, 10, 200, 50);

		g2d.setColor(Color.yellow);
		g2d.drawString("Artificial Wars", 360, 40);

		for (MenuButton button : buttons)
		{
			button.paint(g2d);
		}

		paintInstructions(g2d);

		Toolkit.getDefaultToolkit().sync();
		g.dispose();
	}

	public void paintInstructions(Graphics2D g2d)
	{
		g2d.setColor(Color.black);

		Set<Instruction> instructions = EPH32InstructionSet.getInstance().getAllInstructions();

		g2d.drawString("Use file player.txt to program your robot", 400, 240);
		g2d.drawString("You can use // for comments and each instruction", 400, 260);
		g2d.drawString("must have exactly one parameter.", 400, 280);

		int i = 0, j = 0;
		for (Instruction instruction : instructions)
		{
			MemoryAddressing ma = instruction.getMemoryAddressings().iterator().next();
			String param = ma.getOperandsWordsSize() != 0 ? "<int32>" : "";
			String text = instruction.getName() + " " + param;
			
			g2d.drawString(text, 400 + j, 300 + 20 * (i % 10));
			if (((i + 1) % 10) == 0)
			{
				j += 100;
			}
			
			i++;
		}
	}
}
