package sk.epholl.artificialwars.logic;

public interface SimulationRunner 
{
	void restart();
	void dispose();

	void pause();
	void run(long millisDelay);
	
	void step();
	
	Simulation getSimulation();
}
