package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class GlassBridge {
	private HashSet<Block> bridgeBlocks = new HashSet<Block>();
	private HashSet<Portal> involvedPortals = new HashSet<Portal>();
	private Block startBlock;
	private BlockFace facingSide;

	public GlassBridge(Block startingBlock, BlockFace face)
	{
		startBlock = startingBlock;
		facingSide = face;
	}
	
	public void activate()
	{
		deactivate();
		
		BlockFace face = facingSide;
		Block nextBlock = startBlock.getRelative(face);
		while (true)
		{			
			Portal portal = PortalManager.insideBlocks.get(nextBlock.getLocation());
			if (portal == null) portal = PortalManager.borderBlocks.get(nextBlock.getLocation());
			if (portal != null && portal.isOpen())
			{
				nextBlock = portal.getDestination().getTeleportLocation().getBlock();
				if (!portal.isVertical()) nextBlock = nextBlock.getRelative(BlockFace.DOWN);
				face = portal.getDestination().getTeleportFace().getOppositeFace();
				
				involvedPortals.add(portal);
				GlassBridgeManager.involvedPortals.put(portal, this);
				continue;
			}
			else if (!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR) break;
			
			nextBlock.setType(Material.GLASS);
			bridgeBlocks.add(nextBlock);
			GlassBridgeManager.bridgeBlocks.put(nextBlock, this);
			
			nextBlock = nextBlock.getRelative(face);
		}
	}
	
	public void deactivate()
	{
		for (Block b : bridgeBlocks)
			b.setType(Material.AIR);
		
		for (Block b: bridgeBlocks)
			GlassBridgeManager.bridgeBlocks.remove(b);
		bridgeBlocks.clear();
		for (Portal p: involvedPortals)
			GlassBridgeManager.involvedPortals.remove(p);
		involvedPortals.clear();
	}
	
	public Boolean isBlockNextToBridge(Block check)
	{
		for (Block b : bridgeBlocks)
		{
			for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
			{
				if (b.getRelative(face) == check) return true;
			}
		}
		
		return false;
	}
}


