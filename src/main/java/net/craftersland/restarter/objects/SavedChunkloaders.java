/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.entity.living.player.Player
 */
package net.craftersland.restarter.objects;

import java.util.HashSet;
import java.util.Set;
import net.craftersland.restarter.RR;
import org.spongepowered.api.entity.living.player.Player;

public class SavedChunkloaders {
    private RR pl;
    private Player p;
    private int maxChunkloaders;
    private int savedChunkloaders;
    private Set<String> chunkloaderLocations = new HashSet<String>();

    public SavedChunkloaders(RR plugin, Player player) {
        this.pl = plugin;
        this.p = player;
        this.maxChunkloaders = 0;
        this.savedChunkloaders = 0;
        this.loadDatabaseData();
    }

    public void setMaxChunkloaders(int amount) {
        this.maxChunkloaders = amount;
    }

    public boolean isSavedChunkloader(String locationString) {
        if (this.chunkloaderLocations.size() > 0) {
            for (String s : this.chunkloaderLocations) {
                if (!s.matches(locationString)) continue;
                return true;
            }
        }
        return false;
    }

    public void removeChunkloader(String locationString) {
        this.chunkloaderLocations.remove(locationString);
        this.savedChunkloaders = this.chunkloaderLocations.size();
    }

    public void saveChunkloader(String locationString) {
        this.chunkloaderLocations.add(locationString);
        this.savedChunkloaders = this.chunkloaderLocations.size();
    }

    public int getSavedChunkloadersAmount() {
        return this.savedChunkloaders;
    }

    public int getMaxChunkloadersAmount() {
        return this.maxChunkloaders;
    }

    public void removeLocation(String data) {
        this.chunkloaderLocations.remove(data);
        this.savedChunkloaders = this.chunkloaderLocations.size();
    }

    public Player getPlayer() {
        return this.p;
    }

    public Set<String> getChunkloadersLocation() {
        return this.chunkloaderLocations;
    }

    public void clear() {
        this.chunkloaderLocations.clear();
    }

    private void loadDatabaseData() {
        if (!this.pl.getStorageHandler().hasChunkloaderDatabaseData(this.p.getName().toLowerCase())) {
            this.maxChunkloaders = this.pl.getConfigHandler().getInteger("SavedChunkloaders");
            this.pl.getStorageHandler().createChunkloaderAccount(this.p.getName().toLowerCase());
        } else {
            this.maxChunkloaders = this.pl.getStorageHandler().getMaxChunkLoaders(this.p.getName().toLowerCase());
            this.pl.getStorageHandler().loadChunkLoaders(this.p.getName().toLowerCase(), this.chunkloaderLocations);
            if (this.chunkloaderLocations.size() > 0) {
                this.savedChunkloaders = this.chunkloaderLocations.size();
            }
        }
    }
}

