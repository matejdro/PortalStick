package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

public class GrillManager {
	
	public static List<Grill> grills = new ArrayList<Grill>();
	public static PortalStick plugin;
	
	private static HashSet<Block> border;
	private static boolean complete;
	private static int max = 0;
	
	public GrillManager(PortalStick instance) {
		plugin = instance;
	}

	public static void loadGrill(String blockloc) {
		String[] locarr = blockloc.split(",");
		String world = locarr[0];
		Block b = plugin.getServer().getWorld(world).getBlockAt((int)Double.parseDouble(locarr[1]), (int)Double.parseDouble(locarr[2]), (int)Double.parseDouble(locarr[3]));
		if (!placeRecursiveEmancipationGrill(b))
			Config.deleteGrill(blockloc);
	}
	
    public static Boolean placeEmancipationGrill(Block b)
    {
    	
    	Region region = RegionManager.getRegion(b.getLocation());
    	int x = 0;
    	int z = 0;
    	if (b.getTypeId() == region.getInt(RegionSetting.GRILL_MATERIAL))
    	{
    		if (b.getRelative(1,1,0).getTypeId() == region.getInt(RegionSetting.GRILL_MATERIAL))
    			x = 1;
    		else if (b.getRelative(-1,1,0).getTypeId() == region.getInt(RegionSetting.GRILL_MATERIAL))
    			x = -1;
    		else if (b.getRelative(0,1,1).getTypeId() == region.getInt(RegionSetting.GRILL_MATERIAL))
    			z = 1;
    		else if (b.getRelative(0,1,-1).getTypeId() == region.getInt(RegionSetting.GRILL_MATERIAL))
    			z = -1;
    		else
    			return false;
    	}
    	else
    		return false;
    	HashSet<Block> border = new HashSet<Block>();
    	HashSet<Block> inside = new HashSet<Block>();
    	
    	
    	border.add(b);
    	border.add(b.getRelative(x*1, 1, z*1));
    	border.add(b.getRelative(x*1, 2, z*1));
    	border.add(b.getRelative(0, 3, 0));
    	border.add(b.getRelative(x*-1, 3, z*-1));
    	border.add(b.getRelative(x*-2, 1, z*-2));
    	border.add(b.getRelative(x*-2, 2, z*-2));
    	border.add(b.getRelative(x*-1, 0, z*-1));
    	
    	inside.add(b.getRelative(0,1,0));
    	inside.add(b.getRelative(0,2,0));
    	inside.add(b.getRelative(x*-1,1,z*-1));
    	inside.add(b.getRelative(x*-1,2,z*-1));
    	
    	for (Block block: border)
    	{
    		if (block.getTypeId() != region.getInt(RegionSetting.GRILL_MATERIAL))
    		{
    			return false;
    		}
    	}

    	for (Block block: inside)
    	{
    		if (block.getType() != Material.AIR)
    		{
    			return false;
    		}
    	}
    	    	
    	Grill grill = new Grill(border, inside, b);
    	grills.add(grill);
    	grill.create();
    	
    	Config.saveAll();
    	
    	return true;
    	
    }
    
    public static List<Grill> getGrillList() {
    	return grills;
    }
    
    public static boolean placeRecursiveEmancipationGrill(Block initial) {
    	
    	Region region = RegionManager.getRegion(initial.getLocation());
    	int borderID = region.getInt(RegionSetting.GRILL_MATERIAL);
    	if (initial.getTypeId() != borderID) return false;

    	//Attempt to get complete border
    	startRecurse(initial, borderID, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN);
    	BlockFace iOne = BlockFace.EAST; BlockFace iTwo = BlockFace.WEST;
    	if (!complete) {
    		startRecurse(initial, borderID, BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN);
    		iOne = BlockFace.NORTH; iTwo = BlockFace.SOUTH;
    	}
    	if (!complete) {
    		startRecurse(initial, borderID, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST);
    		iOne = BlockFace.UP; iTwo = BlockFace.DOWN;
    	}
    	if (!complete) {
    		return false;
    	}

    	//Work out maximums and minimums
    	Vector max = initial.getLocation().toVector();
    	Vector min = initial.getLocation().toVector();
    	
    	for (Block block : border.toArray(new Block[0])) {
    		if (block.getX() > max.getX()) max.setX(block.getX());
    		if (block.getY() > max.getY()) max.setY(block.getY());
    		if (block.getZ() > max.getZ()) max.setX(block.getZ());
    		if (block.getX() < min.getX()) min.setX(block.getX());
    		if (block.getY() < min.getY()) min.setY(block.getY());
    		if (block.getZ() < min.getZ()) min.setX(block.getZ());
    	}
    	
    	//Work out inside blocks
    	HashSet<Block> inside = new HashSet<Block>();
    	for (int y = (int)min.getY(); y <= (int)max.getY(); y++) {
    		for (int x = (int)min.getX(); x <= (int)max.getX(); x++) {
    			for (int z = (int)min.getZ(); z <= (int)max.getZ(); z++) {
    			
    				Block block = initial.getWorld().getBlockAt(x, y, z);
    				if (border.contains(block) || inside.contains(block))
    	    			continue;
    	    		boolean add = true;
    	    	
    	    		for (BlockFace face : BlockFace.values()) {
    	    			if (face == BlockFace.SELF || face == iOne || face == iTwo || face == BlockFace.NORTH_EAST || face == BlockFace.NORTH_WEST || face == BlockFace.SOUTH_EAST || face == BlockFace.SOUTH_WEST)
    	    				continue;
    	    			Block temp = block.getFace(face);
    	    			while (temp.getLocation().toVector().isInAABB(min, max)) {
    	    				if (temp.getTypeId() == borderID)
    	    					break;
    	    				temp = temp.getFace(face);
    	    			}
    	    			if (temp.getTypeId() != borderID) {
    	    				add = false;
    	    				break;
    	    			}
    	    		}
    	    		
    	    		if (add)
    	    			inside.add(block);
    	    		
    			}
    		}
    	}
    	
    	//Create grill
    	Grill grill = new Grill(border, inside, initial);
    	grills.add(grill);
    	grill.create();
    	Config.saveAll();
    	return true;
    }
    
    private static void startRecurse(Block initial, int id, BlockFace one, BlockFace two, BlockFace three, BlockFace four) {
    	border = new HashSet<Block>();
    	max = 0;
    	complete = false;
    	recurse(initial, id, initial, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN);
    }
    
    private static void recurse(Block initial, int id, Block block, BlockFace one, BlockFace two, BlockFace three, BlockFace four) {
    	if (max >= 100) return;
    	if (block == initial && border.size() > 2) {
    		complete = true;
    		return;
    	}
    	if (block.getTypeId() == id && !border.contains(block)) {
    		border.add(block);
    		max++;
    		recurse(initial, id, block.getFace(one), one, two, three, four);
    		recurse(initial, id, block.getFace(two), one, two, three, four);
    		recurse(initial, id, block.getFace(three), one, two, three, four);
    		recurse(initial, id, block.getFace(four), one, two, three, four);
    	}
    }
}
