/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.block.BlockSnapshot
 *  org.spongepowered.api.world.Chunk
 *  org.spongepowered.api.world.Location
 *  org.spongepowered.api.world.World
 */
package net.craftersland.restarter.fixes;

import java.util.ArrayList;
import net.craftersland.restarter.RR;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class RemoveBlocks {
    private RR pl;
    private boolean isRunning = false;

    public RemoveBlocks(RR plugin) {
        this.pl = plugin;
    }

    public void removeBlocks(final World world) {
        if (!this.isRunning) {
            this.isRunning = true;
            long startTime = System.currentTimeMillis();
            try {
                RR.log.info("Block removal task started for: " + world.getName());
                ArrayList<Chunk> chunks = new ArrayList<Chunk>();
                for (Chunk ch : world.getLoadedChunks()) {
                    chunks.add(ch);
                }
                if (chunks.size() > 0) {
                    for (final Chunk c : chunks) {
                        if (!c.isLoaded()) continue;
                        ArrayList<Location> toRemove = new ArrayList<Location>();
                        int x = c.getPosition().getX() * 16;
                        while (x < c.getPosition().getX() * 16 + 16) {
                            int y = 0;
                            while (y < 255) {
                                int z = c.getPosition().getZ() * 16;
                                while (z < c.getPosition().getZ() * 16 + 16) {
                                    Location block = c.getLocation(x, y, z);
                                    for (String s : this.pl.getConfigHandler().getStringList("BlockToRemove")) {
                                        String locationString;
                                        String[] data = s.split("#");
                                        if (data.length > 1) {
                                            if (block.getBlock().getType().getName().equals(data[0])) {
                                                if (block.getBlock().getType().getName().matches("ic2:te")) {
                                                    if (this.pl.getDataHandler().getIc2Type(block.createSnapshot().getExtendedState()).contains("chunk_loader")) {
                                                        locationString = this.createLocationString(block.createSnapshot());
                                                        if (!this.pl.getStorageHandler().isLocationInDb(locationString)) {
                                                            toRemove.add(block);
                                                        }
                                                    }
                                                } else if (this.pl.getDataHandler().getBlockUnsafeDamage(block.createSnapshot().getState()) == Integer.parseInt(data[1])) {
                                                    locationString = this.createLocationString(block.createSnapshot());
                                                    if (!this.pl.getStorageHandler().isLocationInDb(locationString)) {
                                                        toRemove.add(block);
                                                    }
                                                }
                                            }
                                        } else if (block.getBlock().getType().getName().equalsIgnoreCase(s)) {
                                            locationString = this.createLocationString(block.createSnapshot());
                                            if (!this.pl.getStorageHandler().isLocationInDb(locationString)) {
                                                toRemove.add(block);
                                            }
                                        }
                                        if (!block.getBlock().getType().getName().contains("chunk")) continue;
                                        locationString = this.createLocationString(block.createSnapshot());
                                        if (!this.pl.getStorageHandler().isLocationInDb(locationString)) continue;
                                        toRemove.remove(block);
                                    }
                                    ++z;
                                }
                                ++y;
                            }
                            ++x;
                        }
                        if (toRemove.isEmpty()) continue;
                        final ArrayList toRemoveFinal = new ArrayList(toRemove);
                        Sponge.getScheduler().createTaskBuilder().execute(new Runnable(){

                            @Override
                            public void run() {
                                RR.log.info("Removing blocks " + toRemoveFinal.size() + " - " + world.getName() + " .Chunk: " + c.toString());
                                for (Location b : toRemoveFinal) {
                                    if (!((Chunk)b.getExtent()).isLoaded()) continue;
                                    b.removeBlock();
                                }
                            }
                        }).submit((Object)this.pl);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            this.isRunning = false;
            long endTime = System.currentTimeMillis() - startTime;
            RR.log.info("Removing blocks completed in " + endTime / 1000L + " seconds. - " + world.getName());
        }
    }

    private String createLocationString(BlockSnapshot block) {
        return String.valueOf(((Location)block.getLocation().get()).getBlockX()) + "#" + ((Location)block.getLocation().get()).getBlockY() + "#" + ((Location)block.getLocation().get()).getBlockZ() + "#" + ((World)((Location)block.getLocation().get()).getExtent()).getName();
    }
}

