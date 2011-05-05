package com.matejdro.bukkit.portalstick;

import org.bukkit.inventory.Inventory;


public class User {
	private Portal blueportal;
	private Portal orangeportal;
	private Inventory inventory;
	
	public User()
	{
		
	}
	
	public Portal getBluePortal()
	{
		return blueportal;
	}
	
	public void setBluePortal(Portal portal)
	{
		blueportal = portal;
	}
	
	public Portal getOrangePortal()
	{
		return orangeportal;
	}
	
	public void setOrangePortal(Portal portal)
	{
		orangeportal = portal;
	}
	
	public Inventory getInventory()
	{
		return inventory;
	}
	
	public void setInventory(Inventory inv) {
		inventory = inv;
	}
}
