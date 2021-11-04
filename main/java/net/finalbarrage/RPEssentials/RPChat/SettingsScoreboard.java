package net.finalbarrage.RPEssentials.RPChat;

import dev.jcsoftware.jscoreboards.JGlobalMethodBasedScoreboard;
import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SettingsScoreboard {

    private RPEssentials rpEssentials;
    private ChatManager chatManager;
    JGlobalMethodBasedScoreboard scoreboard;

    public SettingsScoreboard(RPEssentials rpEssentials, ChatManager chatManager) {
        this.rpEssentials = rpEssentials;
        this.chatManager = chatManager;
        this.scoreboard = new JGlobalMethodBasedScoreboard();
    }

    private void chatSettings() {

        // Title
        String RPCHeader = ChatColor.BOLD + "[ Chat Information ] ";

        // Local Chat Ranges
        int proximityRange = chatManager.ranges.get("Proximity").intValue();
        int meRange = chatManager.ranges.get("Me").intValue();
        int doRange = chatManager.ranges.get("Do").intValue();
        int itRange = chatManager.ranges.get("It").intValue();
        int whisperRange = chatManager.ranges.get("Whisper").intValue();
        int shoutRange = chatManager.ranges.get("Shout").intValue();
        int oocRange = chatManager.ranges.get("OutOfCharacter").intValue();
        int pmRange = chatManager.ranges.get("PrivateMessage").intValue();

        scoreboard.setTitle(RPCHeader);
        scoreboard.setLines(
                "",
                "  [ " + String.format("%02d Blocks", proximityRange ) + " ] - Local",
                "  [ " + String.format("%02d Blocks", meRange) + " ] - Me",
                "  [ " + String.format("%02d Blocks", doRange) + " ] - Do",
                "  [ " + String.format("%02d Blocks", itRange) + " ] - It",
                "  [ " + String.format("%02d Blocks", whisperRange) + " ] - Whisper",
                "  [ " + String.format("%02d Blocks", shoutRange) + " ] - Shout",
                "  [ " + String.format("%02d Blocks", oocRange) + " ] - OOC",
                "");
    }

    public void openSettingsScoreboard(Player player) {
        scoreboard.updateScoreboard();
        scoreboard.addPlayer(player);
        rpEssentials.playersViewingChatSettings.add(player);
        chatSettings();
    }

    public void closeSettingsScoreboard(Player player) {
        scoreboard.removePlayer(player);
        rpEssentials.playersViewingChatSettings.remove(player);
    }

}
