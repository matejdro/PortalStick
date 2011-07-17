package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.matejdro.bukkit.portalstick.util.Config;

public class Grill {
	
	private HashSet<Block> border;
	private HashSet<Block> inside;
	private Block firstBlock;
	private Boolean disabled;
	
	
	public Grill(HashSet<Block> Border, HashSet<Block> Inside, Block FirstBlock)
	{
		border = Border;
		inside = Inside;
		firstBlock = FirstBlock;
		disabled = false;
	}
	
	public void delete()
	{
		deleteInside();
		Config.deleteGrill(getStringLocation());
		GrillManager.grills.remove(this);
		Config.saveAll();
		
		for (Block b : border)
		{
			GrillManager.borderBlocks.remove(b.getLocation());
		}
	}
	
	public void deleteInside()
	{
		for (Block b: inside)
		{
			b.setType(Material.AIR);
			GrillManager.insideBlocks.remove(b.getLocation());
		}
	}
	
	public void disable()
	{
		for (Block b: inside)
		{
			b.setType(Material.AIR);
			disabled = true;
		}
	}
	
	public void enable()
	{
		for (Block b: inside)
		{
			b.setType(Material.SUGAR_CANE_BLOCK);
			disabled = false;
		}
	}
		
	public boolean create()
	{
		boolean complete = true;
		for (Block b: inside)
    	{
			GrillManager.insideBlocks.put(b.getLocation(), this);
			if (b.getType() != Material.SUGAR_CANE_BLOCK) {
				b.setType(Material.SUGAR_CANE_BLOCK);
				complete = false;
			}
			
    	}
		for (Block b : border)
		{
			GrillManager.borderBlocks.put(b.getLocation(), this);
		}
		return complete;
	}
	
	public String getStringLocation()
	{
		Location loc = firstBlock.getLocation();
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}
	
	public HashSet<Block> getBorder()
	{
		return border;
	}
	
	public HashSet<Block> getInside()
	{
		return inside;
	}
	
	public Block getFirstBlock() {
		return firstBlock;
	}
	
	public Boolean isDisabled()
	{
		return disabled;
	}
}
