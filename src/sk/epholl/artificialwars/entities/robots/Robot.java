package sk.epholl.artificialwars.entities.robots;

import java.io.IOError;
import java.io.IOException;

public interface Robot
{
	int getRobotTypeId();
	
	void setFirmware(byte firmware[]) throws IOException;
}
