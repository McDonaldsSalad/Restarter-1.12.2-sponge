/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.data.DataQuery
 *  org.spongepowered.api.data.type.HandTypes
 *  org.spongepowered.api.entity.Entity
 *  org.spongepowered.api.entity.Item
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.item.inventory.DropItemEvent$Dispense
 *  org.spongepowered.api.event.item.inventory.InteractItemEvent$Secondary
 *  org.spongepowered.api.item.inventory.ItemStack
 *  org.spongepowered.api.item.inventory.ItemStackSnapshot
 */
package net.craftersland.restarter.events;

import java.util.Optional;
import java.util.function.Consumer;
import net.craftersland.restarter.RR;
import net.craftersland.restarter.toolkit.utils.BackPackFixRunnable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class BackpackDupeFix {
    private RR pl;

    public BackpackDupeFix(RR pl) {
        this.pl = pl;
    }

    @Listener
    public void onItemInteract(InteractItemEvent.Secondary e) {
        Optional p = e.getCause().first(Player.class);
        if (p.isPresent()) {
            Optional item;
            if (e.getHandType() == HandTypes.MAIN_HAND) {
                Optional item2 = ((Player)p.get()).getItemInHand(HandTypes.MAIN_HAND);
                if (item2.isPresent() && (((ItemStack)item2.get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Items"})) || ((ItemStack)item2.get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Inv"})) || ((ItemStack)item2.get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Slots"})))) {
                    Sponge.getScheduler().createTaskBuilder().intervalTicks(1L).execute((Consumer)new BackPackFixRunnable((Player)p.get(), e.getHandType(), this.pl)).submit((Object)this.pl);
                }
            } else if (e.getHandType() == HandTypes.OFF_HAND && (item = ((Player)p.get()).getItemInHand(HandTypes.OFF_HAND)).isPresent() && (((ItemStack)item.get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Items"})) || ((ItemStack)item.get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Inv"})) || ((ItemStack)item.get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Slots"})))) {
                Sponge.getScheduler().createTaskBuilder().intervalTicks(1L).execute((Consumer)new BackPackFixRunnable((Player)p.get(), e.getHandType(), this.pl)).submit((Object)this.pl);
            }
        }
    }

    @Listener
    public void onItemDrop(DropItemEvent.Dispense e) {
        Optional p = e.getCause().first(Player.class);
        if (p.isPresent()) {
            for (Entity en : e.getEntities()) {
                Item i = (Item)en;
                if (!((ItemStackSnapshot)i.getItemData().item().get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Items"})) && !((ItemStackSnapshot)i.getItemData().item().get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Inv"})) && !((ItemStackSnapshot)i.getItemData().item().get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Slots"}))) continue;
                ((Player)p.get()).closeInventory();
                return;
            }
        }
    }
}

