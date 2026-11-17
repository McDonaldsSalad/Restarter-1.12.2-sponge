/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.command.CommandSource
 *  org.spongepowered.api.scheduler.Task
 *  org.spongepowered.api.text.chat.ChatTypes
 *  org.spongepowered.api.text.serializer.TextSerializers
 */
package net.craftersland.restarter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.craftersland.restarter.RR;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;

public class Engine {
    private RR rr;
    private static long startTime;
    private static long finalTimeStart;
    private static long advertSentTimer;
    private static UUID mainTimerID;
    private static UUID finalTimerID;
    private static boolean isFinalTimerRunning;
    private static boolean isDbReconnecting;
    private Map<String, Integer> otherServerStats = new HashMap<String, Integer>();

    static {
        advertSentTimer = 0L;
        isFinalTimerRunning = false;
        isDbReconnecting = false;
    }

    public Engine(RR rr) {
        this.rr = rr;
        startTime = System.currentTimeMillis();
        this.initializeOtherServers();
        this.mainTimer();
        this.disableWhitelist();
    }

    private void disableWhitelist() {
        if (this.rr.getConfigHandler().getBoolean("DisableWhitelistOnStart").booleanValue()) {
            Sponge.getScheduler().createTaskBuilder().delayTicks(400L).execute(new Runnable(){

                @Override
                public void run() {
                    RR.log.info("Turning off whitelist...");
                    Sponge.getCommandManager().process((CommandSource)Sponge.getServer().getConsole(), "whitelist off");
                }
            }).submit((Object)this.rr);
        }
    }

    public void initializeOtherServers() {
        advertSentTimer = System.currentTimeMillis();
        this.otherServerStats.clear();
        for (String s : this.rr.getConfigHandler().getStringList("OtherServers")) {
            this.otherServerStats.put(s, 0);
        }
    }

    private void updateOtherServers() {
        if (this.rr.getMysqlSetup().getConnection() == null) {
            return;
        }
        for (String s : this.rr.getConfigHandler().getStringList("OtherServers")) {
            int online = this.rr.getStorageHandler().getServerOnlinePlayers(this.rr.getMysqlSetup().getConnection(), s);
            this.otherServerStats.put(s, online);
        }
    }

    private void broadcastAdvert() {
        advertSentTimer = System.currentTimeMillis();
        ArrayList<String> msg = new ArrayList<String>();
        for (String s : this.rr.getConfigHandler().getStringList("ChatMessage", "AdvertTitle")) {
            msg.add(s);
        }
        for (String s : this.otherServerStats.keySet()) {
            String m = this.rr.getConfigHandler().getString("ChatMessage", "AdvertContent");
            m = m.replace("%count%", "" + this.otherServerStats.get(s));
            msg.add(m.replace("%server%", s));
        }
        for (String s : this.rr.getConfigHandler().getStringList("ChatMessage", "AdvertEnd")) {
            msg.add(s);
        }
        for (String s : msg) {
            Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(s), ChatTypes.CHAT);
        }
    }

    /**
     * Monitors the server every 60 seconds. Automatic restart conditions can be
     * switched off without stopping database recovery, telemetry or adverts.
     * The original RestartIn config value was only used by the legacy log line.
     */
    public void mainTimer() {
        RR.log.info("Restart scheduled in " + this.rr.getConfigHandler().getInteger("RestartIn") + " minutes.");
        Task task = Task.builder().async().delayTicks(1200L).intervalTicks(1200L).execute(new Runnable() {
            @Override
            public void run() {
                if (!isDbReconnecting && Engine.this.rr.getMysqlSetup().isDbSetupComplete()
                        && (Engine.this.rr.getMysqlSetup().getConnection() == null
                        || Engine.this.rr.getMysqlSetup().getLocalConnection() == null)) {
                    isDbReconnecting = true;
                    Engine.this.rr.getMysqlSetup().connectToDatabase();
                    isDbReconnecting = false;
                }

                long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000L;
                Engine.this.rr.getServerMonitor().updateData();

                // This guard is also applied to the original bytecode build,
                // before evaluating any of the existing automatic triggers.
                if (Engine.this.rr.getConfigHandler().isAutoRestartEnabled()) {
                    ServerMonitor monitor = Engine.this.rr.getServerMonitor();
                    double lowTps = Engine.this.rr.getConfigHandler().getDouble("Low-TPS-Restart");
                    boolean autoRestart =
                            (monitor.getMemoryUsagePercent1() > 85.0 && monitor.getMemoryUsagePercent2() > 85.0)
                            || (monitor.getTPS1() <= lowTps && monitor.getTPS2() <= lowTps && uptimeSeconds >= 300L)
                            || (monitor.getTPS1() <= 18.0 && monitor.getTPS2() <= 18.0 && uptimeSeconds > 7200L)
                            || (monitor.getTPS1() <= 18.5 && monitor.getTPS2() <= 18.5
                                && uptimeSeconds > 14400L
                                && monitor.getTileEntities() > Engine.this.rr.getConfigHandler().getInteger("MaxTileEntities"))
                            || (monitor.getTPS1() <= 18.5 && monitor.getTPS2() <= 18.5 && uptimeSeconds > 21600L)
                            || (uptimeSeconds > 7200L && monitor.getOnlinePlayers() == 0)
                            || (uptimeSeconds > 28800L && monitor.getOnlinePlayers() < 10);
                    if (autoRestart) {
                        finalTimeStart = System.currentTimeMillis();
                        Engine.this.finalTimer();
                        Sponge.getScheduler().getTaskById(mainTimerID).ifPresent(Task::cancel);
                    }
                }

                Engine.this.updateOtherServers();
                if ((System.currentTimeMillis() - advertSentTimer) / 1000L >= 12000L) {
                    Engine.this.broadcastAdvert();
                }
            }
        }).submit((Object)this.rr);
        mainTimerID = task.getUniqueId();
    }

    private void finalTimer() {
        if (!isFinalTimerRunning) {
            Task task = Task.builder().async().delayTicks(1L).intervalTicks(1L).execute(new Runnable(){

                @Override
                public void run() {
                    if (!Engine.this.rr.getConfigHandler().isAutoRestartEnabled()) {
                        isFinalTimerRunning = false;
                        Sponge.getScheduler().getTaskById(finalTimerID).ifPresent(Task::cancel);
                        // The original restart trigger stopped the main monitor.
                        // Restore it so monitoring and later toggle changes still work.
                        Engine.this.mainTimer();
                        return;
                    }
                    isFinalTimerRunning = true;
                    Long countdown = System.currentTimeMillis() - finalTimeStart;
                    int countdownMili = countdown.intValue();
                    if (countdownMili > 29980 && countdownMili < 30020) {
                        String msg = Engine.this.rr.getConfigHandler().getString("ChatMessage", "RestartBroadcast").replace("%time%", "30 seconds");
                        Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(msg), ChatTypes.CHAT);
                    } else if (countdownMili > 49980 && countdownMili < 50020) {
                        String msg = Engine.this.rr.getConfigHandler().getString("ChatMessage", "RestartBroadcast").replace("%time%", "10 seconds");
                        Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(msg), ChatTypes.CHAT);
                    } else if (countdownMili > 54980 && countdownMili < 55020) {
                        String msg = Engine.this.rr.getConfigHandler().getString("ChatMessage", "RestartBroadcast").replace("%time%", "5 seconds");
                        Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(msg), ChatTypes.CHAT);
                    } else if (countdownMili > 55980 && countdownMili < 56020) {
                        String msg = Engine.this.rr.getConfigHandler().getString("ChatMessage", "RestartBroadcast").replace("%time%", "4 seconds");
                        Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(msg), ChatTypes.CHAT);
                    } else if (countdownMili > 56980 && countdownMili < 57020) {
                        String msg = Engine.this.rr.getConfigHandler().getString("ChatMessage", "RestartBroadcast").replace("%time%", "3 seconds");
                        Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(msg), ChatTypes.CHAT);
                    } else if (countdownMili > 57980 && countdownMili < 58020) {
                        String msg = Engine.this.rr.getConfigHandler().getString("ChatMessage", "RestartBroadcast").replace("%time%", "2 seconds");
                        Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(msg), ChatTypes.CHAT);
                    } else if (countdownMili > 58980 && countdownMili < 59020) {
                        String msg = Engine.this.rr.getConfigHandler().getString("ChatMessage", "RestartBroadcast").replace("%time%", "1 second");
                        Sponge.getServer().getBroadcastChannel().send(TextSerializers.FORMATTING_CODE.deserialize(msg), ChatTypes.CHAT);
                    } else if (countdownMili >= 59020) {
                        Engine.this.rr.getWrapperLink().dispatchLocalUDPPacket("stopwrapper:" + Engine.this.rr.getConfigHandler().getString("ToolKit", "UserPass"));
                        ((Task)Sponge.getScheduler().getTaskById(finalTimerID).get()).cancel();
                    }
                }
            }).submit((Object)this.rr);
            finalTimerID = task.getUniqueId();
        }
    }

    static /* synthetic */ boolean access$0() {
        return isDbReconnecting;
    }

    static /* synthetic */ void access$2(boolean bl) {
        isDbReconnecting = bl;
    }

    static /* synthetic */ long access$3() {
        return startTime;
    }

    static /* synthetic */ void access$4(long l) {
        finalTimeStart = l;
    }

    static /* synthetic */ void access$5(Engine engine) {
        engine.finalTimer();
    }

    static /* synthetic */ UUID access$6() {
        return mainTimerID;
    }

    static /* synthetic */ void access$7(Engine engine) {
        engine.updateOtherServers();
    }

    static /* synthetic */ long access$8() {
        return advertSentTimer;
    }

    static /* synthetic */ void access$9(Engine engine) {
        engine.broadcastAdvert();
    }
}

