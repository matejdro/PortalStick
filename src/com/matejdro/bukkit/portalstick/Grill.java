package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Grill {
	private final PortalStick plugin;
	
	private final HashSet<Block> border;
	private final HashSet<Block> inside;
	private final Block firstBlock;
	private Boolean disabled;
	
	public Grill(PortalStick plugin, HashSet<Block> Border, HashSet<Block> Inside, Block FirstBlock)
	{
		this.plugin = plugin;
		border = Border;
		inside = Inside;
		firstBlock = FirstBlock;
		disabled = false;
	}
	
	public void delete()
	{
		deleteInside();
		plugin.config.deleteGrill(getStringLocation());
		plugin.grillManager.grills.remove(this);
		plugin.config.saveAll();
		
		for (Block b : border)
		{
			plugin.grillManager.borderBlocks.remove(b.getLocation());
		}
	}
	
	public void deleteInside()
	{
		for (Block b: inside)
		{
			b.setType(Material.AIR);
			plugin.grillManager.insideBlocks.remove(b.getLocation());
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
			plugin.grillManager.insideBlocks.put(b.getLocation(), this);
			if (b.getType() != Material.SUGAR_CANE_BLOCK) {
				b.setType(Material.SUGAR_CANE_BLOCK);
				complete = false;
			}
			
    	}
		for (Block b : border)
		{
			plugin.grillManager.borderBlocks.put(b.getLocation(), this);
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
