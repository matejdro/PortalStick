package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class BridgeManager {
	public static HashSet<Bridge> bridges = new HashSet<Bridge>();
	public static HashMap<Portal, Bridge> involvedPortals = new HashMap<Portal, Bridge>();
	public static HashMap<Block, Bridge> bridgeBlocks = new HashMap<Block, Bridge>();
	public static HashMap<Block, Bridge> bridgeMachineBlocks = new HashMap<Block, Bridge>();

	public static Boolean placeGlassBridge(Player player, Block firstIron)
	{
		if (player != null && !Permission.createBridge(player)) return false;
		
		Region region = RegionManager.getRegion(firstIron.getLocation());
		if (!region.getBoolean(RegionSetting.ENABLE_HARD_GLASS_BRIDGES)) return false;
		
		HashSet<Block> machineBlocks = new HashSet<Block>();

		//Check if two blocks are iron
		if (!BlockUtil.compareBlockToString(firstIron, region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL))) return false;
		BlockFace face = null;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
		{
			if (BlockUtil.compareBlockToString(firstIron.getRelative(check).getRelative(check), region.getString(RegionSetting.HARD_GLASS_BRIDGE_BASE_MATERIAL)))
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
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
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
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
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
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
		{
			if (startingBlock.getRelative(check).isEmpty() || startingBlock.getRelative(check).isLiquid())
			{
				face = check;
				machineBlocks.add(secondIron.getRelative(check));
				break;
			}
		}
		if (face == null) return false;
		
		Bridge bridge = new Bridge(firstIron, startingBlock, face, machineBlocks);
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
}
