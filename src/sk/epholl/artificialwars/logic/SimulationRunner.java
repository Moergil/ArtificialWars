package sk.epholl.artificialwars.logic;

public interface SimulationRunner 
{
	void restart() throws Exception;
	void dispose();

	void pause();
	void run(long millisDelay);
	
	void step();
	
	Simulation getSimulation();
}
