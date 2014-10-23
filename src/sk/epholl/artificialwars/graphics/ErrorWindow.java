package sk.epholl.artificialwars.graphics;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class ErrorWindow
{
	private final String title, content;

	public ErrorWindow(String title, String content)
	{
		this.title = title;
		this.content = content;
	}

	public void show()
	{
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setMinimumSize(new Dimension(300, 200));
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				dismissed();
			}
		});
		
		Container contentPane = frame.getContentPane();
		
		JTextArea textarea = new JTextArea();
		textarea.setText(content);
		textarea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		contentPane.add(textarea);
		
		frame.pack();
		
		frame.setVisible(true);
	}
	
	protected void dismissed()
	{
	}
}
