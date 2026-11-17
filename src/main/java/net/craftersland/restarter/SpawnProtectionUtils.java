/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sk89q.worldedit.Vector
 *  com.sk89q.worldedit.regions.CuboidRegion
 */
package net.craftersland.restarter;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.craftersland.restarter.RR;

public class SpawnProtectionUtils {
    private RR pl;
    private CuboidRegion cs;
    private CuboidRegion csf;

    public SpawnProtectionUtils(RR plugin) {
        this.pl = plugin;
        this.cs = new CuboidRegion(new Vector(this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos1", "x").intValue(), this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos1", "y").intValue(), this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos1", "z").intValue()), new Vector(this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos2", "x").intValue(), this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos2", "y").intValue(), this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos2", "z").intValue()));
        this.csf = new CuboidRegion(new Vector(this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos1", "x").intValue(), this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos1", "y").intValue(), this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos1", "z").intValue()), new Vector(this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos2", "x").intValue(), this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos2", "y").intValue(), this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos2", "z").intValue()));
    }

    public CuboidRegion getSpawnArea() {
        this.cs = new CuboidRegion(new Vector(this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos1", "x").intValue(), this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos1", "y").intValue(), this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos1", "z").intValue()), new Vector(this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos2", "x").intValue(), this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos2", "y").intValue(), this.pl.getConfigHandler().getInteger("SpawnProtection", "Pos2", "z").intValue()));
        return this.cs;
    }

    public boolean isInSpawnProtection(int x, int y, int z) {
        return this.cs.contains(new Vector(x, y, z));
    }

    public CuboidRegion getSFWorldSpawnArea() {
        this.csf = new CuboidRegion(new Vector(this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos1", "x").intValue(), this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos1", "y").intValue(), this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos1", "z").intValue()), new Vector(this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos2", "x").intValue(), this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos2", "y").intValue(), this.pl.getConfigHandler().getInteger("SkyFactoryWorldSpawn", "Pos2", "z").intValue()));
        return this.csf;
    }

    public boolean isInSFWorldSpawn(int x, int y, int z) {
        return this.csf.contains(new Vector(x, y, z));
    }
}

