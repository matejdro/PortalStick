package com.matejdro.bukkit.portalstick;

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class EntityManager {

	public static Location teleport(Entity entity, Location LocTo, Vector vector)
	{
		Region regionTo = RegionManager.getRegion(LocTo);
		Portal portal = PortalManager.insideblocks.get(LocTo);
		if (portal == null && ((Math.abs(vector.getBlockX()) > 1 || (Math.abs(vector.getBlockY()) > 1 || (Math.abs(vector.getBlockZ()) > 1))) || entity instanceof Boat)) 
		{
			portal = PortalManager.awayblocksgeneral.get(LocTo);
			if (portal == null && (Math.abs(vector.getX()) > 1)) portal = PortalManager.awayblocksX.get(LocTo);
			if (portal == null && (Math.abs(vector.getY()) > 1))portal = PortalManager.awayblocksY.get(LocTo);
			if (portal == null && (Math.abs(vector.getZ()) > 1)) portal = PortalManager.awayblocksZ.get(LocTo);
		}
		
		
		if (portal != null)
		{
			if (!portal.isOpen() || portal.isDisabled()) return null;
			if (Math.abs(vector.getY()) > 1 && !portal.isVertical()) return null;
			User owner = portal.getOwner();
				 
			Location teleport;
			Portal destination;
			if (portal.isOrange())
				destination = owner.getBluePortal();
			else
				destination = owner.getOrangePortal();
				 				 
			teleport = destination.getTeleportLocation().clone();
								 
			float yaw = 0;
			float pitch = 0;
				
			//Read input velocity
			Double momentum = 0.0;
			switch(portal.getTeleportFace())
	        {
	        	case NORTH:
	        	case SOUTH:
	        		momentum = vector.getX();
	        		break;
	        	case EAST:
	        	case WEST:
	        		momentum = vector.getZ();
	        		break;
	        	case UP:
	        	case DOWN:
	        		momentum = vector.getY();
	        		break;
	        }
				
			momentum = Math.abs(momentum);
			momentum = momentum * regionTo.getDouble(RegionSetting.VELOCITY_MULTIPLIER);

			//reposition velocity to match output portal's orientation
			Vector outvector = entity.getVelocity().zero();
			switch(destination.getTeleportFace())
	        {
	        	case NORTH:
	        		yaw = 270;
	        		outvector = outvector.setX(momentum);
	        		break;
	        	case EAST:
	        		yaw = 0;
	        		outvector = outvector.setZ(momentum);
	        		break;
	        	case SOUTH:
	        		yaw = 90;
	        		outvector = outvector.setX(-momentum);
	        		break;
	        	case WEST:
	        		yaw = 180;
	        		outvector = outvector.setZ(-momentum);
	        		break;
	        	case UP:
	        		pitch = 90;
	        		outvector = outvector.setY(momentum);
	        		break;
	        	case DOWN:
	        		pitch = -90;
	        		outvector = outvector.setY(-momentum);
	        		break;
	        }
				 				
			entity.setFallDistance(0);	
			entity.setVelocity(entity.getVelocity().zero());
				 
			teleport.setPitch(pitch);
			teleport.setYaw(yaw);
				 
			entity.teleport(teleport);
				 				 
			entity.setVelocity(outvector);
				 
			destination.setDisabled(true);
			final Portal disabledportal = portal;
			PortalStick.instance.getServer().getScheduler().scheduleSyncDelayedTask(PortalStick.instance, new Runnable()
				{
					public void run()
					{
						if (disabledportal != null) disabledportal.setDisabled(false);
					}
				}, 10L);
		
			return teleport;
		}
		return null;
	}
}
