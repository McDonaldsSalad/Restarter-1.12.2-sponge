/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.api.Sponge
 */
package net.craftersland.restarter.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import net.craftersland.restarter.RR;
import org.spongepowered.api.Sponge;

public class MysqlSetup {
    private RR plugin;
    private Connection conn = null;
    private Connection localConn = null;
    private boolean dbInitialSetup = false;

    public MysqlSetup(RR rr) {
        this.plugin = rr;
        this.setupDatabase();
        this.databaseMaintenanceTask();
    }

    public boolean isDbSetupComplete() {
        return this.dbInitialSetup;
    }

    public Connection getConnection() {
        this.checkConnection();
        return this.conn;
    }

    public Connection getLocalConnection() {
        this.checkLocalConnection();
        return this.localConn;
    }

    private void checkLocalConnection() {
        try {
            if (this.localConn == null) {
                RR.log.warn("Local database connection failed!");
                this.localConn = null;
            } else if (!this.localConn.isValid(3)) {
                RR.log.warn("Local database connection failed!");
                this.localConn = null;
            } else if (this.localConn.isClosed()) {
                RR.log.warn("Local database connection failed!");
                this.localConn = null;
            }
        }
        catch (Exception e) {
            RR.log.error("Error checking local database connection! Error: " + e.getMessage());
        }
    }

    private void checkConnection() {
        try {
            if (this.conn == null) {
                RR.log.warn("Remote database connection failed!");
                this.conn = null;
            } else if (!this.conn.isValid(3)) {
                RR.log.warn("Remote database connection failed!");
                this.conn = null;
            } else if (this.conn.isClosed()) {
                RR.log.warn("Remote database connection failed!");
                this.conn = null;
            }
        }
        catch (Exception e) {
            RR.log.error("Error checking remote database connection! Error: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            RR.log.info("Closing database connections...");
            if (this.conn != null) {
                this.conn.close();
                this.conn = null;
            }
            if (this.localConn != null) {
                this.localConn.close();
                this.localConn = null;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setupDatabase() {
        this.connectToDatabase();
        this.setupTables();
        this.setupChunkLoadersTables();
        this.dbInitialSetup = true;
    }

    public void setupTables() {
        if (this.conn == null) {
            return;
        }
        Statement query1 = null;
        try {
            try {
                String data = "CREATE TABLE IF NOT EXISTS `" + this.plugin.getConfigHandler().getString("Database", "Mysql", "TableName") + "` (id INT UNSIGNED NOT NULL AUTO_INCREMENT, server_name varchar(16) UNIQUE NOT NULL, start1 char(13) NOT NULL, start2 char(13) NOT NULL, start_index TINYINT(1) NOT NULL, tps1 DOUBLE(4,2) NOT NULL, tps2 DOUBLE(4,2) NOT NULL, online_players SMALLINT(4) NOT NULL, last_update char(13) NOT NULL, PRIMARY KEY(id));";
                query1 = this.conn.prepareStatement(data);
                query1.execute();
            }
            catch (Exception e) {
                RR.log.error("Error creating tables! Error: " + e.getMessage());
                e.printStackTrace();
                try {
                    if (query1 != null) {
                        query1.close();
                    }
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        finally {
            try {
                if (query1 != null) {
                    query1.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setupChunkLoadersTables() {
        if (this.localConn == null) {
            return;
        }
        Statement query1 = null;
        Statement query2 = null;
        try {
            try {
                String data = "CREATE TABLE IF NOT EXISTS `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "PlayersTableName") + "` (id INT UNSIGNED NOT NULL AUTO_INCREMENT, player_name varchar(16) UNIQUE NOT NULL, max_chunkloaders int(2) NOT NULL, last_seen char(13) NOT NULL, PRIMARY KEY(id));";
                query1 = this.localConn.prepareStatement(data);
                query1.execute();
                String data2 = "CREATE TABLE IF NOT EXISTS `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "ChunkLoadersTableName") + "` (id INT UNSIGNED NOT NULL AUTO_INCREMENT, location varchar(255) UNIQUE NOT NULL, player_name varchar(16) NOT NULL, PRIMARY KEY(id));";
                query2 = this.localConn.prepareStatement(data2);
                query2.execute();
            }
            catch (Exception e) {
                RR.log.error("Error creating tables! Error: " + e.getMessage());
                e.printStackTrace();
                try {
                    if (query1 != null) {
                        query1.close();
                    }
                    if (query2 != null) {
                        query2.close();
                    }
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        finally {
            try {
                if (query1 != null) {
                    query1.close();
                }
                if (query2 != null) {
                    query2.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void connectToDatabase() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            if (this.localConn == null) {
                Properties properties2 = new Properties();
                properties2.setProperty("user", this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "User"));
                properties2.setProperty("password", this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "Password"));
                properties2.setProperty("autoReconnect", "true");
                properties2.setProperty("connectTimeout", "6000");
                properties2.setProperty("verifyServerCertificate", "false");
                properties2.setProperty("useSSL", "false");
                properties2.setProperty("requireSSL", "false");
                RR.log.info("Connecting to local database...");
                DriverManager.setLoginTimeout(4);
                this.localConn = DriverManager.getConnection("jdbc:mysql://" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "Host") + ":" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "Port") + "/" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "DatabaseName"), properties2);
                RR.log.info("Local database connection established!");
            }
            if (this.conn == null) {
                Properties properties = new Properties();
                properties.setProperty("user", this.plugin.getConfigHandler().getString("Database", "Mysql", "User"));
                properties.setProperty("password", this.plugin.getConfigHandler().getString("Database", "Mysql", "Password"));
                properties.setProperty("autoReconnect", "true");
                properties.setProperty("connectTimeout", "6000");
                properties.setProperty("verifyServerCertificate", "false");
                properties.setProperty("useSSL", "false");
                properties.setProperty("requireSSL", "false");
                RR.log.info("Connecting to remote database...");
                DriverManager.setLoginTimeout(4);
                this.conn = DriverManager.getConnection("jdbc:mysql://" + this.plugin.getConfigHandler().getString("Database", "Mysql", "Host") + ":" + this.plugin.getConfigHandler().getString("Database", "Mysql", "Port") + "/" + this.plugin.getConfigHandler().getString("Database", "Mysql", "DatabaseName"), properties);
                RR.log.info("Remote database connection established!");
            }
        }
        catch (ClassNotFoundException e) {
            RR.log.error("Could not locate drivers for mysql! Error: " + e.getMessage());
        }
        catch (SQLException e) {
            RR.log.error("Could not connect to mysql database! Error: " + e.getMessage());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void databaseMaintenanceTask() {
        if (this.plugin.getConfigHandler().getBoolean("Database", "RemoveInactiveUsers", "enabled").booleanValue()) {
            Sponge.getScheduler().createTaskBuilder().async().delayTicks(2000L).execute(new Runnable(){

                @Override
                public void run() {
                    if (MysqlSetup.this.localConn != null) {
                        long inactiveTime;
                        block19: {
                            long inactivityDays = Long.parseLong(MysqlSetup.this.plugin.getConfigHandler().getString("Database", "RemoveInactiveUsers", "inactivity"));
                            long inactivityMils = inactivityDays * 24L * 60L * 60L * 1000L;
                            long curentTime = System.currentTimeMillis();
                            inactiveTime = curentTime - inactivityMils;
                            RR.log.info("Database maintenance task started...");
                            Statement preparedUpdateStatement = null;
                            ResultSet result = null;
                            try {
                                try {
                                    String sql = "SELECT `player_name` FROM `" + MysqlSetup.this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "PlayersTableName") + "` WHERE `last_seen` < ?";
                                    preparedUpdateStatement = MysqlSetup.this.localConn.prepareStatement(sql);
                                    preparedUpdateStatement.setString(1, String.valueOf(inactiveTime));
                                    result = preparedUpdateStatement.executeQuery();
                                    while (result.next()) {
                                        MysqlSetup.this.removeChunkLoaders(result.getString("player_name"), MysqlSetup.this.getLocalConnection(), MysqlSetup.this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "ChunkLoadersTableName"));
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    try {
                                        if (result != null) {
                                            result.close();
                                        }
                                        if (preparedUpdateStatement != null) {
                                            preparedUpdateStatement.close();
                                        }
                                        break block19;
                                    }
                                    catch (Exception e2) {
                                        e2.printStackTrace();
                                    }
                                    break block19;
                                }
                            }
                            catch (Throwable throwable) {
                                try {
                                    if (result != null) {
                                        result.close();
                                    }
                                    if (preparedUpdateStatement != null) {
                                        preparedUpdateStatement.close();
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                throw throwable;
                            }
                            try {
                                if (result != null) {
                                    result.close();
                                }
                                if (preparedUpdateStatement != null) {
                                    preparedUpdateStatement.close();
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        MysqlSetup.this.tableMaintenance(inactiveTime, MysqlSetup.this.getLocalConnection(), MysqlSetup.this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "PlayersTableName"));
                        RR.log.info("Database maintenance complete!");
                    }
                }
            }).submit((Object)this.plugin);
        }
    }

    private void tableMaintenance(long inactiveTime, Connection conn, String tableName) {
        if (conn == null) {
            return;
        }
        Statement preparedStatement = null;
        try {
            try {
                String sql = "DELETE FROM `" + tableName + "` WHERE `last_seen` < ?";
                preparedStatement = this.localConn.prepareStatement(sql);
                preparedStatement.setString(1, String.valueOf(inactiveTime));
                preparedStatement.execute();
            }
            catch (Exception e) {
                e.printStackTrace();
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeChunkLoaders(String name, Connection conn, String tableName) {
        if (conn == null) {
            return;
        }
        Statement preparedStatement = null;
        try {
            try {
                String sql = "DELETE FROM `" + tableName + "` WHERE `player_name` = ?";
                preparedStatement = this.localConn.prepareStatement(sql);
                preparedStatement.setString(1, name);
                preparedStatement.execute();
            }
            catch (Exception e) {
                e.printStackTrace();
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

