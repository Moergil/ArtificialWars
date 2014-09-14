package sk.hackcraft.artificialwars.computersim.debugapp;

import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JFrame;

// TODO
public class DebugApp implements Runnable
{
	public static void main(String[] args)
	{
		new DebugApp().run();
	}
	
	private JFrame window = new JFrame("Debug");
	
	public DebugApp()
	{
		Container contentPane = window.getContentPane();
		
		contentPane.setLayout(null);
		
		JButton tickButton = new JButton();
	}
	
	@Override
	public void run()
	{
		window.setVisible(true);
	}
}
