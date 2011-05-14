package com.matejdro.bukkit.portalstick.listeners;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.EntityManager;
import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.GrillManager;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.UserManager;
import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class PortalStickPlayerListener extends PlayerListener {

	public void onPlayerInteract(PlayerInteractEvent event)
	{		
		Player player = event.getPlayer();
		User user = UserManager.getUser(player);

	
		//Portal tool
		if (player.getItemInHand().getTypeId() == Config.PortalTool && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
		{
			
			Region region = RegionManager.getRegion(player.getLocation());
			HashSet<Byte> tb = new HashSet<Byte>();
			for (int i : region.getList(RegionSetting.TRANSPARENT_BLOCKS).toArray(new Integer[0]))
				tb.add((byte) i);

			
			if (region.getBoolean(RegionSetting.CHECK_WORLDGUARD) && PortalStick.worldGuard != null && !PortalStick.worldGuard.canBuild(player, player.getLocation().getBlock()))
				return;
			if (!region.getBoolean(RegionSetting.ENABLE_PORTALS))
				return;
			if (!Permission.placePortal(player))
				return;
		
			List<Block> targetBlocks = event.getPlayer().getLineOfSight(tb, 120);
			if (targetBlocks.size() < 1) return;
			
			if (Config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
			{
				player.sendMessage(Config.MessageRestrictedWorld);
				return;
			}
			
			if (!region.getBoolean(RegionSetting.ENABLE_PORTALS)) return;
			
			if (region.getBoolean(RegionSetting.PREVENT_PORTAL_THROUGH_PORTAL))
			{
				for (Block b : targetBlocks)
				{
					for (Portal p : PortalManager.portals)
					{
						if (p.getInside().contains(b))
						{
							Util.sendMessage(player, Config.MessageCannotPlacePortal);
							return;
						}
					}
				}
			}
			
			if (region.getBoolean(RegionSetting.PREVENT_PORTAL_CLOSED_DOOR))
			{
				for (Block b : targetBlocks)
				{
					if ((b.getType() == Material.IRON_DOOR_BLOCK || b.getType() == Material.WOODEN_DOOR) && ((b.getData() & 4) != 4) )
					{
							Util.sendMessage(player, Config.MessageCannotPlacePortal);
							return;
					}
				}
			}
			
			Boolean orange = false;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				orange = true;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR ||  tb.contains((byte) event.getClickedBlock().getTypeId()))
			{
				Block b = targetBlocks.get(targetBlocks.size() - 1);
				PortalManager.placePortal(b, event.getPlayer(), orange);
			}
			else
			{
				PortalManager.placePortal(event.getClickedBlock(), event.getBlockFace(), event.getPlayer(), orange, true);
			}
			//float dir = (float)Math.toDegrees(Math.atan2(player.getLocation().getBlockX() - b.getX(), b.getZ() - player.getLocation().getBlockZ()));
		}
		//Region tool
		else if (user.getUsingTool() && player.getItemInHand().getTypeId() == Config.RegionTool)
		{
			switch (event.getAction()) {
				case RIGHT_CLICK_BLOCK:
					user.setPointTwo(event.getClickedBlock().getLocation());
					Util.sendMessage(player, "&aRegion point two set`nType /portal setregion to save the region");
					break;
				case LEFT_CLICK_BLOCK:
					user.setPointOne(event.getClickedBlock().getLocation());
					Util.sendMessage(player, "&aRegion point one set`nType /portal setregion to save the region");
					break;
			}
		}
		//Flint and steel
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
			GrillManager.createGrill(player, event.getClickedBlock());
		}

	}
 	    
	public void onPlayerMove(PlayerMoveEvent event)
	{

		Player player = event.getPlayer();
		Vector vector = player.getVelocity();
		Location locTo = event.getTo();
		locTo = new Location(locTo.getWorld(), locTo.getBlockX(), locTo.getBlockY(), locTo.getBlockZ());
		Region regionTo = RegionManager.getRegion(event.getTo());
		Region regionFrom = RegionManager.getRegion(event.getFrom());
		
		//Check for changing regions
		PortalManager.checkPlayerMove(player, regionFrom, regionTo);
		
		//Emancipation grill
		if (regionTo.getBoolean(RegionSetting.ENABLE_GRILLS))
		{
				Grill grill = GrillManager.insideBlocks.get(locTo);
				if (grill != null)
				{
					GrillManager.emancipate(player);
				}
		}
		
		//Aerial faith plate
		Block blockIn = locTo.getBlock();
		Block blockUnder = blockIn.getFace(BlockFace.DOWN);
		Block block = null;
		String faithBlock = regionTo.getString(RegionSetting.FAITH_PLATE_BLOCK);
		Vector velocity = new Vector(0,2,0);
		
		if (blockIn.getType() == Material.STONE_PLATE && BlockUtil.compareBlockToString(blockUnder, faithBlock))
			block = blockUnder;
		else
			block = blockIn;
		
		if (block != null) {
			BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
			BlockFace face = BlockUtil.getFaceOfMaterial(blockIn, faces, faithBlock);
			if (face != null) {
				block = blockIn.getFace(face);
				if (BlockUtil.getFaceOfMaterial(block, new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH}, faithBlock) != null) {
					switch (face) {
						case NORTH:
							velocity.setX(2);
							break;
						case SOUTH:
							velocity.setX(-2);
							break;
						case EAST:
							velocity.setZ(2);
							break;
						case WEST:
							velocity.setZ(-2);
							break;
					}
					if (block == blockUnder) {
						velocity.setX(velocity.getX() * -1);
						velocity.setZ(velocity.getZ() * -1);
					}
					player.setVelocity(velocity);
				}
			}
		}
		
		//Teleport
		if (player.isInsideVehicle()) return;
		
		Boolean permission = UserManager.teleportPermissionCache.get(player);
		if (permission == null)
		{
			permission = Permission.teleport(player);
			UserManager.teleportPermissionCache.put(player, permission);
		}
		if (!permission) return;
		Location out = EntityManager.teleport((Entity) player, locTo, vector);
		if (out != null) event.setTo(out);
		

			
	}
		 
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		User user = UserManager.getUser(player);
		Region region = RegionManager.getRegion(player.getLocation());
		if (region.getBoolean(RegionSetting.GRILLS_CLEAR_ITEM_DROPS))
			user.addDroppedItem(event.getItemDrop());
		
	}
	
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Region regionFrom = RegionManager.getRegion(event.getFrom());
		Region regionTo = RegionManager.getRegion(event.getTo());
		if (!Config.RestoreInvOnWorldChange && !event.getFrom().getWorld().getName().equalsIgnoreCase(event.getTo().getWorld().getName()))
			return;
		PortalManager.checkPlayerMove(event.getPlayer(), regionFrom, regionTo);
	}
		 
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		User user = UserManager.getUser(player);
		
		Region region = RegionManager.getRegion(player.getLocation());
		if (region.Name != "global" && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			user.revertInventory(player);
		if (Config.DeleteOnQuit) {
			PortalManager.deletePortals(user);
			UserManager.deleteUser(player);
		}
		UserManager.deleteDroppedItems(player);
	}
		
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		User user = new User();
		UserManager.createUser(player);
		Region region = RegionManager.getRegion(player.getLocation());
		if (!region.Name.equals("global") && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			user.saveInventory(player);
	}
		 		 
}
