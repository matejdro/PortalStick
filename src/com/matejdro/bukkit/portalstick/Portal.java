package com.matejdro.bukkit.portalstick;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.matejdro.bukkit.portalstick.util.Util;

public class Portal {
	private Location teleport;
	private HashSet<Block> border;
	private HashSet<Block> inside;
	private boolean vertical;
	private User owner;
	private Boolean orange = false;
	private Boolean open = false;
	private boolean disabled = false;
	BlockFace teleportface;
	private HashSet<Location> awayblocks = new HashSet<Location>();
	private HashMap<Location, String> oldblocks = new HashMap<Location, String>();
	
	public Portal()
	{
		border = new HashSet<Block>();
		inside = new HashSet<Block>();
	}
	
	public Portal(Location Teleport, HashSet<Block> Border, HashSet<Block> Inside, User Owner, Boolean Orange, Boolean Vertical, BlockFace Teleportface)
	{
		teleport = Teleport;
		border = Border;
		inside = Inside;
		orange = Orange;
		owner = Owner;
		vertical = Vertical;
		teleportface = Teleportface;
	}
	
	public void delete()
	{
		
		if (orange != null && owner != null) {
			for (Block b: border)
			{
				if (oldblocks.containsKey(b.getLocation()))
					Util.setBlockData(b, oldblocks.get(b.getLocation()));
				PortalManager.borderblocks.remove(b.getLocation());
			}
			for (Block b: inside)
			{
				if (oldblocks.containsKey(b.getLocation()))
					Util.setBlockData(b, oldblocks.get(b.getLocation()));
				PortalManager.insideblocks.remove(b.getLocation());
			}
			for (Location l : awayblocks)
			{
				PortalManager.awayblocksgeneral.remove(l);
				PortalManager.awayblocksX.remove(l);
				PortalManager.awayblocksY.remove(l);
				PortalManager.awayblocksZ.remove(l);
			}
			
			if (orange)
			{
				owner.setOrangePortal(null);
			}
			else
			{
				owner.setBluePortal(null);
			}
			
			if (orange)
	    	{
	    		if (owner.getBluePortal() != null)
	    			owner.getBluePortal().close();
	    	}
	    	else
	    	{
	    		if (owner.getOrangePortal() != null)
	    			owner.getOrangePortal().close();
	
	    	}
		}
				
		PortalManager.portals.remove(this);
		open = false;
	}
	
	public void open()
	{
		for (Block b: inside)
    	{
			b.setType(Material.AIR);    		
    	}
		open = true;
	}
	
	public void close()
	{
		for (Block b: inside)
    	{
    		b.setType(Material.WOOL);
    		if (orange)
    			b.setData((byte) 1);
    		else
    			b.setData((byte) 11);
    		open = false;
    	}
	}
	
	public void create()
	{
    	for (Block b: border)
    	{
    		oldblocks.put(b.getLocation(), Util.getBlockData(b));
    		b.setType(Material.WOOL);
    		if (orange)
    			b.setData((byte) 1);
    		else
    			b.setData((byte) 11);
    		PortalManager.borderblocks.put(b.getLocation(), this);
    	}
    	for (Block b: inside)
    	{
			oldblocks.put(b.getLocation(), Util.getBlockData(b));
    	}
    	
    	if (orange)
    	{
    		if (owner.getBluePortal() == null)
    			close();
    		else
    		{
    			open();
    			owner.getBluePortal().open();
    		}
    			
    		
    	}
    	else
    	{
    		if (owner.getOrangePortal() == null)
    			close();
    		else
    		{
    			open();
    			owner.getOrangePortal().open();
    		}

    	}
    	
    	for (Block b : inside)
    	{
    		PortalManager.insideblocks.put(b.getLocation(), this);
    		
    		for (int x = -2;x<3;x++)
    		{
    			for (int y = -2;y<3;y++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    	    			PortalManager.awayblocksgeneral.put(b.getRelative(x,y,z).getLocation(), this);
    	    			awayblocks.add(b.getRelative(x,y,z).getLocation());
    	    		}
        		}
    		}
    		
    			for (int y = -2;y<3;y++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    	    			PortalManager.awayblocksX.put(b.getRelative(3,y,z).getLocation(), this);
    	    			PortalManager.awayblocksX.put(b.getRelative(-3,y,z).getLocation(), this);
    	    			awayblocks.add(b.getRelative(3,y,z).getLocation());
    	    			awayblocks.add(b.getRelative(-3,y,z).getLocation());
    	    		}
        		}
    			
    			for (int x = -2;x<3;x++)
        		{
    				for (int z = -2;z<3;z++)
    	    		{
    	    			PortalManager.awayblocksY.put(b.getRelative(x,3,z).getLocation(), this);
    	    			PortalManager.awayblocksY.put(b.getRelative(x,-3,z).getLocation(), this);
    	    			awayblocks.add(b.getRelative(x,3,z).getLocation());
    	    			awayblocks.add(b.getRelative(x,-3,z).getLocation());
    	    		}
        		}
    			
    			for (int x = -2;x<3;x++)
        		{
    				for (int y = -2;y<3;y++)
    	    		{
    	    			PortalManager.awayblocksZ.put(b.getRelative(x,y,3).getLocation(), this);
    	    			PortalManager.awayblocksZ.put(b.getRelative(x,y,-3).getLocation(), this);
    	    			awayblocks.add(b.getRelative(x,y,3).getLocation());
    	    			awayblocks.add(b.getRelative(x,y,-3).getLocation());
    	    		}
        		}
    		    		
    	}
    		
    	
    
    		
    	
	}
	
	public Location getTeleportLocation()
	{
		return teleport;
	}
	
	public User getOwner()
	{
		return owner;
	}
	
	public HashSet<Block> getBorder()
	{
		return border;
	}
	
	public HashSet<Block> getInside()
	{
		return inside;
	}
	
	
	public Boolean isOpen()
	{
		return open;
	}
	
	public Boolean isVertical()
	{
		return vertical;
	}
	
	public BlockFace getTeleportFace()
	{
		return teleportface;
	}
	
	public Boolean isOrange()
	{
		return orange;
	}
	
	public Boolean isDisabled()
	{
		return disabled;
	}
	
	public void setDisabled(Boolean input)
	{
		disabled = input;
	}

}
