package sk.hackcraft.artificialwars.computersim.parts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sk.hackcraft.artificialwars.computersim.Pins;
import sk.hackcraft.artificialwars.computersim.parts.ProcessorTEK1608.RegisterView;

public class ProcessorProbe
{
	private final List<RegisterView> registers;
	private final Map<String, RegisterView> registersMap = new HashMap<>();
	
	private Pins pins = Pins.DUMMY;
	
	public ProcessorProbe(Set<RegisterView> registers)
	{
		this.registers = new ArrayList<>(registers);
		
		for (RegisterView register : registers)
		{
			registersMap.put(register.getName(), register);
		}
	}
	
	public RegisterView getRegister(String name)
	{
		return registersMap.get(name);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		for (RegisterView register : registers)
		{
			String name = register.getName();
			String value = register.getValue();
			builder.append(name).append(": ").append(value).append(" ");
		}
		
		return builder.toString();
	}
}
