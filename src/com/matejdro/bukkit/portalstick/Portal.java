package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class Portal {
	private final PortalStick plugin;
	public final Location teleport;
	final HashSet<Block> border;
	public final HashSet<Block> inside;
	private final HashSet<Block> behind;
	final boolean vertical;
	private final Block centerBlock;
	public final User owner;
	public final boolean orange;
	public boolean open = false;
	boolean disabled = false;
	public boolean transmitter = false;
	final BlockFace teleportFace;
	private final HashSet<Location> awayBlocks = new HashSet<Location>();
	private final HashMap<Location, String> oldBlocks = new HashMap<Location, String>();
	private boolean placetorch = false;
	
	public Portal(PortalStick plugin, Location Teleport, Block CenterBlock, HashSet<Block> Border, HashSet<Block> Inside, HashSet<Block> Behind, User Owner, Boolean Orange, Boolean Vertical, BlockFace Teleportface)
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
			for (Block b: border)
			{
				if (oldBlocks.containsKey(b.getLocation()))
					plugin.blockUtil.setBlockData(b, oldBlocks.get(b.getLocation()));
				plugin.portalManager.borderBlocks.remove(b.getLocation());
			}
			for (Block b: inside)
			{
				if (oldBlocks.containsKey(b.getLocation()))
					plugin.blockUtil.setBlockData(b, oldBlocks.get(b.getLocation()));
				plugin.portalManager.insideBlocks.remove(b.getLocation());
			}
			if (plugin.config.FillPortalBack > -1)
			{
				for (Block b: behind)
				{
					if (oldBlocks.containsKey(b.getLocation()))
						plugin.blockUtil.setBlockData(b, oldBlocks.get(b.getLocation()));
					plugin.portalManager.behindBlocks.remove(b.getLocation());
				}
			}
			for (Location l : awayBlocks)
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
			plugin.regionManager.getRegion(centerBlock.getLocation()).portals.remove(this);
						
	    	if (getDestination() != null && getDestination().open)
	    	{
	    		if (getDestination().isRegionPortal())
	    		{
	    			plugin.regionManager.getRegion(getDestination().centerBlock.getLocation()).regionPortalClosed(orange);
	    		}
	    		else
	    		{
	    			getDestination().close();
	    		}
	    	}
	    		    	
    		if (isRegionPortal())
    			plugin.regionManager.getRegion(centerBlock.getLocation()).regionPortalDeleted(this);
		}				
	}
	
	public void open()
	{
		Region region = plugin.regionManager.getRegion(((Block)inside.toArray()[0]).getLocation());
		
		for (Block b: inside)
    	{
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
							 		for (Block b2: destination.inside)
							 			b2.setType(Material.REDSTONE_TORCH_ON);

						 		else
						 			destination.placetorch = true;
						 }
				 }
			 }

    	}
		
		if (placetorch)
		{
			((Block)inside.toArray()[0]).setType(Material.REDSTONE_TORCH_ON);
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
		for (Block b: inside)
    	{
    		b.setType(Material.WOOL);
    		b.setData(color);
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
		
		for (Block b: border)
    	{
    		b.setData(color);
    	}

		if (!open)
		{
			for (Block b: inside)
	    	{
	    		b.setData(color);
	    	}
		}
		
		if (plugin.config.CompactPortal)
		{
			for (Block b: behind)
	    	{
	    		b.setData(color);
	    	}
		}
	}
	
	public void create()
	{
		byte color;
		if (orange)
			color = (byte) plugin.util.getRightPortalColor(owner.colorPreset);
		else
			color = (byte) plugin.util.getLeftPortalColor(owner.colorPreset);			

    	for (Block b: border)
    	{
    		if (plugin.portalManager.borderBlocks.containsKey((b.getLocation())))
    			plugin.portalManager.borderBlocks.get(b.getLocation()).delete();
    		if (plugin.portalManager.insideBlocks.containsKey((b.getLocation())))
    			plugin.portalManager.insideBlocks.get(b.getLocation()).delete();
    		if (plugin.portalManager.behindBlocks.containsKey((b.getLocation())))
    			plugin.portalManager.behindBlocks.get(b.getLocation()).delete();
    		
    		oldBlocks.put(b.getLocation(), plugin.blockUtil.getBlockData(b));
    		b.setType(Material.WOOL);
    		b.setData(color);
    		plugin.portalManager.borderBlocks.put(b.getLocation(), this);
       	}
    	for (Block b: inside)
    	{
			oldBlocks.put(b.getLocation(), plugin.blockUtil.getBlockData(b));
    	}
    	if (plugin.config.FillPortalBack > -1)
    	{
    		for (Block b: behind)
        	{
        		if (plugin.portalManager.borderBlocks.containsKey((b.getLocation())))
        			plugin.portalManager.borderBlocks.get(b.getLocation()).delete();
        		if (plugin.portalManager.insideBlocks.containsKey((b.getLocation())))
        			plugin.portalManager.insideBlocks.get(b.getLocation()).delete();
        		if (plugin.portalManager.behindBlocks.containsKey((b.getLocation())))
        			plugin.portalManager.behindBlocks.get(b.getLocation()).delete();

        		oldBlocks.put(b.getLocation(), plugin.blockUtil.getBlockData(b));
        		if (plugin.config.CompactPortal)
        		{
        			b.setType(Material.WOOL);
            		b.setData(color);
        		}
        		else
        		{
        			b.setTypeId(plugin.config.FillPortalBack);
        		}
        		plugin.portalManager.behindBlocks.put(b.getLocation(), this);
        	}
    	}
    	
    		if (getDestination() == null)
    			close();
    		else
    		{
    			open();
	    		if (getDestination().isRegionPortal())
	    		{
	    			plugin.regionManager.getRegion(centerBlock.getLocation()).regionPortalOpened(orange);
	    		}
	    		else
	    		{
	    			getDestination().open();
	    		}

    		}
    		
    		if (isRegionPortal())
    		{
    			plugin.regionManager.getRegion(centerBlock.getLocation()).regionPortalCreated(orange);
    		}
    			    	
    	for (Block b : inside)
    	{
    		plugin.portalManager.insideBlocks.put(b.getLocation(), this);
    		
    		for (int x = -2;x<3;x++)
    		{
    			for (int y = -2;y<3;y++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    					plugin.portalManager.awayBlocksGeneral.put(b.getRelative(x,y,z).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(x,y,z).getLocation());
    	    		}
        		}
    		}
    		
    			for (int y = -2;y<3;y++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    					plugin.portalManager.awayBlocksX.put(b.getRelative(3,y,z).getLocation(), this);
    					plugin.portalManager.awayBlocksX.put(b.getRelative(-3,y,z).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(3,y,z).getLocation());
    	    			awayBlocks.add(b.getRelative(-3,y,z).getLocation());
    	    		}
        		}
    			
    			for (int x = -2;x<3;x++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    					plugin.portalManager.awayBlocksY.put(b.getRelative(x,3,z).getLocation(), this);
    					plugin.portalManager.awayBlocksY.put(b.getRelative(x,-3,z).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(x,3,z).getLocation());
    	    			awayBlocks.add(b.getRelative(x,-3,z).getLocation());
    	    		}
        		}
    			
    			for (int x = -2;x<3;x++)
        		{
    				for (int y = -2;y<3;y++)
    	    		{
    					plugin.portalManager.awayBlocksZ.put(b.getRelative(x,y,3).getLocation(), this);
    					plugin.portalManager.awayBlocksZ.put(b.getRelative(x,y,-3).getLocation(), this);
    	    			awayBlocks.add(b.getRelative(x,y,3).getLocation());
    	    			awayBlocks.add(b.getRelative(x,y,-3).getLocation());
    	    		}
        		}
    		    		
    	}
	}
	
	public Portal getDestination()
	{
		Region region = plugin.regionManager.getRegion(centerBlock.getLocation());
		
		if (orange)
		{
			if (owner.bluePortal != null) 
				return owner.bluePortal;
			else if (!isRegionPortal())
				return region.bluePortal; //TODO: We return null here!
			else
				return region.bluePortalDest;
		}
		else
		{
			if (owner.orangePortal != null) 
				return owner.orangePortal;
			else if (!isRegionPortal())
				return region.orangePortal; //TODO: We return null here!
			else
				return region.orangePortalDest;

		}
	}
	
	public Boolean isRegionPortal()
	{
		return owner.name.startsWith("region_");
	}
}
