/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.entity.Entity
 *  org.spongepowered.api.entity.Item
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.item.inventory.DropItemEvent$Dispense
 */
package net.craftersland.restarter.events;

import java.util.Optional;
import net.craftersland.restarter.RR;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.DropItemEvent;

public class DevNullDupeFix {
    private RR pl;

    public DevNullDupeFix(RR pl) {
        this.pl = pl;
    }

    @Listener
    public void onItemDrop(DropItemEvent.Dispense e) {
        Optional p = e.getCause().first(Player.class);
        if (p.isPresent()) {
            for (Entity en : e.getEntities()) {
                Item i = (Item)en;
                if (!i.getItemType().getName().contains("null") || !i.getItemType().getName().contains("dank")) continue;
                ((Player)p.get()).closeInventory();
                return;
            }
        }
    }
}

