/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.command.CommandException
 *  org.spongepowered.api.command.CommandResult
 *  org.spongepowered.api.command.CommandSource
 *  org.spongepowered.api.command.args.CommandContext
 *  org.spongepowered.api.command.source.ConsoleSource
 *  org.spongepowered.api.command.spec.CommandExecutor
 *  org.spongepowered.api.data.type.HandTypes
 *  org.spongepowered.api.entity.Entity
 *  org.spongepowered.api.entity.EntityTypes
 *  org.spongepowered.api.entity.living.player.Player
 *  org.spongepowered.api.item.inventory.ItemStack
 *  org.spongepowered.api.scheduler.Task
 *  org.spongepowered.api.text.Text
 *  org.spongepowered.api.text.format.TextColors
 *  org.spongepowered.api.util.blockray.BlockRay
 *  org.spongepowered.api.world.Chunk
 *  org.spongepowered.api.world.World
 */
package net.craftersland.restarter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.craftersland.restarter.RR;
import net.craftersland.restarter.objects.SavedChunkloaders;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

public class CommandHandler
implements CommandExecutor {
    private RR rr;
    private List<Entity> toKill = new ArrayList<Entity>();

    public CommandHandler(RR rr) {
        this.rr = rr;
    }

    /*
     * Enabled aggressive block sorting
     */
    public CommandResult execute(final CommandSource sender, CommandContext cmd) throws CommandException {
        Player p;
        if (!(sender instanceof Player)) {
            if (!(sender instanceof ConsoleSource)) return CommandResult.success();
        }
        String[] args = new String[]{};
        if (cmd.getOne("message").isPresent()) {
            args = ((String)cmd.getOne("message").get()).split(" ");
        }
        final String[] argsF = args;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                Player p2;
                if (sender instanceof Player && !(p2 = (Player)sender).hasPermission("RR.admin")) {
                    sender.sendMessage((Text)Text.builder((String)"You do not have permission!").color(TextColors.RED).build());
                    return CommandResult.success();
                }
                this.rr.getConfigHandler().loadConfig();
                this.rr.getEngine().initializeOtherServers();
                return CommandResult.success();
            }
            if (args[0].equalsIgnoreCase("clearchunk")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage((Text)Text.builder((String)"Not available for console!").color(TextColors.RED).build());
                    return CommandResult.success();
                }
                final Player p3 = (Player)sender;
                if (p3.hasPermission("RR.admin")) {
                    sender.sendMessage((Text)Text.builder((String)"Removing entities in your chunk...").color(TextColors.GREEN).build());
                    Task.builder().async().execute(new Runnable(){

                        @Override
                        public void run() {
                            CommandHandler.this.toKill.clear();
                            Optional c = p3.getWorld().getChunk(p3.getLocation().getChunkPosition());
                            ArrayList le = new ArrayList(((Chunk)c.get()).getEntities());
                            for (Entity e : le) {
                                if (e.getType() == EntityTypes.PLAYER) continue;
                                p3.sendMessage((Text)Text.of((String)("Debug - " + e.getType().getName())));
                                CommandHandler.this.toKill.add(e);
                            }
                            le.clear();
                            Task.builder().execute(new Runnable(){

                                @Override
                                public void run() {
                                    for (Entity e : CommandHandler.this.toKill) {
                                        RR.log.warn("Debug remove - " + e + " - " + e.getType());
                                        e.remove();
                                    }
                                    p3.sendMessage((Text)Text.builder((String)("Clear complete! Removed " + CommandHandler.this.toKill.size() + " entities.")).color(TextColors.GREEN).build());
                                    CommandHandler.this.toKill.clear();
                                }
                            }).submit((Object)CommandHandler.this.rr);
                        }
                    }).submit((Object)this.rr);
                    return CommandResult.success();
                }
                sender.sendMessage((Text)Text.builder((String)"You do not have permission!").color(TextColors.RED).build());
                return CommandResult.success();
            }
            if (args[0].equalsIgnoreCase("info")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage((Text)Text.builder((String)"Not available for console!").color(TextColors.RED).build());
                    return CommandResult.success();
                }
                Player p4 = (Player)sender;
                if (!p4.hasPermission("RR.admin")) {
                    sender.sendMessage((Text)Text.builder((String)"You do not have permission!").color(TextColors.RED).build());
                    return CommandResult.success();
                }
                sender.sendMessage((Text)Text.builder((String)("Item Material: " + ((ItemStack)p4.getItemInHand(HandTypes.MAIN_HAND).get()).getItem().getType().getName())).color(TextColors.GREEN).build());
                sender.sendMessage((Text)Text.builder((String)("Biome: " + p4.getLocation().getBiome() + " - " + ((Chunk)p4.getWorld().getChunk(p4.getLocation().getChunkPosition()).get()).getBiomeCopy())).color(TextColors.GREEN).build());
                BlockRay blockRay = BlockRay.from((Entity)p4).skipFilter(BlockRay.continueAfterFilter((Predicate)BlockRay.onlyAirFilter(), (int)1)).distanceLimit(3.0).build();
                if (!blockRay.hasNext()) return CommandResult.success();
                sender.sendMessage((Text)Text.builder((String)("Block Material: " + blockRay.next().getLocation().createSnapshot().getExtendedState().toString())).color(TextColors.GREEN).build());
                return CommandResult.success();
            }
            if (!args[0].equalsIgnoreCase("clearlag")) {
                sender.sendMessage((Text)Text.builder((String)"Available commands: /rr reload , /rr clearchunk , /rr info , /rr clearLag , /rr addchunkloaders <name> <amount>").color(TextColors.RED).build());
                return CommandResult.success();
            }
            if (sender instanceof Player) {
                Player p5 = (Player)sender;
                if (!p5.hasPermission("RR.admin")) {
                    sender.sendMessage((Text)Text.builder((String)"You do not have permission!").color(TextColors.RED).build());
                    return CommandResult.success();
                }
                sender.sendMessage((Text)Text.builder((String)"Running entity and block cleanup.").color(TextColors.GREEN).build());
            }
            final ArrayList worlds = new ArrayList(Sponge.getServer().getWorlds());
            Sponge.getScheduler().createTaskBuilder().async().execute(new Runnable(){

                @Override
                public void run() {
                    for (World w : worlds) {
                        RR.log.info("Running cleanup for: " + w.getName() + " - " + (worlds.indexOf(w) + 1) + "/" + worlds.size());
                        CommandHandler.this.rr.getRemoveEntities().removeEntities(w);
                        CommandHandler.this.rr.getRemoveBlocks().removeBlocks(w);
                    }
                }
            }).submit((Object)this.rr);
            return CommandResult.success();
        }
        if (args.length != 3) {
            sender.sendMessage((Text)Text.builder((String)"Available commands: /rr reload , /rr clearchunk , /rr info , /rr clearLag , /rr addchunkloaders <name> <amount>").color(TextColors.RED).build());
            return CommandResult.success();
        }
        if (!args[0].equalsIgnoreCase("addchunkloaders")) {
            sender.sendMessage((Text)Text.builder((String)"Available commands: /rr reload , /rr clearchunk , /rr info , /rr clearLag , /rr addchunkloaders <name> <amount>").color(TextColors.RED).build());
            return CommandResult.success();
        }
        if (sender instanceof Player && !(p = (Player)sender).hasPermission("RR.admin")) {
            sender.sendMessage((Text)Text.builder((String)"You do not have permission!").color(TextColors.RED).build());
            return CommandResult.success();
        }
        Sponge.getScheduler().createTaskBuilder().async().execute(new Runnable(){

            @Override
            public void run() {
                Optional dest = Sponge.getServer().getPlayer(argsF[1]);
                int amountToAdd = Integer.parseInt(argsF[2]);
                int maxAmount = CommandHandler.this.rr.getConfigHandler().getInteger("SavedChunkloaders");
                if (dest.isPresent() && ((Player)dest.get()).isOnline()) {
                    SavedChunkloaders data = CommandHandler.this.rr.getChunkloadersLoadedPlayers().get(dest.get());
                    maxAmount = data.getMaxChunkloadersAmount();
                    data.setMaxChunkloaders(maxAmount + amountToAdd);
                }
                if (CommandHandler.this.rr.getStorageHandler().hasChunkloaderDatabaseData(argsF[1].toLowerCase())) {
                    maxAmount = CommandHandler.this.rr.getStorageHandler().getMaxChunkLoaders(argsF[1].toLowerCase());
                    CommandHandler.this.rr.getStorageHandler().setMaxChunkloaders(argsF[1].toLowerCase(), maxAmount + amountToAdd);
                    sender.sendMessage((Text)Text.builder((String)"Chunkloaders added!").color(TextColors.GREEN).build());
                } else {
                    sender.sendMessage((Text)Text.builder((String)"Player not found in the database!").color(TextColors.RED).build());
                }
            }
        }).submit((Object)this.rr);
        return CommandResult.success();
    }
}

