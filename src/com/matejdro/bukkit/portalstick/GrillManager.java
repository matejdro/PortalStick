package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

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
    	startRecurse(initial, borderID, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN);
    	if (!complete) {
    		startRecurse(initial, borderID, BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN);
    		plane = Plane.YZ;
    	}
    	if (!complete) {
    		startRecurse(initial, borderID, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST);
    		plane = Plane.ZX;
    	}
    	if (!complete) {
    		return false;
    	}
    	Util.info(border.size() + " border " + plane.toString());

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
    	
    	Util.info(max.toString() + " maxmin " + min.toString());
    	
    	//Sort into lines
    	Vector range = new Vector(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);
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
    	
    	Util.info(num1 + " grid " + num2);

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
    	int i = 0;
    	World world = initial.getWorld();
    	for (Block[] line : lines) {
    		boolean rep = false;
    		int j = 0;
    		for (Block block : line) {
    			if (block == null) {
	    			switch (plane) {
		    			case XY:
		    				block = world.getBlockAt((int)min.getX() + i, (int)min.getY() + j, (int)min.getZ());
		    				break;
		    			case YZ:
		    				block = world.getBlockAt((int)min.getX(), (int)min.getY() + i, (int)min.getZ() + j);
		    				break;
		    			case ZX:
		    				block = world.getBlockAt((int)min.getX() + j, (int)min.getY(), (int)min.getZ() + i);
		    				break;
	    			}
    			}
    			
    			if (block.getTypeId() == borderID)
    				rep = !rep;
    			else if (rep)
    				inside.add(block);
    			j++;
    		}
    		i++;
    	}
    	Util.info(inside.size() + " inside ");
    	Grill grill = new Grill(border, inside, initial);
    	grills.add(grill);
    	grill.create();
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
    
    private enum Plane {
    	XY,
    	YZ,
    	ZX;
    }
}
