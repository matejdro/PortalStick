package com.matejdro.bukkit.portalstick;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.util.BlockUtil;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class GelManager {
	public static Location useGel(Player player, Location LocTo, Vector vector)
	{
		Vector fallingspeed = player.getVelocity();
		Region region = RegionManager.getRegion(LocTo);
		Block block = LocTo.getBlock();
		
		if (region.getBoolean(RegionSetting.ENABLE_RED_GEL_BLOCKS) && BlockUtil.compareBlockToString(block.getRelative(0,-1,0), region.getString(RegionSetting.RED_GEL_BLOCK))) return redGel(player, vector, region);

		if (region.getBoolean(RegionSetting.ENABLE_BLUE_GEL_BLOCKS))
		{
			if (fallingspeed.getY() <= 0)
			{
				if (BlockUtil.compareBlockToString(block.getRelative(0,-1,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 0);
				else if (Math.abs(vector.getY()) > 1 && BlockUtil.compareBlockToString(block.getRelative(0,-2,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 0);
				else if (Math.abs(vector.getY()) > 1 && BlockUtil.compareBlockToString(block.getRelative(0,-3,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 0);
			}
			if (vector.getX() >= 0)
			{
				if (BlockUtil.compareBlockToString(block.getRelative(1,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 1);
				else if (Math.abs(vector.getX()) > 1 && BlockUtil.compareBlockToString(block.getRelative(2,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 1);
				else if (Math.abs(vector.getX()) > 1 &&BlockUtil.compareBlockToString(block.getRelative(3,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 1);

			}
			else if (vector.getX() <= 0)
			{
				if (BlockUtil.compareBlockToString(block.getRelative(-1,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 2);
				else if (Math.abs(vector.getX()) > 1 && BlockUtil.compareBlockToString(block.getRelative(-2,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 2);
				else if (Math.abs(vector.getX()) > 1 && BlockUtil.compareBlockToString(block.getRelative(-3,0,0), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 2);

			}
			if (vector.getZ() >= 0)
			{
				if (BlockUtil.compareBlockToString(block.getRelative(0,0,1), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 3);
				else if (Math.abs(vector.getZ()) > 1 && BlockUtil.compareBlockToString(block.getRelative(0,0,2), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 3);
				else if (Math.abs(vector.getZ()) > 1 && BlockUtil.compareBlockToString(block.getRelative(0,0,3), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 3);

			}
			else if (vector.getZ() <=0 )
			{
				if (BlockUtil.compareBlockToString(block.getRelative(0,0,-1), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 4);
				else if (Math.abs(vector.getZ()) < 1 && BlockUtil.compareBlockToString(block.getRelative(0,0,-2), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 4);
				else if (Math.abs(vector.getZ()) > 1 &&BlockUtil.compareBlockToString(block.getRelative(0,0,-3), region.getString(RegionSetting.BLUE_GEL_BLOCK))) return BlueGel(player, vector, region, 4);

			}
		}
		return null;

	}
	
	public static Location BlueGel(Player player, Vector vector, Region region, int direction)
	{
		
//		((Player) player).sendMessage(String.valueOf(vector.getX()));
//		((Player) player).sendMessage(String.valueOf(vector.getY()));
//		((Player) player).sendMessage(String.valueOf(vector.getZ()));
//		((Player) player).sendMessage(".");

		if (player instanceof Player && ((Player) player).isSneaking()) return null;
			
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
						
			Util.PlayNote((Player) player, 4, 5);
		return null;
	}
	
	public static Location redGel(Player player, Vector vector, Region region)
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
		
		//player.sendMessage(String.format("%.5g%n", vector.getX()));
				return null;
	}
}
