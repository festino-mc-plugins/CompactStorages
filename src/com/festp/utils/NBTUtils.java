package com.festp.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class NBTUtils
{
	private static JavaPlugin plugin;
	
	public static void setPlugin(JavaPlugin plugin)
	{
		NBTUtils.plugin = plugin;
	}
	private static boolean isValid(ItemStack stack) {
		return stack != null && stack.hasItemMeta();
	}

	/** @return null if couldn't get value*/
	public static Integer getInt(ItemStack stack, String key)
	{
		if (!isValid(stack))
			return null;
		NamespacedKey nameKey = new NamespacedKey(plugin, key);
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if (!container.has(nameKey, PersistentDataType.INTEGER))
			return null;
		return container.get(nameKey, PersistentDataType.INTEGER);
	}

	/** @return modified copy of <b>stack</b><br> <b>null</b> if <b>stack</b> is null or has no item meta */
	public static ItemStack setInt(ItemStack stack, String key, int val)
	{
		if (!isValid(stack))
			return null;
		NamespacedKey nameKey = new NamespacedKey(plugin, key);
		ItemMeta meta = stack.getItemMeta();
		meta.getPersistentDataContainer().set(nameKey, PersistentDataType.INTEGER, val);
		stack = stack.clone();
		stack.setItemMeta(meta);
		return stack;
	}
}
