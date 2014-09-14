package sk.epholl.artificialwars.graphics;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * @author epholl
 */
public class MenuButton implements Button
{
	protected int leftBorder;
	protected int rightBorder;
	protected int upperBorder;
	protected int lowerBorder;

	protected String buttonText;
	
	private Runnable activationListener;

	public MenuButton(int left, int right, int up, int down, String text)
	{
		this.leftBorder = left;
		this.rightBorder = right;
		this.upperBorder = up;
		this.lowerBorder = down;
		this.buttonText = text;
	}
	
	@Override
	public void setActivationListener(Runnable r)
	{
		activationListener = r;
	}

	public boolean checkClicked(int x, int y)
	{
		if (x >= leftBorder && x <= rightBorder && y >= upperBorder && y <= lowerBorder)
			return true;
		return false;
	}

	public void action()
	{
		activationListener.run();
	}

	public void paint(Graphics2D g2d)
	{
		g2d.setColor(new Color(200, 150, 150));
		g2d.fillRect(getLeftBorder(), getUpperBorder(), getSizeX(), getSizeY());

		g2d.setColor(Color.yellow);
		g2d.drawString(getButtonText(), getLeftBorder() + 10, getUpperBorder() + 20);
	}

	public String getButtonText()
	{
		return buttonText;
	}

	public int getLeftBorder()
	{
		return leftBorder;
	}

	public int getLowerBorder()
	{
		return lowerBorder;
	}

	public int getRightBorder()
	{
		return rightBorder;
	}

	public int getUpperBorder()
	{
		return upperBorder;
	}

	public int getSizeX()
	{
		return rightBorder - leftBorder;
	}

	public int getSizeY()
	{
		return lowerBorder - upperBorder;
	}
}
