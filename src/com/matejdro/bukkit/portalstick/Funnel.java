package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

import de.V10lator.PortalStick.V10Location;

public class Funnel extends Bridge {
	private boolean reversed = false;
	
	Funnel(PortalStick plugin, V10Location CreationBlock, V10Location startingBlock, BlockFace face, HashSet<V10Location> machineBlocks) {
		super(plugin, CreationBlock, startingBlock, face, machineBlocks);
	}
	
	public void setReverse(boolean value)
	{
		reversed = value;
		activate();
	}
	
	public BlockFace getDirection(Block block)
	{
		V10Location vb = new V10Location(block);
		if (!bridgeBlocks.containsKey(vb)) return null;
		
		int curnum = bridgeBlocks.get(vb);
		BlockFace face = null;
		for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN})
		{
			vb = new V10Location(block.getRelative(check));
			if (bridgeBlocks.containsKey(vb) && (curnum - bridgeBlocks.get(vb) == 1 || bridgeBlocks.get(vb) > curnum + 1) )			{
				face = check;
				break;
			}
		}
		if (face == null) return null;
		
		if (reversed) face = face.getOppositeFace();
		
		return face;
	}
	
	public BlockFace getDirection(Entity entity)
	{
		Block eb = entity.getLocation().getBlock();
		BlockFace face = getDirection(eb);
		if (face == null)
		{
			for (BlockFace check : BlockFace.values())
			{
				face = getDirection(eb.getRelative(check));
				if (face != null) break;
			}
		}
		
		return face;
	}
	
	public int getCounter(V10Location block)
	{
		return bridgeBlocks.get(block);
	}
	
	@Override
	public void activate()
	{
		//deactivate first for cleanup
		deactivate();
		
		BlockFace face = facingSide;
		V10Location nextV10Location = startBlock;
		Block nextBlock = nextV10Location.getHandle().getBlock();
		int counter = reversed ? 1 : 8;
		while (true)
		{
			Portal portal = null;
			if(plugin.portalManager.insideBlocks.containsKey(nextV10Location))
			{
			  portal = plugin.portalManager.insideBlocks.get(nextV10Location);
			  if(portal.open)
			  {
				Portal destP = portal.getDestination();
				if(destP.horizontal ||portal.inside[0].equals(nextV10Location))
				  nextV10Location = destP.teleport[0];
				else
				  nextV10Location = destP.teleport[1];
			  }
			  else
				return;
			}
			else if(plugin.portalManager.borderBlocks.containsKey(nextV10Location))
			{
			  portal = plugin.portalManager.borderBlocks.get(nextV10Location);
			  if(portal.open)
				nextV10Location = new V10Location(portal.getDestination().teleport[0].getHandle().getBlock().getRelative(BlockFace.DOWN));
			  else
				return;
			}
			
			if(portal != null && portal.open)
			{
			  nextBlock = nextV10Location.getHandle().getBlock();
			  
			  face = portal.getDestination().teleportFace.getOppositeFace();
			  
			  involvedPortals.add(portal);
			  plugin.funnelBridgeManager.involvedPortals.put(portal, this);
			  continue;
			}
			else if (nextBlock.getY() > nextBlock.getWorld().getMaxHeight() - 1 || nextBlock.getY() < 1 || (!nextBlock.isLiquid() && nextBlock.getType() != Material.AIR))
			  break;
			
			if (!nextBlock.getWorld().isChunkLoaded(nextBlock.getChunk())) return;
			
			if (counter < 0) counter = 8;
			if (counter > 0)
			{
				nextBlock.setType(Material.WATER);
				byte data;
				if(reversed)
				  data = (byte)(counter - 1);
				else
				  data = (byte)(8 - counter);
				if (face != BlockFace.UP && face != BlockFace.DOWN) nextBlock.setData(data);
			}
			counter--;
				
			bridgeBlocks.put(nextV10Location, counter);
			plugin.funnelBridgeManager.bridgeBlocks.put(nextV10Location, this);
			
			nextBlock = nextBlock.getRelative(face);
			nextV10Location = new V10Location(nextBlock);
		}
	}
	
	@Override
	public void deactivate()
	{
		for (V10Location b : bridgeBlocks.keySet())
		{
			b.getHandle().getBlock().setType(Material.AIR);
			plugin.funnelBridgeManager.bridgeBlocks.remove(b);
		}
		bridgeBlocks.clear();
		for (Portal p: involvedPortals)
			plugin.funnelBridgeManager.involvedPortals.remove(p);
//		for (Entity e : plugin.funnelBridgeManager.glassBlocks.keySet())
//			plugin.funnelBridgeManager.EntityExitsFunnel(e);
		
		involvedPortals.clear();
	}
}
