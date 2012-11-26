package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class FunnelBridgeManager {
	private final PortalStick plugin;
	
	FunnelBridgeManager(PortalStick plugin)
	{
	  this.plugin = plugin;
	}
	
	public HashSet<Bridge> bridges = new HashSet<Bridge>();
	public HashMap<Portal, Bridge> involvedPortals = new HashMap<Portal, Bridge>();
	public HashMap<V10Location, Bridge> bridgeBlocks = new HashMap<V10Location, Bridge>();
	public HashMap<V10Location, Bridge> bridgeMachineBlocks = new HashMap<V10Location, Bridge>();
//	private HashSet<Entity> inFunnel = new HashSet<Entity>();
	HashMap<Entity, List<V10Location>> glassBlocks = new HashMap<Entity, List<V10Location>>();
//	private HashMap<V10Location, Entity> glassBlockOwners = new HashMap<V10Location, Entity>();

	public boolean placeGlassBridge(Player player, V10Location first)
	{
		if (player != null && !plugin.hasPermission(player, plugin.PERM_CREATE_BRIDGE))
		  return false;
		
		Region region = plugin.regionManager.getRegion(first);
		if (!region.getBoolean(RegionSetting.ENABLE_HARD_GLASS_BRIDGES))
		  return false;
		
		HashSet<V10Location> machineBlocks = new HashSet<V10Location>();

		//Check if two blocks are iron
		if (!plugin.blockUtil.compareBlockToString(first, region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)) && !plugin.blockUtil.compareBlockToString(first, region.getString(RegionSetting.FUNNEL_BASE_MATERIAL))) return false;
		BlockFace face = null;
		Block firstIron = first.getHandle().getBlock();
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
		{
			if (plugin.blockUtil.compareBlockToString(firstIron.getRelative(check).getRelative(check), region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)) || plugin.blockUtil.compareBlockToString(firstIron.getRelative(check).getRelative(check), region.getString(RegionSetting.FUNNEL_BASE_MATERIAL)))
			{
				face = check;
				break;
			}
		}
		
		if (face == null) return false;
		
		Block startingBlock = firstIron.getRelative(face);
		Block secondIron = startingBlock.getRelative(face);
		
		machineBlocks.add(new V10Location(firstIron));
		machineBlocks.add(new V10Location(secondIron));
		
		//Check if two irons have redstone torches on them
		Boolean havetorch = false;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP})
		{
			if (firstIron.getRelative(check).getType() == Material.REDSTONE_TORCH_ON)
			{
				havetorch = true;
				machineBlocks.add(new V10Location(firstIron.getRelative(check)));
				break;
			}
		}
		if (!havetorch) return false;
		havetorch = false;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP})
		{
			if (secondIron.getRelative(check).getType() == Material.REDSTONE_TORCH_ON)
			{
				havetorch = true;
				machineBlocks.add(new V10Location(secondIron.getRelative(check)));
				break;
			}
		}
		if (!havetorch) return false;
		
		//Which way should we create bridge to
		face = null;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP})
		{
			if (startingBlock.getRelative(check).isEmpty() || startingBlock.getRelative(check).isLiquid())
			{
				face = check;
				break;
			}
		}
		if (face == null) return false;
		
		Bridge bridge;
		first = new V10Location(firstIron);
		if (plugin.blockUtil.compareBlockToString(firstIron, region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)))
			bridge = new Bridge(plugin, first, new V10Location(startingBlock), face, machineBlocks);
		else
			bridge = new Funnel(plugin, first, new V10Location(startingBlock), face, machineBlocks);
		bridge.activate();
		
		for (V10Location b: machineBlocks)
			bridgeMachineBlocks.put(b, bridge);
		bridges.add(bridge);
		plugin.config.saveAll();
		return true;
	}
	
	public void reorientBridge(Portal portal)
	{
		Bridge bridge = involvedPortals.get(portal);
		if (bridge != null)
			bridge.activate();
		
		for (Bridge cbridge : bridges)
		{
			for (V10Location b: portal.inside)
			{
			  if(b != null && cbridge.isBlockNextToBridge(b))
				cbridge.activate();
			}
			for (V10Location b: portal.border)
			{
				if (cbridge.isBlockNextToBridge(b))
					cbridge.activate();
			}
		}
	}
	
	public void updateBridge(final V10Location block)
	{
		//delay to make sure all blocks have updated
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

		    public void run() {
		    	for (Bridge cbridge : bridges)
				{
					if (cbridge.isBlockNextToBridge(block))
						cbridge.activate();
				}
		    }
		}, 1L);
		
	}
	
	public void loadBridge(String blockloc) {
		String[] locarr = blockloc.split(",");
		if (!placeGlassBridge(null, new V10Location(plugin.getServer().getWorld(locarr[0]).getBlockAt((int)Double.parseDouble(locarr[1]), (int)Double.parseDouble(locarr[2]), (int)Double.parseDouble(locarr[3])))))
			plugin.config.deleteBridge(blockloc);
	}
	
	public void deleteAll()
	{
		for (Bridge bridge: bridges.toArray(new Bridge[0]))
			bridge.deactivate();
	}
	
/*	public Funnel getFunnelInEntity(Entity entity)
	{
		Bridge bridge = bridgeBlocks.get(new V10Location(entity.getLocation()));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) < 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(0,0,-1)));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) > 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(0,0,1)));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) < 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(-1,0,0)));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) > 0.5)) bridge = bridgeBlocks.get(new V10Location(entity.getLocation().getBlock().getRelative(1,0,0)));

		if (bridge == null)
		{
			Location loc = entity.getLocation();
			for (int i = 1; i < 6; i++)
			{
				loc.subtract(0, 1, 0);
				
				bridge = bridgeBlocks.get(loc.getBlock());
				if (bridge == null && ((loc.getZ() - (double) loc.getBlockZ()) < 0.5)) bridge = bridgeBlocks.get(new V10Location(loc.getBlock().getRelative(0,0,-1)));
				if (bridge == null && ((loc.getZ() - (double) loc.getBlockZ()) > 0.5)) bridge = bridgeBlocks.get(new V10Location(loc.getBlock().getRelative(0,0,1)));
				if (bridge == null && ((loc.getX() - (double) loc.getBlockX()) < 0.5)) bridge = bridgeBlocks.get(new V10Location(loc.getBlock().getRelative(-1,0,0)));
				if (bridge == null && ((loc.getX() - (double) loc.getBlockX()) > 0.5)) bridge = bridgeBlocks.get(new V10Location(loc.getBlock().getRelative(1,0,0)));

				if (bridge != null && bridge instanceof Funnel)
				{
					List<V10Location> list = glassBlocks.get(entity);
					if (list == null)
					{
						list = new ArrayList<V10Location>();
						glassBlocks.put(entity, list);
					}
						
					Block block = entity.getLocation().getBlock().getRelative(BlockFace.DOWN, i + 1);
					if (block.isEmpty())
					{
						block.setType(Material.GLASS);
						list.add(new V10Location(block));
					}
					
					break;

				}
			}
		}
		
		if (bridge != null && bridge instanceof Funnel)
			return (Funnel) bridge;
		else
			return null;
	}
	
	public void EntityMoveCheck(Entity entity)
	{
		Funnel funnel = getFunnelInEntity(entity);
		if (funnel == null && inFunnel.contains(entity))
		{
			EntityExitsFunnel(entity);
		}
		else if (funnel != null)
		{
			if (!inFunnel.contains(entity)) EntityEntersFunnel(entity);
			EntityMoveInFunnel(entity, funnel);
		}
	}
	
	private void EntityEntersFunnel(Entity entity)
	{
		inFunnel.add(entity);
		List<V10Location> list = glassBlocks.get(entity);
		if (list == null)
			glassBlocks.put(entity, new ArrayList<V10Location>());
	}
	
	public void EntityExitsFunnel(Entity entity)
	{
		List<V10Location> list = glassBlocks.get(entity);
		if (list != null) 
			for (V10Location b : list)
				b.getHandle().getBlock().setType(Material.AIR);
		inFunnel.remove(entity);
	
	}

	private void EntityMoveInFunnel(Entity entity, Funnel funnel)
	{
		BlockFace face = funnel.getDirection(entity);
		if (face == null) return;
				
		if (face == BlockFace.UP)
			entity.setVelocity(entity.getVelocity().setY(0.2));
		else if (face == BlockFace.DOWN)
			entity.setVelocity(entity.getVelocity().setY(-0.2));
		else
		{
			if (face.getModX() != 0) entity.setVelocity(entity.getVelocity().setX(((double)face.getModX()) * 0.2));
			if (face.getModZ() != 0) entity.setVelocity(entity.getVelocity().setZ(((double)face.getModZ()) * 0.2));
			
			//Generate glass
			
			Block pblock = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
			
			if (face != BlockFace.UP && face != BlockFace.DOWN && funnel.bridgeBlocks.containsKey(new V10Location(pblock.getRelative(BlockFace.UP))))
			{
				if (pblock.getRelative(face).getType() == Material.AIR) 
				{
						Block block = pblock.getRelative(face);
						block.setType(Material.GLASS);
						V10Location loc = new V10Location(block);
						glassBlocks.get(entity).add(loc);
						glassBlockOwners.put(loc, entity);
				}
				else if (pblock.getRelative(face).getType() == Material.GLASS)
				{
					glassBlockOwners.put(new V10Location(pblock.getRelative(face)), entity);
				}
								
				if (pblock.getRelative(face, 2).getType() == Material.AIR) 
				{
						Block block = pblock.getRelative(face, 2);
						block.setType(Material.GLASS);
						V10Location loc = new V10Location(block);
						glassBlocks.get(entity).add(loc);
						glassBlockOwners.put(loc, entity);
				}
				else if (pblock.getRelative(face, 2).getType() == Material.GLASS)
				{
					glassBlockOwners.put(new V10Location(pblock.getRelative(face, 2)), entity);
				}
				Block block;
				for (V10Location loc : glassBlocks.get(entity).toArray(new V10Location[0]))
				{
					if (loc.getHandle().distanceSquared(entity.getLocation()) > 4) 
					{
						block = loc.getHandle().getBlock();
						if (glassBlockOwners.get(block) == entity)
						{
							block.setType(Material.AIR);
							glassBlocks.get(entity).remove(block);
						}
						if (block.getType() == Material.AIR) glassBlocks.get(entity).remove(block);
						
					}
				}
			}
		}
	}*/
}
