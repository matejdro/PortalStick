package com.matejdro.bukkit.portalstick;

import java.util.HashSet;
import java.util.LinkedHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.matejdro.bukkit.portalstick.util.Util;

public class Bridge {
	protected LinkedHashMap<Block, Integer> bridgeBlocks = new LinkedHashMap<Block, Integer>();
	protected HashSet<Portal> involvedPortals = new HashSet<Portal>();
	protected HashSet<Block> bridgeMachineBlocks;
	protected Block startBlock;
	protected Block creationBlock;
	protected BlockFace facingSide;

	public Bridge(Block CreationBlock, Block startingBlock, BlockFace face, HashSet<Block> machineBlocks)
	{
		startBlock = startingBlock;
		facingSide = face;
		bridgeMachineBlocks = machineBlocks;
		creationBlock = CreationBlock;
	}
	
	public Block getCreationBlock()
	{
		return creationBlock;
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
				face = portal.getDestination().getTeleportFace().getOppositeFace();
				
				involvedPortals.add(portal);
				FunnelBridgeManager.involvedPortals.put(portal, this);
				continue;
			}
			else if (nextBlock.getY() > 127 || (!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR)) break;			
			if (!nextBlock.getWorld().isChunkLoaded(nextBlock.getChunk())) return;
			
			nextBlock.setType(Material.GLASS);
			bridgeBlocks.put(nextBlock, 0);
			FunnelBridgeManager.bridgeBlocks.put(nextBlock, this);
			
			nextBlock = nextBlock.getRelative(face);
		}
	}
	
	public void deactivate()
	{
		for (Block b : bridgeBlocks.keySet())
			b.setType(Material.AIR);
		
		for (Block b: bridgeBlocks.keySet())
			FunnelBridgeManager.bridgeBlocks.remove(b);
		bridgeBlocks.clear();
		for (Portal p: involvedPortals)
			FunnelBridgeManager.involvedPortals.remove(p);
		involvedPortals.clear();
	}
	
	public void delete()
	{
		deactivate();
		Util.info(String.valueOf(FunnelBridgeManager.bridges.remove(this)));
		for (Block b: bridgeMachineBlocks)
			FunnelBridgeManager.bridgeMachineBlocks.remove(b);
	}
	
	public Boolean isBlockNextToBridge(Block check)
	{
		for (Block b : bridgeBlocks.keySet())
		{
			for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN})
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


