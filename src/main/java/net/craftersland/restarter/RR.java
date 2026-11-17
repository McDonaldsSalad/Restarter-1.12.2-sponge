/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Inject
 *  org.slf4j.Logger
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.command.CommandCallable
 *  org.spongepowered.api.command.CommandManager
 *  org.spongepowered.api.command.args.CommandElement
 *  org.spongepowered.api.command.args.GenericArguments
 *  org.spongepowered.api.command.spec.CommandExecutor
 *  org.spongepowered.api.command.spec.CommandSpec
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.game.state.GameStartedServerEvent
 *  org.spongepowered.api.event.game.state.GameStoppedServerEvent
 *  org.spongepowered.api.plugin.Plugin
 *  org.spongepowered.api.plugin.PluginContainer
 *  org.spongepowered.api.scheduler.Task
 *  org.spongepowered.api.text.Text
 */
package net.craftersland.restarter;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import net.craftersland.restarter.CommandHandler;
import net.craftersland.restarter.ConfigHandler;
import net.craftersland.restarter.DataHandler;
import net.craftersland.restarter.Engine;
import net.craftersland.restarter.ServerMonitor;
import net.craftersland.restarter.SoundHandler;
import net.craftersland.restarter.SpawnProtectionUtils;
import net.craftersland.restarter.events.BackpackDupeFix;
import net.craftersland.restarter.events.CreativeMiddleclickFix;
import net.craftersland.restarter.events.DevNullDupeFix;
import net.craftersland.restarter.events.ExplosionDetonate;
import net.craftersland.restarter.events.FireSpread;
import net.craftersland.restarter.events.HacksFixes;
import net.craftersland.restarter.events.NucleusFix;
import net.craftersland.restarter.events.PlayerFirstJoin;
import net.craftersland.restarter.events.PlayerRespawn;
import net.craftersland.restarter.events.SpawnProtection;
import net.craftersland.restarter.fixes.ChunkloaderSave;
import net.craftersland.restarter.fixes.RemoveBlocks;
import net.craftersland.restarter.fixes.RemoveEntities;
import net.craftersland.restarter.objects.SavedChunkloaders;
import net.craftersland.restarter.storage.MysqlSetup;
import net.craftersland.restarter.storage.StorageHandler;
import net.craftersland.restarter.toolkit.WrapperLink;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

@Plugin(id="restarter", name="Restarter", version="1.21.3", description="Server monitor and restart system.", url="https://www.spigotmc.org/resources/authors/brunyman.23534/", authors={"brunyman"})
public class RR {
    @Inject
    private PluginContainer pluginContainer;
    public static Logger log;
    public static RR instance;
    private static ConfigHandler cH;
    private static Engine eng;
    private static WrapperLink wl;
    private static MysqlSetup ms;
    private static StorageHandler sH;
    private static ServerMonitor srvm;
    private static SpawnProtectionUtils spu;
    private static RemoveEntities re;
    private static DataHandler dH;
    private static RemoveBlocks rb;
    private static SoundHandler soH;
    private Map<Player, SavedChunkloaders> loadedPlayers = new HashMap<Player, SavedChunkloaders>();

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        log = this.pluginContainer.getLogger();
        log.info("Loading plugin " + this.pluginContainer.getName() + "...");
        instance = this;
        cH = new ConfigHandler(this);
        soH = new SoundHandler(this);
        dH = new DataHandler(this);
        ms = new MysqlSetup(this);
        re = new RemoveEntities(this);
        rb = new RemoveBlocks(this);
        eng = new Engine(this);
        wl = new WrapperLink(this);
        sH = new StorageHandler(this);
        srvm = new ServerMonitor(this);
        if (cH.getBoolean("Prevent-Explosion-Block-Damage").booleanValue()) {
            Sponge.getEventManager().registerListeners((Object)this, (Object)new ExplosionDetonate(this));
        }
        if (cH.getBoolean("Prevent-Fire-Spread").booleanValue()) {
            Sponge.getEventManager().registerListeners((Object)this, (Object)new FireSpread(this));
        }
        if (cH.getBoolean("SpawnProtection", "Enabled").booleanValue()) {
            if (this.isWorldEditInstalled()) {
                spu = new SpawnProtectionUtils(this);
                Sponge.getEventManager().registerListeners((Object)this, (Object)new SpawnProtection(this));
            } else {
                log.warn("World Edit not found! Spawn protection disabled!");
            }
        }
        if (cH.getBoolean("Fix-First-Join-Location").booleanValue()) {
            Sponge.getEventManager().registerListeners((Object)this, (Object)new PlayerFirstJoin(this));
        }
        if (cH.getBoolean("Fix-Backpack-Drop-Dupe").booleanValue()) {
            Sponge.getEventManager().registerListeners((Object)this, (Object)new BackpackDupeFix(this));
        }
        if (cH.getBoolean("Fix-Nucleus-Delhome-Cmd").booleanValue()) {
            Sponge.getEventManager().registerListeners((Object)this, (Object)new NucleusFix(this));
        }
        if (cH.getBoolean("Fix-DankNull-Dupe").booleanValue()) {
            Sponge.getEventManager().registerListeners((Object)this, (Object)new DevNullDupeFix(this));
        }
        Sponge.getEventManager().registerListeners((Object)this, (Object)new CreativeMiddleclickFix(this));
        Sponge.getEventManager().registerListeners((Object)this, (Object)new ChunkloaderSave(this));
        Sponge.getEventManager().registerListeners((Object)this, (Object)new PlayerRespawn(this));
        Sponge.getEventManager().registerListeners((Object)this, (Object)new HacksFixes(this));
        CommandSpec cmd = CommandSpec.builder().description((Text)Text.of((String)"Usage: /rr")).arguments(GenericArguments.optional((CommandElement)GenericArguments.remainingJoinedStrings((Text)Text.of((String)"message")))).executor((CommandExecutor)new CommandHandler(this)).build();
        Sponge.getCommandManager().register((Object)this, (CommandCallable)cmd, new String[]{"rr"});
        log.info(String.valueOf(this.pluginContainer.getName()) + " loaded successfully!");
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        log.info("Disabling plugin " + this.pluginContainer.getName() + "...");
        Sponge.getEventManager().unregisterPluginListeners((Object)this);
        Sponge.getCommandManager().getOwnedBy((Object)this).forEach(arg_0 -> ((CommandManager)Sponge.getCommandManager()).removeMapping(arg_0));
        Sponge.getScheduler().getScheduledTasks((Object)this).forEach(Task::cancel);
        log.info(String.valueOf(this.pluginContainer.getName()) + " is disabled!");
    }

    public boolean isWorldEditInstalled() {
        return Sponge.getPluginManager().getPlugin("WorldEdit").isPresent();
    }

    public ConfigHandler getConfigHandler() {
        return cH;
    }

    public Engine getEngine() {
        return eng;
    }

    public WrapperLink getWrapperLink() {
        return wl;
    }

    public MysqlSetup getMysqlSetup() {
        return ms;
    }

    public StorageHandler getStorageHandler() {
        return sH;
    }

    public ServerMonitor getServerMonitor() {
        return srvm;
    }

    public PluginContainer getPluginContainer() {
        return this.pluginContainer;
    }

    public SpawnProtectionUtils getSpawnProtectionUtils() {
        return spu;
    }

    public RemoveEntities getRemoveEntities() {
        return re;
    }

    public DataHandler getDataHandler() {
        return dH;
    }

    public RemoveBlocks getRemoveBlocks() {
        return rb;
    }

    public SoundHandler getSoundHandler() {
        return soH;
    }

    public Map<Player, SavedChunkloaders> getChunkloadersLoadedPlayers() {
        return this.loadedPlayers;
    }

    public Logger getLogger() {
        return log;
    }
}

