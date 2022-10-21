package com.festp.config;

import java.io.File;
import java.util.HashMap;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.config.FileUtils;

public class AConfig
{
	private MemoryConfiguration config;
	private final HashMap<String, Object> map = new HashMap<>();
	protected JavaPlugin plugin;
	
	public AConfig(JavaPlugin jp) {
		this.plugin = jp;
	}
	
	public void load() {
		File configFile = new File(plugin.getDataFolder(), "config.yml");
		if (!configFile.exists())
			FileUtils.copyFileFromResource(configFile, "config.yml");
		plugin.reloadConfig();
		config = plugin.getConfig();
		map.putAll(config.getValues(true));
		saveSilently();
	}

	public void save() {
		saveSilently();
	}
	public void saveSilently() {
		for (Key key : Key.values()) {
			config.set(key.toString(), get(key));
		}
		plugin.saveConfig();
	}
	
	public void set(Key key, Object value) {
		map.put(key.toString(), value);
		save();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <T extends Object> T get(Key key, T defaultValue) {
		applyDefault(key, defaultValue);
		
		Class<?> clazz;
		if (defaultValue != null) {
			clazz = defaultValue.getClass();
		} else {
			clazz = key.getDefault().getClass();
		}
		
		Object res = map.getOrDefault(key.toString(), defaultValue);
		if (clazz.isInstance(res)) {
			return (T) res;
		}
		return defaultValue;
	}
	public <T extends Object> T get(Key key) {
		return get(key, null);
	}
	
	

	public Object getObject(Key key, Object defaultValue) {
		return map.getOrDefault(key.toString(), defaultValue);
	}

	public Object getObject(Key key) {
		return getObject(key, key.getDefault());
	}
	
	
	
	private void applyDefault(String key, Object defaultValue) {
		if (!map.containsKey(key)) {
			map.put(key, defaultValue);
		}
	}
	private void applyDefault(Key key, Object defaultValue) {
		if (defaultValue == null)
			applyDefault(key);
		else
			applyDefault(key.toString(), defaultValue);
	}
	private void applyDefault(Key key) {
		applyDefault(key.toString(), key.getDefault());
	}
}
