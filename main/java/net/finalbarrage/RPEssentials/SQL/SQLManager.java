package net.finalbarrage.RPEssentials.SQL;

import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.ChatColor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLManager {
    private static RPEssentials rpEssentials;
    private static Path dbPath;
    private static File dbFile;
    public static Connection connection;

    public SQLManager(RPEssentials rpEssentials) {
        SQLManager.rpEssentials = rpEssentials;
        connect(rpEssentials);
    }

    public static boolean isConnected() { return (connection == null) ? false : true; }

    public static Connection getConnection() { return connection; }

    public static void connect(RPEssentials rpEssentials) {
        if (rpEssentials.config.getConfig().getBoolean("SQL.serverless")) {
            dbPath = Paths.get(rpEssentials.getDataFolder() + "/databases/");
            dbFile = new File(dbPath + "/" + "RPEssentials.db");
            if (pathExists(dbPath) && fileExists(dbFile)) {
                try {
                    String dbURL = "jdbc:sqlite:" + dbFile;
                    connection = DriverManager.getConnection(dbURL);
                    if (isConnected()) {
                        rpEssentials.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[RPEssentials][SQL]: Connected to SQL Database!");
                        SQLCreateTables.createMailboxTable(dbURL);
                        SQLCreateTables.createPlayersTable(dbURL); }
                } catch (SQLException throwables) {
                    rpEssentials.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[RPEssentials][SQL]: Failed to connect to SQL Database!");
                    throwables.printStackTrace(); }
            }
        } else {
            String host = rpEssentials.config.getConfig().getString("SQL.server.host");
            String port = rpEssentials.config.getConfig().getString("SQL.server.port");
            String user = rpEssentials.config.getConfig().getString("SQL.server.user");
            String pass = rpEssentials.config.getConfig().getString("SQL.server.pass");
            try {
                if (!isConnected()) {
                    connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/RPEssentials?useSSL=false", host, port), user, pass);
                    rpEssentials.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[RPEssentials][SQL]: Connected to SQL Server!"); }
            } catch (SQLException e) {
                rpEssentials.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[RPEssentials][SQL]: Failed to connect to SQL Server!");
                e.printStackTrace(); }
        }
    }

    private static boolean pathExists(Path dbPath) {
        try { if (!Files.exists(dbPath) || !Files.isDirectory(dbPath)) { Files.createDirectories(dbPath); }
            return true; } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private static boolean fileExists(File dbFile) {
        if(dbFile.exists()) { return true; } else {
            try { dbFile.createNewFile();
            } catch (Exception e) { e.printStackTrace(); return false; }
        }
        return true;
    }

    public static void disconnect() {
        rpEssentials.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[RPEssentials][SQL]: Connection Closed!");
        try { if (isConnected()) { connection.close(); }
        } catch (SQLException throwables) { throwables.printStackTrace(); }
    }
}

class SQLCreateTables {

    public static void createMailboxTable(String dbURL) {
        String sql = "CREATE TABLE IF NOT EXISTS mailboxes ("
                + " id integer PRIMARY KEY NOT NULL,"
                + " chunkX integer NOT NULL,"
                + " chunkZ integer NOT NULL,"
                + " blockX integer NOT NULL,"
                + " blockY integer NOT NULL,"
                + " blockZ integer NOT NULL,"
                + " type varchar NOT NULL)";

        try (Connection conn = DriverManager.getConnection(dbURL); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void createMail(String dbURL) {

    }

    public static void createPlayersTable(String dbURL) {
        String sql = "CREATE TABLE IF NOT EXISTS players ("
                + " id integer PRIMARY KEY NOT NULL,"
                + " uuid VARCHAR(32) NOT NULL,"
                + " username VARCHAR(16) NOT NULL,"
                + " orbid integer(9) NOT NULL)";

        try (Connection conn = DriverManager.getConnection(dbURL); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
