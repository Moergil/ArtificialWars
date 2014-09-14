package sk.hackcraft.artificialwars.computersim.parts;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sk.hackcraft.artificialwars.computersim.Device;
import sk.hackcraft.artificialwars.computersim.PinUtil;
import sk.hackcraft.artificialwars.computersim.Pins;
import sk.hackcraft.artificialwars.computersim.Util;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.InstructionCompiler;


/**
 * <pre>
 * WRITE     0
 * READ    1
 * ADDRESS  2-17
 * DATA     18-25
 * </pre>
 * 
 * LittleEndian (16bit = low byte, high byte)
 * 
 * TODO implement interrupt and bcd, or decide to throw them away
 * TODO position instructions properly in instruction table
 * TODO test everything!
 */
public class ProcessorTEK1608 implements Device
{
	private static final int
		WRITE_PIN = 0,
		READ_PIN = 1,
		ADDRESS_PINS_START = 2,
		ADDRESS_PINS_COUNT = 16,
		DATA_PINS_START = ADDRESS_PINS_START + ADDRESS_PINS_COUNT,
		DATA_PINS_COUNT = 8;
	
	private byte a, x, y, sp = (byte)0xff, sr;
	private short pc;
	private byte ir;

	private int address;
	
	private interface Reg
	{
		static final int
			A = 1,
			X = 2,
			Y = 4,
			SP = 8,
			PC = 16,
			SR = 32;
	}
	
	private interface Flag
	{
		static final int
			CARRY = 1,
			ZERO = 2,
			INTERRUPT = 4,
			DECIMAL= 8,
			BREAK = 16,
			// 32 is ignored
			OVERFLOW = 64,
			NEGATIVE = 128;
	}
	
	public interface RegisterView
	{
		int getBytesSize();
		String getValue();
		void setValue(String newValue);
		String getName();
		
		@FunctionalInterface
		public interface Setter
		{
			void set(int value);
		}
		
		@FunctionalInterface
		public interface Getter
		{
			int get();
		}
	}
	
	@FunctionalInterface
	public interface InstructionListener
	{
		void instructionLoaded(int pc, int opcode);
	}
	
	private static class RegisterViewControl implements RegisterView
	{
		private static final Map<Integer, String> zeroPatterns = new HashMap<>();
		
		static
		{
			zeroPatterns.put(1, "00");
			zeroPatterns.put(2, "0000");
		}
		
		private final String name;
		private final int bytesSize;
		private final String zeroPattern;
		
		private final Setter setter;
		private final Getter getter;
		
		public RegisterViewControl(String name, int bytesSize, Setter setter, Getter getter)
		{
			this.name = name;
			this.bytesSize = bytesSize;
			
			this.setter = setter;
			this.getter = getter;
			
			zeroPattern = zeroPatterns.get(bytesSize);
		}
		
		@Override
		public String getName()
		{
			return name;
		}
		
		@Override
		public int getBytesSize()
		{
			return bytesSize;
		}
		
		@Override
		public String getValue()
		{
			String rawValue = Integer.toHexString(getter.get());
			
			return "0x" + (zeroPattern + rawValue).substring(rawValue.length()).toUpperCase();
		}
		
		@Override
		public void setValue(String newValue)
		{
			setter.set(Integer.decode(newValue));
		}
	}
	
	private Map<MemoryAddressing, Runnable[]> memoryAddressingSetups = new EnumMap<>(MemoryAddressing.class);
	
	private Pins pins = Pins.DUMMY;
	
	private final boolean[]
			addressBits = new boolean[ADDRESS_PINS_COUNT],
			dataBits = new boolean[DATA_PINS_COUNT];

	private final InstructionSet instructionSet;
	private final Set<RegisterView> registers = new HashSet<>();
	
	private final Operation[] operations = new Operation[256];
	private final Operation loadInstruction = new LoadInstruction();
	
	private Operation currentOperation;
	
	private InstructionListener instructionListener;
	
	public ProcessorTEK1608()
	{
		instructionSet = new InstructionSet();
		
		prepareRegisters();
		
		setInitialOperandLoader(MemoryAddressing.ACCUMULATOR, new Runnable[]{});
		setInitialOperandLoader(MemoryAddressing.ABSOLUTE, new Runnable[]{
			() -> {
				address = readDataBus();
				setAddressBus(pc - 1);
				setReadBus(true);
			},
			() -> setReadBus(false),
			() -> 
			{
				address += readDataBus() << 8;
				setAddressBus(address);
				setReadBus(true);
			},
			() -> setReadBus(false)
		});
		setInitialOperandLoader(MemoryAddressing.ABSOLUTE_INDEXED_X, new Runnable[]{
			() -> {
				address = readDataBus();
				setAddressBus(pc - 1);
				setReadBus(true);
			},
			() -> setReadBus(false),
			() -> 
			{
				address += readDataBus() << 8;
				setAddressBus(address + x);
				setReadBus(true);
			},
			() -> setReadBus(false)
		});
		setInitialOperandLoader(MemoryAddressing.ABSOLUTE_INDEXED_Y, new Runnable[]{
			() -> {
				address = readDataBus();
				setAddressBus(pc - 1);
				setReadBus(true);
			},
			() -> setReadBus(false),
			() -> 
			{
				address += (byte)(x + readDataBus() << 8);
				setAddressBus(address + x);
				setReadBus(true);
			},
			() -> setReadBus(false)
		});
		setInitialOperandLoader(MemoryAddressing.IMMEDIATE, new Runnable[]{});
		setInitialOperandLoader(MemoryAddressing.IMPLIED, new Runnable[]{});
		setInitialOperandLoader(MemoryAddressing.X_INDEXED_INDIRECT, new Runnable[]{
			() -> {
				address = readDataBus();
				setAddressBus(address + x);
				setReadBus(true);
			},
			() -> setAddressBus(address + x + 1),
			() -> {
				address = readDataBus();
				setReadBus(false);
			},
			() -> {
				address = readDataBus() << 8;
				setAddressBus(address);
				setReadBus(true);
			},
			() -> setReadBus(false)
		});
		setInitialOperandLoader(MemoryAddressing.INDIRECT_Y_INDEXED, new Runnable[]{
			() -> {
				address = readDataBus();
				setAddressBus(address);
				setReadBus(true);
			},
			() -> setAddressBus(address + 1),
			() -> {
				address = readDataBus();
				setReadBus(false);
			},
			() -> {
				address = readDataBus() << 8;
				int carry = ((sr & Flag.CARRY) != 0) ? 1 : 0;
				setAddressBus(address + y + carry);
				setReadBus(true);
			},
			() -> setReadBus(false)
		});
		setInitialOperandLoader(MemoryAddressing.RELATIVE, new Runnable[]{});
		setInitialOperandLoader(MemoryAddressing.ZEROPAGE, new Runnable[]{
			() -> {
				int address = readDataBus();
				setAddressBus(address);
				setReadBus(true);
			},
			() -> setReadBus(false)
		});
		setInitialOperandLoader(MemoryAddressing.ZEROPAGE_X_INDEXED, new Runnable[]{
			() -> {
				int address = readDataBus();
				setAddressBus(address + x);
				setReadBus(true);
			},
			() -> setReadBus(false)
		});
		setInitialOperandLoader(MemoryAddressing.ZEROPAGE_Y_INDEXED, new Runnable[]{
			() -> {
				int address = readDataBus();
				setAddressBus(address + y);
				setReadBus(true);
			},
			() -> setReadBus(false)
		});
		
		currentOperation = loadInstruction;

		// ADC add with carry
		setInitialInstruction("ADC", 0x69, MemoryAddressing.IMMEDIATE, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction("ADC", 0x65, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction("ADC", 0x75, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction("ADC", 0x6D, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction("ADC", 0x7D, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction("ADC", 0x79, MemoryAddressing.ABSOLUTE_INDEXED_Y, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction("ADC", 0x61, MemoryAddressing.X_INDEXED_INDIRECT, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction("ADC", 0x71, MemoryAddressing.INDIRECT_Y_INDEXED, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		
		// AND logical and
		setInitialInstruction("AND", 0x29, MemoryAddressing.IMMEDIATE, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction("AND", 0x25, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction("AND", 0x35, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction("AND", 0x2D, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction("AND", 0x3D, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction("AND", 0x39, MemoryAddressing.ABSOLUTE_INDEXED_Y, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction("AND", 0x21, MemoryAddressing.X_INDEXED_INDIRECT, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction("AND", 0x31, MemoryAddressing.INDIRECT_Y_INDEXED, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		
		// ASL shift left one bit
		setInitialInstruction("ASL", 0x0A, MemoryAddressing.ACCUMULATOR, (ma) -> new ModifyRegister(this::shiftLeftAccumulator));
		setInitialInstruction("ASL", 0x06, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyMemory(ma, this::shiftLeft));
		setInitialInstruction("ASL", 0x16, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyMemory(ma, this::shiftLeft));
		setInitialInstruction("ASL", 0x0E, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyMemory(ma, this::shiftLeft));
		setInitialInstruction("ASL", 0x1E, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyMemory(ma, this::shiftLeft));
		
		// BCC branch on carry clear
		setInitialInstruction("BCC", 0x90, MemoryAddressing.RELATIVE, (ma) -> new JumpWhen(Flag.CARRY, false));
		
		// BCS branch on carry set
		setInitialInstruction("BCS", 0xB0, MemoryAddressing.RELATIVE, (ma) -> new JumpWhen(Flag.CARRY, true));
		
		// BEQ branch on result zero
		setInitialInstruction("BEQ", 0xF0, MemoryAddressing.RELATIVE, (ma) -> new JumpWhen(Flag.ZERO, true));
		
		// BIT test bits in memory with accumulator TODO
		setInitialInstruction("BIT", 0x24, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyRegisterWithValue(ma, this::testBits));
		setInitialInstruction("BIT", 0x2C, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyRegisterWithValue(ma, this::testBits));
		
		// BMI branch on result minus
		setInitialInstruction("BMI", 0x30, MemoryAddressing.RELATIVE, (ma) -> new JumpWhen(Flag.NEGATIVE, true));
		
		// BNE branch on result not zero
		setInitialInstruction("BNE", 0xD0, MemoryAddressing.RELATIVE, (ma) -> new JumpWhen(Flag.ZERO, false));
		
		// BPL branch on result plus
		setInitialInstruction("BPL", 0x10, MemoryAddressing.RELATIVE, (ma) -> new JumpWhen(Flag.NEGATIVE, false));
		
		// BRK force break TODO
		//null,
		
		// BVC branch on overflow clear
		setInitialInstruction("BVC", 0x50, MemoryAddressing.RELATIVE, (ma) -> new JumpWhen(Flag.OVERFLOW, false));
		
		// BVS branch on overflow set
		setInitialInstruction("BVS", 0x70, MemoryAddressing.RELATIVE, (ma) -> new JumpWhen(Flag.OVERFLOW, true));
		
		// CLC clear carry flag
		setInitialInstruction("CLC", 0x18, MemoryAddressing.IMPLIED, (ma) -> new ModifyStatusRegister(Flag.CARRY, false));
		
		// CLD clear decimal mode
		//null,
		
		// CLI clear interrupt disable bit
		//null,
		
		// CLV clear overflow flag
		setInitialInstruction("CLV", 0xB8, MemoryAddressing.IMPLIED, (ma) -> new ModifyStatusRegister(Flag.OVERFLOW, false));
		
		// CMP compare memory with accumulator
		setInitialInstruction("CMP", 0xC9, MemoryAddressing.IMMEDIATE, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction("CMP", 0xC5, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction("CMP", 0xD5, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction("CMP", 0xCD, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction("CMP", 0xDD, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction("CMP", 0xD9, MemoryAddressing.ABSOLUTE_INDEXED_Y, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction("CMP", 0xC1, MemoryAddressing.X_INDEXED_INDIRECT, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction("CMP", 0xD1, MemoryAddressing.INDIRECT_Y_INDEXED, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		
		// CPX compare memory and index X
		setInitialInstruction("CPX", 0xE0, MemoryAddressing.IMMEDIATE, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithX));
		setInitialInstruction("CPX", 0xE4, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithX));
		setInitialInstruction("CPX", 0xEC, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithX));
		
		// CPY compare memory and index Y
		setInitialInstruction("CPY", 0xC0, MemoryAddressing.IMMEDIATE, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithY));
		setInitialInstruction("CPY", 0xC4, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithY));
		setInitialInstruction("CPY", 0xCC, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithY));
		
		// DEC decrement memory by one
		setInitialInstruction("DEC", 0xC6, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyMemory(ma, this::decrement));
		setInitialInstruction("DEC", 0xD6, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyMemory(ma, this::decrement));
		setInitialInstruction("DEC", 0xCE, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyMemory(ma, this::decrement));
		setInitialInstruction("DEC", 0xDE, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyMemory(ma, this::decrement));
		
		// DEX decrement index X by one
		setInitialInstruction("DEX", 0xCA, MemoryAddressing.IMPLIED, (ma) -> new ModifyRegister(this::decrementX));
		
		// DEY decrement index Y by one
		setInitialInstruction("DEY", 0x88, MemoryAddressing.IMPLIED, (ma) -> new ModifyRegister(this::decrementY));
		
		// EOR xor memory with accumulator
		setInitialInstruction("EOR", 0x49, MemoryAddressing.IMMEDIATE, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction("EOR", 0x45, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction("EOR", 0x55, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction("EOR", 0x4D, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction("EOR", 0x5D, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction("EOR", 0x59, MemoryAddressing.ABSOLUTE_INDEXED_Y, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction("EOR", 0x41, MemoryAddressing.X_INDEXED_INDIRECT, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction("EOR", 0x51, MemoryAddressing.INDIRECT_Y_INDEXED, (ma) -> new ModifyMemory(ma, this::xor));
		
		// INC increment memory by one
		setInitialInstruction("INC", 0xE6, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyMemory(ma, this::increment));
		setInitialInstruction("INC", 0xF6, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyMemory(ma, this::increment));
		setInitialInstruction("INC", 0xEE, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyMemory(ma, this::increment));
		setInitialInstruction("INC", 0xFE, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyMemory(ma, this::increment));
		
		// INX increment index X by one
		setInitialInstruction("INX", 0xE8, MemoryAddressing.IMPLIED, (ma) -> new ModifyRegister(this::incrementX));
		
		// INY increment index Y by one
		setInitialInstruction("INY", 0xC8, MemoryAddressing.IMPLIED, (ma) -> new ModifyRegister(this::incrementY));
		
		// JMP jump to setInstruction(0x, new location
		setInitialInstruction("JMP", 0x4C, MemoryAddressing.ABSOLUTE, (ma) -> new Jump(MemoryAddressing.ABSOLUTE));
		setInitialInstruction("JMP", 0x6C, MemoryAddressing.INDIRECT, (ma) -> new Jump(MemoryAddressing.INDIRECT));
		
		// JSR jump to setInstruction(0x, new location saving return address
		setInitialInstruction("JSR", 0x20, MemoryAddressing.IMPLIED, (ma) -> new Call());
		
		// LDA load accumulator with memory
		setInitialInstruction("LDA", 0xA9, MemoryAddressing.IMMEDIATE, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction("LDA", 0xA5, MemoryAddressing.ZEROPAGE, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction("LDA", 0xB5, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction("LDA", 0xAD, MemoryAddressing.ABSOLUTE, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction("LDA", 0xBD, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction("LDA", 0xB9, MemoryAddressing.ABSOLUTE_INDEXED_Y, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction("LDA", 0xA1, MemoryAddressing.X_INDEXED_INDIRECT, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction("LDA", 0xB1, MemoryAddressing.INDIRECT_Y_INDEXED, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		
		// LDX load index X with memory
		setInitialInstruction("LDX", 0xA2, MemoryAddressing.IMMEDIATE, (ma) -> new LoadRegister(ma, this::loadX));
		setInitialInstruction("LDX", 0xA6, MemoryAddressing.ZEROPAGE, (ma) -> new LoadRegister(ma, this::loadX));
		setInitialInstruction("LDX", 0xB6, MemoryAddressing.ZEROPAGE_Y_INDEXED, (ma) -> new LoadRegister(ma, this::loadX));
		setInitialInstruction("LDX", 0xAE, MemoryAddressing.ABSOLUTE, (ma) -> new LoadRegister(ma, this::loadX));
		setInitialInstruction("LDX", 0xBE, MemoryAddressing.ABSOLUTE_INDEXED_Y, (ma) -> new LoadRegister(ma, this::loadX));
		
		// LDY load index Y with memory
		setInitialInstruction("LDY", 0xA0, MemoryAddressing.IMMEDIATE, (ma) -> new LoadRegister(ma, this::loadY));
		setInitialInstruction("LDY", 0xA4, MemoryAddressing.ZEROPAGE, (ma) -> new LoadRegister(ma, this::loadY));
		setInitialInstruction("LDY", 0xB4, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new LoadRegister(ma, this::loadY));
		setInitialInstruction("LDY", 0xAC, MemoryAddressing.ABSOLUTE, (ma) -> new LoadRegister(ma, this::loadY));
		setInitialInstruction("LDY", 0xBC, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new LoadRegister(ma, this::loadY));
		
		// LSR shift one bit right
		setInitialInstruction("LSR", 0x4A, MemoryAddressing.ACCUMULATOR, (ma) -> new ModifyRegister(this::shiftRightAccumulator));
		setInitialInstruction("LSR", 0x46, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyMemory(ma, this::shiftRight));
		setInitialInstruction("LSR", 0x56, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyMemory(ma, this::shiftRight));
		setInitialInstruction("LSR", 0x4E, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyMemory(ma, this::shiftRight));
		setInitialInstruction("LSR", 0x5E, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyMemory(ma, this::shiftRight));
		
		// NOP
		setInitialInstruction("", 0xEA, MemoryAddressing.IMPLIED, (ma) -> new NoOperation());
		
		// ORA or memory with accumulator
		setInitialInstruction("ORA", 0x09, MemoryAddressing.IMMEDIATE, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction("ORA", 0x05, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction("ORA", 0x15, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction("ORA", 0x0D, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction("ORA", 0x1D, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction("ORA", 0x19, MemoryAddressing.ABSOLUTE_INDEXED_Y, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction("ORA", 0x01, MemoryAddressing.X_INDEXED_INDIRECT, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction("ORA", 0x11, MemoryAddressing.INDIRECT_Y_INDEXED, (ma) -> new ModifyMemory(ma, this::or));
		
		// PHA push accumulator on stack
		setInitialInstruction("PHA", 0x48, MemoryAddressing.IMPLIED, (ma) -> new Push(() -> a));
		
		// PHP push processor status on stack
		setInitialInstruction("PHP", 0x08, MemoryAddressing.IMPLIED, (ma) -> new Push(() -> sr));
		
		// PLA pull accumulator from stack
		setInitialInstruction("PLA", 0x68, MemoryAddressing.IMPLIED, (ma) -> new Pop((v) -> a = v));
		
		// PLP pull processor status from stack
		setInitialInstruction("PLP", 0x28, MemoryAddressing.IMPLIED, (ma) -> new Pop((v) -> sr = v));
		
		// ROL rotate one bit left
		setInitialInstruction("ROL", 0x2A, MemoryAddressing.ACCUMULATOR, (ma) -> new ModifyRegister(this::rotateLeftAccumulator));
		setInitialInstruction("ROL", 0x26, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyMemory(ma, this::rotateLeft));
		setInitialInstruction("ROL", 0x36, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyMemory(ma, this::rotateLeft));
		setInitialInstruction("ROL", 0x2E, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyMemory(ma, this::rotateLeft));
		setInitialInstruction("ROL", 0x3E, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyMemory(ma, this::rotateLeft));
		
		// ROR rotate one bit right
		setInitialInstruction("ROR", 0x6A, MemoryAddressing.ACCUMULATOR, (ma) -> new ModifyRegister(this::rotateRightAccumulator));
		setInitialInstruction("ROR", 0x66, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyMemory(ma, this::rotateRight));
		setInitialInstruction("ROR", 0x76, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyMemory(ma, this::rotateRight));
		setInitialInstruction("ROR", 0x6E, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyMemory(ma, this::rotateRight));
		setInitialInstruction("ROR", 0x7E, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new ModifyMemory(ma, this::rotateRight));
		
		// RTI return from interrupt
		//null,
		
		// RTS return from subroutine
		setInitialInstruction("RTS", 0x60, MemoryAddressing.IMPLIED, (ma) -> new Return());
		
		// SBC subtract memory from accumulator with borrow
		setInitialInstruction("SBC", 0xE9, MemoryAddressing.IMMEDIATE, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction("SBC", 0xE5, MemoryAddressing.ZEROPAGE, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction("SBC", 0xF5, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction("SBC", 0xED, MemoryAddressing.ABSOLUTE, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction("SBC", 0xFD, MemoryAddressing.ABSOLUTE_INDEXED_X,(ma) -> new ModifyRegisterWithValue(ma,  this::sub));
		setInitialInstruction("SBC", 0xFA, MemoryAddressing.ABSOLUTE_INDEXED_Y, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction("SBC", 0xE1, MemoryAddressing.X_INDEXED_INDIRECT, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction("SBC", 0xF1, MemoryAddressing.INDIRECT_Y_INDEXED, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		
		// SEC set carry flag
		setInitialInstruction("SEC", 0x38, MemoryAddressing.IMPLIED, (ma) -> new ModifyStatusRegister(Flag.CARRY, true));
		
		// SED set decimal flag ***UNSUPPORTED***
		//null,
		
		// SEI set interrupt disable status
		//null,
		
		// STA store accumulator in memory
		setInitialInstruction("STA", 0x85, MemoryAddressing.ZEROPAGE, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction("STA", 0x95, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction("STA", 0x8D, MemoryAddressing.ABSOLUTE, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction("STA", 0x9D, MemoryAddressing.ABSOLUTE_INDEXED_X, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction("STA", 0x99, MemoryAddressing.ABSOLUTE_INDEXED_Y, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction("STA", 0x81, MemoryAddressing.X_INDEXED_INDIRECT, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction("STA", 0x91, MemoryAddressing.INDIRECT_Y_INDEXED, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		
		// STX store index X in memory
		setInitialInstruction("STX", 0x86, MemoryAddressing.ZEROPAGE, (ma) -> new StoreRegister(ma, this::storeX));
		setInitialInstruction("STX", 0x96, MemoryAddressing.ZEROPAGE_Y_INDEXED, (ma) -> new StoreRegister(ma, this::storeX));
		setInitialInstruction("STX", 0x8E, MemoryAddressing.ABSOLUTE, (ma) -> new StoreRegister(ma, this::storeX));
		
		// STY store index Y in memory
		setInitialInstruction("STY", 0x84, MemoryAddressing.ZEROPAGE, (ma) -> new StoreRegister(ma, this::storeY));
		setInitialInstruction("STY", 0x94, MemoryAddressing.ZEROPAGE_X_INDEXED, (ma) -> new StoreRegister(ma, this::storeY));
		setInitialInstruction("STY", 0x8C, MemoryAddressing.ABSOLUTE, (ma) -> new StoreRegister(ma, this::storeY));
		
		// TAX transfer accumulator to index X
		setInitialInstruction("TAX", 0xAA, MemoryAddressing.IMPLIED, (ma) -> new TransferRegister(() -> x = a));
		
		// TAY transfer accumulator to index Y
		setInitialInstruction("TAY", 0xA8, MemoryAddressing.IMPLIED, (ma) -> new TransferRegister(() -> y = a));
		
		// TSX transfer stack register to index X
		setInitialInstruction("TSX", 0xBA, MemoryAddressing.IMPLIED, (ma) -> new TransferRegister(() -> x = sp));
		
		// TXA transfer index X to accumulator
		setInitialInstruction("TXA", 0x8A, MemoryAddressing.IMPLIED, (ma) -> new TransferRegister(() -> a = x));
		
		// TXS transfer index X to stack register
		setInitialInstruction("TXS", 0x9A, MemoryAddressing.IMPLIED, (ma) -> new TransferRegister(() -> sp = x));
		
		// TYA transfer index Y to accumulator
		setInitialInstruction("TYA", 0x98, MemoryAddressing.IMPLIED, (ma) -> new TransferRegister(() -> a = y));
	}
	
	public void setInstructionListener(InstructionListener instructionListener)
	{
		this.instructionListener = instructionListener;
	}
	
	private void prepareRegisters()
	{
		registers.add(new RegisterViewControl("A", Byte.BYTES, (v) -> a = (byte)v, () -> a));
		registers.add(new RegisterViewControl("X", Byte.BYTES, (v) -> x = (byte)v, () -> x));
		registers.add(new RegisterViewControl("Y", Byte.BYTES, (v) -> y = (byte)v, () -> y));
		registers.add(new RegisterViewControl("SP", Byte.BYTES, (v) -> sp = (byte)v, () -> sp));
		registers.add(new RegisterViewControl("PC", Short.BYTES, (v) -> pc = (short)v, () -> pc));
		registers.add(new RegisterViewControl("SR", Byte.BYTES, (v) -> sr = (byte)v, () -> sr));
	}

	private void setInitialInstruction(String name, int code, MemoryAddressing memoryAddressing, OperationCreator creator)
	{
		InstructionCompiler parser = (ins, ma, m, o) -> {
			ins.getCode(ma);
			o.writeByte(ins.getCode(ma));
			
			for (int i = m.groupCount(); i > 0; i--)
			{
				String rawOperand = m.group(i);
				int operand = Integer.decode("0x" + rawOperand);
				o.writeByte(operand);
			}
		};
		
		instructionSet.add(name, code, memoryAddressing, parser);

		operations[code] = creator.create(memoryAddressing);
	}
	
	private interface OperationCreator
	{
		Operation create(MemoryAddressing ma);
	}
	
	private void setInitialOperandLoader(MemoryAddressing memoryAddressing, Runnable[] steps)
	{
		memoryAddressingSetups.put(memoryAddressing, steps);
	}
	
	// TODO extract instruction set
	@Deprecated
	public InstructionSet getInstructionSet()
	{
		return instructionSet;
	}
	
	public Set<RegisterView> getRegisterViews()
	{
		return registers;
	}

	private void add(byte value)
	{
		int result = a + value + (checkFlag(Flag.CARRY) ? 1 : 0);
		
		a = (byte)result;
		
		setNegativeFlagByValue(a);
		setZeroFlagByValue(a);
		setCarryFlagByValue(a);
		setOverflowFlagByValue(a);
	}
	
	private void sub(byte value)
	{
		int result = a - value - (checkFlag(Flag.CARRY) ? 1 : 0);
		
		a = (byte)result;
		
		setNegativeFlagByValue(a);
		setZeroFlagByValue(a);
		setCarryFlagByValue(a);
		setOverflowFlagByValue(a);
	}
	
	private void and(byte value)
	{
		int result = a & value;
		
		a = (byte)result;
		
		setNegativeFlagByValue(a);
		setZeroFlagByValue(a);
		setCarryFlagByValue(a);
	}
	
	private void testBits(byte value)
	{
		setFlag(Flag.NEGATIVE, (value & Flag.NEGATIVE) != 0);
		setFlag(Flag.OVERFLOW, (value & Flag.OVERFLOW) != 0);
		
		if ((a & value) == 0)
		{
			sr |= Flag.ZERO;
		}
	}
	
	private byte shiftLeft(byte value)
	{
		int result = value << 1;

		setZeroFlagByValue(result);
		setCarryFlagByValue(result);
		
		return (byte)result;
	}
	
	private byte shiftRight(byte value)
	{
		int result = value >>> 1;

		setZeroFlagByValue(value);
		setCarryFlagByValue(result);
		
		return (byte)result;
	}
	
	private byte rotateLeft(byte value)
	{
		return (byte)(shiftLeft(value) | value >>> 7);
	}
	
	private byte rotateRight(byte value)
	{
		return (byte)(shiftRight(value) | value << 7);
	}
	
	private void shiftLeftAccumulator()
	{
		a = shiftLeft(a);
	}
	
	private void shiftRightAccumulator()
	{
		a = shiftRight(a);
	}
	
	private void rotateLeftAccumulator()
	{
		a = rotateLeft(a);
	}
	
	private void rotateRightAccumulator()
	{
		a = rotateRight(a);
	}
	
	private boolean checkFlag(int flag)
	{
		return (sr & flag) != 0;
	}
	
	private void setZeroFlagByValue(int value)
	{
		setFlag(Flag.ZERO, value == 0);
	}
	
	private void setNegativeFlagByValue(int value)
	{
		setFlag(Flag.ZERO, value < 0);
	}
	
	private void setCarryFlagByValue(int value)
	{
		value = Util.byteToUnsignedInt(value);
		setFlag(Flag.OVERFLOW, value < 0 || value > 255);
	}
	
	private void setOverflowFlagByValue(int value)
	{
		value = Util.byteToUnsignedInt(value);

		setFlag(Flag.OVERFLOW, value > 255);
	}
	
	private void setFlag(int flag, boolean value)
	{
		if (value)
		{
			sr |= flag;
		}
		else
		{
			sr &= ~flag;
		}
	}
	
	private void compareWithAccumulator(byte value)
	{
		int result = a - value;
		
		setZeroFlagByValue(result);
		setNegativeFlagByValue(result);
		setCarryFlagByValue(value);
	}
	
	private void compareWithX(byte value)
	{
		int result = x - value;
		
		setZeroFlagByValue(result);
		setNegativeFlagByValue(result);
		setCarryFlagByValue(value);
	}
	
	private void compareWithY(byte value)
	{
		int result = y - value;
		
		setZeroFlagByValue(result);
		setNegativeFlagByValue(result);
		setCarryFlagByValue(value);
	}
	
	private byte decrement(byte value)
	{
		value--;
		
		setZeroFlagByValue(value);
		setNegativeFlagByValue(value);
		
		return value;
	}
	
	private byte increment(byte value)
	{
		value++;
		
		setZeroFlagByValue(value);
		setNegativeFlagByValue(value);
		
		return value;
	}
	
	private void decrementX()
	{
		x--;
		
		setZeroFlagByValue(x);
		setNegativeFlagByValue(x);
	}
	
	private void incrementX()
	{
		x++;
		
		setZeroFlagByValue(x);
		setNegativeFlagByValue(x);
	}
	
	private void decrementY()
	{
		y--;
		
		setZeroFlagByValue(y);
		setNegativeFlagByValue(y);
	}
	
	private void incrementY()
	{
		y++;
		
		setZeroFlagByValue(y);
		setNegativeFlagByValue(y);
	}
	
	private byte xor(byte value)
	{
		int result = a ^ value;
		
		setZeroFlagByValue(a);
		setNegativeFlagByValue(a);
		
		return (byte)result;
	}
	
	private byte or(byte value)
	{
		int result = a | value;
		
		setZeroFlagByValue(a);
		setNegativeFlagByValue(a);
		
		return (byte)result;
	}
	
	private void loadAccumulator(byte value)
	{
		a = value;
		
		setNegativeFlagByValue(a);
		setZeroFlagByValue(a);
	}
	
	private void loadX(byte value)
	{
		x = value;
		
		setNegativeFlagByValue(x);
		setZeroFlagByValue(x);
	}
	
	private void loadY(byte value)
	{
		y = value;
		
		setNegativeFlagByValue(y);
		setZeroFlagByValue(y);
	}
	
	private byte storeAccumulator()
	{
		return a;
	}
	
	private byte storeX()
	{
		return x;
	}
	
	private byte storeY()
	{
		return y;
	}
	
	@Override
	public int getPinsCount()
	{
		return 26;
	}
	
	// TODO debug
	@Deprecated
	public void setPC(int address)
	{
		pc = (short)address;
	}
	
	@Override
	public void update()
	{
		tick();
	}
	
	private void tick()
	{
		if (currentOperation.isAlmostFinished())
		{
			Operation nextOperation;
			if (currentOperation instanceof LoadInstruction)
			{
				int index = Util.byteToUnsignedInt(ir); 
				nextOperation = operations[index];
				
				// TODO handle illegal instruction
				if (nextOperation == null)
				{
					String data = String.format("PC: %h IR: %h", pc, ir);
					throw new IllegalStateException("Illegal instruction opcode: " + index + " " + data);
				}
				
				pc += nextOperation.getBytesSize();
				
				if (instructionListener != null)
				{
					instructionListener.instructionLoaded(pc, index);
				}
			}
			else
			{
				nextOperation = loadInstruction;
			}
			
			if (!currentOperation.isFinished())
			{
				runCurrentOperation();
			}
			
			currentOperation = nextOperation;
			currentOperation.prepare();
		}
		
		runCurrentOperation();
	}
	
	private void runCurrentOperation()
	{
		currentOperation.run();
	}

	@Override
	public void setBusConnection(Pins pins)
	{
		this.pins = pins;
	}
	
	private void setAddressBus(int address)
	{
		PinUtil.codeValue(address, addressBits);
		
		pins.setPins(addressBits, ADDRESS_PINS_START, addressBits.length);
	}
	
	private void setDataBus(byte data)
	{
		PinUtil.codeValue(data, dataBits);
		
		pins.setPins(dataBits, DATA_PINS_START, dataBits.length);
	}
	
	private void clearDataBus()
	{
		setDataBus((byte)0);
	}
	
	private byte readDataBus()
	{
		pins.readPins(dataBits, DATA_PINS_START, dataBits.length);
		
		return (byte)PinUtil.decodeValue(dataBits);
	}

	private void setWriteBus(boolean write)
	{
		pins.setPin(WRITE_PIN, write);
	}
	
	private void setReadBus(boolean read)
	{
		pins.setPin(READ_PIN, read);
	}
	
	private interface Operation extends Runnable
	{
		int getBytesSize();
		
		void prepare();
		
		boolean isAlmostFinished();
		boolean isFinished();
	}
	
	private abstract class AbstractOperation implements Operation
	{
		private final int bytesSize;
		
		private final Runnable[] addressSteps, executionSteps;
		
		private Runnable[] activeSteps;
		
		private final int steps;
		
		private int step = 0, offset = 0;
		
		public AbstractOperation(MemoryAddressing memoryAddressing)
		{
			this.bytesSize = memoryAddressing.getOperandsBytesSize() + 1;
			
			this.addressSteps = memoryAddressingSetups.get(memoryAddressing);

			StepsBuilder builder = new StepsBuilder();
			createSteps(builder);
			
			executionSteps = builder.create();
			
			steps = addressSteps.length + executionSteps.length;
			
			prepareActiveSteps();
			
		}

		public int getBytesSize()
		{
			return bytesSize;
		}
		
		public void prepare()
		{
			step = 0;
			offset = 0;
			
			prepareActiveSteps();
		}
		
		private void prepareActiveSteps()
		{
			if (addressSteps.length == 0)
			{
				activeSteps = executionSteps;
			}
			else
			{
				activeSteps = addressSteps;
			}
		}
		
		public abstract void createSteps(StepsBuilder b);

		@Override
		public void run()
		{
			runStep(step);

			if (activeSteps == addressSteps && step >= activeSteps.length - 1)
			{
				offset += addressSteps.length;
				activeSteps = executionSteps;
			}
			
			step++;
		}
		
		private void runStep(int step)
		{
			activeSteps[step - offset].run();
		}
		
		public boolean isAlmostFinished()
		{
			return step >= steps - 1;
		}
		
		@Override
		public boolean isFinished()
		{
			return step >= steps;
		}
		
		@Override
		public String toString()
		{
			return this.getClass().getSimpleName();
		}
		
		public class StepsBuilder
		{
			private List<Runnable> steps = new ArrayList<>();
			
			public StepsBuilder add(Runnable r)
			{
				steps.add(r);
				return this;
			}
			
			public Runnable[] create()
			{
				return steps.toArray(new Runnable[steps.size()]);
			}
		}
	}

	private class LoadInstruction implements Operation
	{
		private int step;
		private Runnable[] steps = {
			() -> {
				setAddressBus(pc);
				setReadBus(true);
			},
			() -> {
				setAddressBus(pc + 1);
			},
			() -> {
				ir = readDataBus();
				setReadBus(false);
			},
			() -> {}
		};
		
		@Override
		public void run()
		{
			steps[step++].run();
		}

		@Override
		public int getBytesSize()
		{
			return 0;
		}

		@Override
		public void prepare()
		{
			step = 0;
		}

		@Override
		public boolean isAlmostFinished()
		{
			return step >= steps.length - 1;
		}

		@Override
		public boolean isFinished()
		{
			return step >= steps.length;
		}
	}
	
	private class LoadRegister extends AbstractOperation
	{
		private final RegisterValueSetter setter;
		
		public LoadRegister(MemoryAddressing memoryAddressing, RegisterValueSetter setter)
		{
			super(memoryAddressing);
			this.setter = setter;
		}
		
		@Override
		public void createSteps(StepsBuilder b)
		{
			b.add(() -> setter.set(readDataBus()));
		}
	}
	
	@FunctionalInterface
	private interface RegisterValueSetter
	{
		void set(byte value);
	}
	
	private class StoreRegister extends AbstractOperation
	{		
		private final RegisterValueGetter getter;
		
		public StoreRegister(MemoryAddressing memoryAddressing, RegisterValueGetter getter)
		{
			super(memoryAddressing);
			this.getter = getter;
		}

		@Override
		public void createSteps(StepsBuilder b)
		{
			b
			.add(() -> {
				setDataBus(getter.get());
				setWriteBus(true);
			})
			.add(() -> {
				setWriteBus(false);
				clearDataBus();
			});
		}
	}
	
	@FunctionalInterface
	private interface RegisterValueGetter
	{
		byte get();
	}
	
	private abstract class OneTickOperation implements Operation
	{
		private boolean runned;

		@Override
		public void run()
		{
			runned = true;
		}

		@Override
		public void prepare()
		{
			this.runned = false;
		}

		@Override
		public boolean isAlmostFinished()
		{
			return true;
		}

		@Override
		public boolean isFinished()
		{
			return runned;
		}
	}
	
	private abstract class OneTickRunnableOperation extends OneTickOperation
	{
		private Runnable runnable;
		
		public OneTickRunnableOperation(Runnable runnable)
		{
			this.runnable = runnable;
		}
		
		@Override
		public void run()
		{
			super.run();
			runnable.run();
		}
	}
	
	private class TransferRegister extends OneTickRunnableOperation
	{
		public TransferRegister(Runnable transfer)
		{
			super(transfer);
		}
		
		@Override
		public int getBytesSize()
		{
			return 1;
		}
	}
	
	private class ModifyRegister extends OneTickRunnableOperation
	{
		public ModifyRegister(Runnable modifier)
		{
			super(modifier);
		}
		
		@Override
		public int getBytesSize()
		{
			return 1;
		}
	}
	
	private class ModifyStatusRegister extends OneTickOperation
	{
		private final int flag;
		private final boolean value;
		
		public ModifyStatusRegister(int flag, boolean value)
		{
			this.flag = flag;
			this.value = value;
		}
		
		@Override
		public void run()
		{
			setFlag(flag, value);
		}
		
		@Override
		public int getBytesSize()
		{
			return 1;
		}
	}
	
	private class ModifyRegisterWithValue extends AbstractOperation
	{
		private final RegisterModifier modifier;
		
		public ModifyRegisterWithValue(MemoryAddressing memoryAddressing, RegisterModifier modifier)
		{
			super(memoryAddressing);
			this.modifier = modifier;
		}
		
		@Override
		public void createSteps(StepsBuilder b)
		{
			b.add(() -> modifier.modify(readDataBus()));
		}
	}
	
	@FunctionalInterface
	private interface RegisterModifier
	{
		void modify(byte value);
	}
	
	private class ModifyMemory extends AbstractOperation
	{
		private final MemoryModifier modifier;
		
		public ModifyMemory(MemoryAddressing memoryAddressing, MemoryModifier modifier)
		{
			super(memoryAddressing);
			this.modifier = modifier;
		}
		
		@Override
		public void createSteps(StepsBuilder b)
		{
			b
			.add(() -> {
				byte value = readDataBus();
				value = modifier.modify(value);
				setDataBus(value);
				setWriteBus(true);
			})
			.add(() -> {
				setWriteBus(false);
				clearDataBus();
			});
		}
	}
	
	@FunctionalInterface
	private interface MemoryModifier
	{
		byte modify(byte value);
	}
	
	private class JumpWhen extends OneTickOperation
	{
		private final int flag;
		private final boolean value;
		
		public JumpWhen(int flag, boolean value)
		{
			this.flag = flag;
			this.value = value;
		}

		@Override
		public void run()
		{
			super.run();
			
			boolean result = (sr & flag) != 0;
			if (result == value)
			{
				pc += readDataBus();
			}
		}

		@Override
		public int getBytesSize()
		{
			return 2;
		}

		@Override
		public void prepare()
		{
		}

		@Override
		public boolean isAlmostFinished()
		{
			return true;
		}
	}
	
	private class Jump implements Operation
	{
		private final int bytesSize;
		
		private final Runnable[] steps;

		private int step;
		
		private int address, indirectAddress;
		
		public Jump(MemoryAddressing memoryAddressing)
		{
			this.bytesSize = memoryAddressing.getOperandsBytesSize() + 1;
			
			switch (memoryAddressing)
			{
				case ABSOLUTE:
					steps = new Runnable[]{
						() -> {
							address = readDataBus();
							setAddressBus(pc - 1);
							setReadBus(true);
						},
						() -> setReadBus(false),
						() -> {
							address |= readDataBus() << 8;
							pc = (short)address;
						}
					};
					break;
				case INDIRECT:
					steps = new Runnable[]{
						() -> {
							indirectAddress = readDataBus();
							setAddressBus(pc - 1);
							setReadBus(true);
						},
						() -> setReadBus(false),
						() -> {
							indirectAddress |= readDataBus() << 8;
							setAddressBus(indirectAddress);
							setReadBus(true);
						},
						() -> setAddressBus(indirectAddress + 1),
						() -> {
							address = readDataBus();
							setReadBus(false);
						},
						() -> {
							address |= readDataBus() << 8;
							pc = (short)address;
						}
					};
					break;
				default:
					steps = new Runnable[]{};
			}
		}
		
		@Override
		public void run()
		{
			steps[step++].run();
		}

		@Override
		public int getBytesSize()
		{
			return bytesSize;
		}

		@Override
		public void prepare()
		{
			step = 0;
		}

		@Override
		public boolean isAlmostFinished()
		{
			return isFinished();
		}
		
		@Override
		public boolean isFinished()
		{
			return step >= steps.length;
		}
	}
	
	@FunctionalInterface
	private interface JumpCondition
	{
		boolean test();
	}
	
	private class Push extends AbstractOperation
	{
		private final RegisterValueGetter getter;
		
		public Push(RegisterValueGetter getter)
		{
			super(MemoryAddressing.IMPLIED);
			this.getter = getter;
		}
		
		@Override
		public void createSteps(StepsBuilder b)
		{
			b
			.add(() -> {
				setAddressBus(sp--);
				setDataBus(getter.get());
				setWriteBus(true);
			})
			.add(() -> {
				setWriteBus(false);
				clearDataBus();
			});
		}
	}
	
	private class Pop extends AbstractOperation
	{
		private final RegisterValueSetter setter;
		
		public Pop(RegisterValueSetter setter)
		{
			super(MemoryAddressing.IMPLIED);
			this.setter = setter;
		}
		
		@Override
		public void createSteps(StepsBuilder b)
		{
			b
			.add(() -> {
				setAddressBus(sp++);
				setReadBus(true);
			})
			.add(() -> setReadBus(false))
			.add(() -> setter.set(readDataBus()));
		}
	}
	
	private class Call extends AbstractOperation
	{
		private int address;
		
		public Call()
		{
			super(MemoryAddressing.ABSOLUTE);
		}
		
		@Override
		public void createSteps(StepsBuilder b)
		{
			b
			.add(() -> {
				address = readDataBus();
				setAddressBus(pc - 1);
			})
			.add(() -> setReadBus(false))
			.add(() -> {
				address |= readDataBus() << 8;
				
				setAddressBus(sp--);
				setDataBus((byte)pc);
				setWriteBus(true);
			})
			.add(() -> {
				setAddressBus(sp--);
				setDataBus((byte)(pc >>= 8));
			})
			.add(() -> {
				setWriteBus(false);
				clearDataBus();
				pc = (short)address;
			})
			.add(() -> {});
		}
	}
	
	private class Return extends AbstractOperation
	{
		public Return()
		{
			super(MemoryAddressing.IMPLIED);
		}

		@Override
		public void createSteps(StepsBuilder b)
		{
			b
			.add(() -> {
				setAddressBus(sp++);
				setReadBus(true);
			})
			.add(() -> setAddressBus(sp++))
			.add(() -> {
				pc = (short)(readDataBus() << 8);
				setReadBus(false);
			})
			.add(() -> {
				pc |= readDataBus();
			})
			.add(() -> {});
		}
	}
	
	private class NoOperation extends OneTickOperation
	{
		@Override
		public int getBytesSize()
		{
			return 1;
		}
	}
	
	public enum MemoryAddressing implements InstructionSet.MemoryAddressing
	{
		/**
		 * Operand is AC.
		 */
		ACCUMULATOR(0),
		/**
		 * Operand is address $HHLL.
		 */
		ABSOLUTE(2),
		/**
		 * Operand is address incremented by X with carry.
		 */
		ABSOLUTE_INDEXED_X(2),
		/**
		 * Operand is address incremented by Y with carry.
		 */
		ABSOLUTE_INDEXED_Y(2),
		/**
		 * Operand is byte (BB).
		 */
		IMMEDIATE(1),
		/**
		 * Operand implied.
		 */
		IMPLIED(0),
		/**
		 * Operand is effective address; effective address is value of address.
		 */
		INDIRECT(2),
		/**
		 * Operand is effective zeropage address; effective address is byte (BB) incremented by X without carry.
		 */
		X_INDEXED_INDIRECT(1),
		/**
		 * Operand is effective address incremented by Y with carry; effective address is word at zeropage address.
		 */
		INDIRECT_Y_INDEXED(1),
		/**
		 * Branch target is PC + offset (BB), bit 7 signifies negative offset.
		 */
		RELATIVE(1),
		/**
		 * Operand is of address; address hibyte = zero ($00xx).
		 */
		ZEROPAGE(1),
		/**
		 * Operand is address incremented by X; address hibyte = zero ($00xx); no page transition.
		 */
		ZEROPAGE_X_INDEXED(1),
		/**
		 * Operand is address incremented by Y; address hibyte = zero ($00xx); no page transition.
		 */
		ZEROPAGE_Y_INDEXED(1);
		
		private final int operandsCount;
		
		private MemoryAddressing(int operandsCount)
		{
			this.operandsCount = operandsCount;
		}
		
		@Override
		public int getOperandsBytesSize()
		{
			return operandsCount;
		}
	}
}
