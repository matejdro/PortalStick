package com.matejdro.bukkit.portalstick;


public class PortalStickUser {
	private PortalStickPortal blueportal;
	private PortalStickPortal orangeportal;
	
	public PortalStickUser()
	{
		
	}
	
	public PortalStickPortal getBluePortal()
	{
		return blueportal;
	}
	
	public void setBluePortal(PortalStickPortal portal)
	{
		blueportal = portal;
	}
	
	public PortalStickPortal getOrangePortal()
	{
		return orangeportal;
	}
	
	public void setOrangePortal(PortalStickPortal portal)
	{
		orangeportal = portal;
	}
}
