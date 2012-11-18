package com.matejdro.bukkit.portalstick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class GrillManager {
	
	public final List<Grill> grills = new ArrayList<Grill>();
	public final HashMap<V10Location, Grill> insideBlocks = new HashMap<V10Location, Grill>();
	public final HashMap<V10Location, Grill> borderBlocks = new HashMap<V10Location, Grill>();
	private final PortalStick plugin; 
	
	private HashSet<V10Location> border;
	private HashSet<V10Location> inside;
	private boolean complete;
	private int max = 0;
	
	public GrillManager(PortalStick instance) {
		plugin = instance;
	}

	public void loadGrill(String blockloc) {
		String[] locarr = blockloc.split(",");
		String world = locarr[0];
		if (!placeRecursiveEmancipationGrill(new V10Location(plugin.getServer().getWorld(world).getBlockAt((int)Double.parseDouble(locarr[1]), (int)Double.parseDouble(locarr[2]), (int)Double.parseDouble(locarr[3])))))
			plugin.config.deleteGrill(blockloc);
	}
	
	public void deleteAll() {
		for (Grill g : grills.toArray(new Grill[0]))
			g.deleteInside();
		grills.clear();
		insideBlocks.clear();
		borderBlocks.clear();
	}
    
    public boolean createGrill(Player player, V10Location block) {
    	boolean ret;
    	if(!plugin.hasPermission(player, plugin.PERM_CREATE_GRILL) || plugin.config.DisabledWorlds.contains(player.getLocation().getWorld().getName()))
    	  ret = false;
    	else if(placeRecursiveEmancipationGrill(block))
    	{
    	  plugin.config.saveAll();
    	  ret = true;
    	}
    	else
    	  ret = false;
    	return ret;
    }
    
    public boolean placeRecursiveEmancipationGrill(V10Location initial) {
    	Region region = plugin.regionManager.getRegion(initial);
    	String borderID = region.getString(RegionSetting.GRILL_MATERIAL);
    	System.out.print("A");
    	if (!plugin.blockUtil.compareBlockToString(initial, borderID) || !region.getBoolean(RegionSetting.ENABLE_GRILLS))
    		return false;
    	System.out.print("B");
    	//Check if initial is already in a grill
    	for (Grill grill : grills)
    		if (grill.border.contains(initial))
    			return false;
    	System.out.print("C");
    	//Attempt to get complete border
    	border = new HashSet<V10Location>();
    	inside = new HashSet<V10Location>();
    	startRecurse(initial, borderID, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP);
    	if (!complete)
    		startRecurse(initial, borderID, BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.NORTH);
    	if (!complete)
    		startRecurse(initial, borderID, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST);
    	if (!complete)
    		return false;
    	//Create grill
    	Grill grill = new Grill(plugin, border, inside, initial);
    	border = inside = null;
    	grills.add(grill);
    	grill.create();
    	return true;
    }
    
    
    private void startRecurse(V10Location initial, String id, BlockFace one, BlockFace two, BlockFace three, BlockFace four, BlockFace iOne, BlockFace iTwo) {
    	border.clear();
    	inside.clear();
    	max = 0;
    	complete = false;
    	recurse(initial, id, initial, one, two, three, four);
    	generateInsideBlocks(id, initial, iOne, iTwo);

    	if (inside.size() == 0)
    		complete = false;
    }
    
    private void generateInsideBlocks(String borderID, V10Location initial, BlockFace iOne, BlockFace iTwo) {
    	
    	//Work out maximums and minimums
    	Vector max = border.toArray(new V10Location[0])[0].getHandle().toVector();
    	Vector min = border.toArray(new V10Location[0])[0].getHandle().toVector();
    	
    	for (V10Location block : border.toArray(new V10Location[0])) {
    		if (block.x >= max.getX()) max.setX(block.x);
    		if (block.y >= max.getY()) max.setY(block.y);
    		if (block.z >= max.getZ()) max.setZ(block.z);
    		if (block.x <= min.getX()) min.setX(block.x);
    		if (block.y <= min.getY()) min.setY(block.y);
    		if (block.z <= min.getZ()) min.setZ(block.z);
    	}
    	
    	//Loop through all blocks in the min-max range checking for 'inside' blocks
    	BlockFace[] faces = new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    	World world = initial.getHandle().getWorld();
    	Block rb;
    	for (int y = (int)min.getY(); y <= (int)max.getY(); y++) {
    		for (int x = (int)min.getX(); x <= (int)max.getX(); x++) {
    			for (int z = (int)min.getZ(); z <= (int)max.getZ(); z++) {
    				rb = world.getBlockAt(x, y, z);
    				initial = new V10Location(rb);
    				if (border.contains(initial) || inside.contains(initial))
    	    			continue;
    	    		boolean add = true;
    	    		for (BlockFace face : faces) {
    	    			if (face == iOne || face == iTwo)
    	    				continue;
    	    			Block temp = rb.getRelative(face);
    	    			while (temp.getLocation().toVector().isInAABB(min, max)) {
    	    				
    	    				if (plugin.blockUtil.compareBlockToString(temp, borderID))
    	    					break;
    	    				temp = temp.getRelative(face);
    	    			}
    	    			if (!plugin.blockUtil.compareBlockToString(temp, borderID)) {
    	    				add = false;
    	    				break;
    	    			}
    	    		}
    	    		
    	    		if (add)
    	    			inside.add(initial);
    			}
    		}
    	}
    }
    
    private void recurse(V10Location initial, String id, V10Location vb, BlockFace one, BlockFace two, BlockFace three, BlockFace four) {
    	if (max >= 100) return;
    	if (vb.equals(initial) && border.size() > 2) {
    		complete = true;
    		return;
    	}
    	if (plugin.blockUtil.compareBlockToString(vb, id) && !border.contains(vb)) {
    		border.add(vb);
    		max++;
    		Block b = vb.getHandle().getBlock();
    		recurse(initial, id, new V10Location(b.getRelative(one)), one, two, three, four);
    		recurse(initial, id, new V10Location(b.getRelative(two)), one, two, three, four);
    		recurse(initial, id, new V10Location(b.getRelative(three)), one, two, three, four);
    		recurse(initial, id, new V10Location(b.getRelative(four)), one, two, three, four);
    	}
    }

	public void emancipate(Player player) {
		
		User user = plugin.userManager.getUser(player);
		Region region = plugin.regionManager.getRegion(new V10Location(player.getLocation()));
		plugin.portalManager.deletePortals(user);
		
		if (region.getBoolean(RegionSetting.GRILLS_CLEAR_INVENTORY) && !user.usingTool)
			plugin.portalManager.setPortalInventory(player, region);
		
		if (region.getBoolean(RegionSetting.GRILLS_CLEAR_ITEM_DROPS)) {
			plugin.userManager.deleteDroppedItems(player);
		}
	}
}
