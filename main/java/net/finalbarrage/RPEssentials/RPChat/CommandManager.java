package net.finalbarrage.RPEssentials.RPChat;

import dev.dbassett.skullcreator.SkullCreator;
import net.finalbarrage.RPEssentials.RPItems.Compass;
import net.finalbarrage.RPEssentials.RPItems.Mailbox;
import net.finalbarrage.RPEssentials.RPItems.OrbOfCommunication;
import net.finalbarrage.RPEssentials.RPItems.PocketWatch;
import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;

public class CommandManager implements CommandExecutor, TabExecutor {

    private RPEssentials rpEssentials;
    private ChatManager chatManager;
    private PrivateMessaging pm;
    private FileConfiguration config;
    private Boolean outOfRange;

    public CommandManager(RPEssentials rpEssentials, ChatManager chatManager) {
        this.rpEssentials = rpEssentials;
        this.chatManager = chatManager;
        this.config = rpEssentials.config.getConfig();
        this.outOfRange = chatManager.getOutOfRange();
        this.pm = new PrivateMessaging(rpEssentials);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player player)) { return false; }

        switch (cmd.getName().toLowerCase()) {

            case "get" -> {
                cmdGet(sender, args);
                return true; }

            case "settings" -> {
                if (args.length > 0) { return false; }
                rpEssentials.playersViewingPocketWatch.remove(player);
                if (rpEssentials.playersViewingChatSettings.contains(player)) {
                    rpEssentials.settingsScoreboard.closeSettingsScoreboard(player); } else {
                    rpEssentials.settingsScoreboard.openSettingsScoreboard(player); }
                return true; }

            case "rpchat" -> {
                if (args.length > 1) { return false; }

                switch (args[0].toLowerCase()) {

                    case "config" -> {

                    }

                }
            }

            case "me" -> {
                cmdMe(player, args);
                return true; }

            case "do" -> {
                cmdDo(player, args);
                return true; }

            case "it" -> {
                cmdIt(player, args);
                return true; }

            case "shout" -> {
                cmdShout(player, args);
                return true; }

            case "whisper" -> {
                cmdWhisper(player, args);
                return true; }

            case "outofcharacter" -> {
                outOfCharacter(player, args);
                return true; }

            case "privatemessage" -> {
                privateMessage(player, args);
                return true; }

        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }


    private void cmdGet(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String item = args[0].toLowerCase();

        switch (item) {

            case "orb" -> {
                if (args.length != 1) { return; }
                if (!rpEssentials.config.getConfig().getBoolean("Items.OrbOfCommunication.toggle-item")) { return; }
                player.getInventory().addItem(OrbOfCommunication.orbOfCommunication); }

            case "watch" -> {
                if (args.length != 1) { return; }
                if (!rpEssentials.config.getConfig().getBoolean("Items.PocketWatch.toggle-item")) { return; }
                player.getInventory().addItem(PocketWatch.pocketWatch); }

            case "mailbox" -> {
                if (args.length != 1) { return; }
                if (!rpEssentials.config.getConfig().getBoolean("Items.Mailbox.toggle-item")) { return; }
                player.getInventory().addItem(Mailbox.mailbox); }

            case "compass" -> {
                if (args.length != 1) { return; }
                if (!rpEssentials.config.getConfig().getBoolean("Items.Compass.toggle-item")) { return; }
                player.getInventory().addItem(Compass.compass); }

            case "skull" -> {
                if (args.length < 2) {
                    player.sendMessage("You need to enter a players name!");
                    return; }

                for (Player p : rpEssentials.onlinePlayers.values()) {
                    if (p.getDisplayName().equalsIgnoreCase(args[1])) {
                        ItemStack skull = SkullCreator.itemFromUuid(p.getUniqueId());
                        player.getInventory().addItem(skull);
                        break; }
                }
            }
        }
    }

    private void cmdShout(Player sender, String[] message) {
        if (!sender.hasPermission("RPEssentials.Command.Shout")) { return; }
        String senderFormat = config.getString("Chat.Action.Shout.sender-format");
        String messageFormat = config.getString("Chat.Action.Shout.message-format");
        String inRangeMessage = chatManager.combineMessage(message);
        Set<Player> inRangeRecipients = chatManager.getNearbyRecipients(sender, "Shout");

        if (outOfRange) {
            Set<Player> outOfRangeRecipients = chatManager.getOutOfRangeRecipients(sender, "Shout");
            String outOfRangeMessage = chatManager.outOfRangeScramble(inRangeMessage, "Shout");

            new BukkitRunnable() {
                @Override
                public void run() {
                    Set<String> recipients;
                    recipients = rpEssentials.logger.mergeRecipients(inRangeRecipients, outOfRangeRecipients);
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Shout", recipients, inRangeMessage); }

                    for (Player recipient : outOfRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + outOfRangeMessage, sender.getDisplayName())); }

                    for (Player recipient : inRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }

                } }.runTask(rpEssentials);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Set<String> recipients;
                    recipients = rpEssentials.logger.getRecipients(inRangeRecipients);
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Shout", recipients, inRangeMessage); }

                    for (Player recipient : inRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }
                } }.runTask(rpEssentials);
        }
        sender.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName()));
    }

    private void cmdWhisper(Player sender, String[] message) {
        if (!sender.hasPermission("RPEssentials.Command.Whisper")) { return; }
        String senderFormat = config.getString("Chat.Action.Whisper.sender-format");
        String messageFormat = config.getString("Chat.Action.Whisper.message-format");
        String inRangeMessage = chatManager.combineMessage(message);
        Set<Player> inRangeRecipients = chatManager.getNearbyRecipients(sender, "Whisper");

        if (outOfRange) {
            Set<Player> outOfRangeRecipients = chatManager.getOutOfRangeRecipients(sender, "Whisper");
            String outOfRangeMessage = chatManager.outOfRangeScramble(inRangeMessage, "Whisper");

            new BukkitRunnable() {
                @Override
                public void run() {
                    Set<String> recipients;
                    recipients = rpEssentials.logger.mergeRecipients(inRangeRecipients, outOfRangeRecipients);
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Whisper", recipients, inRangeMessage); }

                    for (Player recipient : outOfRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + outOfRangeMessage, sender.getDisplayName())); }

                    for (Player recipient : inRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }

                } }.runTask(rpEssentials);

        }

        if (!outOfRange) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    Set<String> recipients;
                    recipients = rpEssentials.logger.getRecipients(inRangeRecipients);
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Whisper", recipients, inRangeMessage); }

                    for (Player recipient : inRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }

                } }.runTask(rpEssentials);

        }
        sender.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName()));
    }

    private void cmdMe(Player sender, String[] message) {
        if (!sender.hasPermission("RPEssentials.Command.Me")) { return; }
        String senderFormat = config.getString("Chat.Action.Me.sender-format");
        String messageFormat = config.getString("Chat.Action.Me.message-format");
        String inRangeMessage = chatManager.combineMessage(message);
        Set<Player> inRangeRecipients = chatManager.getNearbyRecipients(sender, "Me");

        if (outOfRange) {
            Set<Player> outOfRangeRecipients = chatManager.getOutOfRangeRecipients(sender, "Me");
            String outOfRangeMessage = chatManager.outOfRangeScramble(inRangeMessage, "Me");

            new BukkitRunnable() {
                @Override
                public void run() {
                    Set<String> recipients;
                    recipients = rpEssentials.logger.mergeRecipients(inRangeRecipients, outOfRangeRecipients);
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Me", recipients, inRangeMessage); }

                    for (Player recipient : outOfRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + outOfRangeMessage, sender.getDisplayName())); }

                    for (Player recipient : inRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }

                } }.runTask(rpEssentials);


        }
        if (!outOfRange) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    Set<String> recipients;
                    recipients = rpEssentials.logger.getRecipients(inRangeRecipients);
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Me", recipients, inRangeMessage); }

                    for (Player recipient : inRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }
                } }.runTask(rpEssentials);

        }
        sender.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName()));
    }

    private void cmdDo(Player sender, String[] message) {
        if (!sender.hasPermission("RPEssentials.Command.Do")) { return; }
        String senderFormat = config.getString("Chat.Action.Do.sender-format");
        String messageFormat = config.getString("Chat.Action.Do.message-format");
        String inRangeMessage = chatManager.combineMessage(message);
        Set<Player> inRangeRecipients = chatManager.getNearbyRecipients(sender, "Do");
        if (outOfRange) {
            Set<Player> outOfRangeRecipients = chatManager.getOutOfRangeRecipients(sender, "Do");
            String outOfRangeMessage = chatManager.outOfRangeScramble(inRangeMessage, "Do");

            new BukkitRunnable() {
                @Override
                public void run() {
                    Set<String> recipients;
                    recipients = rpEssentials.logger.mergeRecipients(inRangeRecipients, outOfRangeRecipients);
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Do", recipients, inRangeMessage); }

                    for (Player recipient : outOfRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + outOfRangeMessage, sender.getDisplayName())); }

                    for (Player recipient : inRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }
                } }.runTask(rpEssentials);

        }
        if (!outOfRange) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Set<String> recipients;
                    recipients = rpEssentials.logger.getRecipients(inRangeRecipients);
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "Do", recipients, inRangeMessage); }

                    for (Player recipient : inRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }
                } }.runTask(rpEssentials);

        }
        sender.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName()));
    }

    private void cmdIt(Player sender, String[] message) {
        if (!sender.hasPermission("RPEssentials.Command.It")) { return; }
        String senderFormat = config.getString("Chat.Action.It.sender-format");
        String messageFormat = config.getString("Chat.Action.It.message-format");
        String inRangeMessage = chatManager.combineMessage(message);
        Set<Player> inRangeRecipients = chatManager.getNearbyRecipients(sender, "It");

        if (outOfRange) {
            Set<Player> outOfRangeRecipients = chatManager.getOutOfRangeRecipients(sender, "It");
            String outOfRangeMessage = chatManager.outOfRangeScramble(inRangeMessage, "It");

            new BukkitRunnable() {
                @Override
                public void run() {
                    Set<String> recipients;
                    recipients = rpEssentials.logger.mergeRecipients(inRangeRecipients, outOfRangeRecipients);
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "It", recipients, inRangeMessage); }

                    for (Player recipient : outOfRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + outOfRangeMessage, sender.getDisplayName())); }

                    for (Player recipient : inRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }
                } }.runTask(rpEssentials);

        }
        if (!outOfRange) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Set<String> recipients;
                    recipients = rpEssentials.logger.getRecipients(inRangeRecipients);
                    if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "It", recipients, inRangeMessage); }
                    for (Player recipient : inRangeRecipients) {
                        recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }
                } }.runTask(rpEssentials);

        }
        sender.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName()));
    }

    private void outOfCharacter(Player sender, String[] message) {
        if (!sender.hasPermission("RPEssentials.Command.OOC")) { return; }
        String senderFormat = config.getString("Chat.Action.OutOfCharacter.sender-format");
        String messageFormat = config.getString("Chat.Action.OutOfCharacter.message-format");
        String inRangeMessage = chatManager.combineMessage(message);
        Set<Player> inRangeRecipients = chatManager.getNearbyRecipients(sender, "OutOfCharacter");

        new BukkitRunnable() {
            @Override
            public void run() {
                Set<String> recipients = rpEssentials.logger.getRecipients(inRangeRecipients);
                if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "OutOfCharacter", recipients, inRangeMessage); }

                for (Player recipient : inRangeRecipients) { recipient.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName())); }
                } }.runTask(rpEssentials);
        sender.sendMessage(String.format(senderFormat + messageFormat + inRangeMessage, sender.getDisplayName()));
    }

    private void privateMessage(Player sender, String[] args) {
        if (!sender.hasPermission("RPEssentials.Command.PM")) { return; }
        String inRangeMessage = chatManager.combineMessage(args);
        Player receiver;

        if (chatManager.getReceiver(args[0]) == null) {
            sender.sendMessage(String.format("§f[ §bPM §f]: %s", "The person you are attempting to message is currently unavailable.")); return; } else {
            receiver = (Player) chatManager.getReceiver(args[0]); }

        switch (pm.getPMSetting().toLowerCase()) {

            case "off" -> sender.sendMessage(String.format("§f[ §bPM §f]: §4%s", "This server has the PM system disabled."));

            case "orbofcommunication" -> {
                sender.sendMessage(String.format("§f[ §bPM §f]: §4%s", "This server is using the \"Orb of Communication\"."));
                sender.sendMessage(String.format("§f[ §bPM §f]: §4%s", "Hold your Orb and either select \"MindMeld\" or \"Scribe\" to send a message.")); }

            case "vanilla" -> {
                String senderMessagePrefix = config.getString("Chat.Action.PrivateMessage.Vanilla.Sending.message-prefix");
                String senderMessageSender = config.getString("Chat.Action.PrivateMessage.Vanilla.Sending.sender");
                String senderMessageSuffix = config.getString("Chat.Action.PrivateMessage.Vanilla.Sending.suffix");
                String senderMessageFormat = config.getString("Chat.Action.PrivateMessage.Vanilla.Sending.message-format");

                String receiverMessagePrefix = config.getString("Chat.Action.PrivateMessage.Vanilla.Receiving.message-prefix");
                String receiverMessageSender = config.getString("Chat.Action.PrivateMessage.Vanilla.Receiving.sender");
                String receiverMessageSuffix = config.getString("Chat.Action.PrivateMessage.Vanilla.Receiving.suffix");
                String receiverMessageFormat = config.getString("Chat.Action.PrivateMessage.Vanilla.Receiving.message-format");

                Set<Player> inRangeRecipients = chatManager.getNearbyRecipients(sender, "PrivateMessage");

                if (outOfRange) {
                    Set<Player> outOfRangeRecipients = chatManager.getOutOfRangeRecipients(sender, "PrivateMessage");
                    String outOfRangeMessage = chatManager.outOfRangeScramble(inRangeMessage, "PrivateMessage");

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Set<String> recipients;
                            recipients = rpEssentials.logger.mergeRecipients(inRangeRecipients, outOfRangeRecipients);
                            if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "PrivateMessage", recipients, inRangeMessage); }

                            for (Player recipient : outOfRangeRecipients) {
                                if (recipient.equals(receiver)) { continue; }
                                recipient.sendMessage(String.format(
                                                senderMessagePrefix +
                                                senderMessageSender +
                                                senderMessageSuffix +
                                                senderMessageFormat +
                                                outOfRangeMessage,
                                                sender.getDisplayName(),
                                                receiver.getDisplayName())); }

                            for (Player recipient : inRangeRecipients) {
                                if (recipient.equals(receiver)) { continue; }
                                recipient.sendMessage(String.format(
                                                senderMessagePrefix +
                                                senderMessageSender +
                                                senderMessageSuffix +
                                                senderMessageFormat +
                                                inRangeMessage,
                                                sender.getDisplayName(),
                                                receiver.getDisplayName())); }
                        } }.runTask(rpEssentials); }

                if (!outOfRange) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Set<String> recipients = rpEssentials.logger.getRecipients(inRangeRecipients);
                            if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "PrivateMessage", recipients, inRangeMessage); }

                            for (Player recipient : inRangeRecipients) {
                                if (recipient.equals(receiver)) { continue; }
                                recipient.sendMessage(String.format(
                                        senderMessagePrefix +
                                        senderMessageSender +
                                        senderMessageSuffix +
                                        senderMessageFormat +
                                        inRangeMessage,
                                        sender.getDisplayName(),
                                        receiver.getDisplayName())); }
                        } }.runTask(rpEssentials); }

                sender.sendMessage(String.format(
                        senderMessagePrefix +
                        senderMessageSender +
                        senderMessageSuffix +
                        senderMessageFormat +
                        inRangeMessage,
                        sender.getDisplayName(),
                        receiver.getDisplayName()));

                receiver.sendMessage(String.format(
                        receiverMessagePrefix +
                        receiverMessageSender +
                        receiverMessageSuffix +
                        receiverMessageFormat +
                        inRangeMessage,
                        sender.getDisplayName(),
                        receiver.getDisplayName())); }

            case "pseudorp" -> {
                String senderMessagePrefix = config.getString("Chat.Action.PrivateMessage.PseudoRP.Sending.message-prefix");
                String senderMessageSender = config.getString("Chat.Action.PrivateMessage.PseudoRP.Sending.sender");
                String senderMessageSuffix = config.getString("Chat.Action.PrivateMessage.PseudoRP.Sending.suffix");
                String senderMessageFormat = config.getString("Chat.Action.PrivateMessage.PseudoRP.Sending.message-format");

                String receiverMessagePrefix = config.getString("Chat.Action.PrivateMessage.PseudoRP.Receiving.message-prefix");
                String receiverMessageSender = config.getString("Chat.Action.PrivateMessage.PseudoRP.Receiving.sender");
                String receiverMessageSuffix = config.getString("Chat.Action.PrivateMessage.PseudoRP.Receiving.suffix");
                String receiverMessageFormat = config.getString("Chat.Action.PrivateMessage.PseudoRP.Receiving.message-format");

                Set<Player> inRangeRecipients = chatManager.getNearbyRecipients(sender, "PrivateMessage");

                if (outOfRange) {
                    Set<Player> outOfRangeRecipients = chatManager.getOutOfRangeRecipients(sender, "PrivateMessage");
                    String outOfRangeMessage = chatManager.outOfRangeScramble(inRangeMessage, "PrivateMessage");

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Set<String> recipients;
                            recipients = rpEssentials.logger.mergeRecipients(inRangeRecipients, outOfRangeRecipients);
                            if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "PrivateMessage", recipients, inRangeMessage); }

                            for (Player recipient : outOfRangeRecipients) {
                                if (recipient.equals(receiver)) { continue; }
                                recipient.sendMessage(String.format(
                                        senderMessagePrefix +
                                        senderMessageSender +
                                        senderMessageSuffix +
                                        senderMessageFormat +
                                        outOfRangeMessage,
                                        sender.getDisplayName(),
                                        "???")); }

                            for (Player recipient : inRangeRecipients) {
                                if (recipient.equals(receiver)) { continue; }
                                recipient.sendMessage(String.format(
                                        senderMessagePrefix +
                                        senderMessageSender +
                                        senderMessageSuffix +
                                        senderMessageFormat +
                                        inRangeMessage,
                                        sender.getDisplayName(),
                                        "???")); }
                        } }.runTask(rpEssentials); }

                if (!outOfRange) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Set<String> recipients = rpEssentials.logger.getRecipients(inRangeRecipients);
                            if (rpEssentials.loggerStatus) { rpEssentials.logger.writeToLogger(sender, "PrivateMessage", recipients, inRangeMessage); }

                            for (Player recipient : inRangeRecipients) {
                                if (recipient.equals(receiver)) { continue; }
                                recipient.sendMessage(String.format(
                                        senderMessagePrefix +
                                        senderMessageSender +
                                        senderMessageSuffix +
                                        senderMessageFormat +
                                        inRangeMessage,
                                        sender.getDisplayName(),
                                        "???")); }
                        } }.runTask(rpEssentials); }

                sender.sendMessage(String.format(
                        senderMessagePrefix +
                        senderMessageSender +
                        senderMessageSuffix +
                        senderMessageFormat +
                        inRangeMessage,
                        sender.getDisplayName(),
                        receiver.getDisplayName()));

                receiver.sendMessage(String.format(
                        receiverMessagePrefix +
                        receiverMessageSender +
                        receiverMessageSuffix +
                        receiverMessageFormat +
                        inRangeMessage,
                        sender.getDisplayName(),
                        receiver.getDisplayName())); }
        }

    }

}

/*
new BukkitRunnable() {
    @Override
    public void run() {
        //code
    } }.runTask(rpEssentials);
 */
