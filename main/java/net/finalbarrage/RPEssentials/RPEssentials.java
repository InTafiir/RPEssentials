package net.finalbarrage.RPEssentials;

import net.finalbarrage.RPEssentials.RPChat.ChatManager;
import net.finalbarrage.RPEssentials.RPChat.Logger;
import net.finalbarrage.RPEssentials.RPChat.SettingsScoreboard;
import net.finalbarrage.RPEssentials.Configs.ConfigManager;
import net.finalbarrage.RPEssentials.Permissions.PermissionManager;
import net.finalbarrage.RPEssentials.Events.ServerEvents;
import net.finalbarrage.RPEssentials.RPItems.*;
import net.finalbarrage.RPEssentials.SQL.SQLManager;
import net.finalbarrage.RPEssentials.RPChat.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPEssentials extends JavaPlugin {

    public LocalDate currentDate = LocalDate.now();

    public Map<String, Player> onlinePlayers = new HashMap<>();
    public List<Player> playersViewingPocketWatch, playersViewingChatSettings = new ArrayList<>();
    //public List<Player> playersViewingChatSettings = new ArrayList<>();
    public Boolean loggerStatus;

    // Configs
    public ConfigManager config, permissions;
    //public ConfigManager permissions;
    public SQLManager sql;

    // Chat
    public ChatManager chatManager;
    public Logger logger;
    public SettingsScoreboard settingsScoreboard;

    // Permissions
    public PermissionManager permissionManager;

    @Override
    public void onEnable() {

        // Load Configs
        config = new ConfigManager(this, "Config.yml");
        config.reloadConfig();

        // Load Permissions
        permissions = new ConfigManager(this, "permissions.yml");
        permissionManager = new PermissionManager(this);
        permissions.reloadConfig();

        // Load SQL
        sql = new SQLManager(this);

        // Load Modules
        this.loadChatModules();
        this.loadItems();
        this.loadCommands();

        // Load Events
        this.getServer().getPluginManager().registerEvents(new ServerEvents(this), this);

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[RPEssentials]: Enabled!");
    }

    private void loadChatModules() {
        chatManager = new ChatManager(this);
        logger = new Logger(this, chatManager);
        loggerStatus = logger.getLoggerStatus();
        settingsScoreboard = new SettingsScoreboard(this, chatManager);
    }

    private void loadItems() {

        String header = "[RPEssentials][Items]: ";
        List<String> activeItems = new ArrayList<>();
        List<String> inactiveItems = new ArrayList<>();

        boolean orbActive = config.getConfig().getBoolean("Items.OrbOfCommunication.toggle-item");
        if (orbActive) { OrbOfCommunication.init(this); activeItems.add("Orb Of Communication");
        } else { inactiveItems.add("Orb Of Communication"); }

        boolean mailboxActive = config.getConfig().getBoolean("Items.Mailbox.toggle-item");
        if (mailboxActive) { Mailbox.init(this); activeItems.add("Mailbox");
        } else {  inactiveItems.add("Mailbox"); }

        boolean watchActive = config.getConfig().getBoolean("Items.PocketWatch.toggle-item");
        if (watchActive) { PocketWatch.init(this); activeItems.add("Pocket Watch");
        } else {  inactiveItems.add("Pocket Watch"); }

        boolean compassActive = config.getConfig().getBoolean("Items.Compass.toggle-item");
        if (compassActive) { Compass.init(this); activeItems.add("Compass");
        } else {  inactiveItems.add("Compass"); }

        boolean skullDrops = config.getConfig().getBoolean("Items.Skulls.drop-on-death");
        if (skullDrops) { this.getServer().getPluginManager().registerEvents(new Skulls(skullDrops), this); activeItems.add("Skulls (On Death)");
        } else {  inactiveItems.add("Skulls (On Death)"); }

        if (activeItems.size() > 0) { getServer().getConsoleSender().sendMessage(ChatColor.GREEN + header + "Active - " + activeItems); }
        if (inactiveItems.size() > 0) { getServer().getConsoleSender().sendMessage(ChatColor.RED + header + "Inactive - " + inactiveItems); }
    }

    private void loadCommands() {
        CommandManager commandManager = new CommandManager(this, chatManager);
        getCommand("Get").setExecutor(commandManager);
        getCommand("Settings").setExecutor(commandManager);
        getCommand("Me").setExecutor(commandManager);
        getCommand("Do").setExecutor(commandManager);
        getCommand("It").setExecutor(commandManager);
        getCommand("Shout").setExecutor(commandManager);
        getCommand("Whisper").setExecutor(commandManager);
        getCommand("OutOfCharacter").setExecutor(commandManager);
        getCommand("PrivateMessage").setExecutor(commandManager);
    }

    @Override
    public void onDisable() {
        config.saveConfig();
        permissionManager.playerPermissions.clear();
        SQLManager.disconnect();
        Bukkit.getServer().getScheduler().cancelTasks(this);
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[RPEssentials]: Disabled!");
    }

}

//----------------------------[Links]----------------------------\\



// TechnoVision - Spigot: Custom Plugin Tutorial - Custom Items (#6)
// https://www.youtube.com/watch?v=5npPUMrYaYE

// TechnoVision - Spigot: Custom Plugin Tutorial - Custom Inventories (#8)
// https://www.youtube.com/watch?v=dEwv7ay1RCI

// SkullCreator API
// https://github.com/deanveloper/SkullCreator



//----------------------------[Tools]----------------------------\\



// ArmorStand Poser
// https://haselkern.com/Minecraft-ArmorStand/

// Item IDs
// https://minecraftitemids.com/

// Custom Heads for Decoration Blocks
// https://minecraft-heads.com/

// Character Counter
// https://wordcounter.net/character-count



//----------------------------[]
// ----------------------------\\














//----------------------------
// ----------------------------\\