package net.finalbarrage.RPEssentials.Events;

import net.finalbarrage.RPEssentials.RPItems.OrbOfCommunication;
import net.finalbarrage.RPEssentials.RPEssentials;
import net.finalbarrage.RPEssentials.SQL.SQLManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServerEvents implements Listener {

    private RPEssentials rpEssentials;

    public ServerEvents(RPEssentials rpEssentials) {
        this.rpEssentials = rpEssentials;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent joinEvent) {
        Player player = joinEvent.getPlayer();
        joinEvent.setJoinMessage("");

        rpEssentials.onlinePlayers.put(player.getDisplayName(), player);
        rpEssentials.permissionManager.permissionSetter(player);

        if (!ServerEventsSQL.returningPlayer(player)) {
            ServerEventsSQL.newPlayer(player, OrbOfCommunication.newOrbID(rpEssentials));
            rpEssentials.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + String.format("[RPEssentials]: New player %s added to database.", player.getDisplayName()));
        }
    }

    @EventHandler
    private void onPlayerKick(PlayerKickEvent kickEvent) {
        Player player = kickEvent.getPlayer();
        kickEvent.setLeaveMessage("");
        rpEssentials.onlinePlayers.remove(player);
        rpEssentials.permissionManager.playerPermissions.remove(player.getUniqueId());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent quitEvent) {
        Player player = quitEvent.getPlayer();
        quitEvent.setQuitMessage("");
        rpEssentials.onlinePlayers.remove(player);
        rpEssentials.permissionManager.playerPermissions.remove(player.getUniqueId());
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent deathEvent) { deathEvent.setDeathMessage(""); }

}

class ServerEventsSQL {

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
