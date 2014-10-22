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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataListener;

import sk.epholl.artificialwars.logic.RobotFactory;
import sk.epholl.artificialwars.logic.RobotCreator.AbstractRobot;
import sk.epholl.artificialwars.util.FileName;

public class ArenaPropertiesPanel extends JPanel
{
	private static final String CONFIG_FILE_NAME = "arena_config.cfg";

	private static final String
		PLAYER_1 = "player_1",
		PLAYER_2 = "player_2",
		LEVEL = "level";

	private final JComboBox<ComboBoxEntry> robot1FileSelect, robot2FileSelect;
	private final JComboBox<ComboBoxEntry> levelFileSelect;
	
	private final DefaultComboBoxModel<ComboBoxEntry> robot1FileSelectListModel, robot2FileSelectListModel;
	private final DefaultComboBoxModel<ComboBoxEntry> levelFileSelectListModel;
	
	private JButton backButton, startButton;
	
	private final File propertiesFile;
	
	private Map<String, ComboBoxEntry> entries = new HashMap<>();
	
	public ArenaPropertiesPanel()
	{
		setLayout(new GridBagLayout());
		
		JPanel innerPanel = new JPanel();
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

		robot1FileSelectListModel = new DefaultComboBoxModel<>();
		robot1FileSelect = new JComboBox<>(robot1FileSelectListModel);
		robot1FileSelect.setEditable(false);
		
		c.gridx++;
		innerPanel.add(robot1FileSelect, c);
		
		JLabel robot2FileNameLabel = new JLabel("Robot 2 file:");
		
		c.gridx = 0;
		c.gridy++;
		innerPanel.add(robot2FileNameLabel, c);
		
		robot2FileSelectListModel = new DefaultComboBoxModel<>();
		robot2FileSelect = new JComboBox<>(robot2FileSelectListModel);
		robot2FileSelect.setEditable(false);
		
		c.gridx++;
		innerPanel.add(robot2FileSelect, c);
		
		JLabel levelFileNameLabel = new JLabel("Level file:"); 

		c.gridx = 0;
		c.gridy++;
		innerPanel.add(levelFileNameLabel, c);
		
		levelFileSelectListModel = new DefaultComboBoxModel<>();
		levelFileSelect = new JComboBox<>(levelFileSelectListModel);
		levelFileSelect.setEditable(false);

		c.gridx++;
		innerPanel.add(levelFileSelect, c);
		
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
	
	public void loadAvailableFiles()
	{
		File[] robotFiles = new File("./robots").listFiles();
		
		if (robotFiles == null)
		{
			return;
		}
		
		for (File file : robotFiles)
		{
			if (!file.isFile())
			{
				continue;
			}
			
			String fileName = file.getName();

			if (!FileName.isRobotFile(fileName))
			{
				continue;
			}

			try
			{
				String robotName = getRobotName(fileName);
				
				ComboBoxEntry entry = new ComboBoxEntry(fileName, robotName);
				robot1FileSelectListModel.addElement(entry);
				robot2FileSelectListModel.addElement(entry);
				
				entries.put(robotName, entry);
			}
			catch (IOException e)
			{
				System.err.println("Can't parse robot file " + fileName);
			}
		}
		
		File arenaLevelFiles[] = new File(".").listFiles();
		
		if (arenaLevelFiles == null)
		{
			return;
		}
		
		for (File file : arenaLevelFiles)
		{
			if (!file.isFile())
			{
				continue;
			}
			
			String fileName = file.getName();
			
			if (!FileName.isLevelFile(fileName) || !FileName.isLevelType(fileName, "dm"))
			{
				continue;
			}
			
			String levelName = getLevelName(file.getName());
			
			ComboBoxEntry entry = new ComboBoxEntry(fileName, levelName);
			levelFileSelectListModel.addElement(entry);
			
			entries.put(levelName, entry);
		}
	}
	
	private String getRobotName(String fileName) throws IOException
	{
		AbstractRobot abstractRobot = RobotFactory.loadAbstractRobot(fileName);
		
		return abstractRobot.getName();
	}
	
	private String getLevelName(String fileName)
	{
		return FileName.removeExtension(fileName);
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
			robot1FileSelect.setSelectedItem(getComboBoxEntry(player1));
			
			String player2 = properties.getProperty(PLAYER_2);
			robot2FileSelect.setSelectedItem(getComboBoxEntry(player2));
			
			String levelName = properties.getProperty(LEVEL);
			levelFileSelect.setSelectedItem(getComboBoxEntry(levelName));
		}
	}
	
	private ComboBoxEntry getComboBoxEntry(String key)
	{
		return entries.get(key);
	}
	
	public void saveDefaultValues() throws IOException
	{
		Properties properties = new Properties();
		
		try (FileOutputStream output = new FileOutputStream(propertiesFile))
		{
			properties.setProperty(PLAYER_1, getRobot(1).getName());
			properties.setProperty(PLAYER_2, getRobot(2).getName());
			properties.setProperty(LEVEL, getLevel().getName());
			
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

	public ComboBoxEntry getLevel()
	{
		return (ComboBoxEntry)levelFileSelect.getSelectedItem();
	}

	public ComboBoxEntry getRobot(int i)
	{
		ComboBoxEntry entry;
		
		switch (i)
		{
			case 1:
				entry = (ComboBoxEntry)robot1FileSelect.getSelectedItem();
				break;
			case 2:
				entry = (ComboBoxEntry)robot2FileSelect.getSelectedItem();
				break;
			default:
				throw new IllegalArgumentException("Robot index is out of range: " + i);
		}
		
		return entry;
	}
	
	public static class ComboBoxEntry
	{
		private final String fileName, name;

		public ComboBoxEntry(String fileName, String name)
		{
			this.fileName = fileName;
			this.name = name;
		}
		
		public String getFileName()
		{
			return fileName;
		}
		
		public String getName()
		{
			return name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
