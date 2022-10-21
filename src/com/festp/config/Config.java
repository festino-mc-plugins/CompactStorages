package com.festp.config;

import org.bukkit.plugin.java.JavaPlugin;

import com.festp.Logger;

public class Config extends AConfig
{	
	public Config(JavaPlugin jp) {
		super(jp);
	}
	
	public void load()
	{
		super.load();
		Logger.info("Config reloaded.");
	}
	
	public void save()
	{
		super.save();
		Logger.info("Config successfully saved.");
	}
}
