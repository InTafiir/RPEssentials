package net.finalbarrage.RPEssentials.RPChat;

import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ChatManager {

    private RPEssentials rpEssentials;
    private FileConfiguration config;
    public static Boolean openGlobal;
    public static Boolean outOfRange;
    public Map<String, Double> ranges = new HashMap<>();
    private Map<String, String> messageFormats = new HashMap<>();

    public ChatManager(RPEssentials rpEssentials) {
        this.rpEssentials = rpEssentials;
        this.config = rpEssentials.config.getConfig();
        setBooleans();
        setRanges();
        setMessageFormats();
        rpEssentials.getServer().getPluginManager().registerEvents(new Channels(rpEssentials, this), rpEssentials);
    }

    private void setBooleans() {
        openGlobal = config.getBoolean("Chat.Channels.Global.toggle-global");
        outOfRange = config.getBoolean("Chat.Channels.Proximity.outofrange");
    }

    private void setRanges() {
        // Channel Ranges
        ranges.put("Global", (double) 0);
        ranges.put("Proximity", config.getDouble("Chat.Channels.Proximity.range"));

        // Action Ranges
        ranges.put("Me", config.getDouble("Chat.Action.Me.range"));
        ranges.put("Do", config.getDouble("Chat.Action.Do.range"));
        ranges.put("It", config.getDouble("Chat.Action.It.range"));
        ranges.put("Shout", config.getDouble("Chat.Action.Shout.range"));
        ranges.put("Whisper", config.getDouble("Chat.Action.Whisper.range"));
        ranges.put("OutOfCharacter", config.getDouble("Chat.Action.OutOfCharacter.range"));
        ranges.put("PrivateMessage", config.getDouble("Chat.Action.PrivateMessage.range"));
    }

    private void setMessageFormats() {
        // Channel Formats
        messageFormats.put("Proximity", config.getString("Chat.Channels.Proximity.message-format"));

        // Action Formats
        messageFormats.put("Me", config.getString("Chat.Action.Me.message-format"));
        messageFormats.put("Do", config.getString("Chat.Action.Do.message-format"));
        messageFormats.put("It", config.getString("Chat.Action.It.message-format"));
        messageFormats.put("Shout", config.getString("Chat.Action.Shout.message-format"));
        messageFormats.put("Whisper", config.getString("Chat.Action.Whisper.message-format"));
        messageFormats.put("OutOfCharacter", config.getString("Chat.Action.OutOfCharacter.message-format"));

        // Private Messages
        messageFormats.put("PrivateMessage", config.getString("Chat.Action.PrivateMessage.Vanilla.Sending.message-format"));
    }


    public Boolean getOpenGlobal() { return openGlobal; }

    public void toggleOpenGlobal() {
        if (openGlobal) {
            openGlobal = false;
            rpEssentials.config.getConfig().set("Chat.Channels.Global.toggle-global", false); } else {
            openGlobal = true;
            rpEssentials.config.getConfig().set("Chat.Channels.Global.toggle-global", true); }
        rpEssentials.config.saveConfig();
    }

    public Boolean getOutOfRange() { return outOfRange; }

    public void toggleOutOfRange() {
        if (outOfRange) {
            outOfRange = false;
            rpEssentials.config.getConfig().set("Chat.Channels.Proximity.outofrange", false); } else {
            outOfRange = true;
            rpEssentials.config.getConfig().set("Chat.Channels.Proximity.outofrange", true); }
        rpEssentials.config.saveConfig();
    }



    public Set<Player> getNearbyRecipients(Player sender, String action) {
        Double range = ranges.get(action);
        Set<Player> inRangeRecipients = new HashSet<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity ent : sender.getNearbyEntities(range, range, range)) {
                    if (!(ent instanceof Player)) { continue; }
                    inRangeRecipients.add((Player) ent); }
            } }.runTask(rpEssentials);

        return inRangeRecipients;
    }

    public Set<Player> getOutOfRangeRecipients(Player sender, String action) {
        Double range = ranges.get(action);
        Double outOfRange = range + (range/2);
        Set<Player> inRangeRecipients = getNearbyRecipients(sender, action);
        Set<Player> outOfRangeRecipients = new HashSet<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity ent : sender.getNearbyEntities(outOfRange, outOfRange, outOfRange)) {
                    if (!(ent instanceof Player) || inRangeRecipients.contains(ent) ) { continue; }
                    outOfRangeRecipients.add((Player) ent); }
            } }.runTask(rpEssentials);

        return outOfRangeRecipients;
    }

    public Object getReceiver(String receiver) {
        if (!rpEssentials.onlinePlayers.containsKey(receiver)) { return null; }
        return rpEssentials.onlinePlayers.get(receiver);
    }



    public String outOfRangeScramble(String message, String action) {
        String messageFormat = messageFormats.get(action);
        StringBuilder scrambledMessage = new StringBuilder();
        String[] words = message.split(" ");

        for (String word : words) {
            StringBuilder scrambledWord = new StringBuilder();
            String[] characters = word.split("");

            for (String character : characters) {
                if (Math.random() < 0.7) {
                    character = messageFormat + "§k" + character + "§r"; } else {
                    character = messageFormat + character; }
                scrambledWord.append(character); }

            scrambledMessage.append(scrambledWord + " ");
        }
        return scrambledMessage.toString();
    }

    public String combineMessage(String[] message) {
        StringBuilder builtMessage = new StringBuilder();
        for (String word : message) {
            builtMessage.append(word).append(" "); }
        return builtMessage.toString();
    }
}
