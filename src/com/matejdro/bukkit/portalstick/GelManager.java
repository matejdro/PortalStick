package com.matejdro.bukkit.portalstick;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class GelManager {
	private final PortalStick plugin;
	
	GelManager(PortalStick plugin)
	{
		this.plugin = plugin;
	}
	
	public void useGel(Player player, V10Location locTo, Vector vector)
	{
		Region region = plugin.regionManager.getRegion(locTo);
		Block block = locTo.getHandle().getBlock();
		
		if (region.getBoolean(RegionSetting.ENABLE_RED_GEL_BLOCKS) && plugin.blockUtil.compareBlockToString(block.getRelative(0,-1,0), region.getString(RegionSetting.RED_GEL_BLOCK))) return;

		if (region.getBoolean(RegionSetting.ENABLE_BLUE_GEL_BLOCKS))
		{
			if (player.getVelocity().getY() <= 0)
			{
				if (plugin.blockUtil.compareBlockToString(block.getRelative(0,-1,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
				else if (Math.abs(vector.getY()) > 1 && plugin.blockUtil.compareBlockToString(block.getRelative(0,-2,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
				else if (Math.abs(vector.getY()) > 1 && plugin.blockUtil.compareBlockToString(block.getRelative(0,-3,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
			}
			if (vector.getX() >= 0)
			{
				if (plugin.blockUtil.compareBlockToString(block.getRelative(1,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
				else if (Math.abs(vector.getX()) > 1 && plugin.blockUtil.compareBlockToString(block.getRelative(2,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
				else if (Math.abs(vector.getX()) > 1 && plugin.blockUtil.compareBlockToString(block.getRelative(3,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;

			}
			else if (vector.getX() <= 0)
			{
				if (plugin.blockUtil.compareBlockToString(block.getRelative(-1,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
				else if (Math.abs(vector.getX()) > 1 && plugin.blockUtil.compareBlockToString(block.getRelative(-2,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
				else if (Math.abs(vector.getX()) > 1 && plugin.blockUtil.compareBlockToString(block.getRelative(-3,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;

			}
			if (vector.getZ() >= 0)
			{
				if (plugin.blockUtil.compareBlockToString(block.getRelative(0,0,1), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
				else if (Math.abs(vector.getZ()) > 1 && plugin.blockUtil.compareBlockToString(block.getRelative(0,0,2), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
				else if (Math.abs(vector.getZ()) > 1 && plugin.blockUtil.compareBlockToString(block.getRelative(0,0,3), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;

			}
			else if (vector.getZ() <=0 )
			{
				if (plugin.blockUtil.compareBlockToString(block.getRelative(0,0,-1), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
				else if (Math.abs(vector.getZ()) < 1 && plugin.blockUtil.compareBlockToString(block.getRelative(0,0,-2), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;
				else if (Math.abs(vector.getZ()) > 1 &&plugin.blockUtil.compareBlockToString(block.getRelative(0,0,-3), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return;

			}
		}
		return;

	}
	
	public void BlueGel(Player player, Vector vector, Region region, int direction)
	{
		if (player instanceof Player && ((Player) player).isSneaking()) return;
			
			switch (direction)
			{
				case 0:
					vector = vector.setY((Math.abs(vector.getY())) * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
					vector = vector.setX(vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					vector = vector.setZ(vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					if (vector.getY() < region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_BOUNCE_VELOCITY)) vector.setY(region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_BOUNCE_VELOCITY));
					break;
				case 1:
					vector = vector.setX(-vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					vector = vector.setY(vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
					vector = vector.setZ(-vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getX() > -region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setX(-region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
				case 2:
					vector = vector.setX(-vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					vector = vector.setY(vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
					vector = vector.setZ(-vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getX() < region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setX(region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
				case 3:
					vector = vector.setX(-vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					vector = vector.setY(vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
					vector = vector.setZ(-vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getZ() > -region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setZ(-region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
				case 4:
					vector = vector.setX(-vector.getX() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					vector = vector.setY(vector.getY() * region.getDouble(RegionSetting.BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER));
					vector = vector.setZ(-vector.getZ() * region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER));
					if (vector.getY() < 1) vector.setY(1);
					if (vector.getZ() < region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY)) vector.setZ(region.getDouble(RegionSetting.BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY));
					break;
					
					
			}
			player.setVelocity(vector);
						
			plugin.util.PlaySound(Sound.GEL_BLUE_BOUNCE, player, new V10Location(player.getLocation()));
	}
	
	public void redGel(Player player, Vector vector, Region region)
	{
		if (player.isSneaking())
		{
			vector = vector.setX(0);
			vector = vector.setZ(0);
		}
		else
		{
			vector = vector.setX(vector.getX() * region.getDouble(RegionSetting.RED_GEL_VELOCITY_MULTIPLIER));
			vector = vector.setZ(vector.getZ() * region.getDouble(RegionSetting.RED_GEL_VELOCITY_MULTIPLIER));
		}
		
		if (vector.getX() > 11) vector = vector.setX(11);
		if (vector.getZ() > 11) vector = vector.setZ(11);
		if (vector.getX() < -11) vector = vector.setX(-11);
		if (vector.getZ() < -11) vector = vector.setZ(-11);
		
		player.setVelocity(vector);
	}
}
