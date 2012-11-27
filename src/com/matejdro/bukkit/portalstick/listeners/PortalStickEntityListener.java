package com.matejdro.bukkit.portalstick.listeners;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.bergerkiller.bukkit.common.events.EntityAddEvent;
import com.bergerkiller.bukkit.common.events.EntityMoveEvent;
import com.bergerkiller.bukkit.common.events.EntityRemoveEvent;
import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.BlockHolder;
import de.V10lator.PortalStick.V10Location;

public class PortalStickEntityListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickEntityListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if(plugin.config.DisabledWorlds.contains(event.getEntity().getLocation().getWorld().getName()))
		  return;
		
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			if (!plugin.hasPermission(player, plugin.PERM_DAMAGE_BOOTS))
			  return;
			Region region = plugin.regionManager.getRegion(new V10Location(player.getLocation()));
			ItemStack is = player.getInventory().getBoots();
			if (event.getCause() == DamageCause.FALL && region.getBoolean(RegionSetting.ENABLE_FALL_DAMAGE_BOOTS))
			{
			  boolean ok;
			  if(is == null)
				ok = false;
			  else
				ok = region.getInt(RegionSetting.FALL_DAMAGE_BOOTS) == is.getTypeId();
			  if(ok)
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if(plugin.config.DisabledWorlds.contains(event.getLocation().getWorld().getName()))
		  return;
		Region region = plugin.regionManager.getRegion(new V10Location(event.getLocation()));
		Iterator<Block> iter = event.blockList().iterator();
		Block block;
		V10Location loc;
		Portal portal;
		while(iter.hasNext())
		{
			block = iter.next();
			loc = new V10Location(block.getLocation());
			if (block.getType() == Material.WOOL)
			{
				portal = plugin.portalManager.borderBlocks.get(loc);
				if (portal == null)
				  portal = plugin.portalManager.insideBlocks.get(loc);
				if (portal == null)
				  portal = plugin.portalManager.behindBlocks.get(loc);
				if (portal != null)
				{
					if (region.getBoolean(RegionSetting.PROTECT_PORTALS_FROM_TNT))
					  iter.remove();
					else
					{
					  portal.delete();
					  return;
					}
				}
			}
			else if (plugin.blockUtil.compareBlockToString(block, region.getString(RegionSetting.GRILL_MATERIAL)))
			{
				Grill grill = plugin.grillManager.insideBlocks.get(loc);
				if (grill == null) grill = plugin.grillManager.borderBlocks.get(loc);
				if (grill != null )
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void spawn(EntityAddEvent event)
	{
	  Entity entity = event.getEntity();
	  if(plugin.config.DisabledWorlds.contains(entity.getLocation().getWorld().getName()))
		return;
//	  System.out.print("Spawned: "+entity.getType());
	  plugin.userManager.createUser(entity);
	  User user = plugin.userManager.getUser(entity);
	  Region region = plugin.regionManager.getRegion(new V10Location(entity.getLocation()));
	  if(entity instanceof InventoryHolder && !region.name.equals("global") && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
		user.saveInventory((InventoryHolder)entity);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void despawn(EntityRemoveEvent event)
	{
	  Entity entity = event.getEntity();
	  if(plugin.config.DisabledWorlds.contains(entity.getLocation().getWorld().getName()))
		return;
	  
	  if(plugin.gelManager.flyingGels.containsKey(entity.getUniqueId()))
	  {
		V10Location from = plugin.gelManager.flyingGels.get(entity.getUniqueId());
		Location loc = entity.getLocation();
		V10Location vloc = new V10Location(loc);
		Block b = loc.getBlock();
		if(!plugin.grillManager.insideBlocks.containsKey(vloc))
		  b.setType(Material.AIR);
		FallingBlock fb = (FallingBlock)entity;
		Block b2;
		int mat = fb.getBlockId();
		byte data = fb.getBlockData();
		ArrayList<BlockHolder> blocks;
		if(plugin.gelManager.gels.containsKey(from))
		  blocks = plugin.gelManager.gels.get(from);
		else
		{
		  blocks = new ArrayList<BlockHolder>();
		  plugin.gelManager.gels.put(from, blocks);
		}
		BlockHolder bh;
		for(BlockFace face: new BlockFace[] {BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP})
		{
		  b2 = b.getRelative(face);
		  if(b2.getType() != Material.AIR && !b2.isLiquid())
		  {
			vloc = new V10Location(b2);
			if(plugin.portalManager.borderBlocks.containsKey(vloc) ||
					plugin.portalManager.insideBlocks.containsKey(vloc) ||
					plugin.portalManager.behindBlocks.containsKey(vloc) ||
					plugin.grillManager.borderBlocks.containsKey(vloc) ||
					plugin.grillManager.insideBlocks.containsKey(vloc) ||
					plugin.funnelBridgeManager.bridgeBlocks.containsKey(vloc) ||
					plugin.funnelBridgeManager.bridgeMachineBlocks.containsKey(vloc))
			  continue;
			bh = new BlockHolder(b2);
			if(!blocks.contains(bh))
			{
			  if(plugin.gelManager.gelMap.containsKey(bh))
				bh = plugin.gelManager.gelMap.get(bh);
			  else
				plugin.gelManager.gelMap.put(bh, bh);
			  blocks.add(bh);
			  b2.setTypeIdAndData(mat, data, true);
			}
		  }
		}
	  }
	  
	  User user = plugin.userManager.getUser(entity);
//	  System.out.print("Despawned: "+entity.getType());
	  
	  Region region = plugin.regionManager.getRegion(new V10Location(entity.getLocation()));
	  if(entity instanceof InventoryHolder && region.name != "global" && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
		user.revertInventory((InventoryHolder)entity);
	  plugin.userManager.deleteUser(user);
	  if(entity instanceof Player) //TODO
		plugin.gelManager.resetPlayer((Player)entity);
	}
	
	@EventHandler
	public void entityMove(EntityMoveEvent event)
	{
	  Entity entity = event.getEntity();
	  if(entity instanceof Player || (entity instanceof Vehicle && !(entity instanceof Pig)))
		return;
	  World world = entity.getWorld();
	  plugin.entityManager.onEntityMove(entity, new Location(world, event.getFromX(), event.getFromY(), event.getFromZ(), event.getFromYaw(), event.getFromPitch()), new Location(world, event.getToX(), event.getToY(), event.getToZ(), event.getToYaw(), event.getToPitch()), true);
	}
}
