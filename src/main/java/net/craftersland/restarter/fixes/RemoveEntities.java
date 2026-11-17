/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.entity.Entity
 *  org.spongepowered.api.entity.EntityType
 *  org.spongepowered.api.entity.EntityTypes
 *  org.spongepowered.api.entity.Item
 *  org.spongepowered.api.item.inventory.ItemStackSnapshot
 *  org.spongepowered.api.world.World
 */
package net.craftersland.restarter.fixes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import net.craftersland.restarter.RR;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.World;

public class RemoveEntities {
    private RR pl;
    private boolean isRunning = false;

    public RemoveEntities(RR plugin) {
        this.pl = plugin;
    }

    public void removeEntities(World world) {
        if (!this.isRunning) {
            this.isRunning = true;
            long startTime = System.currentTimeMillis();
            try {
                RR.log.info("Entity removal task started for: " + world.getName());
                ArrayList unsortedEntities = new ArrayList(world.getEntities());
                HashMap sortedEntities = new HashMap();
                for (Entity e : unsortedEntities) {
                    boolean skipEntity = false;
                    if (e.getType() == EntityTypes.ITEM) {
                        if (e instanceof Item && ((ItemStackSnapshot)((Item)e).item().get()).getType().toString().matches("APPLIEDENERGISTICS2_ITEMITEMCRYSTALSEED")) {
                            skipEntity = true;
                        }
                    } else if (e.getType() == EntityTypes.ITEM_FRAME || e.getType() == EntityTypes.PLAYER) {
                        skipEntity = true;
                    } else if (e.getType().getName().matches("worm") || e.getType().getName().contains("itemframe") || e.getType().getName().contains("armorstand")) {
                        skipEntity = true;
                    }
                    if (skipEntity) continue;
                    if (!sortedEntities.containsKey(e.getType())) {
                        ArrayList<Entity> entityMap = new ArrayList<Entity>();
                        entityMap.add(e);
                        sortedEntities.put(e.getType(), entityMap);
                        continue;
                    }
                    ((List)sortedEntities.get(e.getType())).add(e);
                }
                for (EntityType et : sortedEntities.keySet()) {
                    if (((List)sortedEntities.get(et)).size() <= 100) continue;
                    RR.log.info("Removing " + ((List)sortedEntities.get(et)).size() + " . Type: " + et + " - " + world.getName());
                    final ArrayList entitiesForRemoval = new ArrayList((Collection)sortedEntities.get(et));
                    Sponge.getScheduler().createTaskBuilder().execute(new Runnable(){

                        @Override
                        public void run() {
                            for (Entity fe : entitiesForRemoval) {
                                fe.remove();
                            }
                        }
                    }).submit((Object)this.pl);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            this.isRunning = false;
            long endTime = System.currentTimeMillis() - startTime;
            RR.log.info("Removing entities completed for: " + world.getName() + " in " + endTime + " miliseconds.");
        }
    }
}

