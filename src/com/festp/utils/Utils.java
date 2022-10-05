package com.festp.utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.festp.DelayedTask;
import com.festp.Main;
import com.festp.TaskList;
import com.festp.storages.Storage;
import com.festp.storages.StorageBottomless;
import com.festp.storages.StorageMultitype;

public class Utils {
	private static Main plugin;
	private static UnsafeValues legacy;
	private static Team teamNoCollide;
	private static final String BUKKIT_PACKAGE = "org.bukkit.craftbukkit.";
	public static final double EPSILON = 0.0001;
	
	public static void setPlugin(Main pl) {
		plugin = pl;
		legacy = pl.getServer().getUnsafe();
	}

	public static Main getPlugin() {
		return plugin;
	}
	
	public static void printError(String msg) {
		plugin.getLogger().severe(msg);
	}

	public static void onEnable()
	{
		//create no collide turtle team
		String team_name = "HPTempNoCollide"; //HP is HodgePodge, limit of 16 characters
		Server server = plugin.getServer();
		Scoreboard sb = server.getScoreboardManager().getMainScoreboard();
		teamNoCollide = sb.getTeam(team_name);
		if (teamNoCollide == null)
			teamNoCollide = sb.registerNewTeam(team_name);
		teamNoCollide.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
	}
	public static void onDisable()
	{
		teamNoCollide.unregister(); //if plugin will be removed anywhen
	}
	
	public static void setNoCollide(Entity e, boolean val)
	{
		String entry = e.getUniqueId().toString();
		if (val) {
			if (!teamNoCollide.hasEntry(entry))
				teamNoCollide.addEntry(entry);
		} else {
			teamNoCollide.removeEntry(entry);
		}
	}

	public static String toString(Vector v) {
		if (v == null)
			return "(null)";
		DecimalFormat dec = new DecimalFormat("#0.00");
		return ("("+dec.format(v.getX())+"; "
				  +dec.format(v.getY())+"; "
				  +dec.format(v.getZ())+")")
				.replace(',', '.');
	}
	public static String toString(Location l) {
		if (l == null) return toString((Vector)null);
		return toString(new Vector(l.getX(), l.getY(), l.getZ()));
	}
	public static String toString(Block b) {
		if (b == null) return toString((Location)null);
		return toString(b.getLocation());
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getHead(String headName, String texture_url) {
		ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
		stack = Bukkit.getUnsafe().modifyItemStack(stack,
				"{SkullOwner:{Id:" + UUID.randomUUID().toString() + ",Properties:{textures:[{Value:\"" + texture_url + "\"}]}}}");
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(headName);
		stack.setItemMeta(meta);
		return stack;
	}
	
	public static String ItemStacks_toString(ItemStack[] stacks) {
		String result = "[";
		for(int i = 0; i < stacks.length; i++)
		{
			Storage st = Storage.getByItemStack(stacks[i]);
			if(st != null)
				result += st.toString();
			else
				result += stacks[i];
			if(i != stacks.length-1)
				result += ", ";
		}
		result += "]";
		return result;
	}
	
	public static boolean contains(Object[] list, Object find) {
		for (Object m : list)
			if (m == find)
				return true;
		return false;
	}

	public static boolean equal_invs(Inventory inv1, Inventory inv2) {
		return inv1.toString().endsWith(inv2.toString().substring(inv2.toString().length()-8));
	}
	
	public static boolean isRenamed(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta().hasDisplayName()
				&& !item.getItemMeta().getDisplayName().equals((new ItemStack(item.getType())).getItemMeta().getDisplayName());
	}
	
	/**@return <b>true</b> if the <b>stack</b> was given<br>
	 * <b>false</b> if the <b>stack</b> can't be given without stacking*/
	public static boolean giveUnstackable(Inventory inv, ItemStack stack)
	{
		ItemStack[] stacks = inv.getStorageContents();
		for (int i = 0; i < stacks.length; i++)
		{
			if (stacks[i] == null)
			{
				stacks[i] = stack.clone();
				inv.setStorageContents(stacks);
				stack.setAmount(0);
				return true;
			}
		}
		return false;
	}
	
	/**@return <b>null</b> if the <b>stack</b> was only given<br>
	 * <b>Item</b> if at least one item was dropped*/
	public static Item giveOrDrop(Inventory inv, ItemStack stack)
	{
		int amount = stack.getAmount();
		amount -= StorageMultitype.grabItemStack_stacking(inv, stack);
		if (amount == 0)
			return null;
		amount -= StorageMultitype.grabItemStack_any(inv, stack, amount);
		if (amount == 0)
			return null;
		stack.setAmount(amount);
		HashMap<Integer, ItemStack> res = inv.addItem(stack);
		if (res.isEmpty())
			return null;
		return dropUngiven(inv.getLocation(), res.get(0));
	}
	/** Can give items only to players.
	 * @return <b>null</b> if the <b>stack</b> was only given<br>
	 * <b>Item</b> if at least one item was dropped*/
	public static Item giveOrDrop(Entity entity, ItemStack stack)
	{
		if (entity instanceof Player)
			return giveOrDrop(((Player)entity).getInventory(), stack);
		return dropUngiven(entity.getLocation(), stack);
	}
	private static Item dropUngiven(Location l, ItemStack stack) {
		Item item = l.getWorld().dropItem(l, stack);
		item.setVelocity(new Vector());
		item.setPickupDelay(0);
		return item;
	}
	
	public static int countEmpty(ItemStack[] inv)
	{
		int empty = 0;
		for (ItemStack is : inv)
			if (is == null)
				empty++;
		return empty;
	}
	
	public static <T> int indexOf(T needle, T[] haystack)
	{
	    for (int i=0; i<haystack.length; i++)
	    {
	        if (haystack[i] != null && haystack[i].equals(needle)
	            || needle == null && haystack[i] == null) return i;
	    }

	    return -1;
	}
	public static <T> T next(T needle, T[] haystack)
	{
		int index = Utils.indexOf(needle, haystack);
		if (index < 0)
			return null;
		return haystack[(index + 1) % haystack.length];
	}
	public static <T> T prev(T needle, T[] haystack)
	{
		int index = Utils.indexOf(needle, haystack);
		if (index < 0)
			return null;
		return haystack[((index - 1) % haystack.length + haystack.length) % haystack.length];
	}
	
	public static ItemStack setShulkerInventory(ItemStack shulker_box, Inventory inv)
	{
    	BlockStateMeta im = (BlockStateMeta)shulker_box.getItemMeta();
    	ShulkerBox shulker = (ShulkerBox) im.getBlockState();
    	shulker.getInventory().setContents(inv.getStorageContents());
    	im.setBlockState(shulker);
    	shulker_box.setItemMeta(im);
    	return shulker_box;
	}
	public static Inventory getShulkerInventory(ItemStack shulker_box)
	{
    	BlockStateMeta im = (BlockStateMeta)shulker_box.getItemMeta();
    	ShulkerBox shulker = (ShulkerBox) im.getBlockState();
    	return shulker.getInventory();
	}
	
	public static void delayUpdate(Inventory inv) {
		DelayedTask task = new DelayedTask(1, new Runnable() {
			@Override
			public void run() {
				StorageBottomless.update_item_counts(inv);
				for (HumanEntity human : inv.getViewers()) {
					((Player)human).updateInventory();
				}
				
				// CAN'T PREVENT OFF HAND GLITCH! (InventoryClickEcent cancelling)
				if (inv instanceof PlayerInventory) {
					PlayerInventory pInv = (PlayerInventory) inv;
					ItemStack item = pInv.getItemInOffHand();
					/*if (item == null || UtilsType.isAir(item.getType())) {
						pInv.setItemInOffHand(new ItemStack(Material.STONE));
					} else {
						pInv.setItemInOffHand(null);
					}*/
					pInv.setItemInOffHand(item);
				}
			}
		});
		TaskList.add(task);
	}
}
