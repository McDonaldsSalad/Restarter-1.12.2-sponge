/*
 * Decompiled with CFR 0.152.
 */
package net.craftersland.restarter.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import net.craftersland.restarter.RR;

public class StorageHandler {
    private RR plugin;

    public StorageHandler(RR rr) {
        this.plugin = rr;
    }

    public void removeChunkLoader(String location) {
        Connection conn = this.plugin.getMysqlSetup().getLocalConnection();
        if (conn == null) {
            return;
        }
        Statement preparedStatement = null;
        try {
            try {
                String sql = "DELETE FROM `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "ChunkLoadersTableName") + "` WHERE `location` = ? LIMIT 1";
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, location);
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
                    RR.log.warn("Error: " + e2.getMessage());
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
                RR.log.warn("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void loadChunkLoaders(String playerName, Set<String> locationList) {
        Connection conn = this.plugin.getMysqlSetup().getLocalConnection();
        Statement preparedUpdateStatement = null;
        ResultSet result = null;
        if (conn != null) {
            try {
                try {
                    String sql = "SELECT `location` FROM `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "ChunkLoadersTableName") + "` WHERE `player_name` = ?";
                    preparedUpdateStatement = conn.prepareStatement(sql);
                    preparedUpdateStatement.setString(1, playerName);
                    result = preparedUpdateStatement.executeQuery();
                    while (result.next()) {
                        locationList.add(result.getString("location"));
                    }
                }
                catch (SQLException e) {
                    RR.log.warn("Error: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        if (result != null) {
                            result.close();
                        }
                        if (preparedUpdateStatement != null) {
                            preparedUpdateStatement.close();
                        }
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
            finally {
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
        }
    }

    public int getMaxChunkLoaders(String playerName) {
        Connection conn = this.plugin.getMysqlSetup().getLocalConnection();
        Statement preparedUpdateStatement = null;
        ResultSet result = null;
        if (conn != null) {
            try {
                String sql = "SELECT `max_chunkloaders` FROM `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "PlayersTableName") + "` WHERE `player_name` = ? LIMIT 1";
                preparedUpdateStatement = conn.prepareStatement(sql);
                preparedUpdateStatement.setString(1, playerName);
                result = preparedUpdateStatement.executeQuery();
                if (result.next()) {
                    int n = result.getInt("max_chunkloaders");
                    return n;
                }
            }
            catch (SQLException e) {
                RR.log.warn("Error: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
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
        }
        return 0;
    }

    public void createChunkloaderAccount(String playerName) {
        Connection conn = this.plugin.getMysqlSetup().getLocalConnection();
        Statement preparedStatement = null;
        if (conn != null) {
            try {
                try {
                    String sql = "INSERT INTO `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "PlayersTableName") + "`(`player_name`, `max_chunkloaders`, `last_seen`) " + "VALUES(?, ?, ?)";
                    preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setString(1, playerName);
                    preparedStatement.setInt(2, this.plugin.getConfigHandler().getInteger("SavedChunkloaders"));
                    preparedStatement.setString(3, String.valueOf(System.currentTimeMillis()));
                    preparedStatement.executeUpdate();
                }
                catch (SQLException e) {
                    RR.log.warn("Error: " + e.getMessage());
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

    public void saveChunkloader(String locationString, String playerName) {
        if (!this.isLocationInDb(locationString)) {
            Connection conn = this.plugin.getMysqlSetup().getLocalConnection();
            Statement preparedStatement = null;
            if (conn != null) {
                try {
                    try {
                        String sql = "INSERT INTO `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "ChunkLoadersTableName") + "`(`location`, `player_name`) " + "VALUES(?, ?)";
                        preparedStatement = conn.prepareStatement(sql);
                        preparedStatement.setString(1, locationString);
                        preparedStatement.setString(2, playerName);
                        preparedStatement.executeUpdate();
                    }
                    catch (SQLException e) {
                        RR.log.warn("Error: " + e.getMessage());
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
    }

    public boolean isLocationInDb(String locationString) {
        Connection conn = this.plugin.getMysqlSetup().getLocalConnection();
        Statement preparedUpdateStatement = null;
        ResultSet result = null;
        if (conn != null) {
            try {
                String sql = "SELECT `location` FROM `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "ChunkLoadersTableName") + "` WHERE `location` = ? LIMIT 1";
                preparedUpdateStatement = conn.prepareStatement(sql);
                preparedUpdateStatement.setString(1, locationString);
                result = preparedUpdateStatement.executeQuery();
                if (result.next()) {
                    return true;
                }
            }
            catch (SQLException e) {
                RR.log.warn("Error: " + e.getMessage());
            }
            finally {
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
        }
        return false;
    }

    public boolean hasChunkloaderDatabaseData(String playerName) {
        Connection conn = this.plugin.getMysqlSetup().getLocalConnection();
        Statement preparedUpdateStatement = null;
        ResultSet result = null;
        if (conn != null) {
            try {
                String sql = "SELECT `player_name` FROM `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "PlayersTableName") + "` WHERE `player_name` = ? LIMIT 1";
                preparedUpdateStatement = conn.prepareStatement(sql);
                preparedUpdateStatement.setString(1, playerName);
                result = preparedUpdateStatement.executeQuery();
                if (result.next()) {
                    return true;
                }
            }
            catch (SQLException e) {
                RR.log.warn("Error: " + e.getMessage());
            }
            finally {
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
        }
        return false;
    }

    public void setMaxChunkloaders(String playerName, int amount) {
        Connection conn = this.plugin.getMysqlSetup().getLocalConnection();
        Statement preparedUpdateStatement = null;
        if (conn != null) {
            try {
                try {
                    String data = "UPDATE `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "PlayersTableName") + "` " + "SET `max_chunkloaders` = ?" + " WHERE `player_name` = ?";
                    preparedUpdateStatement = conn.prepareStatement(data);
                    preparedUpdateStatement.setInt(1, amount);
                    preparedUpdateStatement.setString(2, playerName);
                    preparedUpdateStatement.executeUpdate();
                }
                catch (SQLException e) {
                    RR.log.warn("Error: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        if (preparedUpdateStatement != null) {
                            preparedUpdateStatement.close();
                        }
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
            finally {
                try {
                    if (preparedUpdateStatement != null) {
                        preparedUpdateStatement.close();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateChunkloadersLastSeen(String playerName) {
        Connection conn = this.plugin.getMysqlSetup().getLocalConnection();
        Statement preparedUpdateStatement = null;
        if (conn != null) {
            try {
                try {
                    String data = "UPDATE `" + this.plugin.getConfigHandler().getString("Database", "Mysql-Chunkloaders", "PlayersTableName") + "` " + "SET `last_seen` = ?" + " WHERE `player_name` = ?";
                    preparedUpdateStatement = conn.prepareStatement(data);
                    preparedUpdateStatement.setString(1, String.valueOf(System.currentTimeMillis()));
                    preparedUpdateStatement.setString(2, playerName);
                    preparedUpdateStatement.executeUpdate();
                }
                catch (SQLException e) {
                    RR.log.warn("Error: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        if (preparedUpdateStatement != null) {
                            preparedUpdateStatement.close();
                        }
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
            finally {
                try {
                    if (preparedUpdateStatement != null) {
                        preparedUpdateStatement.close();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean setData(Connection conn, String serverName, String start1, String start2, Integer startIndex, Double tps1, Double tps2, Integer onlinePlayers, String lastUpdate) {
        if (conn == null) {
            return false;
        }
        if (!this.hasAccount(conn, serverName)) {
            this.createAccount(conn, serverName);
        }
        Statement preparedUpdateStatement = null;
        if (conn != null) {
            try {
                String data = "UPDATE `" + this.plugin.getConfigHandler().getString("Database", "Mysql", "TableName") + "` " + "SET `start1` = ?" + ", `start2` = ?" + ", `start_index` = ?" + ", `tps1` = ?" + ", `tps2` = ?" + ", `online_players` = ?" + ", `last_update` = ?" + " WHERE `server_name` = ?";
                preparedUpdateStatement = conn.prepareStatement(data);
                preparedUpdateStatement.setString(1, start1);
                preparedUpdateStatement.setString(2, start2);
                preparedUpdateStatement.setInt(3, startIndex);
                preparedUpdateStatement.setDouble(4, tps1);
                preparedUpdateStatement.setDouble(5, tps2);
                preparedUpdateStatement.setInt(6, onlinePlayers);
                preparedUpdateStatement.setString(7, lastUpdate);
                preparedUpdateStatement.setString(8, serverName);
                preparedUpdateStatement.executeUpdate();
                return true;
            }
            catch (SQLException e) {
                RR.log.warn("Error: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                try {
                    if (preparedUpdateStatement != null) {
                        preparedUpdateStatement.close();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public Integer getServerOnlinePlayers(Connection conn, String serverName) {
        Statement preparedUpdateStatement = null;
        ResultSet result = null;
        if (conn != null) {
            try {
                String sql = "SELECT `online_players` FROM `" + this.plugin.getConfigHandler().getString("Database", "Mysql", "TableName") + "` WHERE `server_name` = ? LIMIT 1";
                preparedUpdateStatement = conn.prepareStatement(sql);
                preparedUpdateStatement.setString(1, serverName);
                result = preparedUpdateStatement.executeQuery();
                if (result.next()) {
                    Integer n = result.getInt("online_players");
                    return n;
                }
            }
            catch (SQLException e) {
                RR.log.warn("Error: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
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
        }
        return null;
    }

    public String[] getData(Connection conn, String serverName) {
        if (!this.hasAccount(conn, serverName)) {
            this.createAccount(conn, serverName);
        }
        Statement preparedUpdateStatement = null;
        ResultSet result = null;
        if (conn != null) {
            try {
                String sql = "SELECT * FROM `" + this.plugin.getConfigHandler().getString("Database", "Mysql", "TableName") + "` WHERE `server_name` = ? LIMIT 1";
                preparedUpdateStatement = conn.prepareStatement(sql);
                preparedUpdateStatement.setString(1, serverName);
                result = preparedUpdateStatement.executeQuery();
                if (result.next()) {
                    String[] data;
                    String[] stringArray = data = new String[]{result.getString("start1"), result.getString("start2"), "" + result.getInt("start_index"), "" + result.getDouble("tps1"), "" + result.getDouble("tps2"), "" + result.getInt("online_players"), result.getString("last_update")};
                    return stringArray;
                }
            }
            catch (SQLException e) {
                RR.log.warn("Error: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
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
        }
        return null;
    }

    public boolean createAccount(Connection conn, String serverName) {
        Statement preparedStatement = null;
        if (conn != null) {
            try {
                String sql = "INSERT INTO `" + this.plugin.getConfigHandler().getString("Database", "Mysql", "TableName") + "`(`server_name`, `start1`, `start2`, `start_index`, `tps1`, `tps2`, `online_players` , `last_update`) " + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, serverName);
                preparedStatement.setString(2, "0");
                preparedStatement.setString(3, "0");
                preparedStatement.setInt(4, 1);
                preparedStatement.setDouble(5, 20.0);
                preparedStatement.setDouble(6, 20.0);
                preparedStatement.setInt(7, 0);
                preparedStatement.setString(8, "0");
                preparedStatement.executeUpdate();
                return true;
            }
            catch (SQLException e) {
                RR.log.warn("Error: " + e.getMessage());
                e.printStackTrace();
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
        return false;
    }

    public boolean hasAccount(Connection conn, String serverName) {
        Statement preparedUpdateStatement = null;
        ResultSet result = null;
        if (conn != null) {
            try {
                String sql = "SELECT `server_name` FROM `" + this.plugin.getConfigHandler().getString("Database", "Mysql", "TableName") + "` WHERE `server_name` = ? LIMIT 1";
                preparedUpdateStatement = conn.prepareStatement(sql);
                preparedUpdateStatement.setString(1, serverName);
                result = preparedUpdateStatement.executeQuery();
                if (result.next()) {
                    return true;
                }
            }
            catch (SQLException e) {
                RR.log.warn("Error: " + e.getMessage());
            }
            finally {
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
        }
        return false;
    }
}

