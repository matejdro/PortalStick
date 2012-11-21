package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Pig;
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
	final HashMap<UUID, Location> oldLocations = new HashMap<UUID, Location>();
	private final Random rand = new Random();

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
		Location teleport;
		final Portal destination;
		boolean ab = portal == null;
		if(!ab)
		{
		  if(!portal.open)
			return null;
		  destination = portal.getDestination();
		  if(destination.horizontal || portal.inside[0].equals(locTo))
			teleport = destination.teleport[0].getHandle();
		  else
			teleport = destination.teleport[1].getHandle();
		}
		else
		{
		  if((entity instanceof FallingBlock || entity instanceof TNTPrimed) && vector.getX() == 0.0D && vector.getZ() == 0.0D)
		  {
			portal = plugin.portalManager.awayBlocksY.get(locTo);
			if(!plugin.portalManager.awayBlocksY.containsKey(locTo))
			  return null;
			portal = plugin.portalManager.awayBlocksY.get(locTo);
			if(!portal.open)
			  return null;
			destination = portal.getDestination();
			teleport = destination.teleport[0].getHandle();
		  }
		  else if((Math.abs(vector.getX()) > 0.5 || (Math.abs(vector.getY()) > 1 || (Math.abs(vector.getZ()) > 0.5))) || entity instanceof Boat)
		  {
			if(!plugin.portalManager.awayBlocks.containsKey(locTo))
			  return null;
			portal = plugin.portalManager.awayBlocks.get(locTo);
			
			if(!portal.open)
			  return null;
			
			destination = portal.getDestination();
			if(portal.horizontal || portal.teleport[0].y <= locTo.y)
			  teleport = destination.teleport[0].getHandle();
			else
			  teleport = destination.teleport[1].getHandle();
			
			Block to = locTo.getHandle().getBlock();
			for(int i = 0; i < 2; i++)
			{
			  BlockFace face = portal.awayBlocksY[i].getHandle().getBlock().getFace(to);
			  if(face == null)
				continue;
			  if(face != BlockFace.SELF)
			  {
				double x = 1.0D, z = 1.0D;
				boolean nef = false;
				boolean nwf = false;
				switch(face)
				{
			  	  case NORTH_WEST:
			  		z = 0.5D;
			  	  case NORTH:
			  		x = 0.5D;
			  		nwf = true;
				  break;
			  	  case NORTH_EAST:
			  		x = 1.5D;
			  	  case EAST:
			  		z = 0.5D;
			  		nef = true;
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
				if(nef)
				{
				  if(oloc.getX() - locTo.x > x || oloc.getZ() - locTo.z < z)
					return null;
				}
				else if(nwf)
				{
				  if(oloc.getX() - locTo.x < x || oloc.getZ() - locTo.z > z)
					return null;
				}
				else if(oloc.getX() - locTo.x > x || oloc.getZ() - locTo.z > z)
				  return null; 
			  }
			  else
				break;
			}
		  }
		  else
			return null;
		}
		
		if(portal.disabled || (Math.abs(vector.getY()) > 1 && !portal.horizontal))
		  return null;
		
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
        		if (portal.teleportFace != BlockFace.UP && portal.teleportFace != BlockFace.DOWN)
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
        		if (portal.teleportFace != BlockFace.UP && portal.teleportFace != BlockFace.DOWN)
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
		
		if (!(entity instanceof Player) && !(entity instanceof Chicken) && !(entity instanceof Bat) && (portal.teleportFace == BlockFace.UP || portal.teleportFace == BlockFace.DOWN) && (destination.teleportFace == BlockFace.UP || destination.teleportFace == BlockFace.DOWN) && rand.nextInt(100) < 5)
		{
		  double d = rand.nextDouble();
		  if(d > 0.5D)
			d -= 0.5D;
		  if(ab)
			d += 0.5D;
		  if(rand.nextBoolean())
			d = -d;
		  if(rand.nextBoolean())
			teleport.setX(teleport.getX() + d);
		  else
			teleport.setZ(teleport.getZ() + d);
		}
		
		entity.setFallDistance(0);
		
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
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){destination.disabled = false;}}, 10L);
		
		if (portal.orange)
			plugin.util.playSound(Sound.PORTAL_EXIT_ORANGE, new V10Location(teleport));
		else
			plugin.util.playSound(Sound.PORTAL_EXIT_BLUE, new V10Location(teleport));
		
		return new V10Teleport(teleport, outvector);
	}
	
	@Override
	public void run()
	{
		UUID uuid;
		Location loc, to;
		for (World w : plugin.getServer().getWorlds())
		{
			if(plugin.config.DisabledWorlds.contains(w.getName()))
			  continue;
			for(Entity e: w.getEntities())
			{
				if (e instanceof Player || (e instanceof Vehicle && !(e instanceof Pig)) || e.isDead())
				  continue;
				uuid = e.getUniqueId();
				loc = oldLocations.get(e.getUniqueId());
				to = onEntityMove(e, loc, e.getLocation(), true);
				if(to != null)
				  oldLocations.put(uuid, to);
				else
				  oldLocations.put(uuid, loc);
			}
		}
	}
	
	public Location onEntityMove(final Entity entity, Location locFrom, Location locTo, boolean tp)
	{
		if (entity.isInsideVehicle())
		  return null;
		
		World world = locTo.getWorld();
		if (plugin.config.DisabledWorlds.contains(world.getName()))
		  return null;
		
		double d = locTo.getBlockY();
		if(d > world.getMaxHeight() - 1 || d < 0)
		  return null;
		
		Vector vec2 = locTo.toVector();
		V10Location vlocTo = new V10Location(locTo);
		Location oloc = locTo;
		locTo = vlocTo.getHandle();
		Vector vec1 = locFrom.toVector();
		V10Location vlocFrom = new V10Location(locFrom);
		if(vlocTo.equals(vlocFrom))
		  return null;
		
	    Vector vector = vec2.subtract(vec1);
	    vector.setY(entity.getVelocity().getY());
	    
	    Region regionTo = plugin.regionManager.getRegion(vlocTo);
		Region regionFrom = plugin.regionManager.getRegion(vlocFrom);
		
		//Check for changing regions
	    plugin.portalManager.checkEntityMove(entity, regionFrom, regionTo);
		
		//Emancipation grill
		if (regionTo.getBoolean(RegionSetting.ENABLE_GRILLS))
		{
			Grill grill = plugin.grillManager.insideBlocks.get(vlocTo);
			if (grill != null && !grill.disabled)
			{
				plugin.grillManager.emancipate(entity);
				return null;
			}
		}
		
		//Aerial faith plate
		if (regionTo.getBoolean(RegionSetting.ENABLE_AERIAL_FAITH_PLATES))
		{
			Block blockIn = locTo.getBlock();
			Block blockUnder = blockIn.getRelative(BlockFace.DOWN);
			Block blockStart = null;
			d = Double.parseDouble(regionTo.getString(RegionSetting.FAITH_PLATE_POWER).split("-")[0]);
			String faithBlock = regionTo.getString(RegionSetting.FAITH_PLATE_BLOCK);
			Vector velocity = new Vector(0, Double.parseDouble(regionTo.getString(RegionSetting.FAITH_PLATE_POWER).split("-")[1]),0);
			
			if (blockIn.getType() == Material.STONE_PLATE && plugin.blockUtil.compareBlockToString(blockUnder, faithBlock))
				blockStart = blockUnder;
			else
				blockStart = blockIn;
			if (blockStart != null) {
				BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
				BlockFace face = plugin.blockUtil.getFaceOfMaterial(blockStart, faces, faithBlock);
				if (face != null) {
					switch (face) {
						case NORTH:
							velocity.setX(d);
							break;
						case SOUTH:
							velocity.setX(-d);
							break;
						case EAST:
							velocity.setZ(d);
							break;
						case WEST:
							velocity.setZ(-d);
							break;
					}
					if (blockStart == blockUnder) {
						velocity.setX(-velocity.getX());
						velocity.setZ(-velocity.getZ());
					}
					entity.setVelocity(velocity);
					plugin.util.playSound(Sound.FAITHPLATE_LAUNCH, new V10Location(blockStart.getLocation()));
					return null;
				}
			}
		
		}
		Location ret = null;
		//Teleport
		if (!(entity instanceof Player) || plugin.hasPermission((Player)entity, plugin.PERM_TELEPORT))
		{
		  final V10Teleport to = teleport(entity, oloc, vlocTo, vector, tp);
		  if(to != null)
		  {
			ret = to.to;
			vlocTo = new V10Location(ret);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){entity.setVelocity(to.velocity);}});
		  }
		}
		
		//Gel
		plugin.gelManager.useGel(entity, vlocTo, vector);
		
		//Funnel
		plugin.funnelBridgeManager.EntityMoveCheck(entity);
		
		return ret;
	}
}
