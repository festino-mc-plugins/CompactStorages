	package com.festp;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.festp.storages.Storage;
import com.festp.utils.UtilsType;
import com.festp.storages.StorageBottomless;
import com.festp.storages.StorageMultitype;
import com.festp.storages.StoragesFileManager;

public class CraftManager implements Listener {
	public enum CraftTag { KEEP_DATA, ONLY_SPECIFIC };
	
	Server server;
	Main plugin;
	
	List<NamespacedKey> recipe_keys = new ArrayList<>();
	
	public CraftManager(Main plugin, Server server) {
		this.plugin = plugin;
		this.server = server;
	}
	
	public void addCrafts() {
		plugin.stcraft.addStorageCrafts();
	}
	
	public void giveRecipe(Player p, String recipe) {
		Bukkit.getServer().dispatchCommand(p, "recipe give "+p.getName()+" "+recipe);
	}
	public void giveOwnRecipe(Player p, String recipe) {
		giveRecipe(p, plugin.getName().toLowerCase()+":"+recipe);
	}
	public void giveRecipe(HumanEntity player, NamespacedKey key) {
		player.discoverRecipe(key);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		for(NamespacedKey recipe_name : recipe_keys) {
			giveRecipe(p, recipe_name);
		}
	}
	
	public boolean addCraftbookRecipe(NamespacedKey key) {
		if (recipe_keys.contains(key))
			return false;
		recipe_keys.add(key);
		return true;
	}
	
	public ItemStack applyTag(ItemStack item, CraftTag tag)
	{
    	NamespacedKey key = new NamespacedKey(plugin, tag.toString().toLowerCase());
    	ItemMeta meta = item.getItemMeta();
    	meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte)1);
    	item.setItemMeta(meta);
		return item;
	}
	
	public boolean hasTag(ItemStack item, CraftTag tag)
	{
		NamespacedKey key = new NamespacedKey(plugin, tag.toString().toLowerCase());
    	ItemMeta meta = item.getItemMeta();
    	return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
	}
	
	//test necessary item tags (in craft grid)
	@EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event)
	{
		CraftingInventory ci = event.getInventory();
		ItemStack[] matrix = ci.getMatrix();

		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i] != null && hasTag(matrix[i], CraftTag.ONLY_SPECIFIC))
			{
				//try to find these recipes
				ci.setResult(null);
				return;
			}
		}

		// Uncraft storage
		if (ci.getResult() != null && ci.getResult().getType() == Material.SHULKER_BOX)
		{
			int items = 0;
			boolean have_storage = false, have_firework_star = false;
			for (int i = 0; i < matrix.length; i++)
				if (matrix[i] != null) {
					items++;
					if (Storage.isStorage(matrix[i]))
						have_storage = true;
					if (matrix[i].getType() == Material.FIREWORK_STAR)
						have_firework_star = true;
				}
			if (have_storage && items != 1 || !have_storage && have_firework_star && items == 1)
				ci.setResult(null);
		}
		
		// Storages
		if (Storage.isStorage(ci.getResult()))
		{
			int empty_boxes = 0, boxes = 0;
			for (int i = 0; i < matrix.length; i++)
				if (matrix[i] != null && UtilsType.isShulkerBox(matrix[i].getType()))
				{
					boxes++;
					if (isShulkerBoxEmpty(matrix[i]))
						empty_boxes++;
				}
			if (boxes != empty_boxes)
				ci.setResult(null);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST/*, ignoreCancelled = true*/)
    public void onCraft(CraftItemEvent event)
	{
		if(event.isCancelled())
			return;

		ItemStack craft_result = event.getCurrentItem();
		
		if (Storage.isStorage(craft_result)) {
			int id = StoragesFileManager.nextID++;
			craft_result = Storage.setID(craft_result, id);
			Storage st;
			if (craft_result.getEnchantmentLevel(Enchantment.ARROW_INFINITE) > 0)
				st = new StorageBottomless(id, plugin.mainworld.getFullTime(), StorageBottomless.UNDEFINED_MATERIAL);
			else if (craft_result.getEnchantmentLevel(Enchantment.DIG_SPEED) > 0) {
				int lvl = craft_result.getEnchantmentLevel(Enchantment.DIG_SPEED);
				st = new StorageMultitype(id, plugin.mainworld.getFullTime(), lvl);
			} else {
				event.setCancelled(true);
				return;
			}
			
			st.saveToFile();
			
			event.setCurrentItem(st.getItemStack());
		}
	}
	
	private boolean isShulkerBoxEmpty(ItemStack sb)
	{
		ItemStack[] inv = ((ShulkerBox)((BlockStateMeta) sb.getItemMeta()).getBlockState()).getInventory().getContents();
		for (ItemStack is : inv)
			if (is != null)
				return false;
		return true;
	}
}
