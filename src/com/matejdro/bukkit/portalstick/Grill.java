package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Grill {
	
	private HashSet<Block> border;
	private HashSet<Block> inside;
	private Block firstBlock;
	
	public Grill()
	{
		border = new HashSet<Block>();
		inside = new HashSet<Block>();
	}
	
	public Grill(HashSet<Block> Border, HashSet<Block> Inside, Block FirstBlock)
	{
		border = Border;
		inside = Inside;
		firstBlock = FirstBlock;
	}
	
	public void delete()
	{
		for (Block b: inside)
		{
			b.setType(Material.AIR);
		}
		
		GrillManager.grills.remove(this);
	}
		
	public void create()
	{
		for (Block b: inside)
    	{
    		b.setType(Material.SUGAR_CANE_BLOCK);
    	}    	
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
}
