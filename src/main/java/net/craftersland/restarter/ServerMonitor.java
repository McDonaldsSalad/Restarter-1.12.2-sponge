/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.world.Chunk
 *  org.spongepowered.api.world.World
 */
package net.craftersland.restarter;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import net.craftersland.restarter.RR;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

public class ServerMonitor {
    private RR rr;
    private long start1;
    private long start2;
    private double usagePercent1 = 0.0;
    private double usagePercent2 = 0.0;
    private int startIndex;
    private double tps1;
    private double tps2;
    private int tpsIndex = 1;
    private int onlinePlayers;
    private int tileEntities = 0;
    private boolean lockedRemoval = false;
    private boolean isLagging = false;
    private static DecimalFormat df2 = new DecimalFormat("#.##");

    public ServerMonitor(RR rr) {
        this.rr = rr;
        this.getExistingData();
        this.updateStartData();
        this.checkForCrashLoop();
    }

    public boolean isServerLagging() {
        return this.isLagging;
    }

    public int getOnlinePlayers() {
        return this.onlinePlayers;
    }

    private int getLoadedChunks() {
        ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        for (Chunk ch : ((World)Sponge.getServer().getWorld("world").get()).getLoadedChunks()) {
            chunks.add(ch);
        }
        return chunks.size();
    }

    public int getTileEntities() {
        return this.tileEntities;
    }

    private void getTileEntitiesCount() {
        this.tileEntities = ((World)Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get()).getTileEntities().size();
    }

    public double getMemoryUsagePercent1() {
        return this.usagePercent1;
    }

    public double getMemoryUsagePercent2() {
        return this.usagePercent2;
    }

    private void updateMemoryUsage() {
        double maxMemory = Runtime.getRuntime().totalMemory() / 1024L;
        double freeMemory = Runtime.getRuntime().freeMemory() / 1024L;
        double usedMemory = maxMemory - freeMemory;
        double usagePercentage = usedMemory / maxMemory * 100.0;
        if (this.tpsIndex == 1) {
            this.usagePercent1 = usagePercentage;
        } else {
            this.usagePercent2 = usagePercentage;
        }
        df2.setRoundingMode(RoundingMode.UP);
        RR.log.info("Memory usage - " + df2.format(this.usagePercent1) + "% - " + df2.format(this.usagePercent2) + "% - TPS " + df2.format(this.tps1) + " " + df2.format(this.tps2));
    }

    private void checkTPS() {
        if (this.tps1 <= 18.0 && this.tps2 <= 18.0) {
            this.isLagging = true;
            this.checkEntitiesAndBlocks();
        } else if (this.tps1 <= 19.0 && this.tps2 <= 19.0) {
            this.isLagging = true;
            this.checkEntities();
        } else if (this.tps1 > 19.0 && this.tps2 > 19.0) {
            this.isLagging = false;
        }
    }

    private void checkEntities() {
        if (!this.lockedRemoval) {
            this.lockedRemoval = true;
            if (this.start1 != 0L && this.start2 != 0L) {
                long time1 = (System.currentTimeMillis() - this.start1) / 1000L;
                long time2 = (System.currentTimeMillis() - this.start2) / 1000L;
                if (time1 > 300L && time2 > 300L) {
                    ArrayList worlds = new ArrayList(Sponge.getServer().getWorlds());
                    for (World w : worlds) {
                        this.rr.getRemoveEntities().removeEntities(w);
                    }
                }
            }
            this.lockedRemoval = false;
        }
    }

    private void checkEntitiesAndBlocks() {
        if (!this.lockedRemoval) {
            this.lockedRemoval = true;
            if (this.start1 != 0L && this.start2 != 0L) {
                long time1 = (System.currentTimeMillis() - this.start1) / 1000L;
                long time2 = (System.currentTimeMillis() - this.start2) / 1000L;
                if (time1 > 300L && time2 > 300L) {
                    ArrayList worlds = new ArrayList(Sponge.getServer().getWorlds());
                    for (World w : worlds) {
                        this.rr.getRemoveEntities().removeEntities(w);
                        this.rr.getRemoveBlocks().removeBlocks(w);
                        if (this.getLoadedChunks() <= this.rr.getConfigHandler().getInteger("MaxLoadedChunks")) continue;
                        this.rr.getRemoveBlocks().removeBlocks(w);
                    }
                }
            }
            this.lockedRemoval = false;
        }
    }

    private void updateStartData() {
        if (this.rr.getMysqlSetup().getConnection() != null) {
            RR.log.info("Updating start data...");
            if (this.startIndex == 1) {
                this.start1 = System.currentTimeMillis();
                this.startIndex = 2;
            } else {
                this.start2 = System.currentTimeMillis();
                this.startIndex = 1;
            }
            this.rr.getStorageHandler().setData(this.rr.getMysqlSetup().getConnection(), this.rr.getConfigHandler().getString("ServerName"), "" + this.start1, "" + this.start2, this.startIndex, this.tps1, this.tps2, this.onlinePlayers, "" + System.currentTimeMillis());
        } else {
            RR.log.info("Connection down! Cant update start data.");
        }
    }

    private void checkForCrashLoop() {
        RR.log.info("Checking for crash loop...");
        if (this.start1 != 0L && this.start2 != 0L && this.rr.getConfigHandler().getBoolean("DisableWhitelistOnStart").booleanValue()) {
            long data1 = (System.currentTimeMillis() - this.start1) / 1000L;
            long data2 = (System.currentTimeMillis() - this.start2) / 1000L;
            if (data1 / 60L <= (long)this.rr.getConfigHandler().getInteger("CrashDetectionTrigger").intValue() && data2 / 60L <= (long)this.rr.getConfigHandler().getInteger("CrashDetectionTrigger").intValue()) {
                RR.log.warn("Crash loop detected! Stopping the server to break the crash loop...");
                this.start1 = 0L;
                this.start2 = 0L;
                this.startIndex = 1;
                this.rr.getStorageHandler().setData(this.rr.getMysqlSetup().getConnection(), this.rr.getConfigHandler().getString("ServerName"), "" + this.start1, "" + this.start2, this.startIndex, this.tps1, this.tps2, this.onlinePlayers, "" + System.currentTimeMillis());
                this.rr.getWrapperLink().dispatchLocalUDPPacket("stopwrapper:" + this.rr.getConfigHandler().getString("ToolKit", "UserPass"));
            }
        }
    }

    private void getExistingData() {
        if (this.rr.getMysqlSetup().getConnection() != null) {
            RR.log.info("Loading data from database...");
            String[] data = this.rr.getStorageHandler().getData(this.rr.getMysqlSetup().getConnection(), this.rr.getConfigHandler().getString("ServerName"));
            this.start1 = Long.parseLong(data[0]);
            this.start2 = Long.parseLong(data[1]);
            this.startIndex = Integer.parseInt(data[2]);
            this.tps1 = Double.parseDouble(data[3]);
            this.tps2 = Double.parseDouble(data[4]);
            this.onlinePlayers = Integer.parseInt(data[5]);
        } else {
            RR.log.info("Connection down! Cant load existing data.");
        }
    }

    public void updateData() {
        this.updateTPS();
        this.updateMemoryUsage();
        this.onlinePlayers = Sponge.getServer().getOnlinePlayers().size();
        this.getTileEntitiesCount();
        this.rr.getStorageHandler().setData(this.rr.getMysqlSetup().getConnection(), this.rr.getConfigHandler().getString("ServerName"), "" + this.start1, "" + this.start2, this.startIndex, this.tps1, this.tps2, this.onlinePlayers, "" + System.currentTimeMillis());
    }

    public double getTPS1() {
        return this.tps1;
    }

    public double getTPS2() {
        return this.tps2;
    }

    public void updateTPS() {
        if (this.tpsIndex == 1) {
            this.tps1 = Sponge.getServer().getTicksPerSecond();
            this.tpsIndex = 2;
        } else {
            this.tps2 = Sponge.getServer().getTicksPerSecond();
            this.tpsIndex = 1;
        }
        this.checkTPS();
    }
}

