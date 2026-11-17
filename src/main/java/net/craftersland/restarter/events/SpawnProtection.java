/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.flowpowered.math.vector.Vector3d
 *  com.sk89q.worldedit.Vector
 *  org.spongepowered.api.block.BlockSnapshot
 *  org.spongepowered.api.data.Transaction
 *  org.spongepowered.api.data.type.HandTypes
 *  org.spongepowered.api.entity.Entity
 *  org.spongepowered.api.entity.EntityTypes
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.block.ChangeBlockEvent$Place
 *  org.spongepowered.api.event.block.InteractBlockEvent
 *  org.spongepowered.api.event.entity.SpawnEntityEvent
 *  org.spongepowered.api.event.item.inventory.InteractItemEvent
 *  org.spongepowered.api.item.inventory.ItemStack
 *  org.spongepowered.api.world.Location
 */
package net.craftersland.restarter.events;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.worldedit.Vector;
import java.util.Optional;
import net.craftersland.restarter.RR;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;

public class SpawnProtection {
    private RR pl;

    public SpawnProtection(RR pl) {
        this.pl = pl;
    }

    @Listener
    public void onItemInteract(InteractItemEvent e) {
        Optional p = e.getCause().first(Player.class);
        if (p.isPresent() && ((Player)p.get()).getWorld().getName().matches(this.pl.getConfigHandler().getString("SpawnProtection", "World")) && !((Player)p.get()).hasPermission("RR.admin") && e.getInteractionPoint().isPresent()) {
            Vector locV = new Vector(((Vector3d)e.getInteractionPoint().get()).getX(), ((Vector3d)e.getInteractionPoint().get()).getY(), ((Vector3d)e.getInteractionPoint().get()).getZ());
            if (this.pl.getSpawnProtectionUtils().getSpawnArea().contains(locV)) {
                for (String s : this.pl.getConfigHandler().getStringList("Spawn-Item-Usage-Restricted")) {
                    if (!e.getItemStack().getType().getName().matches(s)) continue;
                    e.setCancelled(true);
                    if (((Player)p.get()).getItemInHand(HandTypes.MAIN_HAND).isPresent() && ((ItemStack)((Player)p.get()).getItemInHand(HandTypes.MAIN_HAND).get()).getType() == e.getItemStack().getType()) {
                        ((Player)p.get()).setItemInHand(HandTypes.MAIN_HAND, null);
                    } else if (((Player)p.get()).getItemInHand(HandTypes.OFF_HAND).isPresent() && ((ItemStack)((Player)p.get()).getItemInHand(HandTypes.OFF_HAND).get()).getType() == e.getItemStack().getType()) {
                        ((Player)p.get()).setItemInHand(HandTypes.OFF_HAND, null);
                    }
                    this.pl.getSoundHandler().sendFailedSound((Player)p.get());
                    ((Player)p.get()).sendMessage(this.pl.getConfigHandler().getTextWithColor("ChatMessage", "SpawnUsageBlocked"));
                    return;
                }
            }
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place e) {
        Optional p = e.getCause().first(Player.class);
        if (p.isPresent() && ((Player)p.get()).getWorld().getName().matches(this.pl.getConfigHandler().getString("SpawnProtection", "World")) && e.getCause().toString().contains("EntityLightningBolt")) {
            for (Transaction bs : e.getTransactions()) {
                Vector locV = new Vector(((Location)((BlockSnapshot)bs.getFinal()).getLocation().get()).getBlockX(), ((Location)((BlockSnapshot)bs.getFinal()).getLocation().get()).getBlockY(), ((Location)((BlockSnapshot)bs.getFinal()).getLocation().get()).getBlockZ());
                if (!this.pl.getSpawnProtectionUtils().getSpawnArea().contains(locV)) continue;
                e.setCancelled(true);
                break;
            }
        }
    }

    @Listener
    public void onEntitySpawn(SpawnEntityEvent e) {
        for (Entity en : e.getEntities()) {
            if (en.getType() == EntityTypes.PLAYER || en.getType() == EntityTypes.ITEM_FRAME || en.getType() == EntityTypes.ITEM || en.getType() == EntityTypes.FIREWORK || en.getType() == EntityTypes.LIGHTNING || en.getType() == EntityTypes.ARMOR_STAND || !en.getWorld().getName().matches(this.pl.getConfigHandler().getString("SpawnProtection", "World"))) continue;
            Vector locV = new Vector(en.getLocation().getBlockX(), en.getLocation().getBlockY(), en.getLocation().getBlockZ());
            if (!this.pl.getSpawnProtectionUtils().getSpawnArea().contains(locV)) continue;
            if (en.getType().getName().contains("itemframe") || en.getType().getName().contains("armorstand")) {
                return;
            }
            if (this.pl.getConfigHandler().getBoolean("Debug").booleanValue()) {
                RR.log.warn("Entity Spawn protection - Entity Type: " + en.getType() + " - Entity Name: " + en.getType().getName());
            }
            e.setCancelled(true);
            break;
        }
    }

    @Listener
    public void onInteractBlock(InteractBlockEvent e) {
        Optional p = e.getCause().first(Player.class);
        if (p.isPresent() && e.getTargetBlock().getLocation().isPresent()) {
            Vector locV = new Vector(((Location)e.getTargetBlock().getLocation().get()).getBlockX(), ((Location)e.getTargetBlock().getLocation().get()).getBlockY(), ((Location)e.getTargetBlock().getLocation().get()).getBlockZ());
            if (this.pl.getSpawnProtectionUtils().getSpawnArea().contains(locV)) {
                if (e.getTargetBlock().getState().getType().getName().matches("extrautils2:screen") && !((Player)p.get()).getName().matches("brunyman")) {
                    e.setCancelled(true);
                    ((Player)p.get()).closeInventory();
                } else if (this.isChiselItemInHand((Player)p.get())) {
                    e.setCancelled(true);
                    ((Player)p.get()).closeInventory();
                    ((Location)e.getTargetBlock().getLocation().get()).setBlock(e.getTargetBlock().getState());
                }
            }
        }
    }

    private boolean isChiselItemInHand(Player p) {
        boolean chisel = false;
        ItemStack mainHand = null;
        ItemStack offHand = null;
        String[] chiselMaterials = new String[]{"chisel:chisel_iron", "chisel:chisel_diamond", "chiselsandbits:chisel_stone", "chiselsandbits:chisel_iron", "chiselsandbits:chisel_gold", "chiselsandbits:chisel_diamond", "chiselsandbits:bitsaw_diamond"};
        if (p.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            mainHand = (ItemStack)p.getItemInHand(HandTypes.MAIN_HAND).get();
        }
        if (p.getItemInHand(HandTypes.OFF_HAND).isPresent()) {
            offHand = (ItemStack)p.getItemInHand(HandTypes.OFF_HAND).get();
        }
        if (mainHand != null || offHand != null) {
            String[] stringArray = chiselMaterials;
            int n = chiselMaterials.length;
            int n2 = 0;
            while (n2 < n) {
                String s = stringArray[n2];
                if (mainHand != null && mainHand.getItem().getType().getName().matches(s)) {
                    chisel = true;
                    break;
                }
                if (offHand != null && offHand.getItem().getType().getName().matches(s)) {
                    chisel = true;
                    break;
                }
                ++n2;
            }
        }
        return chisel;
    }
}

