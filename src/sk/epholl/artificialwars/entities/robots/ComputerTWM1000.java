package sk.epholl.artificialwars.entities.robots;

import sk.hackcraft.artificialwars.computersim.Bus;
import sk.hackcraft.artificialwars.computersim.Computer;
import sk.hackcraft.artificialwars.computersim.TEK1608InstructionSet;
import sk.hackcraft.artificialwars.computersim.parts.BusProbe;
import sk.hackcraft.artificialwars.computersim.parts.MEXTIOChip;
import sk.hackcraft.artificialwars.computersim.parts.MemChip1024;
import sk.hackcraft.artificialwars.computersim.parts.MemoryProbe;
import sk.hackcraft.artificialwars.computersim.parts.ProbeMEXTIOChip;
import sk.hackcraft.artificialwars.computersim.parts.ProbeProcessorTEK1608;
import sk.hackcraft.artificialwars.computersim.parts.ProbeProcessorTEK1608.RegisterTEK1608;
import sk.hackcraft.artificialwars.computersim.parts.ProcessorTEK1608;
import sk.hackcraft.artificialwars.computersim.parts.Serial8SegmentDisplay;

public class ComputerTWM1000 extends Computer
{
	public static final int
		DATA_RANGE = 8,
		ADDRESS_RANGE = 16,
		CONTROL_RANGE = 1,
		CHIP_SELECT_RANGE = 3;
	
	public static final int busPinsCount = DATA_RANGE + ADDRESS_RANGE + CONTROL_RANGE;
	public static final int allPinsCount = busPinsCount + CHIP_SELECT_RANGE;
	
	private final ProcessorTEK1608 processor;
	private final MemChip1024 memory;
	private final MEXTIOChip io;
	private final Serial8SegmentDisplay display;
	
	private final ProbeProcessorTEK1608 processorProbe;
	private final BusProbe busProbe;
	private final MemoryProbe memoryProbe;
	private final ProbeMEXTIOChip ioProbe;

	public ComputerTWM1000()
	{
		super(busPinsCount);
		
		Bus bus = getBus();
	
		int
		D0 = 0,
		D1 = 1,
		D2 = 2,
		D3 = 3,
		D4 = 4,
		D5 = 5,
		D6 = 6,
		D7 = 7,
		A0 = 8,
		A1 = 9,
		A2 = 10,
		A3 = 11,
		A4 = 12,
		A5 = 13,
		A6 = 14,
		A7 = 15,
		A8 = 16,
		A9 = 17,
		A10 = 18,
		A11 = 19,
		A12 = 20,
		A13 = 21,
		A14 = 22,
		A15 = 23,
		RW = 24,
		CS0 = 25,
		CS1 = 26,
		CS2 = 27;

		bus.addCircuit((b) -> !b.readBusPin(A10), CS0);
		bus.addCircuit((b) -> b.readBusPin(A10) && !b.readBusPin(A4), CS1);
		bus.addCircuit((b) -> b.readBusPin(A10) && b.readBusPin(A4), CS2);
		
		busProbe = new BusProbe(allPinsCount, (builder, bits) -> {
			for (int i = bits.length - 1; i >= 0; i--)
			{
				if (i == D7)
				{
					builder.append(" D");
				}
				else if (i == A15)
				{
					builder.append(" A");
				}
				else if (i == RW)
				{
					builder.append(" RW");
				}
				else if (i == CS2)
				{
					builder.append(" CS");
				}
		
				builder.append(bits[i] ? "1" : "0");
			}
		});
		
		// data(0-7), address(0-15), readwrite, chipSelect(0-2)
		bus.connectDevice(busProbe, new int[]{D0, D1, D2, D3, D4, D5, D6, D7, A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, RW, CS0, CS1, CS2});
		
		processor = new ProcessorTEK1608();		
		// readwrite, address(0-15), data(0-7)
		bus.connectDevice(processor, new int[]{RW, A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, D0, D1, D2, D3, D4, D5, D6, D7});
		
		// TODO debug
		//processor.setInstructionListener((pc, opcode) -> System.out.printf("INS: %d %d %s%n", pc, opcode, TEK1608InstructionSet.getInstance().getOpcode(opcode).getInstructionName()));
		
		processorProbe = new ProbeProcessorTEK1608(processor);
		
		memory = new MemChip1024();
		// readwrite, chipSelect, address(0-9), data(0-7)
		bus.connectDevice(memory, new int[]{RW, CS0, A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, D0, D1, D2, D3, D4, D5, D6, D7});
		
		memoryProbe = new MemoryProbe(memory);
		
		io = new MEXTIOChip();
		// data(0-7), address(8-11), readwrite, chipSelect
		bus.connectDevice(io, new int[]{D0, D1, D2, D3, D4, D5, D6, D7, A0, A1, A2, A3, RW, CS1});
		
		ioProbe = new ProbeMEXTIOChip(io);
		
		display = new Serial8SegmentDisplay(8);
		// readwrite, address, data(0-7), chipSelect
		bus.connectDevice(display, new int[]{RW, D0, D1, D2, D3, D4, D5, D6, D7, CS2});

		addPart(processor);
		addPart(memory);
		addPart(io);
		
		addPart(display);
		addPart(busProbe);		
	}
	
	public void loadFirmware(short pc, byte firmware[])
	{
		processorProbe.setValue(RegisterTEK1608.PC, pc);
		
		byte memoryData[] = memory.getMemory();
		
		System.arraycopy(firmware, 0, memoryData, 0, firmware.length);
	}
	
	public MEXTIOChip getIO()
	{
		return io;
	}
	
	public Serial8SegmentDisplay getDisplay()
	{
		return display;
	}
	
	public ProbeProcessorTEK1608 getProcessorProbe()
	{
		return processorProbe;
	}
	
	public BusProbe getBusProbe()
	{
		return busProbe;
	}
	
	public MemoryProbe getMemoryProbe()
	{
		return memoryProbe;
	}
	
	public ProbeMEXTIOChip getIoProbe()
	{
		return ioProbe;
	}
}
