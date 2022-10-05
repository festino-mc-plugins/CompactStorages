package com.festp;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	
	private static JavaPlugin plugin;
	private static MemoryConfiguration c;
	
	public static int storage_signal_radius = 30;
	
	public Config(JavaPlugin jp) {
		this.plugin = jp;
		this.c = jp.getConfig();
	}
	
	public static void loadConfig()
	{
		c.addDefault("storage-signal-radius", 30);
		c.options().copyDefaults(true);
		plugin.saveConfig();

		Config.storage_signal_radius = plugin.getConfig().getInt("storage-signal-radius");

		Logger.info("Config reloaded.");
	}
	
	public static void saveConfig()
	{
		c.set("storage-signal-radius", Config.storage_signal_radius);

		plugin.saveConfig();

		Logger.info("Config successfully saved.");
	}
	
	public static JavaPlugin plugin() {
		return plugin;
	}
}
