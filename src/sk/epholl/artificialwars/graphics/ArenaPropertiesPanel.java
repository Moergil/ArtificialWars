package sk.epholl.artificialwars.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ArenaPropertiesPanel extends JPanel
{
	private static final String CONFIG_FILE_NAME = "arena_config.cfg";

	private static final String
		PLAYER_1 = "player_1",
		PLAYER_2 = "player_2",
		LEVEL = "level";

	private final JTextField robot1FileNameField, robot2FileNameField;
	private final JTextField levelFileNameField;
	
	private JButton backButton, startButton;
	
	private final File propertiesFile;
	
	public ArenaPropertiesPanel()
	{
		setLayout(new GridBagLayout());
		
		JPanel innerPanel = new JPanel();
		innerPanel.setPreferredSize(new Dimension(400, 200));
		innerPanel.setLayout(new GridBagLayout());
		
		add(innerPanel, new GridBagConstraints());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.ipadx = c.ipady = 5;
		c.insets = new Insets(5, 5, 5, 5);
		
		JLabel robot1FileLabel = new JLabel("Robot 1 file:");
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		innerPanel.add(robot1FileLabel, c);

		robot1FileNameField = new JTextField();
		
		c.gridx++;
		innerPanel.add(robot1FileNameField, c);
		
		JLabel robot2FileNameLabel = new JLabel("Robot 2 file:");
		
		c.gridx = 0;
		c.gridy++;
		innerPanel.add(robot2FileNameLabel, c);
		
		robot2FileNameField = new JTextField();
		
		c.gridx++;
		innerPanel.add(robot2FileNameField, c);
		
		JLabel levelFileNameLabel = new JLabel("Level file:"); 

		c.gridx = 0;
		c.gridy++;
		innerPanel.add(levelFileNameLabel, c);
		
		levelFileNameField = new JTextField();

		c.gridx++;
		innerPanel.add(levelFileNameField, c);
		
		backButton = new JButton("Back");
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		innerPanel.add(backButton, c);
		
		startButton = new JButton("Start");
		
		c.gridx++;
		innerPanel.add(startButton, c);
		
		propertiesFile = new File(CONFIG_FILE_NAME);
	}
	
	public void loadDefaultValues() throws IOException
	{
		if (!propertiesFile.exists())
		{
			return;
		}
		
		Properties properties = new Properties();
		
		try (FileInputStream input = new FileInputStream(propertiesFile))
		{
			properties.load(input);
			
			String player1 = properties.getProperty(PLAYER_1);
			robot1FileNameField.setText(player1);
			
			String player2 = properties.getProperty(PLAYER_2);
			robot2FileNameField.setText(player2);
			
			String levelName = properties.getProperty(LEVEL);
			levelFileNameField.setText(levelName);
		}
	}
	
	public void saveDefaultValues() throws IOException
	{
		Properties properties = new Properties();
		
		try (FileOutputStream output = new FileOutputStream(propertiesFile))
		{
			properties.setProperty(PLAYER_1, robot1FileNameField.getText());
			properties.setProperty(PLAYER_2, robot2FileNameField.getText());
			properties.setProperty(LEVEL, levelFileNameField.getText());
			
			properties.store(output, "Arena Default Values");
		}
	}
	
	public void setBackListener(ActionListener backListener)
	{
		backButton.addActionListener(backListener);
	}
	
	public void setStartListener(ActionListener startListener)
	{
		startButton.addActionListener(startListener);
	}

	public String getLevelName()
	{
		return levelFileNameField.getText();
	}

	public String getRobotName(int i)
	{
		switch (i)
		{
			case 1:
				return robot1FileNameField.getText();
			case 2:
				return robot2FileNameField.getText();
			default:
				throw new IllegalArgumentException("Robot index is out of range: " + i);
		}
	}
}
