package com.festp.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.festp.Main;

public class BeamedPair implements Listener {	
	private static List<BeamedPair> beamed_entities = new ArrayList<>();
	private static Main plugin;
	
	Entity beamer;
	LivingEntity beamed;
	private Guardian workaround;
	private final int really_huge_duration = 1000000000;
	private final ItemStack identificator = new ItemStack(Material.TURTLE_HELMET);
	{
		ItemMeta meta = identificator.getItemMeta();
		
		identificator.setItemMeta(meta);
	}
	
	public BeamedPair(Entity beamer, LivingEntity beamed) {
		this.beamer = beamer;
		this.beamed = beamed;
		workaround = beamer.getWorld().spawn(beamer.getLocation(), Guardian.class);
		Utils.setNoCollide(workaround, true);
		workaround.setInvulnerable(true);
		workaround.setSilent(true);
		workaround.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, really_huge_duration, 1, false, false));
		workaround.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
		workaround.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0);
		workaround.getEquipment().setHelmet(identificator.clone());
		beamer.addPassenger(workaround);
		tick();
	}
	
	public boolean tick() {
		if(!canBeBeamed(beamer, beamed)) {
			workaround.remove();
			return false;
		}
		workaround.teleport(beamer);
		beamer.addPassenger(workaround);
		
		workaround.setPortalCooldown(100);
		workaround.setTarget(beamed);
		return true;
	}
	
	public static boolean canBeBeamed(Entity beamer, LivingEntity beamed) {
		return beamer != null && beamed != null &&
				!beamer.isDead() && !beamed.isDead() &&
				beamer.getWorld() == beamed.getWorld();
				//&& beamed.hasLineOfSight(beamer);
	}

	public static void setPlugin(Main plugin) {
		BeamedPair.plugin = plugin;
	}
	
	public static boolean existsBeamer(Entity beamer) {
		for(BeamedPair beamed : beamed_entities)
			if(beamed.beamer == beamer) {
				return true;
			}
		return false;
	}
	
	public static void add(Entity beamer, LivingEntity beamed) {
		BeamedPair pair = new BeamedPair(beamer, beamed);
    	plugin.getServer().getPluginManager().registerEvents(pair, plugin);
		beamed_entities.add(pair);
	}
	
	public static void tickAll() {
		for(int i = beamed_entities.size()-1; i >= 0; i--) {
			BeamedPair beamed = beamed_entities.get(i);
			if(!beamed.tick())
				beamed_entities.remove(i);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if(event.getDamager() == workaround)
			event.setCancelled(true);
	}
}
