package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import de.V10lator.PortalStick.V10Location;

public class Grill {
	private final PortalStick plugin;
	
	final HashSet<V10Location> border;
	private final HashSet<V10Location> inside;
	final V10Location firstBlock;
	public boolean disabled;
	
	public Grill(PortalStick plugin, HashSet<V10Location> Border, HashSet<V10Location> Inside, V10Location FirstBlock)
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
		
		for (V10Location b : border)
			plugin.grillManager.borderBlocks.remove(b);
	}
	
	public void deleteInside()
	{
		for (V10Location b: inside)
		{
			b.getHandle().getBlock().setType(Material.AIR);
			plugin.grillManager.insideBlocks.remove(b);
		}
	}
	
	public void disable()
	{
		for (V10Location b: inside)
		{
			b.getHandle().getBlock().setType(Material.AIR);
			disabled = true;
		}
	}
	
	public void enable()
	{
		for (V10Location b: inside)
		{
			b.getHandle().getBlock().setType(Material.SUGAR_CANE_BLOCK);
			disabled = false;
		}
	}
		
	public boolean create()
	{
		boolean complete = true;
		Block rb;
		for (V10Location b: inside)
    	{
			rb = b.getHandle().getBlock();
			plugin.grillManager.insideBlocks.put(b, this);
			if (rb.getType() != Material.SUGAR_CANE_BLOCK) {
				rb.setType(Material.SUGAR_CANE_BLOCK);
				complete = false;
			}
			
    	}
		for (V10Location b : border)
			plugin.grillManager.borderBlocks.put(b, this);
		return complete;
	}
	
	public String getStringLocation()
	{
		Location loc = firstBlock.getHandle();
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
	}
}
