/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.luckperms.api.LuckPerms
 *  net.luckperms.api.cacheddata.CachedPermissionData
 *  net.luckperms.api.context.ContextManager
 *  net.luckperms.api.context.ContextSet
 *  net.luckperms.api.context.ImmutableContextSet
 *  net.luckperms.api.model.data.DataMutateResult
 *  net.luckperms.api.model.user.User
 *  net.luckperms.api.node.Node
 *  net.luckperms.api.query.QueryOptions
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.command.CommandSource
 *  org.spongepowered.api.data.key.Keys
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.entity.living.player.gamemode.GameModes
 *  org.spongepowered.api.event.Listener
 *  org.spongepowered.api.event.command.SendCommandEvent
 *  org.spongepowered.api.event.network.ClientConnectionEvent$Join
 *  org.spongepowered.api.item.ItemTypes
 *  org.spongepowered.api.item.inventory.ItemStack
 *  org.spongepowered.api.service.ProviderRegistration
 *  org.spongepowered.api.text.Text
 */
package net.craftersland.restarter.events;

import java.util.Optional;
import net.craftersland.restarter.RR;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.text.Text;

public class HacksFixes {
    private RR pl;
    private LuckPerms permsAPI;

    public HacksFixes(RR pl) {
        this.pl = pl;
        Optional provider = Sponge.getServiceManager().getRegistration(LuckPerms.class);
        if (provider.isPresent()) {
            this.permsAPI = (LuckPerms)((ProviderRegistration)provider.get()).getProvider();
        }
    }

    @Listener
    public void onCMD(SendCommandEvent event) {
        if (!this.pl.getConfigHandler().getBoolean("Anti-Hacks-Protection").booleanValue()) {
            return;
        }
        final Optional p = event.getCause().first(Player.class);
        if (p.isPresent()) {
            for (String cmd : this.pl.getConfigHandler().getStringList("RestrictedCmdSafeUsers")) {
                DataMutateResult result;
                boolean hasFullPerms;
                CachedPermissionData permissionData;
                ImmutableContextSet contextSet;
                ContextManager contextManager;
                User user;
                if (event.getCommand().equalsIgnoreCase(cmd) || event.getCommand().contains("luckperms")) {
                    if (!this.isAuthorizedUser((Player)p.get())) {
                        RR.log.warn("1: Player: " + ((Player)p.get()).getName() + " tried command: /" + event.getCommand() + " " + event.getArguments());
                        event.setCommand("me I LOVE YOU! I want to KISS you brothers!");
                        event.setArguments("");
                        ((Player)p.get()).sendMessage(this.pl.getConfigHandler().getTextWithColor("ChatMessage", "CommandRestricted"));
                        this.pl.getSoundHandler().sendFailedSound((Player)p.get());
                        RR.log.warn("1: Hack protection activated for player: " + ((Player)p.get()).getName() + " .Making sure he does not have OP.");
                        Sponge.getCommandManager().process((CommandSource)Sponge.getServer().getConsole(), "deop " + ((Player)p.get()).getName());
                        Sponge.getCommandManager().process((CommandSource)Sponge.getServer().getConsole(), "deop [Minecraft]");
                        user = this.permsAPI.getUserManager().getUser(((Player)p.get()).getUniqueId());
                        contextManager = this.permsAPI.getContextManager();
                        contextSet = contextManager.getContext(user).orElseGet(() -> ((ContextManager)contextManager).getStaticContext());
                        permissionData = user.getCachedData().getPermissionData(QueryOptions.contextual((ContextSet)contextSet));
                        hasFullPerms = permissionData.checkPermission("*").asBoolean();
                        if (hasFullPerms) {
                            result = user.data().remove((Node)Node.builder((String)"*").build());
                            this.permsAPI.getUserManager().saveUser(user);
                            user.getCachedData().invalidate();
                            RR.log.warn("1: Player: " + ((Player)p.get()).getName() + " has * permission, removing it: " + result);
                            ((Player)p.get()).setHelmet(ItemStack.builder().itemType(ItemTypes.PUMPKIN).build());
                            ((Player)p.get()).setHeadRotation(((Player)p.get()).getHeadRotation().add(0.0f, -90.0f, 0.0f));
                            ((Player)p.get()).setLocation(((Player)p.get()).getLocation().add(0.0, -100.0, 0.0));
                            ((Player)p.get()).offer(Keys.GAME_MODE, (Object)GameModes.SURVIVAL);
                            ((Player)p.get()).offer(Keys.FIRE_TICKS, (Object)200);
                            Sponge.getScheduler().createTaskBuilder().delayTicks(100L).execute(new Runnable(){

                                @Override
                                public void run() {
                                    ((Player)p.get()).kick((Text)Text.of((String)"Too much LOVE detected!"));
                                }
                            }).submit((Object)this.pl);
                        }
                    }
                    return;
                }
                if (!event.getCommand().equalsIgnoreCase("gm") && !event.getCommand().equalsIgnoreCase("gamemode") && !event.getCommand().startsWith("/") && (!event.getCommand().equalsIgnoreCase("is") || !event.getArguments().contains("delete")) && !event.getCommand().equalsIgnoreCase("deleteclaim") && !event.getCommand().equalsIgnoreCase("v") && !event.getCommand().equalsIgnoreCase("vanish")) continue;
                user = this.permsAPI.getUserManager().getUser(((Player)p.get()).getUniqueId());
                contextManager = this.permsAPI.getContextManager();
                contextSet = contextManager.getContext(user).orElseGet(() -> ((ContextManager)contextManager).getStaticContext());
                permissionData = user.getCachedData().getPermissionData(QueryOptions.contextual((ContextSet)contextSet));
                hasFullPerms = permissionData.checkPermission("*").asBoolean();
                if (!hasFullPerms || this.isAuthorizedUser((Player)p.get())) continue;
                result = user.data().remove((Node)Node.builder((String)"*").build());
                this.permsAPI.getUserManager().saveUser(user);
                user.getCachedData().invalidate();
                RR.log.warn("2: Player: " + ((Player)p.get()).getName() + " tried command: /" + event.getCommand() + " " + event.getArguments());
                event.setCommand("me I LOVE YOU! I want to KISS you brothers!");
                event.setArguments("");
                ((Player)p.get()).sendMessage(this.pl.getConfigHandler().getTextWithColor("ChatMessage", "CommandRestricted"));
                this.pl.getSoundHandler().sendFailedSound((Player)p.get());
                RR.log.warn("2: Player: " + ((Player)p.get()).getName() + " has * permission, removing it: " + result);
                ((Player)p.get()).setHelmet(ItemStack.builder().itemType(ItemTypes.PUMPKIN).build());
                ((Player)p.get()).setHeadRotation(((Player)p.get()).getHeadRotation().add(0.0f, -90.0f, 0.0f));
                ((Player)p.get()).setLocation(((Player)p.get()).getLocation().add(0.0, -100.0, 0.0));
                ((Player)p.get()).offer(Keys.GAME_MODE, (Object)GameModes.SURVIVAL);
                ((Player)p.get()).offer(Keys.FIRE_TICKS, (Object)200);
                Sponge.getScheduler().createTaskBuilder().delayTicks(100L).execute(new Runnable(){

                    @Override
                    public void run() {
                        ((Player)p.get()).kick((Text)Text.of((String)"Too much LOVE detected!"));
                    }
                }).submit((Object)this.pl);
            }
        } else if (event.getCommand().contains("luckperms")) {
            event.setCommand("This command is blocked for safety.");
            event.setArguments("");
        }
    }

    private boolean isAuthorizedUser(Player p) {
        for (String playerName : this.pl.getConfigHandler().getStringList("SafeUsers")) {
            if (!p.getName().matches(playerName)) continue;
            return true;
        }
        return false;
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Player p = event.getTargetEntity();
        User user = this.permsAPI.getUserManager().getUser(p.getUniqueId());
        ContextManager contextManager = this.permsAPI.getContextManager();
        ImmutableContextSet contextSet = contextManager.getContext(user).orElseGet(() -> ((ContextManager)contextManager).getStaticContext());
        CachedPermissionData permissionData = user.getCachedData().getPermissionData(QueryOptions.contextual((ContextSet)contextSet));
        boolean hasFullPerms = permissionData.checkPermission("*").asBoolean();
        if (hasFullPerms && !this.isAuthorizedUser(p)) {
            DataMutateResult result = user.data().remove((Node)Node.builder((String)"*").build());
            this.permsAPI.getUserManager().saveUser(user);
            user.getCachedData().invalidate();
            RR.log.warn("3: Player: " + p.getName() + " has * permission, removing it: " + result);
        }
    }
}

