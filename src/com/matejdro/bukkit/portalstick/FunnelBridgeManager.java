package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class FunnelBridgeManager {
	public static HashSet<Bridge> bridges = new HashSet<Bridge>();
	public static HashMap<Portal, Bridge> involvedPortals = new HashMap<Portal, Bridge>();
	public static HashMap<Block, Bridge> bridgeBlocks = new HashMap<Block, Bridge>();
	public static HashMap<Block, Bridge> bridgeMachineBlocks = new HashMap<Block, Bridge>();
	public static HashSet<Entity> inFunnel = new HashSet<Entity>();
	public static HashMap<Entity, List<Block>> glassBlocks = new HashMap<Entity, List<Block>>();
	public static HashMap<Block, Entity> glassBlockOwners = new HashMap<Block, Entity>();

	public static Boolean placeGlassBridge(Player player, Block firstIron)
	{
		if (player != null && !Permission.createBridge(player)) return false;
		
		Region region = RegionManager.getRegion(firstIron.getLocation());
		if (!region.getBoolean(RegionSetting.ENABLE_HARD_GLASS_BRIDGES)) return false;
		
		HashSet<Block> machineBlocks = new HashSet<Block>();

		//Check if two blocks are iron
		if (!BlockUtil.compareBlockToString(firstIron, region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)) && !BlockUtil.compareBlockToString(firstIron, region.getString(RegionSetting.FUNNEL_BASE_MATERIAL))) return false;
		BlockFace face = null;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
		{
			if (BlockUtil.compareBlockToString(firstIron.getRelative(check).getRelative(check), region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)) || BlockUtil.compareBlockToString(firstIron.getRelative(check).getRelative(check), region.getString(RegionSetting.FUNNEL_BASE_MATERIAL)))
			{
				face = check;
				break;
			}
		}
		
		if (face == null) return false;
		
		Block secondIron = firstIron.getRelative(face).getRelative(face);
		Block startingBlock = firstIron.getRelative(face);
		
		machineBlocks.add(firstIron);
		machineBlocks.add(secondIron);
		
		//Check if two irons have redstone torches on them
		Boolean havetorch = false;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP})
		{
			if (firstIron.getRelative(check).getType() == Material.REDSTONE_TORCH_ON)
			{
				havetorch = true;
				machineBlocks.add(firstIron.getRelative(check));
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
				machineBlocks.add(secondIron.getRelative(check));
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
		if (BlockUtil.compareBlockToString(firstIron, region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)))
			bridge = new Bridge(firstIron, startingBlock, face, machineBlocks);
		else
			bridge = new Funnel(firstIron, startingBlock, face, machineBlocks);
		bridge.activate();
		
		for (Block b : machineBlocks)
			bridgeMachineBlocks.put(b, bridge);
		bridges.add(bridge);
		Config.saveAll();
		return true;
	}
	
	public static void reorientBridge(Portal portal)
	{
		Bridge bridge = involvedPortals.get(portal);
		if (bridge != null)
			bridge.activate();
		
		for (Bridge cbridge : bridges)
		{
			for (Block b: portal.getInside())
			{
				if (cbridge.isBlockNextToBridge(b))
					cbridge.activate();
			}
			for (Block b: portal.getBorder())
			{
				if (cbridge.isBlockNextToBridge(b))
					cbridge.activate();
			}
		}
	}
	
	public static void updateBridge(final Block block)
	{
		//delay to make sure all blocks have updated
		PortalStick.instance.getServer().getScheduler().scheduleSyncDelayedTask(PortalStick.instance, new Runnable() {

		    public void run() {
		    	for (Bridge cbridge : bridges)
				{
					if (cbridge.isBlockNextToBridge(block))
						cbridge.activate();
				}
		    }
		}, 1L);
		
	}
	
	public static void loadBridge(String blockloc) {
		String[] locarr = blockloc.split(",");
		String world = locarr[0];
		Block b = PortalStick.instance.getServer().getWorld(world).getBlockAt((int)Double.parseDouble(locarr[1]), (int)Double.parseDouble(locarr[2]), (int)Double.parseDouble(locarr[3]));
		if (!placeGlassBridge(null, b))
			Config.deleteBridge(blockloc);
	}
	
	public static void deleteAll()
	{
		for (Bridge bridge: bridges.toArray(new Bridge[0]))
			bridge.deactivate();
	}
	
	public static Funnel getFunnelInEntity(Entity entity)
	{
		Bridge bridge = FunnelBridgeManager.bridgeBlocks.get(entity.getLocation().getBlock());
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) < 0.5)) bridge = FunnelBridgeManager.bridgeBlocks.get(entity.getLocation().getBlock().getRelative(0,0,-1));
		if (bridge == null && ((entity.getLocation().getZ() - (double) entity.getLocation().getBlockZ()) > 0.5)) bridge = FunnelBridgeManager.bridgeBlocks.get(entity.getLocation().getBlock().getRelative(0,0,1));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) < 0.5)) bridge = FunnelBridgeManager.bridgeBlocks.get(entity.getLocation().getBlock().getRelative(-1,0,0));
		if (bridge == null && ((entity.getLocation().getX() - (double) entity.getLocation().getBlockX()) > 0.5)) bridge = FunnelBridgeManager.bridgeBlocks.get(entity.getLocation().getBlock().getRelative(1,0,0));

		if (bridge == null)
		{
			Location loc = entity.getLocation();
			for (int i = 1; i < 6; i++)
			{
				loc.subtract(0, 1, 0);
				
				bridge = FunnelBridgeManager.bridgeBlocks.get(loc.getBlock());
				if (bridge == null && ((loc.getZ() - (double) loc.getBlockZ()) < 0.5)) bridge = FunnelBridgeManager.bridgeBlocks.get(loc.getBlock().getRelative(0,0,-1));
				if (bridge == null && ((loc.getZ() - (double) loc.getBlockZ()) > 0.5)) bridge = FunnelBridgeManager.bridgeBlocks.get(loc.getBlock().getRelative(0,0,1));
				if (bridge == null && ((loc.getX() - (double) loc.getBlockX()) < 0.5)) bridge = FunnelBridgeManager.bridgeBlocks.get(loc.getBlock().getRelative(-1,0,0));
				if (bridge == null && ((loc.getX() - (double) loc.getBlockX()) > 0.5)) bridge = FunnelBridgeManager.bridgeBlocks.get(loc.getBlock().getRelative(1,0,0));

				if (bridge != null && bridge instanceof Funnel)
				{
					List<Block> list = glassBlocks.get(entity);
					if (list == null)
					{
						glassBlocks.put(entity, new ArrayList<Block>());
						list = glassBlocks.get(entity);
					}
						
					Block block = entity.getLocation().getBlock().getRelative(BlockFace.DOWN, i + 1);
					if (block.isEmpty())
					{
						block.setType(Material.GLASS);
						list.add(block);
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
	
	public static void EntityMoveCheck(Entity entity)
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
	
	private static void EntityEntersFunnel(Entity entity)
	{
		inFunnel.add(entity);
		List<Block> list = glassBlocks.get(entity);
		if (list == null)
			glassBlocks.put(entity, new ArrayList<Block>());
	}
	
	public static void EntityExitsFunnel(Entity entity)
	{
		List<Block> list = glassBlocks.get(entity);
		if (list != null) 
		{
			for (Block b : list)
			{
				b.setType(Material.AIR);
			}
		}
		inFunnel.remove(entity);
	
	}

	private static void EntityMoveInFunnel(Entity entity, Funnel funnel)
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
			
			if (face != BlockFace.UP && face != BlockFace.DOWN && funnel.bridgeBlocks.containsKey(pblock.getRelative(BlockFace.UP)))
			{
				if (pblock.getRelative(face).getType() == Material.AIR) 
				{
						Block block = pblock.getRelative(face);
						BlockUtil.setBlockThreadSafe(block, Material.GLASS);
						glassBlocks.get(entity).add(block);
						glassBlockOwners.put(block, entity);
				}
				else if (pblock.getRelative(face).getType() == Material.GLASS)
				{
					glassBlockOwners.put(pblock.getRelative(face), entity);
				}
								
				if (pblock.getRelative(face, 2).getType() == Material.AIR) 
				{
						Block block = pblock.getRelative(face, 2);
						BlockUtil.setBlockThreadSafe(block, Material.GLASS);
						glassBlocks.get(entity).add(block);
						glassBlockOwners.put(block, entity);
				}
				else if (pblock.getRelative(face, 2).getType() == Material.GLASS)
				{
					glassBlockOwners.put(pblock.getRelative(face, 2), entity);
				}
				for (Block block : glassBlocks.get(entity).toArray(new Block[0]))
				{
					if (block.getLocation().distanceSquared(entity.getLocation()) > 4) 
					{
						if (glassBlockOwners.get(block) == entity)
						{
							BlockUtil.setBlockThreadSafe(block, Material.AIR);
							glassBlocks.get(entity).remove(block);
						}
						if (block.getType() == Material.AIR) glassBlocks.get(entity).remove(block);
						
					}
				}
				

			}
			
		}
	}
}
