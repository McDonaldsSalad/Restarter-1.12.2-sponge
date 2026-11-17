/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.data.type.HandTypes
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.item.inventory.ClickInventoryEvent$Creative
 *  org.spongepowered.api.item.ItemTypes
 *  org.spongepowered.api.item.inventory.ItemStack
 *  org.spongepowered.api.item.inventory.ItemStackSnapshot
 *  org.spongepowered.api.item.inventory.transaction.SlotTransaction
 */
package net.craftersland.restarter.events;

import java.util.Optional;
import net.craftersland.restarter.RR;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

public class CreativeMiddleclickFix {
    private RR pl;

    public CreativeMiddleclickFix(RR pl) {
        this.pl = pl;
    }

    @Listener
    public void onCreativeClick(final ClickInventoryEvent.Creative e) {
        final Optional p = e.getCause().first(Player.class);
        if (p.isPresent() && !e.getTransactions().isEmpty()) {
            Sponge.getScheduler().createTaskBuilder().async().delayTicks(1L).execute(new Runnable(){

                @Override
                public void run() {
                    Optional i;
                    if (((ItemStackSnapshot)((SlotTransaction)e.getTransactions().get(0)).getOriginal()).getType() == ItemTypes.AIR && (i = ((Player)p.get()).getItemInHand(HandTypes.MAIN_HAND)).isPresent() && ((ItemStack)i.get()).getType() != ItemTypes.AIR) {
                        if (CreativeMiddleclickFix.this.pl.getDataHandler().hasNbtTag((ItemStack)i.get()).booleanValue()) {
                            Sponge.getScheduler().createTaskBuilder().execute(new Runnable(){

                                @Override
                                public void run() {
                                    ((Player)p.get()).setItemInHand(HandTypes.MAIN_HAND, CreativeMiddleclickFix.this.pl.getDataHandler().removeNbtTag((ItemStack)i.get()));
                                }
                            }).submit((Object)CreativeMiddleclickFix.this.pl);
                        } else if (CreativeMiddleclickFix.this.isStrongbox((ItemStack)i.get()) && CreativeMiddleclickFix.this.pl.getDataHandler().hasInventory((ItemStack)i.get()).booleanValue()) {
                            Sponge.getScheduler().createTaskBuilder().execute(new Runnable(){

                                @Override
                                public void run() {
                                    ((Player)p.get()).setItemInHand(HandTypes.MAIN_HAND, CreativeMiddleclickFix.this.pl.getDataHandler().removeInventory((ItemStack)i.get()));
                                }
                            }).submit((Object)CreativeMiddleclickFix.this.pl);
                        }
                    }
                }
            }).submit((Object)this.pl);
        }
    }

    private boolean isStrongbox(ItemStack item) {
        return item.getType().getName().matches(this.pl.getConfigHandler().getString("StrongBox-Material")) && this.pl.getDataHandler().isCreativeStrongbox(item) == false;
    }
}

