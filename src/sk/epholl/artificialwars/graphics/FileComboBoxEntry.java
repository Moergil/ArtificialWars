package sk.epholl.artificialwars.graphics;

public class FileComboBoxEntry
{
	private final String fileName, name;

	public FileComboBoxEntry(String fileName, String name)
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