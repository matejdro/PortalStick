package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class Portal {
	private final PortalStick plugin;
	public final V10Location teleport;
	final HashSet<V10Location> border;
	public final HashSet<V10Location> inside;
	private final HashSet<V10Location> behind;
	final boolean vertical;
	private final V10Location centerBlock;
	public final User owner;
	public final boolean orange;
	public boolean open = false;
	boolean disabled = false;
	public boolean transmitter = false;
	final BlockFace teleportFace;
	private final HashSet<V10Location> awayBlocks = new HashSet<V10Location>();
	private final HashMap<V10Location, String> oldBlocks = new HashMap<V10Location, String>();
	private boolean placetorch = false;
	
	public Portal(PortalStick plugin, V10Location Teleport, V10Location CenterBlock, HashSet<V10Location> Border, HashSet<V10Location> Inside, HashSet<V10Location> Behind, User Owner, boolean Orange, boolean Vertical, BlockFace Teleportface)
	{
		this.plugin = plugin;
		teleport = Teleport;
		border = Border;
		inside = Inside;
		orange = Orange;
		owner = Owner;
		vertical = Vertical;
		teleportFace = Teleportface;
		behind = Behind;
		centerBlock = CenterBlock;
	}
	
	public void delete()
	{
		if (owner != null) { //TODO: What was the null check for?
			for (V10Location loc: border)
			{
				if (oldBlocks.containsKey(loc))
					plugin.blockUtil.setBlockData(loc, oldBlocks.get(loc));
				plugin.portalManager.borderBlocks.remove(loc);
			}
			for (V10Location loc: inside)
			{
				if (oldBlocks.containsKey(loc))
					plugin.blockUtil.setBlockData(loc, oldBlocks.get(loc));
				plugin.portalManager.insideBlocks.remove(loc);
			}
			if (plugin.config.FillPortalBack > -1)
			{
				for (V10Location loc: behind)
				{
					if (oldBlocks.containsKey(loc))
						plugin.blockUtil.setBlockData(loc, oldBlocks.get(loc));
					plugin.portalManager.behindBlocks.remove(loc);
				}
			}
			for (V10Location l : awayBlocks)
			{
				plugin.portalManager.awayBlocksGeneral.remove(l);
				plugin.portalManager.awayBlocksX.remove(l);
				plugin.portalManager.awayBlocksY.remove(l);
				plugin.portalManager.awayBlocksZ.remove(l);
			}
			
			
			if (orange)
				owner.orangePortal = null;
			else
				owner.bluePortal = null;
			
			open = false;
			
			plugin.portalManager.portals.remove(this);
			plugin.regionManager.getRegion(centerBlock).portals.remove(this);
						
	    	if (getDestination() != null && getDestination().open)
	    	{
	    		if (getDestination().isRegionPortal())
	    		{
	    			V10Location loc = getDestination().centerBlock;
	    			plugin.regionManager.getRegion(loc).regionPortalClosed(orange);
	    		}
	    		else
	    			getDestination().close();
	    	}
	    		    	
    		if (isRegionPortal())
    			plugin.regionManager.getRegion(centerBlock).regionPortalDeleted(this);
		}				
	}
	
	public void open()
	{
		Region region = plugin.regionManager.getRegion((inside.toArray(new V10Location[0])[0]));
		
		Block b;
		for (V10Location loc: inside)
    	{
			b = loc.getHandle().getBlock();
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
							 			b2.getHandle().getBlock().setType(Material.REDSTONE_TORCH_ON);
						 		else
						 			destination.placetorch = true;
						 }
				 }
			 }

    	}
		
		if (placetorch)
		{
			(inside.toArray(new V10Location[0])[0]).getHandle().getBlock().setType(Material.REDSTONE_TORCH_ON);
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
    		b.getHandle().getBlock().setTypeIdAndData(w, color, true);
    		open = false;
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
    	for (V10Location loc: border)
    	{
    		if (plugin.portalManager.borderBlocks.containsKey(loc))
    			plugin.portalManager.borderBlocks.get(loc).delete();
    		if (plugin.portalManager.insideBlocks.containsKey(loc))
    			plugin.portalManager.insideBlocks.get(loc).delete();
    		if (plugin.portalManager.behindBlocks.containsKey(loc))
    			plugin.portalManager.behindBlocks.get(loc).delete();
    		
    		rb = loc.getHandle().getBlock();
    		oldBlocks.put(loc, plugin.blockUtil.getBlockData(rb));
    		rb.setType(Material.WOOL);
    		rb.setData(color);
    		plugin.portalManager.borderBlocks.put(loc, this);
       	}
    	for (V10Location loc: inside)
    	{
    		rb = loc.getHandle().getBlock();
			oldBlocks.put(loc, plugin.blockUtil.getBlockData(rb));
    	}
    	if (plugin.config.FillPortalBack > -1)
    	{
    		for (V10Location loc: behind)
        	{
        		if (plugin.portalManager.borderBlocks.containsKey(loc))
        			plugin.portalManager.borderBlocks.get(loc).delete();
        		if (plugin.portalManager.insideBlocks.containsKey(loc))
        			plugin.portalManager.insideBlocks.get(loc).delete();
        		if (plugin.portalManager.behindBlocks.containsKey(loc))
        			plugin.portalManager.behindBlocks.get(loc).delete();

        		rb = loc.getHandle().getBlock();
        		oldBlocks.put(loc, plugin.blockUtil.getBlockData(rb));
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
    		close();
    	else
    	{
    		open();
	    	if (getDestination().isRegionPortal())
	    		plugin.regionManager.getRegion(centerBlock).regionPortalOpened(orange);
	    	else
	    		getDestination().open();
    	}
    	
    	if (isRegionPortal())
    		plugin.regionManager.getRegion(centerBlock).regionPortalCreated(orange);
    	
    	for (V10Location loc : inside)
    	{
    		plugin.portalManager.insideBlocks.put(loc, this);
    		
    		for (int x = -2;x<3;x++)
    		{
    			for (int y = -2;y<3;y++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    					loc = new V10Location(loc.world, loc.x + x, loc.y + y, loc.z + z);
    					plugin.portalManager.awayBlocksGeneral.put(loc, this);
    	    			awayBlocks.add(loc);
    	    		}
        		}
    		}
    		
    			for (int y = -2;y<3;y++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    					loc = new V10Location(loc.world, loc.x + 3, loc.y + y, loc.z + z);
    					plugin.portalManager.awayBlocksX.put(loc, this);
    					awayBlocks.add(loc);
    					loc = new V10Location(loc.world, loc.x + -3, loc.y + y, loc.z + z);
    					plugin.portalManager.awayBlocksX.put(loc, this);
    	    			awayBlocks.add(loc);
    	    		}
        		}
    			
    			for (int x = -2;x<3;x++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    					loc = new V10Location(loc.world, loc.x + x, loc.y + 3, loc.z + z);
    					plugin.portalManager.awayBlocksY.put(loc, this);
    					awayBlocks.add(loc);
    					loc = new V10Location(loc.world, loc.x + x, loc.y + 3, loc.z + z);
    					plugin.portalManager.awayBlocksY.put(loc, this);
    	    			awayBlocks.add(loc);
    	    		}
        		}
    			
    			for (int x = -2;x<3;x++)
        		{
    				for (int y = -2;y<3;y++)
    	    		{
    					loc = new V10Location(loc.world, loc.x + x, loc.y + y, loc.z + 3);
    					plugin.portalManager.awayBlocksZ.put(loc, this);
    	    			awayBlocks.add(loc);
    					loc = new V10Location(loc.world, loc.x + x, loc.y + y, loc.z + 3);
    					plugin.portalManager.awayBlocksZ.put(loc, this);
    	    			awayBlocks.add(loc);
    	    		}
        		}
    		    		
    	}
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
				return region.bluePortalDest;
		}
		else
		{
			if (owner.orangePortal != null) 
				return owner.orangePortal;
			else if (!isRegionPortal())
				return region.orangePortal;
			else
				return region.orangePortalDest;

		}
	}
	
	public Boolean isRegionPortal()
	{
		return owner.name.startsWith("region_");
	}
}
