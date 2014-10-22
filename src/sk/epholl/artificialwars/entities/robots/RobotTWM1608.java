package sk.epholl.artificialwars.entities.robots;

import java.awt.Color;
import java.io.IOError;
import java.io.IOException;
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
import sk.epholl.artificialwars.logic.Simulation;
import sk.epholl.artificialwars.logic.Vector2D;
import sk.epholl.artificialwars.logic.Vector2DMath;
import sk.hackcraft.artificialwars.computersim.Util;
import sk.hackcraft.artificialwars.computersim.debug.CommonValueFormatter;
import sk.hackcraft.artificialwars.computersim.parts.ComputerTWM1000;
import sk.hackcraft.artificialwars.computersim.parts.MEXTIOChip;

public class RobotTWM1608 extends Robot
{	
	private final Random random;

	private int hitpoints = 5;
	private final int GUN_COOLDOWN_TIME = 30;
	private int actualGunCooldown = 0;
	
	private int computerFrequency = 100;
	
	private final ComputerTWM1000 computer;
	
	private final short PROGRAM_OFFSET = 0x0200;
	
	private final SegmentDetector segmentDetector;
	private final GradientDetector gradientDetector;
	
	private static final int SEGMENT_DETECTOR_SEGMENTS_COUNT = 8;
	private static final double SEGMENT_DETECTOR_DETECTING_THRESHOLD = 0.1;
	private static final double GRADIENT_DETECTOR_ANGLE = Math.PI / 2;
	
	private static final double MOVE_SPEED = 1;
	private static final double ROTATION_SPEED = Math.PI / 128;
	
	public RobotTWM1608(Simulation game, long seed)
	{
		super(game);
		
		this.random = new Random(seed);
		
		computer = new ComputerTWM1000();
		
		SpotsProvider provider = new ExterminatorDetectorSpotsProvider();
		
		// TODO debug
		this.segmentDetector = new SegmentDetector(provider, SEGMENT_DETECTOR_SEGMENTS_COUNT, SEGMENT_DETECTOR_DETECTING_THRESHOLD);
		this.gradientDetector = new GradientDetector(provider, GRADIENT_DETECTOR_ANGLE);
	}
	
	@Override
	public int getRobotTypeId()
	{
		return StockRobotsId.RobotWTM1608;
	}
	
	@Override
	public void setFirmware(byte firmware[]) throws IOException
	{
		computer.loadFirmware(PROGRAM_OFFSET, firmware);
	}
	
	public ComputerTWM1000 getComputer()
	{
		return computer;
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
	public void act()
	{
		for (int i = 0; i < computerFrequency; i++)
		{
			computer.tick();
		}
		
		MEXTIOChip io = computer.getIO();
		
		updateGun(io);

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
			chipDetectionGradient = Util.UNSIGNED_BYTE_MAX_VALUE_BITS;
		}
		else
		{
			chipDetectionGradient = (byte)(excitation * Util.UNSIGNED_BYTE_MAX_VALUE);
		}

		io.setDetectionGradient(chipDetectionGradient);
		
		updateCompass(io);

		updateMovement(io);
		updateRotation(io);
		
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
		io.setFlags(MEXTIOChip.Flag.COLLIDING, isColliding());
	}
	
	private void updateCompass(MEXTIOChip io)
	{
		Vector2D direction = getDirection();
		
		double angle = Vector2DMath.getRelativeSignedAngle(direction, Vector2D.NORTH);
		double signum = Math.signum(angle);
		
		double chunk = Byte.MAX_VALUE / Math.PI;
		
		double bytesAngle = angle * chunk;
		byte hibyte = (byte)bytesAngle;

		double absBytesAngle = Math.abs(bytesAngle);
		double rest = (absBytesAngle - Math.floor(absBytesAngle)) * signum;
		double restBytesAngle = rest * Byte.MAX_VALUE;
		byte lobyte = (byte)restBytesAngle;
		
		io.setAbsoluteRotation(hibyte, lobyte);
	}
	
	private void updateMovement(MEXTIOChip io)
	{
		byte movementOrder = io.getMoveOrderValue();
		
		int signum = (int)Math.signum(movementOrder);
		double newMoveSpeed = (movementOrder != 0) ? signum * MOVE_SPEED : 0;
		setMoveSpeed(newMoveSpeed);
		
		byte newMovementOrder = (byte)(movementOrder - signum);
		io.setMoveOrderValue(newMovementOrder);
	}
	
	private void updateRotation(MEXTIOChip io)
	{
		short rotationOrder = io.getRotationOrderValue();

		byte rotationHibyte = (byte)(rotationOrder >>> 8);
		
		int signum = (int)Math.signum(rotationHibyte);
		double newRotationSpeed = (rotationHibyte != 0) ? signum * ROTATION_SPEED : 0;
		
		byte newHibyteOrder = (byte)(rotationHibyte - signum);
		
		byte rotationLobyte = (byte)(rotationOrder);
		byte newLobyteOrder;
		if (newRotationSpeed == 0)
		{
			double preciousOffset = (rotationLobyte != 0) ? (ROTATION_SPEED / Util.UNSIGNED_BYTE_MAX_VALUE) * rotationLobyte : 0;
			
			newRotationSpeed += preciousOffset;
			
			signum = (int)Math.signum(rotationLobyte);
			newLobyteOrder = 0;
		}
		else
		{
			newLobyteOrder = rotationLobyte;
		}

		io.setRotationOrder(newHibyteOrder, newLobyteOrder);
		
		setRotateSpeed(newRotationSpeed);
	}
	
	private boolean isGunReady()
	{
		return actualGunCooldown == 0;
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
		return gradientDetector.getExcitation() > 0;
	}
	
	private void updateGun(MEXTIOChip io)
	{
		if (actualGunCooldown > 0)
		{
			actualGunCooldown--;
		}
		
		boolean fireOrder = io.areSet(MEXTIOChip.Flag.FIRE_ORDER);
		if (isGunReady() && fireOrder)
		{
			fire();
			actualGunCooldown = GUN_COOLDOWN_TIME;
			io.setFlags(MEXTIOChip.Flag.FIRE_ORDER, false);
		}
	}
	
	private void fire()
	{
		Projectile shot = new Projectile(game, this);
		
		shot.setCenterPosition(getCenterPosition());
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

		Explosion explosion = Explosion.create(game, getCenterPosition());
		game.addEntity(explosion);
	}
	
	@Override
	public void beHit(Projectile shot)
	{
		super.beHit(shot);
		this.hitpoints -= shot.getDamage();
		
		Explosion explosion = Explosion.create(game, getCenterPosition());
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
				
				// TODO modify to use detector mechanics, like heat or radar waves
				// TODO modify this condition to be politically correct
				if (e.isDestructible())
				{
					targets.add(e);
				}
			}
			
			HashSet<DetectorSpot> spots = new HashSet<>();
			
			Vector2D robotPosition = getCenterPosition();
			Vector2D robotDirection = getDirection();
			
			for (Entity target : targets)
			{
				Vector2D targetVector = new Vector2D(robotPosition, target.getCenterPosition()).normalise();

				double relativeRotation = Vector2DMath.getRelativeSignedAngle(robotDirection, targetVector);
				double distance = target.getDistance(RobotTWM1608.this);
				double width = Math.max(target.getWidth(), target.getHeight());
				
				if (target instanceof Projectile)
				{
					width *= 4;
				}
				
				spots.add(new DetectorSpot(relativeRotation, distance, width));
			}
			
			return spots;
		}
	}
}
