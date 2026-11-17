/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.command.SendCommandEvent
 */
package net.craftersland.restarter.events;

import java.util.Optional;
import net.craftersland.restarter.RR;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;

public class NucleusFix {
    public NucleusFix(RR pl) {
    }

    @Listener
    public void onCmd(SendCommandEvent e) {
        Optional p = e.getCause().first(Player.class);
        if (p.isPresent() && e.getCommand().equalsIgnoreCase("delhome")) {
            e.setCommand("deletehome");
        }
    }
}

