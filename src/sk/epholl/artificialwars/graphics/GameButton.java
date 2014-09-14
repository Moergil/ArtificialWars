package sk.epholl.artificialwars.graphics;

import java.awt.Color;
import java.awt.Graphics2D;

public class GameButton extends MenuButton
{
	public GameButton(int left, int right, int up, int down, String text)
	{
		super(left, right, up, down, text);
	}

	@Override
	public void paint(Graphics2D g2d)
	{
		g2d.setColor(new Color(170, 130, 130));
		g2d.fillRect(getLeftBorder(), getUpperBorder(), getSizeX(), getSizeY());

		g2d.setColor(Color.yellow);
		g2d.drawString(getButtonText(), getLeftBorder() + 10, getUpperBorder() + 14);
	}
}
