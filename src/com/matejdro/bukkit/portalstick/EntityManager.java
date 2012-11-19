package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;
import de.V10lator.PortalStick.V10Teleport;

public class EntityManager implements Runnable {
	private final PortalStick plugin;
	private final HashSet<Entity> blockedEntities = new HashSet<Entity>();

	EntityManager(PortalStick instance)
	{
		plugin = instance;
	}

	public V10Teleport teleport(Entity entity, Location oloc, V10Location locTo, Vector vector, boolean really)
	{
		if (entity == null || entity.isDead() || blockedEntities.contains(entity))
		  return null;

		Region regionTo = plugin.regionManager.getRegion(locTo);
		Portal portal = plugin.portalManager.insideBlocks.get(locTo);
		
		if(portal == null)
		{
		  if((entity instanceof FallingBlock || entity instanceof TNTPrimed) && vector.getX() == 0.0D && vector.getZ() == 0.0D)
		  {
			portal = plugin.portalManager.awayBlocksY.get(locTo);
		  }
		  else if((Math.abs(vector.getX()) > 0.5 || (Math.abs(vector.getY()) > 1 || (Math.abs(vector.getZ()) > 0.5))) || entity instanceof Boat)
		  {
			portal = plugin.portalManager.awayBlocks.get(locTo); //TODO: Improve. This seems to be still to much.
			if(portal == null)
			  return null;
			Block to = locTo.getHandle().getBlock();
			for(int i = 0; i < 2; i++)
			{
			  BlockFace face = portal.awayBlocksY[i].getHandle().getBlock().getFace(to);
			  if(face == null)
				continue;
			  if(face != BlockFace.SELF)
			  {
				double x = 1.0D, z = 1.0D;
				switch(face)
				{
			  	  case NORTH_WEST:
			  		z = 0.5D;
			  	  case NORTH:
			  		x = 1.5D;
				  break;
			  	  case NORTH_EAST:
			  		x = 1.5D;
			  	  case EAST:
			  		z = 1.5D;
			  		break;
			  	  case SOUTH_EAST:
			  		z = 1.5D;
			  	  case SOUTH:
			  		x = 0.5D;
			  		break;
			  	  case SOUTH_WEST:
			  		x = 0.5D;
			  	  default:
			  		z = 0.5D;
				}
				if(oloc.getX() - locTo.x > x || oloc.getZ() - locTo.z > z)
				  return null; 
			  }
			  else
			  {
				System.out.print("Self!");
				break;
			  }
			}
		  }
		  else
			return null;
		}
		
		if(!portal.open || portal.disabled || (Math.abs(vector.getY()) > 1 && !portal.vertical))
		  return null;
		
/*		double x, y, z;
		
		for (V10Location b : portal.inside)
		{
			x = b.x;
			y = b.y;
			z = b.z;
			
			if (!portal.vertical)
			{
				if (x + 0.5 < entity.getLocation().getX() && vector.getX() > 0) return null;
				else if (x - 0.5 > entity.getLocation().getX() && vector.getX() < 0) return null;
				else if (y + 0.5 < entity.getLocation().getY() && vector.getY() > 0) return null;
				else if (z - 0.5 > entity.getLocation().getZ() && vector.getZ() < 0) return null;
			}
			else
			{
				if (y + 0.5 < entity.getLocation().getY() && vector.getY() > 0) return null;
				if (y - 0.5 > entity.getLocation().getY() && vector.getY() < -0.1) return null;
			}
		}
		plugin.getServer().broadcastMessage("Velocity Y: "+vector.getY());
		*/
		
		Portal destination = portal.getDestination();	 
		Location teleport = destination.teleport.getHandle();
		
		teleport.setX(teleport.getX() + 0.5D);
		teleport.setZ(teleport.getZ() + 0.5D);
							 
		float yaw = entity.getLocation().getYaw();
		float pitch = entity.getLocation().getPitch();
		float startyaw = yaw;
		switch(portal.teleportFace)
	       {
	       	case EAST:
	       		yaw -= 90;
	       		break;
	       	case SOUTH:
	       		yaw -= 180;
	       		break;
	       	case WEST:
	       		yaw -= 270;
	       		break;
	       	case DOWN:
	       		teleport.add(0, 1, 0);
	       	case UP:
	       		yaw = pitch;
	       		pitch = 0;
	       		break;
	       }
			
		//Read input velocity
		double momentum = 0.0;
		switch(portal.teleportFace)
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
		momentum *= regionTo.getDouble(RegionSetting.VELOCITY_MULTIPLIER);
			//reposition velocity to match output portal's orientation
		Vector outvector = entity.getVelocity().zero();
		switch(destination.teleportFace)
        {
        	case NORTH:
        		yaw += 180;
        		outvector = outvector.setX(momentum);
        		break;
        	case EAST:
        		yaw += 270;
        		outvector = outvector.setZ(momentum);
        		break;
        	case SOUTH:
        		yaw += 360;
        		outvector = outvector.setX(-momentum);
        		break;
        	case WEST:
        		yaw += 450;
        		outvector = outvector.setZ(-momentum);
        		break;
        	case DOWN:
        		if (portal.teleportFace != BlockFace.UP && portal.teleportFace != BlockFace.DOWN) //TODO: || to &&
        		{
        			yaw = pitch;
	        		pitch = startyaw;
        		}
        		else
        		{
        			pitch = yaw;
        			yaw = startyaw;
        		}
        		outvector = outvector.setY(momentum);
        		break;
        	case UP:
        		if (portal.teleportFace != BlockFace.UP && portal.teleportFace != BlockFace.DOWN) //TODO: || to &&
        		{
        			yaw = pitch;
	        		pitch = startyaw + 180;
        		}
        		else
        		{
        			pitch = yaw;
        			yaw = startyaw;
        		}
        		outvector = outvector.setY(-momentum);
        		break;
        }
		
		if (!(entity instanceof Player) && !(entity instanceof Chicken) && momentum < 0.5 && (portal.teleportFace == BlockFace.UP || portal.teleportFace == BlockFace.DOWN) && (destination.teleportFace == BlockFace.UP || destination.teleportFace == BlockFace.DOWN))
			teleport.setX(teleport.getX() + 0.5D);
		
		entity.setFallDistance(0);
//		entity.setVelocity(entity.getVelocity().zero());
		
		teleport.setPitch(pitch);
		teleport.setYaw(yaw);
		
		if (entity instanceof Arrow)
			teleport.setY(teleport.getY() + 0.5);
		if(really)
		{
		  if(!entity.teleport(teleport))
			return null;
		  entity.setVelocity(outvector);
		}
		
		destination.disabled = true;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new enablePortal(destination), 10L);
		
		if (portal.orange)
			plugin.util.PlaySound(Sound.PORTAL_EXIT_ORANGE, entity instanceof Player ? (Player) entity : null, new V10Location(teleport));
		else
			plugin.util.PlaySound(Sound.PORTAL_EXIT_BLUE, entity instanceof Player ? (Player) entity : null, new V10Location(teleport));
		
		return new V10Teleport(teleport, outvector);
	}
	
	@Override
	public void run() {
		Location oloc;
		Vector vector;
		for (World w : plugin.getServer().getWorlds())
		{
			for(Entity e: w.getEntities())
			{
				if (e instanceof Player || e instanceof Vehicle || e.isDead())
				  continue;

				vector = e.getVelocity();
				oloc = e.getLocation();
				teleport(e, oloc, new V10Location(oloc), vector, true);
				plugin.funnelBridgeManager.EntityMoveCheck(e);
			}
		}
	}
	
	public class enablePortal implements Runnable
	{
		Portal portal;
		public enablePortal(Portal instance)
		{
			portal = instance;
		}

		@Override
		public void run() {
			if (portal != null)
			  portal.disabled = false;
		}
	}
}
