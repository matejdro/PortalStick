package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.BlockHolder;
import de.V10lator.PortalStick.V10Location;

public class GelManager {
	private final PortalStick plugin;
	final HashMap<String, Float> onRedGel = new HashMap<String, Float>();
	private final HashSet<Entity> ignore = new HashSet<Entity>();
	final HashMap<String, Integer> redTasks = new HashMap<String, Integer>();
	public final HashMap<V10Location, Integer> tubePids = new HashMap<V10Location, Integer>();
	public final HashSet<V10Location> activeGelTubes = new HashSet<V10Location>();
	public final HashMap<UUID, V10Location> flyingGels = new HashMap<UUID, V10Location>();
	public final HashMap<V10Location, ArrayList<BlockHolder>> gels = new HashMap<V10Location, ArrayList<BlockHolder>>();
	public final HashMap<BlockHolder, BlockHolder> gelMap = new HashMap<BlockHolder, BlockHolder>();
	
	GelManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public void useGel(Entity entity, V10Location locTo, Vector vector)
	{
		Region region = plugin.regionManager.getRegion(locTo);
		Block block = locTo.getHandle().getBlock();
		
		if(region.getBoolean(RegionSetting.ENABLE_RED_GEL_BLOCKS))
		  redGel(entity, block.getRelative(BlockFace.DOWN), region);

		if (region.getBoolean(RegionSetting.ENABLE_BLUE_GEL_BLOCKS))
		{
			if(ignore.contains(entity) || (entity instanceof Player && ((Player)entity).isSneaking()))
			  return;
			String bg = region.getString(RegionSetting.BLUE_GEL_BLOCK);
			Block block2;
			for(BlockFace face: new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
			{
			  block2 = block.getRelative(face);
			  if(plugin.blockUtil.compareBlockToString(block2, bg))
			  {
				if(isPortal(new V10Location(block2)))
				  continue;
				byte dir;
				switch(face)
				{
				  case DOWN:
					dir = 0;
				    break;
				  case NORTH:
				  case SOUTH:
					dir = 1;
					break;
				  default:
					dir = 2;
				}
				blueGel(entity, region, dir, vector);
				break;
			  }
			}
		}
	}
	
	private boolean isPortal(V10Location vl)
	{
	  for(V10Location loc: plugin.portalManager.borderBlocks.keySet())
		if(loc.equals(vl))
		  return true;
	  for(V10Location loc: plugin.portalManager.insideBlocks.keySet())
		if(loc.equals(vl))
		  return true;
	  return false;
	}
	
	private void blueGel(final Entity entity, Region region, byte dir, Vector vector)
	{
//		Vector vector = player.getVelocity(); //We need a self-calculated vector from the player move event as this has 0.0 everywhere.
//		vector.multiply(region.getDouble(RegionSetting.BLUE_GEL_VELOCITY_MULTIPLIER));
		
		switch(dir)
		{
		  case 0:
			double y = vector.getY();
		  	if(y >= 0)
			  return;
			y = -y;
			vector.setY(y);
			break;
		  case 1:
			vector.setX(-vector.getX());
		  	break;
		  default:
			vector.setZ(-vector.getZ());
		}
		
		entity.setVelocity(vector);
		
		plugin.util.playSound(Sound.GEL_BLUE_BOUNCE, new V10Location(entity.getLocation()));
		
		ignore.add(entity);
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { public void run() { ignore.remove(entity); }}, 10L);
	}
	
	private boolean redGel(Entity entity, Block under, Region region)
	{
	  if(!(entity instanceof Player)) // TODO
		return false;
	  
	  final Player player = (Player)entity;
	  if(isPortal(new V10Location(under)))
	  {
		resetPlayer(player);
		return false;
	  }
	  
	  final String pn = player.getName();
	  String rg = region.getString(RegionSetting.RED_GEL_BLOCK);
	  
	  if(!plugin.blockUtil.compareBlockToString(under, rg))
		return false;
	  
	  BukkitScheduler s = plugin.getServer().getScheduler();
	  if(redTasks.containsKey(pn))
		s.cancelTask(redTasks.get(pn));
	  redTasks.put(pn, s.scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){resetPlayer(player);}} , 10L));
	  
	  float os = player.getWalkSpeed();
	  float ns = os * (float)region.getDouble(RegionSetting.RED_GEL_VELOCITY_MULTIPLIER);
	  if(ns > (float)region.getDouble(RegionSetting.RED_GEL_MAX_VELOCITY))
		return true;
	  player.setWalkSpeed(ns);
	  if(!onRedGel.containsKey(pn))
		onRedGel.put(pn, os);
	  return true;
	}
	
	public void resetPlayer(Player player)
	{
	  String pn = player.getName();
	  if(!onRedGel.containsKey(pn))
		return;
	  player.setWalkSpeed(onRedGel.get(pn));
	  onRedGel.remove(pn);
	  redTasks.remove(pn);
	}
	
	public void stopGelTube(V10Location loc)
	{
	  if(!tubePids.containsKey(loc))
		return;
	  plugin.getServer().getScheduler().cancelTask(tubePids.get(loc));
	  tubePids.remove(loc);
	  activeGelTubes.remove(loc);
	  ArrayList<BlockHolder> tc = new ArrayList<BlockHolder>();
	  if(gels.containsKey(loc))
	  {
		for(BlockHolder bh: gels.get(loc))
		{
		  bh.reset();
		  gelMap.remove(bh);
		  tc.add(bh);
		}
		gels.remove(loc);
		for(ArrayList<BlockHolder> blocks: gels.values())
		  for(BlockHolder bh: tc)
			blocks.remove(bh);
	  }
	  World world = plugin.getServer().getWorld(loc.world);
	  UUID uuid;
	  for(Chunk c: world.getLoadedChunks())
		for(Entity e: c.getEntities())
		{
		  uuid = e.getUniqueId();
		  if(flyingGels.containsKey(uuid))
		  {
			e.remove();
			flyingGels.remove(uuid);
		  }
		}
	}
}
