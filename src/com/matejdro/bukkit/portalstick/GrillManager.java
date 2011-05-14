package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class GrillManager implements Runnable {
	
	public static List<Grill> grills = new ArrayList<Grill>();
	public static HashMap<Location, Grill> insideblocks = new HashMap<Location, Grill>();
	public static HashMap<Location, Grill> borderblocks = new HashMap<Location, Grill>();
	public static PortalStick plugin; 
	
	private static HashSet<Block> border;
	private static HashSet<Block> inside;
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
	
	public static void deleteAll() {
		for (Grill g : grills.toArray(new Grill[0]))
			g.deleteInside();
		grills = new ArrayList<Grill>();
		insideblocks.clear();
		borderblocks.clear();
	}
    
    public static List<Grill> getGrillList() {
    	return grills;
    }
    
    public static boolean createGrill(Player player, Block block) {
    	if (!Permission.createGrill(player)) return false;
		if (Config.DisabledWorlds.contains(player.getLocation().getWorld().getName()))
		{
			Util.sendMessage(player, Config.MessageRestrictedWorld);
			return false;
		}
		if (GrillManager.placeRecursiveEmancipationGrill(block)) return true;
		return false;
    }
    
    public static boolean placeRecursiveEmancipationGrill(Block initial) {
    	
    	Region region = RegionManager.getRegion(initial.getLocation());
    	String borderID = region.getString(RegionSetting.GRILL_MATERIAL);
    	if (!Util.compareBlockToString(initial, borderID)) return false;
    	if (!region.getBoolean(RegionSetting.ENABLE_GRILLS)) return false;
    	
    	//Check if initial is already in a grill
    	for (Grill grill : grills)
    		if (grill.getBorder().contains(initial))
    			return false;
    	
    	//Attempt to get complete border
    	startRecurse(initial, borderID, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP);
    	if (!complete)
    		startRecurse(initial, borderID, BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.NORTH);
    	if (!complete)
    		startRecurse(initial, borderID, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST);
    	if (!complete)
    		return false;
    	
    	//Create grill
    	Grill grill = new Grill(border, inside, initial);
    	grills.add(grill);
    	grill.create();
    	Config.saveAll();
    	return true;
    }
    
    
    private static void startRecurse(Block initial, String id, BlockFace one, BlockFace two, BlockFace three, BlockFace four, BlockFace iOne, BlockFace iTwo) {
    	border = new HashSet<Block>();
    	inside = new HashSet<Block>();
    	max = 0;
    	complete = false;
    	recurse(initial, id, initial, one, two, three, four);
    	generateInsideBlocks(id, initial, iOne, iTwo);
    	if (inside.size() == 0)
    		complete = false;
    }
    
    private static void generateInsideBlocks(String borderID, Block initial, BlockFace iOne, BlockFace iTwo) {
    	
    	//Work out maximums and minimums
    	Vector max = border.toArray(new Block[0])[0].getLocation().toVector();
    	Vector min = border.toArray(new Block[0])[0].getLocation().toVector();
    	
    	for (Block block : border.toArray(new Block[0])) {
    		if (block.getX() >= max.getX()) max.setX(block.getX());
    		if (block.getY() >= max.getY()) max.setY(block.getY());
    		if (block.getZ() >= max.getZ()) max.setZ(block.getZ());
    		if (block.getX() <= min.getX()) min.setX(block.getX());
    		if (block.getY() <= min.getY()) min.setY(block.getY());
    		if (block.getZ() <= min.getZ()) min.setZ(block.getZ());
    	}
    	
    	//Loop through all blocks in the min-max range checking for 'inside' blocks
    	for (int y = (int)min.getY(); y <= (int)max.getY(); y++) {
    		for (int x = (int)min.getX(); x <= (int)max.getX(); x++) {
    			for (int z = (int)min.getZ(); z <= (int)max.getZ(); z++) {
    			
    				Block block = initial.getWorld().getBlockAt(x, y, z);
    				if (border.contains(block) || inside.contains(block))
    	    			continue;
    	    		boolean add = true;
    	    	
    	    		for (BlockFace face : BlockFace.values()) {
    	    			if (face == iOne || face == iTwo || face == BlockFace.SELF || face == BlockFace.NORTH_EAST || face == BlockFace.NORTH_WEST || face == BlockFace.SOUTH_EAST || face == BlockFace.SOUTH_WEST)
    	    				continue;
    	    			Block temp = block.getFace(face);
    	    			while (temp.getLocation().toVector().isInAABB(min, max)) {
    	    				if (Util.compareBlockToString(temp, borderID))
    	    					break;
    	    				temp = temp.getFace(face);
    	    			}
    	    			if (!Util.compareBlockToString(temp, borderID)) {
    	    				add = false;
    	    				break;
    	    			}
    	    		}
    	    		
    	    		if (add)
    	    			inside.add(block);
    	    		
    			}
    		}
    	}
    }
    
    private static void recurse(Block initial, String id, Block block, BlockFace one, BlockFace two, BlockFace three, BlockFace four) {
    	if (max >= 100) return;
    	if (block == initial && border.size() > 2) {
    		complete = true;
    		return;
    	}
    	if (Util.compareBlockToString(block, id) && !border.contains(block)) {
    		border.add(block);
    		max++;
    		recurse(initial, id, block.getFace(one), one, two, three, four);
    		recurse(initial, id, block.getFace(two), one, two, three, four);
    		recurse(initial, id, block.getFace(three), one, two, three, four);
    		recurse(initial, id, block.getFace(four), one, two, three, four);
    	}
    }

	public static void emancipate(Player player) {
		
		User user = UserManager.getUser(player);
		Region region = RegionManager.getRegion(player.getLocation());
		PortalManager.deletePortals(user);
		
		if (region.getBoolean(RegionSetting.GRILLS_CLEAR_INVENTORY) && !user.getUsingTool())
			PortalManager.setPortalInventory(player);
		
		if (region.getBoolean(RegionSetting.GRILLS_CLEAR_ITEM_DROPS)) {
			UserManager.deleteDroppedItems(player);
		}
		
	}

	@Override
	public void run() {
		for (Grill g : grills.toArray(new Grill[0])) {
			if (!g.create()) {
				Block b = g.getFirstBlock();
				g.delete();
				GrillManager.placeRecursiveEmancipationGrill(b);
			}
		}
	}
}
