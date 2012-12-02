package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.BlockHolder;
import de.V10lator.PortalStick.V10Location;

public class Portal {
	private final PortalStick plugin;
	final HashSet<V10Location> border;
	public final V10Location[] inside;
	public final V10Location[] teleport;
	private final V10Location[] behind;
	public final boolean horizontal;
	private final V10Location centerBlock;
	public final User owner;
	public final boolean orange;
	public boolean open = false;
	boolean disabled = false;
	public boolean transmitter = false;
	public final BlockFace teleportFace;
	private final HashSet<V10Location> awayBlocks;
	final V10Location[] awayBlocksY = new V10Location[2];
	private boolean placetorch = false;
	
	public Portal(PortalStick plugin, V10Location[] teleport, V10Location CenterBlock, HashSet<V10Location> Border, V10Location[] inside, V10Location[] behind, User Owner, boolean Orange, boolean horizontal, BlockFace Teleportface)
	{
		this.plugin = plugin;
		this.teleport = teleport;
		border = Border;
		this.inside = inside;
		orange = Orange;
		owner = Owner;
		this.horizontal = horizontal;
		teleportFace = Teleportface;
		this.behind = behind;
		centerBlock = CenterBlock;
		if(horizontal)
		  awayBlocks = new HashSet<V10Location>();
		else
		  awayBlocks = null;
	}
	
	public void delete()
	{
		BlockHolder bh;
		for (V10Location loc: border)
		{
			if (plugin.portalManager.oldBlocks.containsKey(loc))
			{
				bh = plugin.portalManager.oldBlocks.get(loc);
				bh.reset();
				if(plugin.gelManager.gelMap.containsKey(bh))
					plugin.gelManager.removeGel(bh);
				plugin.portalManager.oldBlocks.remove(loc);
			}
			plugin.portalManager.borderBlocks.remove(loc);
		}
		for (V10Location loc: inside)
		{
		  if(loc == null)
			continue;
		  if (plugin.portalManager.oldBlocks.containsKey(loc))
			{
				bh = plugin.portalManager.oldBlocks.get(loc);
				bh.reset();
				if(plugin.gelManager.gelMap.containsKey(bh))
					plugin.gelManager.removeGel(bh);
				plugin.portalManager.oldBlocks.remove(loc);
			}
		  plugin.portalManager.insideBlocks.remove(loc);
		}
		if (plugin.config.FillPortalBack > -1)
		{
			for (V10Location loc: behind)
			{
				if (plugin.portalManager.oldBlocks.containsKey(loc))
				{
					bh = plugin.portalManager.oldBlocks.get(loc);
					bh.reset();
					if(plugin.gelManager.gelMap.containsKey(bh))
						plugin.gelManager.removeGel(bh);
					plugin.portalManager.oldBlocks.remove(loc);
				}
				plugin.portalManager.behindBlocks.remove(loc);
			}
		}
		if(horizontal)
		{
		  for(V10Location l: awayBlocks)
			plugin.portalManager.awayBlocks.remove(l);
		  plugin.portalManager.awayBlocksY.remove(awayBlocksY[0]);
		  plugin.portalManager.awayBlocksY.remove(awayBlocksY[1]);
		}
		
		Portal oldDestination = getDestination();
		
		if (orange)
			owner.orangePortal = null;
		else
			owner.bluePortal = null;
			
		open = false;
				
		plugin.portalManager.portals.remove(this);
		plugin.regionManager.getRegion(centerBlock).portals.remove(this);	
		
    	plugin.regionManager.getRegion(centerBlock).portalDeleted(this, oldDestination);
		if (oldDestination != null && oldDestination.getDestination() == null) oldDestination.close();

   	}
	
	public void open()
	{
		Region region = plugin.regionManager.getRegion(inside[0]);
		
		Block b;
//		BlockHolder bh;
		for (V10Location loc: inside)
    	{
		  if(loc == null)
			continue;
			b = loc.getHandle().getBlock();
//			bh = new BlockHolder(b);
//			if(plugin.gelManager.gelMap.containsKey(bh))
//			  plugin.gelManager.removeGel(bh);
			b.setType(Material.AIR); 
			
			if (region.getBoolean(RegionSetting.ENABLE_REDSTONE_TRANSFER))
			 {			 				 
				 for (int i = 0; i < 4; i++)
				 {
					 BlockFace face = BlockFace.values()[i];
					 if (b.getRelative(face).getBlockPower() > 0) 
						 {						 
						 	Portal destination = getDestination();
						 	if (destination == null || destination.transmitter) continue;
						 
						 		transmitter = true;
						 		if (destination.open)
							 		for (V10Location b2: destination.inside)
							 		  if(b2 != null)
							 			b2.getHandle().getBlock().setType(Material.REDSTONE_TORCH_ON);
						 		else
						 			destination.placetorch = true;
						 }
				 }
			 }

    	}
		
		if (placetorch)
		{
			inside[0].getHandle().getBlock().setType(Material.REDSTONE_TORCH_ON);
			placetorch = false;
		}
		
		open = true;
		plugin.funnelBridgeManager.reorientBridge(this);
	}
	
	public void close()
	{
		byte color;
		if (orange)
			color = (byte) plugin.util.getRightPortalColor(owner.colorPreset);
		else
			color = (byte) plugin.util.getLeftPortalColor(owner.colorPreset);
		int w = Material.WOOL.getId();
		for (V10Location b: inside)
    	{
		  if(b != null)
		  {
    		b.getHandle().getBlock().setTypeIdAndData(w, color, true);
    		open = false;
		  }
    	}
		
		plugin.funnelBridgeManager.reorientBridge(this);
	}
	
	public void recreate()
	{
		byte color;
		if (orange)
			color = (byte) plugin.util.getRightPortalColor(owner.colorPreset);
		else
			color = (byte) plugin.util.getLeftPortalColor(owner.colorPreset);			
		
		for (V10Location b: border)
    		b.getHandle().getBlock().setData(color);

		if (!open)
			for (V10Location b: inside)
			  if(b != null)
	    		b.getHandle().getBlock().setData(color);
		
		if (plugin.config.CompactPortal)
			for (V10Location b: behind)
	    		b.getHandle().getBlock().setData(color);
	}
	
	public void create()
	{
		byte color;
		if (orange)
			color = (byte) plugin.util.getRightPortalColor(owner.colorPreset);
		else
			color = (byte) plugin.util.getLeftPortalColor(owner.colorPreset);			

		Block rb;
		BlockHolder bh;
    	for (V10Location loc: border)
    	{
    		if (plugin.portalManager.insideBlocks.containsKey(loc))
    			plugin.portalManager.insideBlocks.get(loc).delete();
    		if (plugin.portalManager.behindBlocks.containsKey(loc))
    			plugin.portalManager.behindBlocks.get(loc).delete();
    		
    		rb = loc.getHandle().getBlock();
    		bh = new BlockHolder(rb);
    		if(plugin.gelManager.gelMap.containsKey(bh))
    		{
    		  bh = plugin.gelManager.gelMap.get(bh);
    		  plugin.gelManager.removeGel(bh);
    		}
    		plugin.portalManager.oldBlocks.put(loc, bh);
    		rb.setType(Material.WOOL);
    		rb.setData(color);
    		plugin.portalManager.borderBlocks.put(loc, this);
       	}
    	for (V10Location loc: inside)
    	{
    	  if(loc != null)
    	  {
    		rb = loc.getHandle().getBlock();
    		bh = new BlockHolder(rb);
    		if(plugin.gelManager.gelMap.containsKey(bh))
    		{
      		  bh = plugin.gelManager.gelMap.get(bh);
      		  plugin.gelManager.removeGel(bh);
    		}
    		plugin.portalManager.oldBlocks.put(loc, bh);
    	  }
    	}
    	if (plugin.config.FillPortalBack > -1)
    	{
    		for (V10Location loc: behind)
        	{
        		if (plugin.portalManager.borderBlocks.containsKey(loc))
        			plugin.portalManager.borderBlocks.get(loc).delete();
        		if (plugin.portalManager.insideBlocks.containsKey(loc))
        			plugin.portalManager.insideBlocks.get(loc).delete();

        		rb = loc.getHandle().getBlock();
        		bh = new BlockHolder(rb);
        		if(plugin.gelManager.gelMap.containsKey(bh))
        		{
          		  bh = plugin.gelManager.gelMap.get(bh);
          		  plugin.gelManager.removeGel(bh);
        		}
        		plugin.portalManager.oldBlocks.put(loc, bh);
        		if (plugin.config.CompactPortal)
        		{
        			rb.setType(Material.WOOL);
            		rb.setData(color);
        		}
        		else
        		{
        			rb.setTypeId(plugin.config.FillPortalBack);
        		}
        		plugin.portalManager.behindBlocks.put(loc, this);
        	}
    	}
    	
    	if (getDestination() == null)
    	{
    		close();
    	}
    		
    	else
    	{
    		open();
    		getDestination().open();
    	}
    	
    	
    	V10Location oloc;
    	V10Location loc;
    	int i;
    	oloc = inside[0].clone();
    	plugin.portalManager.insideBlocks.put(inside[0], this);
    	if(inside[1] != null)
    	  plugin.portalManager.insideBlocks.put(inside[1], this);
    	
    	if(horizontal)
    	{
    	  for (int y = -1;y<2;y++)
    	  {
    		if(y != 0)
    		{
    		  loc = new V10Location(oloc.world, oloc.x, oloc.y + y, oloc.z);
    		  plugin.portalManager.awayBlocksY.put(loc, this);
    		  if(y < 1)
    			i = 0;
    		  else
    			i = 1;
    		  awayBlocksY[i] = loc;
    		}
    		for (int x = -1;x<2;x++)
    		{
    		  for (int z = -1;z<2;z++)
    		  {
    			loc = new V10Location(oloc.world, oloc.x + x, oloc.y + y, oloc.z + z);
    			plugin.portalManager.awayBlocks.put(loc, this);
    			awayBlocks.add(loc);
    		  }
    		}
    	  }
    	}
    	
    	plugin.regionManager.getRegion(centerBlock).portalCreated(this);
	}
	
	public Portal getDestination()
	{
		Region region = plugin.regionManager.getRegion(centerBlock);
		
		if (orange)
		{
			if (owner.bluePortal != null) 
				return owner.bluePortal;
			else if (!isRegionPortal())
				return region.bluePortal;
			else
				return region.orangeDestination;
		}
		else
		{
			if (owner.orangePortal != null) 
				return owner.orangePortal;
			else if (!isRegionPortal())
				return region.orangePortal;
			else
				return region.blueDestination;

		}
	}
	
	public boolean isRegionPortal()
	{
		return owner.name.startsWith("§region§_");
	}
}
