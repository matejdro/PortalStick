package com.matejdro.bukkit.portalstick.listeners;

import java.util.HashSet;
import java.util.List;

import org.bukkit.DyeColor;
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
import com.matejdro.bukkit.portalstick.Funnel;
import com.matejdro.bukkit.portalstick.FunnelBridgeManager;
import com.matejdro.bukkit.portalstick.GelManager;
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
import com.matejdro.bukkit.portalstick.util.Config.Sound;
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
							Util.PlaySound(Sound.PORTAL_CANNOT_CREATE, player, b.getLocation());
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
							Util.PlaySound(Sound.PORTAL_CANNOT_CREATE, player, b.getLocation());
							return;
					}
					else if (b.getType() == Material.TRAP_DOOR && (b.getData() & 4) == 0)
					{
						Util.sendMessage(player, Config.MessageCannotPlacePortal);
						Util.PlaySound(Sound.PORTAL_CANNOT_CREATE, player, b.getLocation());
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
				Block b2 = targetBlocks.get(targetBlocks.size() - 2);
				BlockFace face = b.getFace(b2);
				if (face == null)
					PortalManager.placePortal(b, event.getPlayer(), orange);
				else
					PortalManager.placePortal(b, face, event.getPlayer(), orange, true);
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
					break;
				case LEFT_CLICK_BLOCK:
					user.setPointOne(event.getClickedBlock().getLocation());
					Util.sendMessage(player, "&aRegion point one set`nType /portal setregion to save the region");
					break;
			}
		}
		//Flint and steel
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
		{
			if (GrillManager.createGrill(player, event.getClickedBlock())) 
			{
				event.setCancelled(true);
				return;
			}
			if (FunnelBridgeManager.placeGlassBridge(player, event.getClickedBlock())) 
			{
				event.setCancelled(true);
				return;
			}
		}
			
		}
		//Color changing
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getTypeId() == 0 && event.getClickedBlock().getType() == Material.WOOL)
		{
			Portal portal = PortalManager.borderBlocks.get(event.getClickedBlock().getLocation());
			if (portal == null) portal = PortalManager.insideBlocks.get(event.getClickedBlock().getLocation());
			if (portal == null && Config.CompactPortal) portal = PortalManager.behindBlocks.get(event.getClickedBlock().getLocation());
			if (portal == null) return;
			if (portal.getOwner().name != player.getName()) return;
		
			
			int preset = user.getColorPreset();
			if (preset == Config.ColorPresets.size() - 1)
				preset = 0;
			else
				preset++;
			
			user.setColorPreset(preset);
			user.recreatePortals();

			String color1 = DyeColor.values()[Util.getLeftPortalColor(preset)].toString().replace("_", " ");
			String color2 = DyeColor.values()[Util.getRightPortalColor(preset)].toString().replace("_", " ");

			Util.sendMessage(player, "Your new portal color is " + color1 + " - " + color2);
		}

	}
 	    
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
				
		if (player.isInsideVehicle()) return;

		Location locTo = event.getTo();
		locTo = new Location(locTo.getWorld(), locTo.getBlockX(), locTo.getBlockY(), locTo.getBlockZ());
		Region regionTo = RegionManager.getRegion(event.getTo());
		Region regionFrom = RegionManager.getRegion(event.getFrom());
		
		Vector vec2 = event.getTo().toVector();
	    Vector vec1 = event.getFrom().toVector();
	    Vector vector = vec2.subtract(vec1);
		
	    if (Config.DisabledWorlds.contains(locTo.getWorld().getName())) return;
	    
		//Check for changing regions
		PortalManager.checkPlayerMove(player, regionFrom, regionTo);
		
		//Emancipation grill
		if (regionTo.getBoolean(RegionSetting.ENABLE_GRILLS))
		{
				Grill grill = GrillManager.insideBlocks.get(locTo);
				if (grill != null && !grill.isDisabled())
				{
					GrillManager.emancipate(player);
				}
		}
		
		//Aerial faith plate
		if (regionTo.getBoolean(RegionSetting.ENABLE_AERIAL_FAITH_PLATES))
		{
			Block blockIn = locTo.getBlock();
			Block blockUnder = blockIn.getRelative(BlockFace.DOWN);
			Block blockStart = null;
			Double horPower = Double.parseDouble(regionTo.getString(RegionSetting.FAITH_PLATE_POWER).split("-")[0]);
			String faithBlock = regionTo.getString(RegionSetting.FAITH_PLATE_BLOCK);
			Vector velocity = new Vector(0, Double.parseDouble(regionTo.getString(RegionSetting.FAITH_PLATE_POWER).split("-")[1]),0);
			
			if (blockIn.getType() == Material.STONE_PLATE && BlockUtil.compareBlockToString(blockUnder, faithBlock))
				blockStart = blockUnder;
			else
				blockStart = blockIn;
			
			if (blockStart != null) {
				BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
				BlockFace face = BlockUtil.getFaceOfMaterial(blockStart, faces, faithBlock);
				if (face != null) {
					switch (face) {
						case NORTH:
							velocity.setX(horPower);
							break;
						case SOUTH:
							velocity.setX(-horPower);
							break;
						case EAST:
							velocity.setZ(horPower);
							break;
						case WEST:
							velocity.setZ(-horPower);
							break;
					}
					if (blockStart == blockUnder) {
						velocity.setX(-velocity.getX());
						velocity.setZ(-velocity.getZ());
					}
					player.setVelocity(velocity);
					Util.PlaySound(Sound.FAITHPLATE_LAUNCH, player, blockStart.getLocation());
				}
			}
		
		}
		
		GelManager.useGel( player, locTo, vector);		
		
		//Teleport		
		Boolean permission = UserManager.teleportPermissionCache.get(player);
		if (permission == null)
		{
			permission = Permission.teleport(player);
			UserManager.teleportPermissionCache.put(player, permission);
		}
		if (permission) 
		{
			EntityManager.teleport((Entity) player, locTo, vector.setY(player.getVelocity().getY()));
		}
		
		//Funnel
		FunnelBridgeManager.EntityMoveCheck(player);
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
		User user = new User(player.getName());
		UserManager.createUser(player);
		Region region = RegionManager.getRegion(player.getLocation());
		if (!region.Name.equals("global") && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			user.saveInventory(player);
	}
		 		 
}
