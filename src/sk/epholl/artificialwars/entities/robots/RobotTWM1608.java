package sk.epholl.artificialwars.entities.robots;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import sk.epholl.artificialwars.entities.Entity;
import sk.epholl.artificialwars.entities.Explosion;
import sk.epholl.artificialwars.entities.Projectile;
import sk.epholl.artificialwars.entities.robots.parts.DetectorSpot;
import sk.epholl.artificialwars.entities.robots.parts.GradientDetector;
import sk.epholl.artificialwars.entities.robots.parts.SegmentDetector;
import sk.epholl.artificialwars.entities.robots.parts.SpotsProvider;
import sk.epholl.artificialwars.logic.GameLogic;
import sk.epholl.artificialwars.logic.Vector2D;
import sk.epholl.artificialwars.logic.Vector2DMath;
import sk.hackcraft.artificialwars.computersim.Util;
import sk.hackcraft.artificialwars.computersim.parts.MEXTIOChip;

public class RobotTWM1608 extends Entity
{
	public static void main(String[] args) throws Exception
	{
		RobotTWM1608 e = new RobotTWM1608(Color.BLACK, 0, null, 0);
		
		FirmwareLoader.loadFirmwareExterminator("fibonacci.asm", e);
		
		for (int i = 0; i < 1000; i++)
		{
			e.turn();
		}
		
		System.out.println("end.");
	}
	
	private final Random random;
	private int player;
	private int hitpoints = 5;
	private final int GUN_COOLDOWN_TIME = 30;
	private int actualGunCooldown = 0;
	
	private int computerFrequency = 1;
	
	private double rotationsPerSecond = 1 / 3;
	private double movementPerSecond = 10;
	private double rotationStep = 360 / 65536;
	
	private final ComputerTWM1000 computer;
	
	private final short PROGRAM_OFFSET = 0x0200;
	
	private final SegmentDetector segmentDetector;
	private final GradientDetector gradientDetector;
	
	private static final int SEGMENT_DETECTOR_SEGMENTS_COUNT = 8;
	private static final double SEGMENT_DETECTOR_DETECTING_THRESHOLD = 2 / 50;
	private static final double GRADIENT_DETECTOR_ANGLE = (Math.PI / 4) * 2;
	private static final double GRADIENT_DETECTOR_DETECTING_THRESHOLD = 1;
	
	public RobotTWM1608(Color color, int player, GameLogic game, long seed)
	{
		super(game);
		
		this.color = color;
		this.player = player;
		
		this.random = new Random(seed);
		
		computer = new ComputerTWM1000();
		
		SpotsProvider provider = new ExterminatorDetectorSpotsProvider();
		
		this.segmentDetector = new SegmentDetector(provider, SEGMENT_DETECTOR_SEGMENTS_COUNT, SEGMENT_DETECTOR_DETECTING_THRESHOLD);
		this.gradientDetector = new GradientDetector(provider, GRADIENT_DETECTOR_ANGLE);
	}
	
	public void loadFirmware(byte firmware[])
	{
		computer.loadFirmware(PROGRAM_OFFSET, firmware);
	}
	
	@Override
	public void update(Set<Entity> nearbyEntities)
	{
		super.update(nearbyEntities);
		
		if (hitpoints <= 0)
		{
			destroy();
		}
	}
	
	@Override
	public void turn()
	{
		for (int i = 0; i < computerFrequency; i++)
		{
			System.out.println(computer.getBusProbe());
			System.out.println(computer.getProcessorProbe());
			System.out.println(computer.getMemoryProbe().getMemory(0, 32, 8));
			computer.tick();
		}
		
		MEXTIOChip io = computer.getIO();
		
		boolean fireOrder = io.areSet(MEXTIOChip.Flag.FIRE_ORDER);
		updateGun(fireOrder);

		segmentDetector.update();
		gradientDetector.update();
		
		byte chipDetectionSegment = 0;
		byte bit = 1;
		for (int i = 0; i < SEGMENT_DETECTOR_SEGMENTS_COUNT; i++)
		{
			if (segmentDetector.isActivated(i))
			{
				chipDetectionSegment |= bit;
			}
			
			bit <<= 1;
		}
		
		io.setDetectionSegment(chipDetectionSegment);
		
		double excitation = gradientDetector.getExcitation();
		byte chipDetectionGradient;
		
		if (excitation < 0)
		{
			chipDetectionGradient = 0;
		}
		else if (excitation > 1)
		{
			chipDetectionGradient = Util.UNSIGNED_BYTE_MAX_VALUE_RAW;
		}
		else
		{
			chipDetectionGradient = (byte)(excitation * Util.UNSIGNED_BYTE_MAX_VALUE_RAW);
		}
		
		io.setDetectionGradient(chipDetectionGradient);
		
		int movementOrder = io.getMoveOrderValue();
		int rotationOrder = io.getRotationOrderValue();
		// TODO movement
		// TODO rotation
		
		if (io.areSet(MEXTIOChip.Flag.NOISE))
		{
			byte noise = (byte)random.nextInt();
			io.setNoise(noise);
		}
		
		io.setFlags(MEXTIOChip.Flag.GUN_READY, isGunReady());
		io.setFlags(MEXTIOChip.Flag.MOVING, isMoving());
		io.setFlags(MEXTIOChip.Flag.ROTATING, isRotating());
		io.setFlags(MEXTIOChip.Flag.DETECTION_SEGMENT, isSegmentDetecting());
		io.setFlags(MEXTIOChip.Flag.DETECTION_GRADIENT, isGradientDetecting());

		System.out.println(computer.getDisplay());
	}
	
	private boolean isGunReady()
	{
		return actualGunCooldown == 0;
	}
	
	@Override
	public boolean isMoving()
	{
		return super.isMoving();
	}
	
	private boolean isRotating()
	{
		return false;
	}
	
	private boolean isSegmentDetecting()
	{
		for (int i = 0; i < segmentDetector.getSegmentsCount(); i++)
		{
			if (segmentDetector.isActivated(i))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isGradientDetecting()
	{
		return gradientDetector.getExcitation() > GRADIENT_DETECTOR_DETECTING_THRESHOLD;
	}
	
	private void updateGun(boolean fireOrder)
	{
		if (actualGunCooldown > 0)
		{
			actualGunCooldown--;
		}
		
		if (isGunReady() && fireOrder)
		{
			fire();
			actualGunCooldown = GUN_COOLDOWN_TIME;
		}
	}
	
	private void fire()
	{
		Projectile shot = new Projectile(game, this);
		
		shot.setCenterPosition(getPosition());
		shot.setDirection(getDirection());
		
		game.addEntity(shot);
	}

	@Override
	public boolean isDestructible()
	{
		return true;
	}

	@Override
	public boolean hasPlayer()
	{
		return true;
	}

	@Override
	public boolean isCollidable()
	{
		return true;
	}

	@Override
	public boolean isSolid()
	{
		return true;
	}

	@Override
	public void destroy()
	{
		super.destroy();

		Explosion explosion = Explosion.create(game, getPosition());
		game.addEntity(explosion);
	}
	
	@Override
	public void beHit(Projectile shot)
	{
		super.beHit(shot);
		this.hitpoints -= shot.getDamage();
		
		Explosion explosion = Explosion.create(game, getPosition());
		game.addEntity(explosion);
	}
	
	@Override
	public int getPlayer()
	{
		return player;
	}
	
	private class ExterminatorDetectorSpotsProvider implements SpotsProvider
	{
		@Override
		public Set<DetectorSpot> getDetectorSpots()
		{
			ArrayList<Entity> targets = new ArrayList<Entity>();

			for (Entity e : game.getEntities())
			{
				if (e == RobotTWM1608.this)
				{
					continue;
				}
				
				if (e.getPlayer() == getPlayer())
				{
					continue;
				}
				
				if (e.isDestructible())
				{
					targets.add(e);
				}
			}
			
			HashSet<DetectorSpot> spots = new HashSet<>();
			
			Vector2D exterminatorVector = getDirection();
			
			for (Entity target : targets)
			{
				Vector2D targetVector = new Vector2D(exterminatorVector, target.getPosition());
				
				double relativeRotation = Vector2DMath.getRelativeSignedAngle(exterminatorVector, targetVector);
				double distance = target.getDistance(RobotTWM1608.this);
				double width = Math.max(target.getWidth(), target.getHeight());
				
				spots.add(new DetectorSpot(relativeRotation, distance, width));
			}
			
			return spots;
		}
	}
}
