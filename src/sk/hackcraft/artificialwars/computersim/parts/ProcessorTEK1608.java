package sk.hackcraft.artificialwars.computersim.parts;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import sk.hackcraft.artificialwars.computersim.Device;
import sk.hackcraft.artificialwars.computersim.PinUtil;
import sk.hackcraft.artificialwars.computersim.Pins;
import sk.hackcraft.artificialwars.computersim.TEK1608InstructionSet;
import sk.hackcraft.artificialwars.computersim.TEK1608InstructionSet.TEK1608MemoryAddressing;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.MemoryAddressing;
import sk.hackcraft.artificialwars.computersim.toolchain.InstructionSet.Opcode;

/**
 * LittleEndian (16bit = low byte, high byte)
 * 
 * TODO implement interrupt and bcd, or decide to throw them away
 * TODO position instructions properly in instruction table
 * TODO test everything!
 */
public class ProcessorTEK1608 implements Device
{
	private static final int
		READWRITE_PIN = 0,
		ADDRESS_PINS_START = READWRITE_PIN + 1,
		ADDRESS_PINS_COUNT = 16,
		DATA_PINS_START = ADDRESS_PINS_START + ADDRESS_PINS_COUNT,
		DATA_PINS_COUNT = 8;
	
	private static final Runnable NOP = () -> {};
	
	protected byte a, x, y, sp = (byte)0xff, sr;
	protected short pc;
	protected byte ir;

	protected int mar;
	
	private interface Flag
	{
		static final int
			CARRY = 1,
			ZERO = 2,
			INTERRUPT = 4,
			// DECIMAL MODE NOT SUPPORTED
			BREAK = 16,
			// 32 is ignored
			OVERFLOW = 64,
			NEGATIVE = 128;
	}
	
	@FunctionalInterface
	public interface InstructionListener
	{
		void instructionLoaded(int pc, int opcode);
	}

	private Map<TEK1608MemoryAddressing, Runnable[]> memoryAddressingSetups = new EnumMap<>(TEK1608MemoryAddressing.class);
	
	private Pins pins = Pins.DUMMY;
	
	private final boolean
			addressBits[] = new boolean[ADDRESS_PINS_COUNT],
			dataBits[] = new boolean[DATA_PINS_COUNT];
	
	private final int
		addressIndexes[] = PinUtil.createSequenceIndexes(ADDRESS_PINS_START, ADDRESS_PINS_COUNT),
		dataIndexes[] = PinUtil.createSequenceIndexes(DATA_PINS_START, DATA_PINS_COUNT);

	private final InstructionSet instructionSet;
	
	private final Operation[] operations = new Operation[256];
	private final Operation loadInstruction = new LoadInstruction();
	
	private Operation currentOperation;
	
	private InstructionListener instructionListener;
	
	public ProcessorTEK1608()
	{
		setInitialOperandLoader(TEK1608MemoryAddressing.ACCUMULATOR, new Runnable[]{});
		setInitialOperandLoader(TEK1608MemoryAddressing.ABSOLUTE, new Runnable[]{
			() -> {
				mar = readDataBus();
				setAddressBus(pc - 1);
			},
			NOP,
			() -> 
			{
				mar += readDataBus() << 8;
				setAddressBus(mar);
			},
			NOP
		});
		setInitialOperandLoader(TEK1608MemoryAddressing.ABSOLUTE_INDEXED_X, new Runnable[]{
			() -> {
				mar = readDataBus();
				setAddressBus(pc - 1);
			},
			NOP,
			() -> 
			{
				mar += readDataBus() << 8;
				setAddressBus(mar + x);
			},
			NOP
		});
		setInitialOperandLoader(TEK1608MemoryAddressing.ABSOLUTE_INDEXED_Y, new Runnable[]{
			() -> {
				mar = readDataBus();
				setAddressBus(pc - 1);
			},
			NOP,
			() -> 
			{
				mar += (byte)(x + readDataBus() << 8);
				setAddressBus(mar + x);
			},
			NOP
		});
		setInitialOperandLoader(TEK1608MemoryAddressing.IMMEDIATE, new Runnable[]{});
		setInitialOperandLoader(TEK1608MemoryAddressing.IMPLIED, new Runnable[]{});
		setInitialOperandLoader(TEK1608MemoryAddressing.X_INDEXED_INDIRECT, new Runnable[]{
			() -> {
				mar = readDataBus();
				setAddressBus(mar + x);
			},
			() -> setAddressBus(mar + x + 1),
			() -> {
				mar = readDataBus();
			},
			() -> {
				mar = readDataBus() << 8;
				setAddressBus(mar);
			},
			NOP
		});
		setInitialOperandLoader(TEK1608MemoryAddressing.INDIRECT_Y_INDEXED, new Runnable[]{
			() -> {
				mar = readDataBus();
				setAddressBus(mar);
			},
			() -> setAddressBus(mar + 1),
			() -> {
				mar = readDataBus();
			},
			() -> {
				mar = readDataBus() << 8;
				int carry = ((sr & Flag.CARRY) != 0) ? 1 : 0;
				setAddressBus(mar + y + carry);
			},
			NOP
		});
		setInitialOperandLoader(TEK1608MemoryAddressing.RELATIVE, new Runnable[]{});
		setInitialOperandLoader(TEK1608MemoryAddressing.ZEROPAGE, new Runnable[]{
			() -> {
				mar = readDataBus();
				setAddressBus(mar);
			},
			NOP
		});
		setInitialOperandLoader(TEK1608MemoryAddressing.ZEROPAGE_X_INDEXED, new Runnable[]{
			() -> {
				mar = readDataBus();
				setAddressBus(mar + x);
			},
			NOP
		});
		setInitialOperandLoader(TEK1608MemoryAddressing.ZEROPAGE_Y_INDEXED, new Runnable[]{
			() -> {
				mar = readDataBus();
				setAddressBus(mar + y);
			},
			NOP
		});
		
		currentOperation = loadInstruction;
		
		instructionSet = TEK1608InstructionSet.getInstance();
		
		// ADC add with carry
		setInitialInstruction(0x69, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction(0x65, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction(0x75, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction(0x6D, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction(0x7D, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction(0x79, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction(0x61, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		setInitialInstruction(0x71, (ma) -> new ModifyRegisterWithValue(ma, this::add));
		
		// AND logical and
		setInitialInstruction(0x29, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction(0x25, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction(0x35, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction(0x2D, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction(0x3D, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction(0x39, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction(0x21, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		setInitialInstruction(0x31, (ma) -> new ModifyRegisterWithValue(ma, this::and));
		
		// ASL shift left one bit
		setInitialInstruction(0x0A, (ma) -> new ModifyRegister(this::shiftLeftAccumulator));
		setInitialInstruction(0x06, (ma) -> new ModifyMemory(ma, this::shiftLeft));
		setInitialInstruction(0x16, (ma) -> new ModifyMemory(ma, this::shiftLeft));
		setInitialInstruction(0x0E, (ma) -> new ModifyMemory(ma, this::shiftLeft));
		setInitialInstruction(0x1E, (ma) -> new ModifyMemory(ma, this::shiftLeft));
		
		// BCC branch on carry clear
		setInitialInstruction(0x90, (ma) -> new JumpWhen(Flag.CARRY, false));
		
		// BCS branch on carry set
		setInitialInstruction(0xB0, (ma) -> new JumpWhen(Flag.CARRY, true));
		
		// BEQ branch on result zero
		setInitialInstruction(0xF0, (ma) -> new JumpWhen(Flag.ZERO, true));
		
		// BIT test bits in memory with accumulator TODO
		setInitialInstruction(0x24, (ma) -> new ModifyRegisterWithValue(ma, this::testBits));
		setInitialInstruction(0x2C, (ma) -> new ModifyRegisterWithValue(ma, this::testBits));
		
		// BMI branch on result minus
		setInitialInstruction(0x30, (ma) -> new JumpWhen(Flag.NEGATIVE, true));
		
		// BNE branch on result not zero
		setInitialInstruction(0xD0, (ma) -> new JumpWhen(Flag.ZERO, false));
		
		// BPL branch on result plus
		setInitialInstruction(0x10, (ma) -> new JumpWhen(Flag.NEGATIVE, false));
		
		// BRK force break TODO
		//null,
		
		// BVC branch on overflow clear
		setInitialInstruction(0x50, (ma) -> new JumpWhen(Flag.OVERFLOW, false));
		
		// BVS branch on overflow set
		setInitialInstruction(0x70, (ma) -> new JumpWhen(Flag.OVERFLOW, true));
		
		// CLC clear carry flag
		setInitialInstruction(0x18, (ma) -> new ModifyStatusRegister(Flag.CARRY, false));
		
		// CLD clear decimal mode
		//null,
		
		// CLI clear interrupt disable bit
		//null,
		
		// CLV clear overflow flag
		setInitialInstruction(0xB8, (ma) -> new ModifyStatusRegister(Flag.OVERFLOW, false));
		
		// CMP compare memory with accumulator
		setInitialInstruction(0xC9, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction(0xC5, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction(0xD5, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction(0xCD, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction(0xDD, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction(0xD9, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction(0xC1, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		setInitialInstruction(0xD1, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithAccumulator));
		
		// CPX compare memory and index X
		setInitialInstruction(0xE0, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithX));
		setInitialInstruction(0xE4, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithX));
		setInitialInstruction(0xEC, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithX));
		
		// CPY compare memory and index Y
		setInitialInstruction(0xC0, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithY));
		setInitialInstruction(0xC4, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithY));
		setInitialInstruction(0xCC, (ma) -> new ModifyRegisterWithValue(ma, this::compareWithY));
		
		// DEC decrement memory by one
		setInitialInstruction(0xC6, (ma) -> new ModifyMemory(ma, this::decrement));
		setInitialInstruction(0xD6, (ma) -> new ModifyMemory(ma, this::decrement));
		setInitialInstruction(0xCE, (ma) -> new ModifyMemory(ma, this::decrement));
		setInitialInstruction(0xDE, (ma) -> new ModifyMemory(ma, this::decrement));
		
		// DEX decrement index X by one
		setInitialInstruction(0xCA, (ma) -> new ModifyRegister(this::decrementX));
		
		// DEY decrement index Y by one
		setInitialInstruction(0x88, (ma) -> new ModifyRegister(this::decrementY));
		
		// EOR xor memory with accumulator
		setInitialInstruction(0x49, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction(0x45, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction(0x55, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction(0x4D, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction(0x5D, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction(0x59, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction(0x41, (ma) -> new ModifyMemory(ma, this::xor));
		setInitialInstruction(0x51, (ma) -> new ModifyMemory(ma, this::xor));
		
		// INC increment memory by one
		setInitialInstruction(0xE6, (ma) -> new ModifyMemory(ma, this::increment));
		setInitialInstruction(0xF6, (ma) -> new ModifyMemory(ma, this::increment));
		setInitialInstruction(0xEE, (ma) -> new ModifyMemory(ma, this::increment));
		setInitialInstruction(0xFE, (ma) -> new ModifyMemory(ma, this::increment));
		
		// INX increment index X by one
		setInitialInstruction(0xE8, (ma) -> new ModifyRegister(this::incrementX));
		
		// INY increment index Y by one
		setInitialInstruction(0xC8, (ma) -> new ModifyRegister(this::incrementY));
		
		// JMP jump to setInstruction(0x, new location
		setInitialInstruction(0x4C, (ma) -> new Jump(TEK1608MemoryAddressing.ABSOLUTE));
		setInitialInstruction(0x6C, (ma) -> new Jump(TEK1608MemoryAddressing.INDIRECT));
		
		// JSR jump to setInstruction(0x, new location saving return address
		setInitialInstruction(0x20, (ma) -> new Call());
		
		// LDA load accumulator with memory
		setInitialInstruction(0xA9, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction(0xA5, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction(0xB5, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction(0xAD, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction(0xBD, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction(0xB9, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction(0xA1, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		setInitialInstruction(0xB1, (ma) -> new LoadRegister(ma, this::loadAccumulator));
		
		// LDX load index X with memory
		setInitialInstruction(0xA2, (ma) -> new LoadRegister(ma, this::loadX));
		setInitialInstruction(0xA6, (ma) -> new LoadRegister(ma, this::loadX));
		setInitialInstruction(0xB6, (ma) -> new LoadRegister(ma, this::loadX));
		setInitialInstruction(0xAE, (ma) -> new LoadRegister(ma, this::loadX));
		setInitialInstruction(0xBE, (ma) -> new LoadRegister(ma, this::loadX));
		
		// LDY load index Y with memory
		setInitialInstruction(0xA0, (ma) -> new LoadRegister(ma, this::loadY));
		setInitialInstruction(0xA4, (ma) -> new LoadRegister(ma, this::loadY));
		setInitialInstruction(0xB4, (ma) -> new LoadRegister(ma, this::loadY));
		setInitialInstruction(0xAC, (ma) -> new LoadRegister(ma, this::loadY));
		setInitialInstruction(0xBC, (ma) -> new LoadRegister(ma, this::loadY));
		
		// LSR shift one bit right
		setInitialInstruction(0x4A, (ma) -> new ModifyRegister(this::shiftRightAccumulator));
		setInitialInstruction(0x46, (ma) -> new ModifyMemory(ma, this::shiftRight));
		setInitialInstruction(0x56, (ma) -> new ModifyMemory(ma, this::shiftRight));
		setInitialInstruction(0x4E, (ma) -> new ModifyMemory(ma, this::shiftRight));
		setInitialInstruction(0x5E, (ma) -> new ModifyMemory(ma, this::shiftRight));
		
		// NOP
		setInitialInstruction(0xEA, (ma) -> new NoOperation());
		
		// ORA or memory with accumulator
		setInitialInstruction(0x09, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction(0x05, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction(0x15, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction(0x0D, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction(0x1D, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction(0x19, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction(0x01, (ma) -> new ModifyMemory(ma, this::or));
		setInitialInstruction(0x11, (ma) -> new ModifyMemory(ma, this::or));
		
		// PHA push accumulator on stack
		setInitialInstruction(0x48, (ma) -> new Push(() -> a));
		
		// PHP push processor status on stack
		setInitialInstruction(0x08, (ma) -> new Push(() -> sr));
		
		// PLA pull accumulator from stack
		setInitialInstruction(0x68, (ma) -> new Pop((v) -> a = v));
		
		// PLP pull processor status from stack
		setInitialInstruction(0x28, (ma) -> new Pop((v) -> sr = v));
		
		// ROL rotate one bit left
		setInitialInstruction(0x2A, (ma) -> new ModifyRegister(this::rotateLeftAccumulator));
		setInitialInstruction(0x26, (ma) -> new ModifyMemory(ma, this::rotateLeft));
		setInitialInstruction(0x36, (ma) -> new ModifyMemory(ma, this::rotateLeft));
		setInitialInstruction(0x2E, (ma) -> new ModifyMemory(ma, this::rotateLeft));
		setInitialInstruction(0x3E, (ma) -> new ModifyMemory(ma, this::rotateLeft));
		
		// ROR rotate one bit right
		setInitialInstruction(0x6A, (ma) -> new ModifyRegister(this::rotateRightAccumulator));
		setInitialInstruction(0x66, (ma) -> new ModifyMemory(ma, this::rotateRight));
		setInitialInstruction(0x76, (ma) -> new ModifyMemory(ma, this::rotateRight));
		setInitialInstruction(0x6E, (ma) -> new ModifyMemory(ma, this::rotateRight));
		setInitialInstruction(0x7E, (ma) -> new ModifyMemory(ma, this::rotateRight));
		
		// RTI return from interrupt
		//null,
		
		// RTS return from subroutine
		setInitialInstruction(0x60, (ma) -> new Return());
		
		// SBC subtract memory from accumulator with borrow
		setInitialInstruction(0xE9, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction(0xE5, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction(0xF5, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction(0xED, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction(0xFD, (ma) -> new ModifyRegisterWithValue(ma,  this::sub));
		setInitialInstruction(0xFA, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction(0xE1, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		setInitialInstruction(0xF1, (ma) -> new ModifyRegisterWithValue(ma, this::sub));
		
		// SEC set carry flag
		setInitialInstruction(0x38, (ma) -> new ModifyStatusRegister(Flag.CARRY, true));
		
		// SED set decimal flag ***UNSUPPORTED***
		//null,
		
		// SEI set interrupt disable status
		//null,
		
		// STA store accumulator in memory
		setInitialInstruction(0x85, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction(0x95, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction(0x8D, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction(0x9D, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction(0x99, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction(0x81, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		setInitialInstruction(0x91, (ma) -> new StoreRegister(ma, this::storeAccumulator));
		
		// STX store index X in memory
		setInitialInstruction(0x86, (ma) -> new StoreRegister(ma, this::storeX));
		setInitialInstruction(0x96, (ma) -> new StoreRegister(ma, this::storeX));
		setInitialInstruction(0x8E, (ma) -> new StoreRegister(ma, this::storeX));
		
		// STY store index Y in memory
		setInitialInstruction(0x84, (ma) -> new StoreRegister(ma, this::storeY));
		setInitialInstruction(0x94, (ma) -> new StoreRegister(ma, this::storeY));
		setInitialInstruction(0x8C, (ma) -> new StoreRegister(ma, this::storeY));
		
		// TAX transfer accumulator to index X
		setInitialInstruction(0xAA, (ma) -> new TransferRegister(() -> x = a));
		
		// TAY transfer accumulator to index Y
		setInitialInstruction(0xA8, (ma) -> new TransferRegister(() -> y = a));
		
		// TSX transfer stack register to index X
		setInitialInstruction(0xBA, (ma) -> new TransferRegister(() -> x = sp));
		
		// TXA transfer index X to accumulator
		setInitialInstruction(0x8A, (ma) -> new TransferRegister(() -> a = x));
		
		// TXS transfer index X to stack register
		setInitialInstruction(0x9A, (ma) -> new TransferRegister(() -> sp = x));
		
		// TYA transfer index Y to accumulator
		setInitialInstruction(0x98, (ma) -> new TransferRegister(() -> a = y));
	}
	
	@Override
	public String getName()
	{
		return "Processor TEK1608";
	}
	
	public void setInstructionListener(InstructionListener instructionListener)
	{
		this.instructionListener = instructionListener;
	}

	private void setInitialInstruction(int code, OperationCreator creator)
	{
		Opcode opcode = instructionSet.getOpcode(code);
		MemoryAddressing memoryAddressing = opcode.getMemoryAddressing();
		
		operations[code] = creator.create(memoryAddressing);
	}
	
	private interface OperationCreator
	{
		Operation create(MemoryAddressing ma);
	}
	
	private void setInitialOperandLoader(TEK1608MemoryAddressing memoryAddressing, Runnable[] steps)
	{
		memoryAddressingSetups.put(memoryAddressing, steps);
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
		value = Byte.toUnsignedInt((byte)value);
		setFlag(Flag.OVERFLOW, value < 0 || value > 255);
	}
	
	private void setOverflowFlagByValue(int value)
	{
		value = Byte.toUnsignedInt((byte)value);

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
		if (currentOperation.isMemoryFinished())
		{
			Operation nextOperation;
			if (currentOperation instanceof LoadInstruction)
			{
				int index = Byte.toUnsignedInt((byte)ir);
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
		pins.setPins(addressIndexes, addressBits);
	}
	
	private void setDataBus(byte data)
	{
		PinUtil.codeValue(data, dataBits);
		pins.setPins(dataIndexes, dataBits);
	}
	
	private void clearDataBus()
	{
		setDataBus((byte)0);
	}
	
	private byte readDataBus()
	{
		pins.readPins(dataIndexes, dataBits);
		return (byte)PinUtil.decodeValue(dataBits);
	}

	private void setWrite(boolean value)
	{
		pins.setPin(READWRITE_PIN, value);
	}
	
	private short getMemorySP()
	{
		return (short)(0x0100 + sp);
	}
	
	private interface Operation extends Runnable
	{
		int getBytesSize();
		
		void prepare();
		
		boolean isMemoryFinished();
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
		
		public boolean isMemoryFinished()
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
			},
			() -> {
				setAddressBus(pc + 1);
			},
			() -> {
				ir = readDataBus();
			},
			NOP
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
		public boolean isMemoryFinished()
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
				setWrite(true);
			})
			.add(NOP)
			.add(() -> {
				setWrite(false);
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
		public boolean isMemoryFinished()
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
				setWrite(true);
			})
			.add(NOP)
			.add(() -> {
				setWrite(false);
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
		public boolean isMemoryFinished()
		{
			return true;
		}
	}
	
	private class Jump implements Operation
	{
		private final int bytesSize;
		
		private final Runnable[] steps;

		private int step;
		
		public Jump(TEK1608MemoryAddressing memoryAddressing)
		{
			this.bytesSize = memoryAddressing.getOperandsBytesSize() + 1;

			switch (memoryAddressing)
			{
				case ABSOLUTE:
					steps = new Runnable[]{
						() -> {
							mar = readDataBus();
							setAddressBus(pc - 1);
						},
						NOP,
						() -> {
							mar |= readDataBus() << 8;
							pc = (short)mar;
						}
					};
					break;
				case INDIRECT:
					steps = new Runnable[]{
						() -> {
							mar = readDataBus();
							setAddressBus(pc - 1);
						},
						NOP,
						() -> {
							mar |= readDataBus() << 8;
							setAddressBus(mar);
						},
						() -> setAddressBus(mar + 1),
						() -> {
							mar = readDataBus();
						},
						() -> {
							mar |= readDataBus() << 8;
							pc = (short)mar;
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
		public boolean isMemoryFinished()
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
			super(TEK1608MemoryAddressing.IMPLIED);
			this.getter = getter;
		}
		
		@Override
		public void createSteps(StepsBuilder b)
		{
			b
			.add(() -> {
				setAddressBus(getMemorySP());
				sp++;
				setDataBus(getter.get());
				setWrite(true);
			})
			.add(NOP)
			.add(() -> {
				setWrite(false);
				clearDataBus();
			});
		}
	}
	
	private class Pop extends AbstractOperation
	{
		private final RegisterValueSetter setter;
		
		public Pop(RegisterValueSetter setter)
		{
			super(TEK1608MemoryAddressing.IMPLIED);
			this.setter = setter;
		}
		
		@Override
		public void createSteps(StepsBuilder b)
		{
			b
			.add(() -> {
				setAddressBus(getMemorySP());
				sp++;
			})
			.add(NOP)
			.add(() -> setter.set(readDataBus()));
		}
	}
	
	private class Call extends AbstractOperation
	{
		private int address;
		
		public Call()
		{
			super(TEK1608MemoryAddressing.ABSOLUTE);
		}
		
		@Override
		public void createSteps(StepsBuilder b)
		{
			b
			.add(() -> {
				address = readDataBus();
				setAddressBus(pc - 1);
			})
			.add(NOP)
			.add(() -> {
				address |= readDataBus() << 8;
	
				setAddressBus(getMemorySP());
				sp--;
				setDataBus((byte)pc);
				setWrite(true);
			})
			.add(NOP)
			.add(() -> {
				setAddressBus(getMemorySP());
				sp--;
				setDataBus((byte)(pc >>= 8));
			})
			.add(() -> {
				setWrite(false);
				clearDataBus();
				pc = (short)address;
			})
			.add(NOP);
		}
	}
	
	private class Return extends AbstractOperation
	{
		public Return()
		{
			super(TEK1608MemoryAddressing.IMPLIED);
		}

		@Override
		public void createSteps(StepsBuilder b)
		{
			b
			.add(() -> {
				setAddressBus(getMemorySP());
				sp++;
			})
			.add(() -> {
				setAddressBus(getMemorySP());
				sp++;
			})
			.add(() -> {
				pc = (short)(readDataBus() << 8);
			})
			.add(() -> {
				pc |= readDataBus();
			})
			.add(NOP);
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
}
