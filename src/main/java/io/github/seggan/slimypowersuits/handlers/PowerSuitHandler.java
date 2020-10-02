package io.github.seggan.slimypowersuits.handlers;

import io.github.seggan.slimypowersuits.SuitUtils;
import io.github.seggan.slimypowersuits.modules.ModuleType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectableAction;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;

public class PowerSuitHandler implements Listener {

    @EventHandler
    public void onPlayerSprint(PlayerToggleSprintEvent e) {
        Player p = e.getPlayer();
        ItemStack leggings = p.getInventory().getLeggings();
        if (leggings == null || leggings.getType() == Material.AIR) {
            return;
        }
        if (!SuitUtils.isPowerSuitPiece(leggings)) {
            return;
        }
        if (SuitUtils.getInstalledModules(leggings).contains(ModuleType.SPEED)) {
            if (e.isSprinting()) {
                p.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        Integer.MAX_VALUE,
                        2,
                        false,
                        false
                ));
            } else {
                p.removePotionEffect(PotionEffectType.SPEED);
            }
        }
    }

    @EventHandler
    public void onPlayerHurt(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            PlayerInventory inv = p.getInventory();

            switch (e.getCause()) {
                case FALL:
                    ItemStack boots = inv.getBoots();
                    if (SuitUtils.isPowerSuitPiece(boots)) {
                        if (SuitUtils.getInstalledModules(boots).contains(ModuleType.NO_FALL_DMG)) {
                            e.setDamage(0);
                        }
                    }
                    break;
                case VOID:
                case LIGHTNING:
                case MAGIC:
                case WITHER:
                case POISON:
                case SUFFOCATION:
                case SUICIDE:
                case STARVATION:
                    return;
                default:
                    if (SlimefunPlugin.getProtectionManager()
                        .hasPermission(p, e.getEntity().getLocation(), ProtectableAction.PVP)) {
                        int pieces = 0;
                        int percent = 0;
                        for (ItemStack armorPiece : inv.getArmorContents()) {
                            if (SuitUtils.isPowerSuitPiece(armorPiece)) {
                                pieces += 1;
                            }
                        }
                        if (pieces == 4) {
                            percent = 30;
                        }
                        for (ItemStack armorPiece : inv.getArmorContents()) {
                            if (SuitUtils.isPowerSuitPiece(armorPiece)) {
                                int protModules = Collections.frequency(
                                    SuitUtils.getInstalledModules(armorPiece),
                                    ModuleType.RESISTANCE
                                );
                                for (int i = 0; i < protModules; i++) {
                                    percent += Math.floorDiv(100 - percent, 4);
                                }
                            }
                        }
                        p.setHealth(p.getHealth() - ((100 - percent) / 100) * e.getFinalDamage());
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void onCrouch(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        ItemStack helmet = p.getInventory().getHelmet();
        if (helmet == null || helmet.getType() == Material.AIR) {
            return;
        }
        if (!SuitUtils.isPowerSuitPiece(helmet)) {
            return;
        }
        if (SuitUtils.getInstalledModules(helmet).contains(ModuleType.GLOWING)) {
            if (e.isSneaking()) {
                for (Entity entity : p.getNearbyEntities(20, 20, 20)) {
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(
                            PotionEffectType.GLOWING,
                            100,
                            0,
                            false,
                            false,
                            false
                        ));
                    }
                }
            }
        }
    }
}
