package net.finalbarrage.RPEssentials.RPItems;

import dev.dbassett.skullcreator.SkullCreator;
import net.finalbarrage.RPEssentials.RPEssentials;
import net.finalbarrage.RPEssentials.SQL.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class OrbOfCommunication {
    public static ItemStack orbOfCommunication;

    public static void init(RPEssentials rpEssentials) {
        rpEssentials.getServer().getPluginManager().registerEvents(new OrbEvents(), rpEssentials);
        Boolean toggleDescription = rpEssentials.config.getConfig().getBoolean("Items.OrbOfCommunication.toggle-description");
        Boolean isCraftable = rpEssentials.config.getConfig().getBoolean("Items.OrbOfCommunication.craftable");
        loadOrb(toggleDescription, isCraftable);
    }

    private static void loadOrb(Boolean toggleDescription, Boolean isCraftable) {
        ItemStack item = new ItemStack(Material.ENDER_EYE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§3Orb Of Communication");
        List<String> lore = new ArrayList<>();
        if (toggleDescription) { lore.add("§3This Orb allows your to contact other players over vast distances or even other worlds."); }
        meta.setLore(lore);
        meta.addEnchant(Enchantment.LUCK, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        orbOfCommunication = item;

        // Shaped Crafting Recipe
        if (isCraftable) {
            ShapedRecipe sr = new ShapedRecipe(NamespacedKey.minecraft("orb"), item);
            sr.shape(
                    "RPT",
                    "REB",
                    "RLG");
            sr.setIngredient('R', Material.REDSTONE);
            sr.setIngredient('P', Material.ENDER_PEARL);
            sr.setIngredient('T', Material.REDSTONE_TORCH);
            sr.setIngredient('E', Material.ENDER_EYE);
            sr.setIngredient('B', Material.STONE_BUTTON);
            sr.setIngredient('L', Material.LIGHTNING_ROD);
            sr.setIngredient('G', Material.GOLD_INGOT);
            Bukkit.getServer().addRecipe(sr);
        }
    }

    public static Integer newOrbID(RPEssentials rpEssentials) {
        Random random = new Random();
        List<Integer> newID = new ArrayList<>();
        while (newID.size() < rpEssentials.config.getConfig().getInt("Items.OrbOfCommunication.id-length")) { newID.add(random.nextInt(9 + 1)); }
        StringBuilder id = new StringBuilder();
        for (Integer i : newID) { id.append(i); }
        return Integer.valueOf(id.toString());
    }

}




class OrbEvents implements Listener {

    @EventHandler
    private void useOrb(PlayerInteractEvent interactEvent) {
        Player player = interactEvent.getPlayer();
        Action action = interactEvent.getAction();
        ItemStack item = interactEvent.getItem();
        if (item == null) { return; }
        if (!player.hasPermission("RPEssentials.Orb.Use")) { interactEvent.setCancelled(true); }
        if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {
            if (item.getItemMeta().equals(OrbOfCommunication.orbOfCommunication.getItemMeta())) {
                interactEvent.setCancelled(true);
                OrbGUIHome home = new OrbGUIHome();
                player.openInventory(home.getInventory());
                player.sendMessage("§3* You gaze into the orb...");
            }
        }
    }

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) { return; }

        // Orb - Home Screen
        if (event.getClickedInventory().getHolder() instanceof OrbGUIHome) {

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null) { return; }

            // MindMeld
            if (event.getCurrentItem().getType() == Material.POPPED_CHORUS_FRUIT) {
                player.sendMessage("MindMeld");
            }

            // Contacts
            if (event.getCurrentItem().getType() == Material.BOOK) {
                OrbGUIContacts contacts = new OrbGUIContacts();
                player.openInventory(contacts.getInventory());
            }

            // Reply
            if (event.getCurrentItem().getType() == Material.WRITABLE_BOOK) {
                player.sendMessage("Reply");
            }
        }

        // Orb - Contacts Screen
        if (event.getClickedInventory().getHolder() instanceof OrbGUIContacts) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null) { return; }

            // Border
            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) { return; }

            // Add Contact
            if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                player.sendMessage("Add Contact");
            }

            // Home Button
            if (event.getCurrentItem().getType() == Material.ENDER_EYE) {
                OrbGUIHome home = new OrbGUIHome();
                player.openInventory(home.getInventory());
            }

            // Remove Contact
            if (event.getCurrentItem().getType() == Material.SKELETON_SKULL) { return; }

            // Page Up
            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Page Up")) { return; }

            // Page Down
            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Page Down")) { return; }

        }
    }
}

// GUIs

class OrbGUIHome implements InventoryHolder {

    private Inventory inv;

    public OrbGUIHome() {
        inv = Bukkit.createInventory(this, 9, "-----------[Home]----------");
        init();
    }

    private void init() {
        ItemStack item;

        // Calling System
        item = createButton("MindMeld", Material.POPPED_CHORUS_FRUIT, true, Collections.singletonList("Connect your mind with another person to hold a conversation."));
        inv.setItem(2, item);

        // Contacts Book
        item = createButton("Contacts", Material.BOOK, true, Collections.singletonList("A list of all of your contacts."));
        inv.setItem(4, item);

        // Single Message
        item = createButton("Scribe", Material.WRITABLE_BOOK, true, Collections.singletonList("Reply to the last message you were sent."));
        inv.setItem(6, item);

        // Fill Empty
        for (int i = 0; i < 6; i++) {
            item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
            inv.setItem(getInventory().firstEmpty(), item);
        }

    }

    private ItemStack createButton(String name, Material material, Boolean isEnchant, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (isEnchant) {
            meta.addEnchant(Enchantment.LUCK, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); }
        if (!lore.isEmpty()) { meta.setLore(lore); }
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBorderItem(Material material) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}

class OrbGUIContacts implements InventoryHolder {

    private Inventory inv;

    public OrbGUIContacts() {
        inv = Bukkit.createInventory(this, 54, "----------Contacts---------");
        init();
    }

    private void init() {
        ItemStack item;

        // Row 1
        for (int i = 0; i < 9; i++) {
            item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
            inv.setItem(i, item);
        }

        // Row 2
        item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
        inv.setItem(9, item);
        item = createButton(ChatColor.DARK_PURPLE + "Page Up", Material.PAPER, false);
        inv.setItem(17, item);

        // Row 3
        item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
        inv.setItem(18, item);
        item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
        inv.setItem(26, item);

        // Row 4
        item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
        inv.setItem(27, item);
        item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
        inv.setItem(35, item);

        // Row 5
        item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
        inv.setItem(36, item);
        item = createButton(ChatColor.DARK_PURPLE + "Page Down", Material.PAPER, false);
        inv.setItem(44, item);

        // Row 6
        for (int i = 45; i < 48; i++) {
            item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
            inv.setItem(i, item); }

        item = createButton(ChatColor.DARK_GREEN + "Add Contact", Material.PLAYER_HEAD, false);
        inv.setItem(48, item);

        item = createButton(ChatColor.DARK_AQUA + "Home", Material.ENDER_EYE, true);
        inv.setItem(49, item);

        item = createButton(ChatColor.DARK_RED + "Remove Contact", Material.SKELETON_SKULL, false);
        inv.setItem(50, item);

        for (int i = 51; i < 54; i++) {
            item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
            inv.setItem(i, item); }
    }

    private ItemStack createButton(String name, Material material, Boolean isEnchant) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (isEnchant) {
            meta.addEnchant(Enchantment.LUCK, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); }
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBorderItem(Material material) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createContact(Player sender) {

        // SQL GET CONTACTS



        ItemStack head = new ItemStack(SkullCreator.itemFromUuid(sender.getUniqueId()));
        SkullMeta skull = (SkullMeta) head.getItemMeta();
        skull.setDisplayName("§6Mailbox");
        head.setItemMeta(skull);
        return head;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}

class OrbSQL {

    public static void getContacts() {
        String sql = "SELECT id FROM mailboxes WHERE (SELECT x = ? AND y = ? AND z = ?);";
        try {
            Connection conn = SQLManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
