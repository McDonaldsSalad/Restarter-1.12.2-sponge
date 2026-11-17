/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sk89q.worldedit.Vector
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.command.CommandSource
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.network.ClientConnectionEvent$Disconnect
 *  org.spongepowered.api.event.network.ClientConnectionEvent$Join
 *  org.spongepowered.api.world.World
 */
package net.craftersland.restarter.events;

import com.sk89q.worldedit.Vector;
import java.io.File;
import net.craftersland.restarter.RR;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.World;

public class PlayerFirstJoin {
    private RR pl;

    public PlayerFirstJoin(RR pl) {
        this.pl = pl;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join e) {
        File dataFile = new File(String.valueOf(Sponge.getServer().getDefaultWorldName()) + System.getProperty("file.separator") + "playerdata" + System.getProperty("file.separator") + e.getTargetEntity().getUniqueId() + ".dat");
        if (!dataFile.exists()) {
            Sponge.getCommandManager().process((CommandSource)e.getTargetEntity().getPlayer().get(), "spawn");
            if (this.pl.getConfigHandler().getBoolean("Give-Claim-Kit").booleanValue()) {
                Sponge.getCommandManager().process((CommandSource)Sponge.getServer().getConsole(), "kit give " + e.getTargetEntity().getName() + " claim");
            }
        } else if (this.pl.getConfigHandler().getBoolean("Fix-SkyFactory-SpawnJoin").booleanValue()) {
            Player p = e.getTargetEntity();
            if (((World)p.getLocation().getExtent()).getName().matches("world")) {
                Vector locV = new Vector(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
                if (this.pl.getSpawnProtectionUtils().getSFWorldSpawnArea().contains(locV)) {
                    p.transferToWorld((World)Sponge.getServer().getWorld("spawn").get());
                }
            }
        } else if (this.pl.getConfigHandler().getBoolean("Fix-MiningWorld-Join").booleanValue()) {
            Player p = e.getTargetEntity();
            if (((World)p.getLocation().getExtent()).getName().matches(this.pl.getConfigHandler().getString("MiningWorldName"))) {
                p.transferToWorld((World)Sponge.getServer().getWorld("world").get());
            }
        }
    }

    @Listener
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect e) {
        Player p;
        if (this.pl.getConfigHandler().getBoolean("Fix-SkyFactory-SpawnJoin").booleanValue() && ((World)(p = e.getTargetEntity()).getLocation().getExtent()).getName().matches("spawn")) {
            p.transferToWorld((World)Sponge.getServer().getWorld("world").get());
        }
    }
}

