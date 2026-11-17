/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.block.BlockSnapshot
 *  org.spongepowered.api.block.BlockTypes
 *  org.spongepowered.api.data.Transaction
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.block.ChangeBlockEvent$Break
 *  org.spongepowered.api.event.block.ChangeBlockEvent$Place
 */
package net.craftersland.restarter.events;

import net.craftersland.restarter.RR;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

public class FireSpread {
    public FireSpread(RR pl) {
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place e) {
        if (e.getCause().root().toString().contains("blockState=minecraft:fire")) {
            e.setCancelled(true);
        }
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break e) {
        if (e.getCause().root().toString().contains("blockState=minecraft:fire")) {
            for (Transaction tr : e.getTransactions()) {
                if (((BlockSnapshot)tr.getOriginal()).getState().getType() == BlockTypes.FIRE) continue;
                e.setCancelled(true);
                break;
            }
        }
    }
}

