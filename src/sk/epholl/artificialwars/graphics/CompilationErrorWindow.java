package sk.epholl.artificialwars.graphics;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import sk.hackcraft.artificialwars.computersim.toolchain.CodeProcessor.ProgramException;

public class CompilationErrorWindow
{
	public void show(ProgramException exception)
	{
		JFrame frame = new JFrame("Compilation error");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setMinimumSize(new Dimension(300, 200));
		
		Container contentPane = frame.getContentPane();
		
		JTextArea textarea = new JTextArea();
		textarea.setText(exception.getMessage());
		textarea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		contentPane.add(textarea);
		
		frame.pack();
		
		frame.setVisible(true);
	}
}
