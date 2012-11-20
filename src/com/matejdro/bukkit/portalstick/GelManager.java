package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class GelManager {
	private final PortalStick plugin;
	final HashMap<String, Float> onRedGel = new HashMap<String, Float>();
	private final HashSet<Entity> ignore = new HashSet<Entity>();
	
	GelManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public void useGel(Entity entity, V10Location locTo, Vector vector)
	{
		V10Location vl = new V10Location(locTo.world, locTo.x, locTo.y - 1, locTo.z);
		for(V10Location loc: plugin.portalManager.borderBlocks.keySet())
		{
		  if(loc.equals(vl))
		  {
			if(entity instanceof Player) //TODO
			  resetPlayer(((Player)entity));
			return;
		  }
		}
		for(V10Location loc: plugin.portalManager.insideBlocks.keySet())
		{
		  if(loc.equals(vl))
		  {
			if(entity instanceof Player) //TODO
			  resetPlayer(((Player)entity));
			return;
		  }
		}
		Region region = plugin.regionManager.getRegion(locTo);
		Block block = locTo.getHandle().getBlock();
		
		if(region.getBoolean(RegionSetting.ENABLE_RED_GEL_BLOCKS))
		  redGel(entity, block.getRelative(BlockFace.DOWN), region);

		if (region.getBoolean(RegionSetting.ENABLE_BLUE_GEL_BLOCKS))
		{
			if(ignore.contains(entity) || (entity instanceof Player && ((Player)entity).isSneaking()))
			  return;
			String bg = region.getString(RegionSetting.BLUE_GEL_BLOCK);
			for(BlockFace face: new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
			  if(plugin.blockUtil.compareBlockToString(block.getRelative(face), bg))
			  {
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
	
	//TODO: Vertical is still bugged :(
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
	  Player player = (Player)entity;
	  String pn = player.getName();
	  String rg = region.getString(RegionSetting.RED_GEL_BLOCK);
	  if(onRedGel.containsKey(pn))
	  {
		if(!plugin.blockUtil.compareBlockToString(under, rg))
		{
		  resetPlayer(player);
		  return false;
		}
		return true;
	  }
	  
	  if(!plugin.blockUtil.compareBlockToString(under, rg))
		return false;
	  
	  float os = player.getWalkSpeed();
	  player.setWalkSpeed(os * (float)region.getDouble(RegionSetting.RED_GEL_VELOCITY_MULTIPLIER));
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
	}
}
