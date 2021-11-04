package net.finalbarrage.RPEssentials.RPChat;

import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Set;

class Channels implements Listener {

    private RPEssentials rpEssentials;
    private ChatManager chatManager;
    private Boolean openGlobal;
    private Boolean outOfRange;
    private static String messageFormat;
    private static String senderFormat;

    public Channels(RPEssentials rpEssentials, ChatManager chatManager) {
        this.rpEssentials = rpEssentials;
        this.chatManager = chatManager;
        this.openGlobal = chatManager.getOpenGlobal();
        this.outOfRange = chatManager.getOutOfRange();
        messageFormat = rpEssentials.config.getConfig().getString("Chat.Channels.Proximity.message-format");
        senderFormat = rpEssentials.config.getConfig().getString("Chat.Channels.Proximity.sender-format");
    }

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent chatEvent) {
        Player sender = chatEvent.getPlayer();
        String message = chatEvent.getMessage();
        String senderName = sender.getDisplayName();

        // GLOBAL
        if (openGlobal) {
            if (!sender.hasPermission("RPEssentials.Global.Talk")) { chatEvent.setCancelled(true); }
            String globalPrefix = rpEssentials.config.getConfig().getString("Chat.Channels.Global.Prefix");

            new BukkitRunnable() {
                @Override
                public void run() {
                    String recipients = "ALL";
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Shout", Collections.singleton(recipients), message); }

                    for (Player recipient : rpEssentials.onlinePlayers.values()) {
                        recipient.sendMessage(globalPrefix + sender.getDisplayName() + " : " + message);
                    }
                } }.runTask(rpEssentials);
            sender.sendMessage(globalPrefix + sender.getDisplayName() + " : " + message);
        }

        // PROXIMITY
        if (!openGlobal) {
            if (!sender.hasPermission("RPEssentials.Proximity.Talk")) { chatEvent.setCancelled(true); }
            Set<Player> inRangeRecipients = chatManager.getNearbyRecipients(sender, "Proximity");

            if (outOfRange) {
                Set<Player> outOfRangeRecipients = chatManager.getOutOfRangeRecipients(sender, "Proximity");
                String outOfRangeMessage = chatManager.outOfRangeScramble(message, "Proximity");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Set<String> recipients;
                        recipients = rpEssentials.logger.mergeRecipients(inRangeRecipients, outOfRangeRecipients);
                        if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Proximity", recipients, message); }

                        for (Player recipient : outOfRangeRecipients) {
                            recipient.sendMessage(String.format(senderFormat + messageFormat + outOfRangeMessage, senderName)); }

                        for (Player recipient : inRangeRecipients) {
                            recipient.sendMessage(String.format(senderFormat + messageFormat + message, senderName)); }
                    } }.runTask(rpEssentials); }

            if (!outOfRange) {

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Set<String> recipients;
                        recipients = rpEssentials.logger.getRecipients(inRangeRecipients);
                        if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Proximity", recipients, message); }

                        for (Player recipient : inRangeRecipients) {
                            recipient.sendMessage(String.format(senderFormat + messageFormat + message, senderName)); }
                    } }.runTask(rpEssentials); }

            sender.sendMessage(String.format(senderFormat + messageFormat + message, senderName));
        }
        chatEvent.setCancelled(true);
    }

}
