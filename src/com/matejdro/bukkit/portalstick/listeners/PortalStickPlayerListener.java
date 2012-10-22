package com.matejdro.bukkit.portalstick.listeners;

import java.util.HashSet;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class PortalStickPlayerListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickPlayerListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler()
	public void onPlayerInteract(PlayerInteractEvent event)
	{	
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		User user = plugin.userManager.getUser(player);
	
		//Portal tool
		if (player.getItemInHand().getTypeId() == Config.PortalTool && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
		{
			event.setCancelled(true);
			Region region = plugin.regionManager.getRegion(player.getLocation());
			HashSet<Byte> tb = new HashSet<Byte>();
			for (int i : region.getList(RegionSetting.TRANSPARENT_BLOCKS).toArray(new Integer[0]))
				tb.add((byte) i);

			
			if (region.getBoolean(RegionSetting.CHECK_WORLDGUARD) && plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, player.getLocation().getBlock()))
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
					for (Portal p : plugin.portalManager.portals)
					{
						if (p.inside.contains(b))
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
				Block b2 = targetBlocks.size() >= 2 ? targetBlocks.get(targetBlocks.size() - 2) : null;
		        if (targetBlocks.size() < 2 || b.getFace(b2) == null)
		        	plugin.portalManager.placePortal(b, event.getPlayer(), orange);
		        else
		    	   plugin.portalManager.placePortal(b, b.getFace(b2), event.getPlayer(), orange, true); 
			}
			else
			{
				plugin.portalManager.placePortal(event.getClickedBlock(), event.getBlockFace(), event.getPlayer(), orange, true);
			}
			//float dir = (float)Math.toDegrees(Math.atan2(player.getLocation().getBlockX() - b.getX(), b.getZ() - player.getLocation().getBlockZ()));
		}
		//Region tool
		else if (user.usingTool && player.getItemInHand().getTypeId() == Config.RegionTool)
		{
			switch (event.getAction()) {
				case RIGHT_CLICK_BLOCK:
					user.pointTwo = event.getClickedBlock().getLocation();
					break;
				case LEFT_CLICK_BLOCK:
					user.pointOne = event.getClickedBlock().getLocation();
					Util.sendMessage(player, "&aRegion point one set`nType /portal setregion to save the region");
					break;
			}
		}
		//Flint and steel
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
		{
			if (plugin.grillManager.createGrill(player, event.getClickedBlock()) || plugin.funnelBridgeManager.placeGlassBridge(player, event.getClickedBlock())) 
				event.setCancelled(true);
		}
			
		}
		//Color changing
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getTypeId() == 0 && event.getClickedBlock().getType() == Material.WOOL)
		{
			Portal portal = plugin.portalManager.borderBlocks.get(event.getClickedBlock().getLocation());
			if (portal == null) portal = plugin.portalManager.insideBlocks.get(event.getClickedBlock().getLocation());
			if (portal == null && Config.CompactPortal) portal = plugin.portalManager.behindBlocks.get(event.getClickedBlock().getLocation());
			if (portal == null) return;
			if (portal.owner.name != player.getName()) return;
		
			
			int preset = user.colorPreset;
			if (preset == Config.ColorPresets.size() - 1)
				preset = 0;
			else
				preset++;
			
			user.colorPreset = preset;
			user.recreatePortals();

			String color1 = DyeColor.values()[Util.getLeftPortalColor(preset)].toString().replace("_", " ");
			String color2 = DyeColor.values()[Util.getRightPortalColor(preset)].toString().replace("_", " ");

			Util.sendMessage(player, "Your new portal color is " + color1 + " - " + color2);
		}

	}
 	    
	@EventHandler()
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
				
		if (player.isInsideVehicle()) return;

		Location locTo = event.getTo();
		locTo = new Location(locTo.getWorld(), locTo.getBlockX(), locTo.getBlockY(), locTo.getBlockZ());
		Region regionTo = plugin.regionManager.getRegion(event.getTo());
		Region regionFrom = plugin.regionManager.getRegion(event.getFrom());
		
		Vector vec2 = event.getTo().toVector();
	    Vector vec1 = event.getFrom().toVector();
	    Vector vector = vec2.subtract(vec1);
		
	    if (Config.DisabledWorlds.contains(locTo.getWorld().getName())) return;
	    
		//Check for changing regions
	    plugin.portalManager.checkPlayerMove(player, regionFrom, regionTo);
		
		//Emancipation grill
		if (regionTo.getBoolean(RegionSetting.ENABLE_GRILLS))
		{
				Grill grill = plugin.grillManager.insideBlocks.get(locTo);
				if (grill != null && !grill.isDisabled())
				{
					plugin.grillManager.emancipate(player);
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
		
		plugin.gelManager.useGel( player, locTo, vector);
		
		//Teleport
		if (Permission.teleport(player))
		  plugin.entityManager.teleport(player, locTo, vector.setY(player.getVelocity().getY()));
		
		//Funnel
		plugin.funnelBridgeManager.EntityMoveCheck(player);
	}
		 
	@EventHandler()
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		User user = plugin.userManager.getUser(player);
		Region region = plugin.regionManager.getRegion(player.getLocation());
		if (region.getBoolean(RegionSetting.GRILLS_CLEAR_ITEM_DROPS))
			user.droppedItems.add(event.getItemDrop());
		
	}
	
	@EventHandler()
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Region regionFrom = plugin.regionManager.getRegion(event.getFrom());
		Region regionTo = plugin.regionManager.getRegion(event.getTo());
		if (!Config.RestoreInvOnWorldChange && !event.getFrom().getWorld().getName().equalsIgnoreCase(event.getTo().getWorld().getName()))
			return;
		plugin.portalManager.checkPlayerMove(event.getPlayer(), regionFrom, regionTo);
		}
		 
	@EventHandler()
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		User user = plugin.userManager.getUser(player);
		
		Region region = plugin.regionManager.getRegion(player.getLocation());
		if (region.name != "global" && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			user.revertInventory(player);
		if (Config.DeleteOnQuit) {
			plugin.portalManager.deletePortals(user);
			plugin.userManager.deleteUser(player);
		}
		plugin.userManager.deleteDroppedItems(player);
	}
		
	@EventHandler()
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		User user = new User(player.getName());
		plugin.userManager.createUser(player);
		Region region = plugin.regionManager.getRegion(player.getLocation());
		if (!region.name.equals("global") && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			user.saveInventory(player);
	}
		 		 
}
