package sk.epholl.artificialwars.graphics;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sk.epholl.artificialwars.util.FileName;

public class CampaignsPanel extends JPanel
{
	private final JComboBox<FileComboBoxEntry> campaignSelect;
	private final JComboBox<FileComboBoxEntry> levelSelect;
	
	private final DefaultComboBoxModel<FileComboBoxEntry> campaignSelectListModel;
	private final DefaultComboBoxModel<FileComboBoxEntry> levelFileSelectListModel;
	
	private JButton backButton, startButton;
	
	private Map<String, FileComboBoxEntry> entries = new HashMap<>();
	
	public CampaignsPanel()
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
		
		JLabel campaignLabel = new JLabel("Campaign:");
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		innerPanel.add(campaignLabel, c);

		campaignSelectListModel = new DefaultComboBoxModel<>();
		campaignSelect = new JComboBox<>(campaignSelectListModel);
		campaignSelect.setEditable(false);
		campaignSelect.addItemListener((e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				updateLevelsSelection();
			}
		});
		
		c.gridx++;
		innerPanel.add(campaignSelect, c);
		
		JLabel levelNameLabel = new JLabel("Level file:"); 

		c.gridx = 0;
		c.gridy++;
		innerPanel.add(levelNameLabel, c);
		
		levelFileSelectListModel = new DefaultComboBoxModel<>();
		levelSelect = new JComboBox<>(levelFileSelectListModel);
		levelSelect.setEditable(false);

		c.gridx++;
		innerPanel.add(levelSelect, c);
		
		backButton = new JButton("Back");
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		innerPanel.add(backButton, c);
		
		startButton = new JButton("Start");
		
		c.gridx++;
		innerPanel.add(startButton, c);
	}
	
	public void loadAvailableCampaignFiles()
	{
		File campaignsDirectories[] = new File("./levels/campaigns").listFiles();
		
		if (campaignsDirectories == null)
		{
			return;
		}
		
		for (File dir : campaignsDirectories)
		{
			if (!dir.isDirectory())
			{
				continue;
			}
			
			String campaignName = dir.getName();
			
			FileComboBoxEntry entry = new FileComboBoxEntry(campaignName, campaignName);
			
			campaignSelectListModel.addElement(entry);
			
			entries.put(campaignName, entry);
		}
	}
	
	private void updateLevelsSelection()
	{
		levelFileSelectListModel.removeAllElements();

		String campaign = campaignSelect.getItemAt(campaignSelect.getSelectedIndex()).getFileName();
		File campaignDirectory = new File("./levels/campaigns/" + campaign);
		
		File campaignFiles[] = campaignDirectory.listFiles();
		
		if (campaignFiles == null)
		{
			return;
		}
		
		for (File file : campaignFiles)
		{
			String fileName = file.getName();
			
			if (FileName.isLevelFile(fileName))
			{
				String name = FileName.removeExtension(fileName);
				FileComboBoxEntry entry = new FileComboBoxEntry(fileName, name);
				levelFileSelectListModel.addElement(entry);
			}
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

	public String getLevelPath()
	{
		String campaign = ((FileComboBoxEntry)campaignSelect.getSelectedItem()).getFileName();
		String level = ((FileComboBoxEntry)levelSelect.getSelectedItem()).getFileName();
		
		return "campaigns/" + campaign + "/" + level;
	}
}