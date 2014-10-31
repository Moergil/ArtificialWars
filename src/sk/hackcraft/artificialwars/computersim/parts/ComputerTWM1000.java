package sk.hackcraft.artificialwars.computersim.parts;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import sk.hackcraft.artificialwars.computersim.Bus;
import sk.hackcraft.artificialwars.computersim.Computer;
import sk.hackcraft.artificialwars.computersim.parts.ProbeProcessorTEK1608.RegisterTEK1608;

// TODO remap IO from $0400 to $4000
// TODO add another memchip as 1kB is starting to be not sufficient enough
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
	private final TextLineDisplay display;
	
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
		RES = 25,
		NMI = 26,
		IRQ = 27,
		MCS0 = 28,
		MCS1 = 29,
		IOCS0 = 30,
		IOCS1 = 31;

		bus.addCircuit((b) -> !b.readBusPin(A10) || b.readBusPin(A15), MCS0);
		bus.addCircuit((b) -> b.readBusPin(A15), MCS1);
		bus.addCircuit((b) -> b.readBusPin(A10) && !b.readBusPin(A15) && !b.readBusPin(A4), IOCS0);
		bus.addCircuit((b) -> b.readBusPin(A10) && !b.readBusPin(A15) && b.readBusPin(A4), IOCS1);
		
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
				else if (i == RES)
				{
					builder.append("I");
				}
				else if (i == MCS0)
				{
					builder.append("CS");
				}
		
				builder.append(bits[i] ? "1" : "0");
			}
		});
		
		// data(0-7), address(0-15), readwrite, chipSelect(0-2)
		bus.connectDevice(busProbe, new int[]{D0, D1, D2, D3, D4, D5, D6, D7, A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, RW, RES, NMI, IRQ, MCS0, MCS1, IOCS0, IOCS1});
		
		processor = new ProcessorTEK1608();		
		// readwrite, address(0-15), data(0-7)
		bus.connectDevice(processor, new int[]{RW, A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, D0, D1, D2, D3, D4, D5, D6, D7, RES, NMI, IRQ});
		
		processorProbe = new ProbeProcessorTEK1608(processor);
		
		memory = new MemChip1024();
		// readwrite, chipSelect, address(0-9), data(0-7)
		bus.connectDevice(memory, new int[]{RW, MCS0, A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, D0, D1, D2, D3, D4, D5, D6, D7});
		
		memoryProbe = new MemoryProbe(memory);
		
		io = new MEXTIOChip();
		// data(0-7), address(8-11), readwrite, chipSelect
		bus.connectDevice(io, new int[]{D0, D1, D2, D3, D4, D5, D6, D7, A0, A1, A2, A3, RW, IOCS0});
		
		ioProbe = new ProbeMEXTIOChip(io);
		
		display = new TextLineDisplay(8);
		// readwrite, address, data(0-7), chipSelect
		bus.connectDevice(display, new int[]{RW, D0, D1, D2, D3, D4, D5, D6, D7, IOCS1});

		addPart(processor);
		addPart(memory);
		addPart(io);
		
		addPart(display);
		addPart(busProbe);		
	}
	
	@Deprecated
	// use some kind of persisent storage, ROM or such
	public void loadFirmware(byte firmware[]) throws IOException
	{
		ByteArrayInputStream input = new ByteArrayInputStream(firmware);
		DataInputStream dataInput = new DataInputStream(input);
		
		byte memoryData[] = memory.getMemory();
		
		// setting address for interrupt subroutines
		int offset = 0x03FA;
		byte interruptAddress[] = new byte[2];
		
		for (int i = 0; i < 3; i++)
		{
			dataInput.readFully(interruptAddress);
			
			System.arraycopy(interruptAddress, 0, memoryData, offset, interruptAddress.length);
			offset += 2;
		}
		
		// loading segments
		int start, length;
		byte data[];
		
		int segmentsCount = dataInput.readUnsignedByte();
		for (int i = 0; i < segmentsCount; i++)
		{
			// loading program
			start = dataInput.readInt();
			length = dataInput.readInt();
			
			data = new byte[length];
			dataInput.readFully(data, 0, length);
			
			System.arraycopy(data, 0, memoryData, start, data.length);
		}
	}
	
	public byte getMemoryValue(int address)
	{
		if (address < 0 || address >= memory.getMemory().length)
		{
			return 0;
		}

		return memory.getMemory()[address];
	}
	
	public MEXTIOChip getIO()
	{
		return io;
	}
	
	public TextLineDisplay getDisplay()
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
