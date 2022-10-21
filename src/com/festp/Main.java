package com.festp;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.commands.StorageCommand;
import com.festp.config.Config;
import com.festp.menu.InventoryMenu;
import com.festp.storages.StorageCraftManager;
import com.festp.storages.StorageHandler;
import com.festp.storages.StoragesFileManager;
import com.festp.storages.StoragesList;
import com.festp.utils.BeamedPair;
import com.festp.utils.NBTUtils;
import com.festp.utils.TimeUtils;
import com.festp.utils.Utils;

public class Main extends JavaPlugin implements Listener
{
	public static final String storagesdir = "Storages";
	private static String PATH = "plugins" + System.getProperty("file.separator") + "HodgePodge" + System.getProperty("file.separator");
	private static String pluginname;
	
	Config config;
	private CraftManager craftManager;

	public StoragesList stlist = new StoragesList();
	public StoragesFileManager ststorage = new StoragesFileManager();
	public StorageHandler sthandler;
	public StorageCraftManager stcraft;
	
	public World mainworld = null;
	
	public static String getPath() {
		return PATH;
	}
	
	long t1;
	public void onEnable()
	{
		Logger.setLogger(getLogger());
		pluginname = getName();
		PATH = "plugins" + System.getProperty("file.separator") + pluginname + System.getProperty("file.separator");
    	PluginManager pm = getServer().getPluginManager();
    	
		InventoryMenu.setPlugin(this);
		BeamedPair.setPlugin(this);
		Utils.setPlugin(this);
		Utils.onEnable();
		NBTUtils.setPlugin(this);
		getServer().getPluginManager().registerEvents(this, this);
		
		// would better getting main world from server config
		for (World temp_world : getServer().getWorlds())
			if (temp_world.getEnvironment() == Environment.NORMAL) {
				mainworld = temp_world;
				break;
			}
		if (mainworld == null)
			mainworld = getServer().getWorlds().get(0);
		
		config = new Config(this);
		config.load();
    	craftManager = new CraftManager(this, getServer());
    	stcraft = new StorageCraftManager(this, getServer());
    	sthandler = new StorageHandler(this, config, ststorage, stlist);
		
		int maxID = 0;
		for (Integer ID : StoragesFileManager.getIDList())
			if (ID > maxID)
				maxID = ID;
		StoragesFileManager.nextID = maxID+1;
		getLogger().info("New storages will start from ID " + StoragesFileManager.nextID + ".");
    	
    	StorageCommand storage_worker = new StorageCommand(stlist, ststorage);
    	getCommand(StorageCommand.ST_COMMAND).setExecutor(storage_worker);

    	pm.registerEvents(sthandler, this);
    	
    	craftManager.addCrafts();
    	pm.registerEvents(craftManager, this);
    	
		t1 = System.nanoTime();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
			new Runnable() {
				public void run() {
					TimeUtils.addTick();
					
					TaskList.tick();
					
					// beam, unload, save, process
					sthandler.onTick();
					
					// move and remove
					BeamedPair.tickAll();
				}
			}, 0L, 1L);
		
	}
	
	public CraftManager getCraftManager()
	{
		return craftManager;
	}
	
	public void onDisable()
	{
		Utils.onDisable();
		
		stlist.saveStorages();
		for (Player p : getServer().getOnlinePlayers()) {
			p.closeInventory();
		}
	}
}
