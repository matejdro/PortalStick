package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class EntityManager implements Runnable {
	private final PortalStick plugin;
	final HashSet<String> processingWorlds = new HashSet<String>();
	private final HashSet<Entity> blockedEntities = new HashSet<Entity>();

	EntityManager(PortalStick instance)
	{
		plugin = instance;
	}

	public Location teleport(Entity entity, V10Location locTo, Vector vector)
	{
		if (entity == null || entity.isDead() || blockedEntities.contains(entity)) return null;

		Region regionTo = plugin.regionManager.getRegion(locTo);
		Portal portal = plugin.portalManager.insideBlocks.get(locTo);
		if (portal == null && ((Math.abs(vector.getX()) > 0.5 || (Math.abs(vector.getY()) > 1 || (Math.abs(vector.getZ()) > 0.5))) || entity instanceof Boat)) 
		{
			portal = plugin.portalManager.awayBlocksGeneral.get(locTo);
			if (portal == null && (Math.abs(vector.getX()) > 0.5)) portal = plugin.portalManager.awayBlocksX.get(locTo);
			if (portal == null && (Math.abs(vector.getY()) > 1)) portal = plugin.portalManager.awayBlocksY.get(locTo);
			if (portal == null && (Math.abs(vector.getZ()) > 0.5)) portal = plugin.portalManager.awayBlocksZ.get(locTo);
		}
		if (portal == null && (entity instanceof FallingBlock || entity instanceof TNTPrimed))
		  portal = plugin.portalManager.awayBlocksY.get(locTo);
		if (portal == null ||!portal.open || portal.disabled || (Math.abs(vector.getY()) > 1 && !portal.vertical))
		  return null;
		
		double x, y, z;
		
		for (V10Location b : portal.inside)
		{
			x = b.x;
			y = b.y;
			z = b.z;
			
			if (!portal.vertical)
			{
				if (x + 0.5 < entity.getLocation().getX() && vector.getX() > 0) return null;
				else if (x - 0.5 > entity.getLocation().getX() && vector.getX() < 0) return null;
				else if (y + 0.5 < entity.getLocation().getZ() && vector.getZ() > 0) return null;
				else if (z - 0.5 > entity.getLocation().getZ() && vector.getZ() < 0) return null;
			}
			else
			{
				if (y + 0.5 < entity.getLocation().getY() && vector.getY() > 0) return null;
				if (y - 0.5 > entity.getLocation().getY() && vector.getY() < -0.1) return null;
			}
		}
		
		
		Portal destination = portal.getDestination();	 
		Location teleport = destination.teleport.getHandle();
		
		teleport.setX(teleport.getX() + 0.5D);
		teleport.setY(teleport.getY() + 0.5D);
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
	       		yaw = -270;
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
        		yaw += 430;
        		outvector = outvector.setZ(-momentum);
        		break;
        	case DOWN:
        		if (portal.teleportFace != BlockFace.UP && portal.teleportFace != BlockFace.DOWN) //TODO: || to &&
        		{
	        		pitch = startyaw;
	        		yaw = 0;
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
	        		pitch = startyaw + 180;
	        		yaw = 0;
        		}
        		else
        		{
        			pitch = yaw;
        			yaw = startyaw;
        		}
        		outvector = outvector.setY(-momentum);
        		break;
        }
		
		if (!(entity instanceof Player) && momentum < 0.5 && (portal.teleportFace == BlockFace.UP || portal.teleportFace == BlockFace.DOWN) && (destination.teleportFace == BlockFace.UP || destination.teleportFace == BlockFace.DOWN))
		 	return null;
		
		entity.setFallDistance(0);
		entity.setVelocity(entity.getVelocity().zero());
		
		teleport.setPitch(pitch);
		teleport.setYaw(yaw);
		
		if (entity instanceof Arrow)
		{
			teleport.setY(teleport.getY() + 0.5);
			entity.remove();
			teleport.getWorld().spawnArrow(teleport, outvector, (float) (momentum * 1.0f), 12.0f);
		}			
		else if (entity instanceof FallingBlock)
		{
			FallingBlock sand = (FallingBlock)entity;
			sand = teleport.getWorld().spawnFallingBlock(teleport, sand.getBlockId(), sand.getBlockData());
			entity.remove();
		}
		else if (entity instanceof Item)
		{
			Item item = (Item)entity;
			item = teleport.getWorld().dropItem(teleport, item.getItemStack());
			entity.remove();
		}
		else if (entity instanceof Player || entity instanceof Vehicle)
		{
			blockedEntities.add(entity);
			final Location tploc = teleport;
			final Vector outVector = outvector;
			final Entity entity2 = entity;
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				    public void run() {
			        entity2.teleport(tploc);
			        entity2.setVelocity(outVector);
			        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						    public void run() {
					    	blockedEntities.remove(entity2);
					    }
					}, 1L);
			    }
			}, 1L);
		} 
		else
		{
			entity.teleport(teleport);
			entity.setVelocity(outvector);
		}
		destination.disabled = true;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new enablePortal(destination), 10L);
	
		if (portal.orange)
			plugin.util.PlaySound(Sound.PORTAL_EXIT_ORANGE, entity instanceof Player ? (Player) entity : null, new V10Location(teleport));
		else
			plugin.util.PlaySound(Sound.PORTAL_EXIT_BLUE, entity instanceof Player ? (Player) entity : null, new V10Location(teleport));
			return teleport;
	}
	
	@Override
	public void run() {
		for (World w : plugin.getServer().getWorlds())
		{
			String world = w.getName();
			if (plugin.config.DisabledWorlds.contains(world) || processingWorlds.contains(world))
				return;
			EntityManagerHelper emh = new EntityManagerHelper(plugin, world, w.getEntities().iterator());
			emh.setPid(plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, emh, 0L, 1L));
			processingWorlds.add(world);
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
