package sk.hackcraft.artificialwars.computersim;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import sk.hackcraft.artificialwars.computersim.parts.BusProbe;
import sk.hackcraft.artificialwars.computersim.parts.MemChip1024;
import sk.hackcraft.artificialwars.computersim.parts.ProcessorProbe;
import sk.hackcraft.artificialwars.computersim.parts.ProcessorTEK1608;
import sk.hackcraft.artificialwars.computersim.parts.SegmentDisplay4b8;
import sk.hackcraft.artificialwars.computersim.toolchain.AssemblerTEK1608;
import sk.hackcraft.artificialwars.computersim.toolchain.Preprocessor;



public class Testing
{		
	public static void main(String[] args) throws Exception
	{
		/**
		 * BUS:
		 * 0		WRITE
		 * 1		READ
		 * 2-17		A0-A15
		 * 18-25	D0-D7
		 */
		Bus bus = new Bus(26);
		
		Computer computer = new Computer(bus);
		
		BusProbe probe = new BusProbe(26, (builder, bits) -> {
			for (int i = bits.length - 1; i >= 0; i--)
			{
				if (i == 0)
				{
					builder.append(" W");
				}
				else if (i == 1)
				{
					builder.append(" R");
				}
				else if (i == 17)
				{
					builder.append(" A");
				}
				else if (i == 25)
				{
					builder.append("D");
				}

				builder.append(bits[i] ? "1" : "0");
			}
		});
		
		computer.addPart(probe);
		
		int[] probePinout = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
		bus.connectDevice(probe, probePinout);
		
		ProcessorTEK1608 processor = new ProcessorTEK1608();
		computer.addPart(processor);
		
		processor.setInstructionListener((pc, opcode) -> System.out.println("INS: " + pc + " " + opcode + " " + processor.getInstructionSet().getName(opcode)));
		
		int processorPinout[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
		bus.connectDevice(processor, processorPinout);

		ProcessorProbe processorProbe = new ProcessorProbe(processor.getRegisterViews());

		Preprocessor preprocessor = new Preprocessor(";");
		AssemblerTEK1608 assembler = new AssemblerTEK1608();
		
		byte assembly[] = Files.readAllBytes(new File("test.asm").toPath());
		byte preprocessedAssembly[] = preprocessor.process(assembly);
		byte objectCode[] = assembler.process(preprocessedAssembly);
		
		// memory chip 1
		MemChip1024 memory1 = new MemChip1024();
		computer.addPart(memory1);
		bus.addCircuit((b) -> !b.readBusPin(12) && !b.readBusPin(13), 26);
		
		int[] memory1Pinout = {
				1, 0, 26, // read, write, chip select
				2, 3, 4, 5, 6, 7, 8, 9 , 10, 11, // 10bit address
				18, 19, 20, 21, 22, 23, 24, 25 // 8bit data
		};
		
		bus.connectDevice(memory1, memory1Pinout);
		
		byte[] memoryArray = memory1.getMemory();
		
		System.arraycopy(objectCode, 0, memoryArray, 0x0a, objectCode.length);
		
		// memory chip 2
		MemChip1024 memory2 = new MemChip1024();
		computer.addPart(memory2);
		bus.addCircuit((b) -> b.readBusPin(12) && !b.readBusPin(13), 27);
		
		int[] memory2Pinout = {
				1, 0, 27, // read, write, chip select
				2, 3, 4, 5, 6, 7, 8, 9 , 10, 11, // 10bit address
				18, 19, 20, 21, 22, 23, 24, 25 // 8bit data
		};
		
		bus.connectDevice(memory2, memory2Pinout);

		processorProbe.getRegister("PC").setValue("10");
		
		for (int i = 0; i < 1000; i++)
		{
			computer.tick();
			System.out.println("Cycle " + i);
			System.out.println(processorProbe);
			printMemory(memory1.getMemory(), 32);
			System.out.println();
		}
	}
	
	private static void printMemory(byte data[], int count)
	{
		for (int i = 0; i < count; i++)
		{
			System.out.print((int)(data[i] & 0xff) + " ");
		}
		System.out.println();
	}
}
