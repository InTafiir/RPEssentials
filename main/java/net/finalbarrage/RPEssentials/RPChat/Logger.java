package net.finalbarrage.RPEssentials.RPChat;

import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Logger {

    private RPEssentials rpEssentials;
    private FileConfiguration config;
    private ConsoleCommandSender consoleMessage;
    private Map<String, Double> ranges;
    private static String messageInformation;
    private static String message;
    public static Boolean loggerStatus;

    private Path logPath;
    private File currentLog;

    public Logger(RPEssentials rpEssentials, ChatManager chatManager) {
        this.rpEssentials = rpEssentials;
        this.ranges = chatManager.ranges;
        this.config = rpEssentials.config.getConfig();
        consoleMessage = rpEssentials.getServer().getConsoleSender();
        loggerStatus = getLoggerStatus();
        messageInformation = config.getString("Chat.Modules.Chatlogs.Format.MessageInfo");
        message = config.getString("Chat.Modules.Chatlogs.Format.Message");

        if (loggerStatus) { init(); }
    }

    public Boolean getLoggerStatus() {
        return config.getBoolean("Chat.Modules.Chatlogs.Active");
    }

    private void init() {
        logPath = Paths.get(rpEssentials.getDataFolder() + "/Chatlogs/");
        currentLog = new File(logPath + "/" + rpEssentials.currentDate + ".txt");
        if (checkLogPathExists(logPath)) {
            consoleMessage.sendMessage(ChatColor.GREEN + "[RPEssentials][Chatlogger]: Log Path Found.");
        } else {
            consoleMessage.sendMessage(ChatColor.RED + "[RPEssentials][Chatlogger]: Log Path Not Found.");
            consoleMessage.sendMessage(ChatColor.YELLOW + "[RPEssentials][Chatlogger]: Creating Log Path..");
            createLogPath(logPath);
        }

        if (checkLogExists(currentLog)) {
            consoleMessage.sendMessage(ChatColor.GREEN + "[RPEssentials][Chatlogger]: Log Found in Path: " + logPath + currentLog);
        } else {
            consoleMessage.sendMessage(ChatColor.RED + "[RPEssentials][Chatlogger]: Log Not Found in Path.");
            consoleMessage.sendMessage(ChatColor.YELLOW + "[RPEssentials][Chatlogger]: Creating New Log File: " + currentLog);
            createNewLog(currentLog);
        }
    }

    private Boolean checkLogPathExists(Path logPath) { return Files.exists(logPath) && Files.isDirectory(logPath); }

    private Boolean checkLogExists(File currentLog) { return currentLog.exists(); }

    private void createLogPath(Path logPath) {
        try { Files.createDirectories(logPath);
            if (checkLogPathExists(logPath)) { consoleMessage.sendMessage(ChatColor.GREEN + "[RPEssentials][Chatlogger]: Path was Created!"); }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void createNewLog(File currentLog) {
        try {
            if (currentLog.createNewFile()) { consoleMessage.sendMessage(String.format(ChatColor.GREEN + "[RPEssentials][Chatlogger]: Log \"%s\" was created!", currentLog)); }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public Set<String> mergeRecipients(Set<Player> inRange, Set<Player> outOfRange) {
        Set<String> players = new HashSet<>();
        for (Player recipient : inRange) { players.add(recipient.getDisplayName()); }
        for (Player recipient : outOfRange) { players.add(recipient.getDisplayName()); }
        return players;
    }

    public Set<String> getRecipients(Set<Player> recipients) {
        Set<String> players = new HashSet<>();
        for (Player recipient : recipients) { players.add(recipient.getDisplayName()); }
        return players;
    }

    private void appendToFile(List<String> infoToLog) {
        int bufferSize = 8 * 1024;
        try (OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(this.currentLog, true), bufferSize), "windows-1252")) {
            writer.append(infoToLog.get(0)).append("\n");
            writer.append(infoToLog.get(1)).append("\n");
            writer.flush();
        } catch (FileNotFoundException e) { createNewLog(this.currentLog); } catch (IOException e) { e.printStackTrace(); }
    }

    public void writeToLogger(Player player, String action, Set<String> recipients, String messageSent) {
        Date time = new Date();
        Double range = ranges.get(action);
        List<String> combinedInfo = new ArrayList<>();
        String sender = player.getDisplayName();

        // MessageInfo: "[ %tT ] | [ Channel: %s | Range: %f ] | [ sender: %s ] | [ Recipients: %s ]"
        String msgInfo = String.format(messageInformation, time, action, range, sender, recipients);
        combinedInfo.add(msgInfo);

        // Message: "[ %tT ] | [ %s ]: %s"
        String msg = String.format(message, time, sender, messageSent);
        combinedInfo.add(msg);

        appendToFile(combinedInfo);
    }

}

// Great Tutorials for Files/Directories.
// https://mkyong.com/java/how-to-create-directory-in-java/
