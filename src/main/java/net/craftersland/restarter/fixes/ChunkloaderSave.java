/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.block.BlockSnapshot
 *  org.spongepowered.api.data.Transaction
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.block.ChangeBlockEvent$Break
 *  org.spongepowered.api.event.block.ChangeBlockEvent$Place
 *  org.spongepowered.api.event.network.ClientConnectionEvent$Disconnect
 *  org.spongepowered.api.event.network.ClientConnectionEvent$Join
 *  org.spongepowered.api.world.Location
 *  org.spongepowered.api.world.World
 */
package net.craftersland.restarter.fixes;

import java.util.HashSet;
import java.util.Optional;
import net.craftersland.restarter.RR;
import net.craftersland.restarter.objects.SavedChunkloaders;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class ChunkloaderSave {
    private RR pl;

    public ChunkloaderSave(RR plugin) {
        this.pl = plugin;
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        for (Transaction tr : event.getTransactions()) {
            Optional p;
            if (!this.isChunkLoader((BlockSnapshot)tr.getOriginal()) || !(p = event.getCause().first(Player.class)).isPresent() || !this.isSavedWorld(((Player)p.get()).getWorld())) continue;
            final String locationString = this.createLocationString((BlockSnapshot)tr.getOriginal());
            SavedChunkloaders chunkData = this.pl.getChunkloadersLoadedPlayers().get(p.get());
            if (chunkData.isSavedChunkloader(locationString)) {
                chunkData.removeChunkloader(locationString);
                this.pl.getSoundHandler().sendConfirmSound((Player)p.get());
                ((Player)p.get()).sendMessage(this.pl.getConfigHandler().getTextWithColorAndPlaceholders(String.valueOf(chunkData.getMaxChunkloadersAmount()), String.valueOf(chunkData.getSavedChunkloadersAmount()), "ChatMessage", "ChunkLoaderRemoved"));
                Sponge.getScheduler().createTaskBuilder().async().execute(new Runnable(){

                    @Override
                    public void run() {
                        ChunkloaderSave.this.pl.getStorageHandler().removeChunkLoader(locationString);
                    }
                }).submit((Object)this.pl);
                continue;
            }
            Sponge.getScheduler().createTaskBuilder().async().execute(new Runnable(){

                @Override
                public void run() {
                    if (ChunkloaderSave.this.pl.getStorageHandler().isLocationInDb(locationString)) {
                        ChunkloaderSave.this.pl.getStorageHandler().removeChunkLoader(locationString);
                    }
                }
            }).submit((Object)this.pl);
        }
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event) {
        for (Transaction tr : event.getTransactions()) {
            Optional p;
            if (!this.isChunkLoader((BlockSnapshot)tr.getFinal()) || !(p = event.getCause().first(Player.class)).isPresent() || !this.isSavedWorld(((Player)p.get()).getWorld())) continue;
            SavedChunkloaders chunkData = this.pl.getChunkloadersLoadedPlayers().get(p.get());
            if (chunkData.getMaxChunkloadersAmount() - chunkData.getSavedChunkloadersAmount() > 0) {
                final String locationString = this.createLocationString((BlockSnapshot)tr.getFinal());
                chunkData.saveChunkloader(locationString);
                this.pl.getSoundHandler().sendCompleteSound((Player)p.get());
                ((Player)p.get()).sendMessage(this.pl.getConfigHandler().getTextWithColorAndPlaceholders(String.valueOf(chunkData.getMaxChunkloadersAmount()), String.valueOf(chunkData.getSavedChunkloadersAmount()), "ChatMessage", "ChunkLoaderSaved"));
                Sponge.getScheduler().createTaskBuilder().async().execute(new Runnable(){

                    @Override
                    public void run() {
                        ChunkloaderSave.this.pl.getStorageHandler().saveChunkloader(locationString, ((Player)p.get()).getName().toLowerCase());
                    }
                }).submit((Object)this.pl);
                continue;
            }
            this.pl.getSoundHandler().sendFailedSound((Player)p.get());
            ((Player)p.get()).sendMessage(this.pl.getConfigHandler().getTextWithColorAndPlaceholders(String.valueOf(chunkData.getMaxChunkloadersAmount()), null, "ChatMessage", "MaxChunkLoaderReached"));
        }
    }

    @Listener
    public void onJoin(final ClientConnectionEvent.Join event) {
        if (this.pl.getConfigHandler().getBoolean("Debug").booleanValue()) {
            RR.log.warn("Debug join 1 - name - " + event.getTargetEntity().getName());
        }
        Sponge.getScheduler().createTaskBuilder().async().execute(new Runnable(){

            @Override
            public void run() {
                if (event.getTargetEntity().isOnline()) {
                    SavedChunkloaders data = new SavedChunkloaders(ChunkloaderSave.this.pl, event.getTargetEntity());
                    ChunkloaderSave.this.pl.getChunkloadersLoadedPlayers().put(event.getTargetEntity(), data);
                    ChunkloaderSave.this.checkIfChunkloaderIsPresent(data);
                    if (ChunkloaderSave.this.pl.getConfigHandler().getBoolean("Debug").booleanValue()) {
                        RR.log.warn("Debug join 2 - saved chunkloaders - " + data.getSavedChunkloadersAmount() + " - " + event.getTargetEntity().getName());
                    }
                }
            }
        }).submit((Object)this.pl);
    }

    @Listener
    public void onDisconnect(final ClientConnectionEvent.Disconnect event) {
        if (this.pl.getConfigHandler().getBoolean("Debug").booleanValue()) {
            RR.log.warn("Debug leave 1 - saved chunkloaders - " + event.getTargetEntity().getName());
        }
        if (this.pl.getChunkloadersLoadedPlayers().containsKey(event.getTargetEntity())) {
            Sponge.getScheduler().createTaskBuilder().async().execute(new Runnable(){

                @Override
                public void run() {
                    ChunkloaderSave.this.pl.getChunkloadersLoadedPlayers().remove(event.getTargetEntity());
                    ChunkloaderSave.this.pl.getStorageHandler().updateChunkloadersLastSeen(event.getTargetEntity().getName().toLowerCase());
                    if (ChunkloaderSave.this.pl.getConfigHandler().getBoolean("Debug").booleanValue()) {
                        RR.log.warn("Debug leave 2 - saved chunkloaders - " + event.getTargetEntity().getName());
                    }
                }
            }).submit((Object)this.pl);
        }
    }

    private boolean isSavedWorld(World world) {
        for (String s : this.pl.getConfigHandler().getStringList("SavedChunloadersWorlds")) {
            if (!world.getName().matches(s)) continue;
            return true;
        }
        return false;
    }

    private boolean isChunkLoader(BlockSnapshot block) {
        boolean result = false;
        for (String s : this.pl.getConfigHandler().getStringList("Chunkloaders")) {
            String[] data = s.split("#");
            if (data.length > 1) {
                if (!block.getState().getType().getName().equals(data[0])) continue;
                if (block.getState().getType().getName().matches("ic2:te")) {
                    String rawData = this.pl.getDataHandler().getIc2Type(block.getExtendedState());
                    if (!rawData.contains("invalid") && !rawData.contains("chunk_loader")) continue;
                    result = true;
                    continue;
                }
                if (this.pl.getDataHandler().getBlockUnsafeDamage(block.getState()) != Integer.parseInt(data[1])) continue;
                result = true;
                continue;
            }
            if (!block.getState().getType().getName().equals(s)) continue;
            result = true;
        }
        return result;
    }

    private String createLocationString(BlockSnapshot block) {
        return String.valueOf(((Location)block.getLocation().get()).getBlockX()) + "#" + ((Location)block.getLocation().get()).getBlockY() + "#" + ((Location)block.getLocation().get()).getBlockZ() + "#" + ((World)((Location)block.getLocation().get()).getExtent()).getName();
    }

    private void checkIfChunkloaderIsPresent(final SavedChunkloaders data) {
        Sponge.getScheduler().createTaskBuilder().delayTicks(20L).execute(new Runnable(){

            @Override
            public void run() {
                if (!data.getChunkloadersLocation().isEmpty()) {
                    for (final String s : new HashSet<String>(data.getChunkloadersLocation())) {
                        Location l;
                        String[] rawLocation = s.split("#");
                        Optional w = Sponge.getServer().getWorld(rawLocation[3]);
                        if (!w.isPresent() || ChunkloaderSave.this.isChunkLoader((l = ((World)w.get()).getLocation(Integer.parseInt(rawLocation[0]), Integer.parseInt(rawLocation[1]), Integer.parseInt(rawLocation[2]))).createSnapshot())) continue;
                        data.removeLocation(s);
                        Sponge.getScheduler().createTaskBuilder().async().execute(new Runnable(){

                            @Override
                            public void run() {
                                ChunkloaderSave.this.pl.getStorageHandler().removeChunkLoader(s);
                            }
                        }).submit((Object)ChunkloaderSave.this.pl);
                    }
                }
            }
        }).submit((Object)this.pl);
    }
}

