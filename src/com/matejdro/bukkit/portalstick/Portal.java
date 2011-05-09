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
	private Boolean orange;
	private Boolean open = false;
	private boolean disabled = false;
	BlockFace teleportface;
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
		for (Block b: border)
		{
			if (oldblocks.containsKey(b.getLocation()))
				Util.setBlockData(b, oldblocks.get(b.getLocation()));
		}
		for (Block b: inside)
		{
			if (oldblocks.containsKey(b.getLocation()))
				Util.setBlockData(b, oldblocks.get(b.getLocation()));
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
