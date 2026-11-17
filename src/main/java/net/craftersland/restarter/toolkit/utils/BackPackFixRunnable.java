/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.data.DataQuery
 *  org.spongepowered.api.data.type.HandType
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.item.inventory.ItemStack
 *  org.spongepowered.api.scheduler.Task
 */
package net.craftersland.restarter.toolkit.utils;

import java.util.Optional;
import java.util.function.Consumer;
import net.craftersland.restarter.RR;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;

public class BackPackFixRunnable
implements Consumer<Task> {
    Player p;
    HandType h;
    RR pl;

    public BackPackFixRunnable(Player player, HandType hand, RR plugin) {
        this.p = player;
        this.h = hand;
        this.pl = plugin;
    }

    @Override
    public void accept(Task task) {
        try {
            Optional i = this.p.getItemInHand(this.h);
            if (!i.isPresent()) {
                this.p.closeInventory();
                task.cancel();
            } else if (!(((ItemStack)i.get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Items"})) || ((ItemStack)i.get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Inv"})) || ((ItemStack)i.get()).toContainer().contains(DataQuery.of((String[])new String[]{"UnsafeData", "Slots"})))) {
                this.p.closeInventory();
                task.cancel();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            this.p.closeInventory();
            task.cancel();
        }
    }
}

