package net.finalbarrage.RPEssentials.RPLogin;

import net.finalbarrage.RPEssentials.RPEssentials;
import net.finalbarrage.RPEssentials.SQL.SQLManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginManager {

    private RPEssentials rpEssentials;
    private FileConfiguration config;
    private String loginMode;

    public LoginManager(RPEssentials rpEssentials) {
        this.rpEssentials = rpEssentials;
        this.config = rpEssentials.config.getConfig();
        this.loginMode = rpEssentials.config.getConfig().getString("Login-System.mode");
    }

    public String getLoginMode() { return this.loginMode; }

    public void setLoginMode(String mode) {
        if (mode.equalsIgnoreCase("vanilla")) {
            this.loginMode = "Vanilla";
            config.set("Login-System.mode", "Vanilla"); }

        if (mode.equalsIgnoreCase("pseudorp")) {
            this.loginMode = "PseudoRP";
            config.set("Login-System.mode", "PseudoRP"); }

        if (mode.equalsIgnoreCase("strict")) {
            this.loginMode = "Strict";
            config.set("Login-System.mode", "Strict"); }
    }

}

class LoginGUI {
}

class LoginEvents implements Listener {

}

class LoginSQL {

    public static Boolean returningPlayer(Player player) {
        Boolean bool = false;
        String uuid = player.getUniqueId().toString();
        String sql = "SELECT uuid FROM players WHERE uuid = ?";
        try {
            Connection conn = SQLManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uuid);
            ResultSet rs = pstmt.executeQuery();
            bool = rs.next();
            pstmt.close();
            return bool;
        } catch (SQLException e) { e.printStackTrace(); }
        return bool;
    }

    public static void newPlayer(Player player, Integer orbID) {
        String uuid = player.getUniqueId().toString();
        String username = player.getDisplayName();

        String sql = "INSERT INTO players(uuid, username, orbid) VALUES(?, ?, ?)";
        try {
            Connection conn = SQLManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uuid);
            pstmt.setString(2, username);
            pstmt.setInt(3, orbID);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
