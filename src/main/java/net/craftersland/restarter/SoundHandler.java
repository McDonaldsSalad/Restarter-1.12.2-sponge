/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.effect.sound.SoundCategories
 *  org.spongepowered.api.effect.sound.SoundTypes
 *  org.spongepowered.api.entity.living.player.Player
 */
package net.craftersland.restarter;

import net.craftersland.restarter.RR;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;

public class SoundHandler {
    public SoundHandler(RR plugin) {
    }

    public void sendFailedSound(Player p) {
        p.playSound(SoundTypes.BLOCK_NOTE_PLING, SoundCategories.PLAYER, p.getLocation().getPosition(), 1.0, 0.6);
    }

    public void sendCompleteSound(Player p) {
        p.playSound(SoundTypes.ENTITY_PLAYER_LEVELUP, SoundCategories.PLAYER, p.getLocation().getPosition(), 1.0);
    }

    public void sendConfirmSound(Player p) {
        p.playSound(SoundTypes.ENTITY_ARROW_HIT_PLAYER, SoundCategories.PLAYER, p.getLocation().getPosition(), 1.0, 0.6);
    }
}

