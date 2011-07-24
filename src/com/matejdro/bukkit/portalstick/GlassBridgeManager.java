package com.matejdro.bukkit.portalstick;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class GlassBridgeManager {
	public static HashMap<Block, GlassBridge> bridges = new HashMap<Block, GlassBridge>();
	public static HashMap<Portal, GlassBridge> involvedPortals = new HashMap<Portal, GlassBridge>();
	public static HashMap<Block, GlassBridge> bridgeBlocks = new HashMap<Block, GlassBridge>();

	public static Boolean placeGlassBridge(Block startingBlock)
	{
		BlockFace face = null;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
		{
			if (startingBlock.getRelative(check).isLiquid() || startingBlock.getRelative(check).isEmpty())
			{
				face = check;
				break;
			}
		}
		
		if (face == null) return false;
		
		GlassBridge bridge = new GlassBridge(startingBlock, face);
		bridge.activate();
		
		bridges.put(startingBlock, bridge);
		return true;
	}
	
	public static void reorientBridge(Portal portal)
	{
		GlassBridge bridge = involvedPortals.get(portal);
		if (bridge != null)
			bridge.activate();
		
		for (GlassBridge cbridge : bridges.values())
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
}
