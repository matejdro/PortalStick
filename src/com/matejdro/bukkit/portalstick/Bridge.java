package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Bridge {
	private HashSet<Block> bridgeBlocks = new HashSet<Block>();
	private HashSet<Portal> involvedPortals = new HashSet<Portal>();
	private HashSet<Block> bridgeMachineBlocks;
	private Block startBlock;
	private Block creationBlock;
	private BlockFace facingSide;

	public Bridge(Block CreationBlock, Block startingBlock, BlockFace face, HashSet<Block> machineBlocks)
	{
		startBlock = startingBlock;
		facingSide = face;
		bridgeMachineBlocks = machineBlocks;
		creationBlock = CreationBlock;
	}
	
	public void activate()
	{
		//deactivate first for cleanup
		deactivate();
		
		BlockFace face = facingSide;
		Block nextBlock = startBlock;
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
				BridgeManager.involvedPortals.put(portal, this);
				continue;
			}
			else if (!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR) break;
			
			if (!nextBlock.getWorld().isChunkLoaded(nextBlock.getChunk())) return;
			
			nextBlock.setType(Material.GLASS);
			bridgeBlocks.add(nextBlock);
			BridgeManager.bridgeBlocks.put(nextBlock, this);
			
			nextBlock = nextBlock.getRelative(face);
		}
	}
	
	public void deactivate()
	{
		for (Block b : bridgeBlocks)
			b.setType(Material.AIR);
		
		for (Block b: bridgeBlocks)
			BridgeManager.bridgeBlocks.remove(b);
		bridgeBlocks.clear();
		for (Portal p: involvedPortals)
			BridgeManager.involvedPortals.remove(p);
		involvedPortals.clear();
	}
	
	public void delete()
	{
		deactivate();
		BridgeManager.bridges.remove(startBlock);
		for (Block b: bridgeMachineBlocks)
			BridgeManager.bridgeMachineBlocks.remove(b);
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
	
	public String getStringLocation()
	{
		Location loc = creationBlock.getLocation();
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}
}


