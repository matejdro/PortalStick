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
import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;
import de.V10lator.PortalStick.V10Teleport;

public class PortalStickPlayerListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickPlayerListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		User user = plugin.userManager.getUser(player);
	
		//Portal tool
		if (player.getItemInHand().getTypeId() == plugin.config.PortalTool && player.getItemInHand().getDurability() == plugin.config.portalToolData && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
		{
			event.setCancelled(true);
			Region region = plugin.regionManager.getRegion(new V10Location(player.getLocation()));
			HashSet<Byte> tb = new HashSet<Byte>();
			for (int i : region.getList(RegionSetting.TRANSPARENT_BLOCKS).toArray(new Integer[0]))
				tb.add((byte) i);

			
			if (region.getBoolean(RegionSetting.CHECK_WORLDGUARD) && plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, player.getLocation().getBlock()))
				return;
			if (!region.getBoolean(RegionSetting.ENABLE_PORTALS) || !plugin.hasPermission(player, plugin.PERM_PLACE_PORTAL))
				return;
		
			List<Block> targetBlocks = event.getPlayer().getLineOfSight(tb, 120);
			if (targetBlocks.size() < 1 || plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()) || !region.getBoolean(RegionSetting.ENABLE_PORTALS))
				return;
			
			if (region.getBoolean(RegionSetting.PREVENT_PORTAL_THROUGH_PORTAL))
			{
				for (Block b : targetBlocks)
				{
					for (Portal p : plugin.portalManager.portals)
					{
						if (p.inside.contains(b))
						{
							plugin.util.sendMessage(player, plugin.config.MessageCannotPlacePortal);
							plugin.util.PlaySound(Sound.PORTAL_CANNOT_CREATE, player, new V10Location(b.getLocation()));
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
						plugin.util.sendMessage(player, plugin.config.MessageCannotPlacePortal);
						plugin.util.PlaySound(Sound.PORTAL_CANNOT_CREATE, player, new V10Location(b));
						return;
					}
					else if (b.getType() == Material.TRAP_DOOR && (b.getData() & 4) == 0)
					{
						plugin.util.sendMessage(player, plugin.config.MessageCannotPlacePortal);
						plugin.util.PlaySound(Sound.PORTAL_CANNOT_CREATE, player, new V10Location(b));
						return;

					}
				}
			}
			
			boolean orange = false;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				orange = true;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || tb.contains((byte) event.getClickedBlock().getTypeId()))
			{
				Block b = targetBlocks.get(targetBlocks.size() - 1);
				V10Location loc = new V10Location(b);
		        if (targetBlocks.size() < 2)
		        	plugin.portalManager.placePortal(loc, event.getPlayer(), orange);
		        else
		    	   plugin.portalManager.placePortal(loc, b.getFace(targetBlocks.get(targetBlocks.size() - 2)), event.getPlayer(), orange, true);
			}
			else
				plugin.portalManager.placePortal(new V10Location(event.getClickedBlock()), event.getBlockFace(), event.getPlayer(), orange, true);
		}
		//Region tool
		else if (user.usingTool && player.getItemInHand().getTypeId() == plugin.config.RegionTool)
		{
			switch (event.getAction()) {
				case RIGHT_CLICK_BLOCK:
					user.pointTwo = new V10Location(event.getClickedBlock());
					plugin.util.sendMessage(player, "&aRegion point two set`nType /portal setregion to save the region");
					break;
				case LEFT_CLICK_BLOCK:
					user.pointOne = new V10Location(event.getClickedBlock());
					plugin.util.sendMessage(player, "&aRegion point one set`nType /portal setregion to save the region");
			}
		}
		//Flint and steel
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
		{
			V10Location loc = new V10Location(event.getClickedBlock());
			if (plugin.grillManager.createGrill(player, loc) || plugin.funnelBridgeManager.placeGlassBridge(player, loc)) 
				event.setCancelled(true);
		}
			
		}
		//Color changing
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getTypeId() == 0 && event.getClickedBlock().getType() == Material.WOOL)
		{
			V10Location loc = new V10Location(event.getClickedBlock());
			Portal portal = plugin.portalManager.borderBlocks.get(loc);
			if (portal == null) portal = plugin.portalManager.insideBlocks.get(loc);
			if (portal == null && plugin.config.CompactPortal) portal = plugin.portalManager.behindBlocks.get(loc);
			if (portal == null) return;
			if (portal.owner.name != player.getName()) return;
		
			
			int preset = user.colorPreset;
			if (preset == plugin.config.ColorPresets.size() - 1)
				preset = 0;
			else
				preset++;
			
			user.colorPreset = preset;
			user.recreatePortals();

			String color1 = DyeColor.values()[plugin.util.getLeftPortalColor(preset)].toString().replace("_", " ");
			String color2 = DyeColor.values()[plugin.util.getRightPortalColor(preset)].toString().replace("_", " ");

			plugin.util.sendMessage(player, "Your new portal color is " + color1 + " - " + color2);
		}

	}
 	    
	@EventHandler(ignoreCancelled = false)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		final Player player = event.getPlayer();
		if (player.isInsideVehicle())
		  return;

		Location locTo = event.getTo();
		if (plugin.config.DisabledWorlds.contains(locTo.getWorld().getName()))
		  return;
		
		Vector vec2 = locTo.toVector();
		V10Location vlocTo = new V10Location(locTo);
		locTo = vlocTo.getHandle();
		Location locFrom = event.getFrom();
		Vector vec1 = locFrom.toVector();
		V10Location vlocFrom = new V10Location(locFrom);
		if(vlocTo.equals(vlocFrom))
		  return;
		
	    Vector vector = vec2.subtract(vec1);
	    vector.setY(player.getVelocity().getY());
	    
	    Region regionTo = plugin.regionManager.getRegion(vlocTo);
		Region regionFrom = plugin.regionManager.getRegion(vlocFrom);
		
		//Check for changing regions
	    plugin.portalManager.checkPlayerMove(player, regionFrom, regionTo);
		
		//Emancipation grill
		if (regionTo.getBoolean(RegionSetting.ENABLE_GRILLS))
		{
			Grill grill = plugin.grillManager.insideBlocks.get(vlocTo);
			if (grill != null && !grill.disabled)
			{
				plugin.grillManager.emancipate(player);
				return;
			}
		}
		
		//Aerial faith plate
		if (regionTo.getBoolean(RegionSetting.ENABLE_AERIAL_FAITH_PLATES))
		{
			Block blockIn = locTo.getBlock();
			Block blockUnder = blockIn.getRelative(BlockFace.DOWN);
			Block blockStart = null;
			double horPower = Double.parseDouble(regionTo.getString(RegionSetting.FAITH_PLATE_POWER).split("-")[0]);
			String faithBlock = regionTo.getString(RegionSetting.FAITH_PLATE_BLOCK);
			Vector velocity = new Vector(0, Double.parseDouble(regionTo.getString(RegionSetting.FAITH_PLATE_POWER).split("-")[1]),0);
			
			if (blockIn.getType() == Material.STONE_PLATE && plugin.blockUtil.compareBlockToString(blockUnder, faithBlock))
				blockStart = blockUnder;
			else
				blockStart = blockIn;
			if (blockStart != null) {
				BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
				BlockFace face = plugin.blockUtil.getFaceOfMaterial(blockStart, faces, faithBlock);
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
					plugin.util.PlaySound(Sound.FAITHPLATE_LAUNCH, player, new V10Location(blockStart.getLocation()));
					return;
				}
			}
		
		}
		
		//Teleport
		if (plugin.hasPermission(player, plugin.PERM_TELEPORT))
		{
		  final V10Teleport to = plugin.entityManager.teleport(player, vlocTo, vector, true);
		  if(to != null)
		  {
			event.setTo(to.to);
			vlocTo = new V10Location(to.to);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){player.setVelocity(to.velocity);}});
		  }
		}
		
		plugin.gelManager.useGel(player, vlocTo, vector);
		
		//Funnel
		plugin.funnelBridgeManager.EntityMoveCheck(player);
	}
		 
	@EventHandler()
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		User user = plugin.userManager.getUser(player);
		Region region = plugin.regionManager.getRegion(new V10Location(player.getLocation()));
		if (region.getBoolean(RegionSetting.GRILLS_CLEAR_ITEM_DROPS))
			user.droppedItems.add(event.getItemDrop());
		
	}
	
	@EventHandler()
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Region regionFrom = plugin.regionManager.getRegion(new V10Location(event.getFrom()));
		Region regionTo = plugin.regionManager.getRegion(new V10Location(event.getTo()));
		if (!plugin.config.RestoreInvOnWorldChange && !event.getFrom().getWorld().getName().equalsIgnoreCase(event.getTo().getWorld().getName()))
			return;
		plugin.portalManager.checkPlayerMove(event.getPlayer(), regionFrom, regionTo);
	}
		 
	@EventHandler()
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		User user = plugin.userManager.getUser(player);
		
		Region region = plugin.regionManager.getRegion(new V10Location(player.getLocation()));
		if (region.name != "global" && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			user.revertInventory(player);
		if (plugin.config.DeleteOnQuit)
			plugin.userManager.deleteUser(player);
		plugin.userManager.deleteDroppedItems(player);
		plugin.gelManager.resetPlayer(player);
	}
		
	@EventHandler()
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		User user = new User(player.getName());
		plugin.userManager.createUser(player);
		Region region = plugin.regionManager.getRegion(new V10Location(player.getLocation()));
		if (!region.name.equals("global") && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			user.saveInventory(player);
	}
		 		 
}
