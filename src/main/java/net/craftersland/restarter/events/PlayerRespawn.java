/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.command.CommandSource
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent
 */
package net.craftersland.restarter.events;

import net.craftersland.restarter.RR;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;

public class PlayerRespawn {
    private RR pl;

    public PlayerRespawn(RR pl) {
        this.pl = pl;
    }

    @Listener
    public void onPlayerRespawn(final RespawnPlayerEvent e) {
        if (this.pl.getConfigHandler().getBoolean("RandomTeleportOnRespawn").booleanValue()) {
            Sponge.getScheduler().createTaskBuilder().delayTicks(40L).execute(new Runnable(){

                @Override
                public void run() {
                    Sponge.getCommandManager().process((CommandSource)e.getOriginalPlayer(), "rtp");
                }
            }).submit((Object)this.pl);
        }
    }
}

