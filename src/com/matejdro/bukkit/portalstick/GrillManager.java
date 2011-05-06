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
	
	private static HashSet<Block> border = new HashSet<Block>();
	private static boolean complete = false;
	
	public GrillManager(PortalStick instance) {
		plugin = instance;
	}

	public static void loadGrill(String blockloc) {
		String[] locarr = blockloc.split(",");
		String world = locarr[0];
		Block b = plugin.getServer().getWorld(world).getBlockAt(Integer.parseInt(locarr[1]), Integer.parseInt(locarr[2]), Integer.parseInt(locarr[3]));
		if (!placeEmancipationGrill(b))
			Config.deleteGrill(blockloc);
		Config.saveAll();
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
    	Plane plane = Plane.XY;
    	recurse(initial, borderID, 0, initial, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN);
    	if (!complete) {
    		recurse(initial, borderID, 0, initial, BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN);
    		plane = Plane.YZ;
    	}
    	if (!complete) {
    		recurse(initial, borderID, 0, initial, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST);
    		plane = Plane.ZX;
    	}
    	if (!complete)
    		return false;

    	//Work out maximums and minimums
    	Vector max = new Vector();
    	Vector min = new Vector();
    	for (Block block : border.toArray(new Block[0])) {
    		if (block.getX() > max.getX()) max.setX(block.getX());
    		if (block.getY() > max.getY()) max.setY(block.getY());
    		if (block.getZ() > max.getZ()) max.setX(block.getZ());
    		if (block.getX() > min.getX()) min.setX(block.getX());
    		if (block.getY() > min.getY()) min.setY(block.getY());
    		if (block.getZ() > min.getZ()) min.setX(block.getZ());
    	}
    	
    	//Sort into lines
    	Vector range = new Vector(max.getX() - min.getX(), max.getY() - min.getY(), max.getZ() - min.getZ());
    	double num1 = 0;
    	double num2 = 0;
    	switch (plane) {
	    	case XY:
	    		num1 = range.getX();
	    		num2 = range.getY();
	    		break;
	    	case YZ:
	    		num1 = range.getY();
	    		num2 = range.getZ();
	    		break;
	    	case ZX:
	    		num1 = range.getZ();
	    		num2 = range.getX();
	    		break;
    	}

    	Block[][] lines = new Block[(int) num1][(int) num2];
    	for (Block block : border.toArray(new Block[0])) {
    		switch (plane) {
	    		case XY:
	    			lines[(int) (block.getX() - min.getX())][(int) (block.getY() - min.getY())] = block;
	    			break;
	    		case YZ:
	    			lines[(int) (block.getY() - min.getY())][(int) (block.getZ() - min.getZ())] = block;
	    			break;
	    		case ZX:
	    			lines[(int) (block.getZ() - min.getZ())][(int) (block.getX() - min.getX())] = block;
	    			break;
    		}
    	}
    	
    	//Loop through lines detecting internal grill blocks
    	HashSet<Block> inside = new HashSet<Block>();
    	for (Block[] line : lines) {
    		boolean rep = false;
    		for (Block block : line) {
    			if (block.getTypeId() == borderID) {
    				if (rep == false) rep = true;
    				if (rep == true) rep = false;
    			}
    			if (block.getType() == Material.AIR && rep == true)
    				inside.add(block);
    		}
    	}
    	grills.add(new Grill(border, inside, initial));
    	return true;
    }
    
    private static void recurse(Block initial, int id, int max, Block block, BlockFace one, BlockFace two, BlockFace three, BlockFace four) {
    	if (max >= 100) return;
    	if (block == initial && border.size() > 2) {
    		complete = true;
    		return;
    	}
    	if (block.getTypeId() == id) {
    		border.add(block);
    		max++;
    		recurse(initial, id, max, block.getFace(one), one, two, three, four);
    		recurse(initial, id, max, block.getFace(two), one, two, three, four);
    		recurse(initial, id, max, block.getFace(three), one, two, three, four);
    		recurse(initial, id, max, block.getFace(four), one, two, three, four);
    	}
    }
    
    private enum Plane {
    	XY,
    	YZ,
    	ZX;
    }
}
