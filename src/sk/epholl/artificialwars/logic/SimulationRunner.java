package sk.epholl.artificialwars.logic;

public interface SimulationRunner 
{
	void restart() throws Exception;
	void dispose();

	void pause();
	void run();
	
	void step();
	
	void setSpeed(double speed);
	void setAutoStart(boolean autoStart);
	void setAutoRestart(boolean autoRestart);
	
	Simulation getSimulation();
}
