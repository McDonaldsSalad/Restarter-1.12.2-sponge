/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.world.ExplosionEvent$Detonate
 */
package net.craftersland.restarter.events;

import net.craftersland.restarter.RR;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.ExplosionEvent;

public class ExplosionDetonate {
    public ExplosionDetonate(RR pl) {
    }

    @Listener
    public void onBlockExplode(ExplosionEvent.Detonate e) {
        e.getAffectedLocations().clear();
    }
}

