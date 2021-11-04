package net.finalbarrage.RPEssentials.RPItems;

import dev.dbassett.skullcreator.SkullCreator;
import net.finalbarrage.RPEssentials.RPEssentials;
import net.finalbarrage.RPEssentials.SQL.SQLManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class Mailbox {
    public static ItemStack mailbox;

    public static void init(RPEssentials rpEssentials) {
        rpEssentials.getServer().getPluginManager().registerEvents(new MailboxEvents(), rpEssentials);
        loadMailbox();
    }

    private static void loadMailbox() {
        String skin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIxNTJhMDQ5MDU4YTliZGQ1NjJmNTQxNGIxMzhjZDUzNWJmMDEyZjdjYzRiMzg3MTIzMjhhOWZmMGJhOWQ2YiJ9fX0=";
        ItemStack head = new ItemStack(SkullCreator.itemFromBase64(skin));
        SkullMeta skull = (SkullMeta) head.getItemMeta();
        assert skull != null;
        skull.setDisplayName("§6Mailbox");
        head.setItemMeta(skull);
        mailbox = head;
    }

}

class MailboxEvents implements Listener {

    @EventHandler
    private void placeMailbox(BlockPlaceEvent placeEvent) {
        Player player = placeEvent.getPlayer();
        Location location = placeEvent.getBlockPlaced().getLocation();
        Chunk chunk = location.getChunk();
        Material type = placeEvent.getBlockPlaced().getType();
        DecimalFormat numform = new DecimalFormat(" 0;-0");
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();

        if (!player.hasPermission("RPEssentials.Mailbox.Place")) { placeEvent.setCancelled(true); }

        if (placeEvent.getItemInHand().getItemMeta().getDisplayName().equals("§6Mailbox")) {

            if (MailboxSQL.mailboxInChunk(chunkX, chunkZ)) {
                player.sendMessage("There is already a §6Mailbox §fnearby.");
                placeEvent.setCancelled(true);
                return; }

            player.sendMessage(String.format(
                    "You have placed a %s §fat coordinates [ §6X: %s, Y: %s, Z: %s §f]",
                    placeEvent.getItemInHand().getItemMeta().getDisplayName(),
                    numform.format(blockX),
                    numform.format(blockY),
                    numform.format(blockZ)
            ));

            MailboxSQL.createMailbox(chunkX, chunkZ, blockX, blockY, blockZ, type.toString());

            location.setX(blockX + 0.5D);
            location.setY(blockY + 0.5D);
            location.setZ(blockZ + 0.5D);

            Entity entity = placeEvent.getBlockPlaced().getLocation().getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            ArmorStand armorStand = (ArmorStand)entity;
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName("§6Mailbox");
        }
    }

    @EventHandler
    private void removeMailbox(BlockBreakEvent breakEvent) {
        Player player = breakEvent.getPlayer();
        Block block = breakEvent.getBlock();
        Material type = breakEvent.getBlock().getType();
        Location location = breakEvent.getBlock().getLocation();
        Entity[] entities = location.getChunk().getEntities();
        Chunk chunk = location.getChunk();
        ItemStack mailbox = Mailbox.mailbox;
        if (!player.hasPermission("RPEssentials.Mailbox.Break")) { breakEvent.setCancelled(true); }

        if (type.equals(Material.PLAYER_HEAD) || type.equals(Material.PLAYER_WALL_HEAD)) {
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            int blockX = location.getBlockX();
            int blockY = location.getBlockY();
            int blockZ = location.getBlockZ();

            if (MailboxSQL.containsMailbox(blockX, blockY, blockZ, type.toString())) {

                MailboxSQL.removeMailbox(chunkX, chunkZ, blockX, blockY, blockZ, type.toString());
                breakEvent.setDropItems(false);

                // TODO
                if (player.getGameMode().equals(GameMode.CREATIVE) && !player.getInventory().containsAtLeast(mailbox, 1)) { player.getInventory().addItem(mailbox); }
                if (!player.getInventory().containsAtLeast(mailbox, 1)) { player.getInventory().addItem(mailbox); }

                for (Entity ent : entities) {
                    if (!(ent instanceof ArmorStand) || ent.getCustomName() == null) { continue; }
                    if (ent.getCustomName().equals("§6Mailbox")) { ent.remove(); }
                }
            }
        }
    }

    @EventHandler
    private void useMailbox(PlayerInteractEvent interactEvent) {
        Player player = interactEvent.getPlayer();
        Action action = interactEvent.getAction();
        Block block = interactEvent.getClickedBlock();

        if (!player.hasPermission("RPEssentials.Mailbox.Use")) { interactEvent.setCancelled(true); }

        if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (block.getBlockData().getMaterial().equals(Material.PLAYER_HEAD) || block.getBlockData().getMaterial().equals(Material.PLAYER_WALL_HEAD)) {
                if (MailboxSQL.containsMailbox(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ(), block.getBlockData().getMaterial().toString())) {
                    MailboxGUI gui = new MailboxGUI();
                    player.openInventory(gui.getInventory()); }
            }
        }
    }


    @EventHandler
    private void useInterface(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) { return; }

        if (event.getClickedInventory().getHolder() instanceof MailboxGUI) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) { return; }

            Player player = (Player) event.getWhoClicked();
            MailboxGUI gui = new MailboxGUI();
            player.openInventory(gui.getInventory());

            switch (event.getCurrentItem().getItemMeta().getDisplayName()) {

                case "Inbox" -> {
                    gui.setSlot(0, "Inbox", Material.ENDER_CHEST);
                    gui.setSlot(9, "Outbox", Material.CHEST); }

                case "Outbox" -> {
                    gui.setSlot(0, "Inbox", Material.CHEST);
                    gui.setSlot(9, "Outbox", Material.ENDER_CHEST); }

            }
        }
    }
}

class MailboxSQL {

    public static void createMailbox(int chunkX, int chunkZ, int blockX, int blockY, int blockZ, String type) {
        String sql = "INSERT INTO mailboxes(chunkX, chunkZ, blockX, blockY, blockZ, type) VALUES(?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = SQLManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, chunkX);
            pstmt.setInt(2, chunkZ);
            pstmt.setInt(3, blockX);
            pstmt.setInt(4, blockY);
            pstmt.setInt(5, blockZ);
            pstmt.setString(6, type);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void removeMailbox(int chunkX, int chunkZ, int blockX, int blockY, int blockZ, String type) {
        String sql = "DELETE FROM mailboxes WHERE chunkX = ? AND chunkZ = ? AND blockX = ? AND blockY = ? AND blockZ = ? AND type = ?";
        try {
            Connection conn = SQLManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, chunkX);
            pstmt.setInt(2, chunkZ);
            pstmt.setInt(3, blockX);
            pstmt.setInt(4, blockY);
            pstmt.setInt(5, blockZ);
            pstmt.setString(6, type);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static Boolean containsMailbox(int blockX, int blockY, int blockZ, String type) {
        String sql = "SELECT id FROM mailboxes WHERE (SELECT blockX = ? AND blockY = ? AND blockZ = ? AND type = ?)";
        Boolean bool = false;
        try {
            Connection conn = SQLManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, blockX);
            pstmt.setInt(2, blockY);
            pstmt.setInt(3, blockZ);
            pstmt.setString(4, type);
            ResultSet rs = pstmt.executeQuery();
            bool = rs.next();
            pstmt.close();
            return bool;
        } catch (SQLException e) { e.printStackTrace(); }
        return bool;
    }

    public static Boolean mailboxInChunk(int chunkX, int chunkZ) {
        String sql = "SELECT id FROM mailboxes WHERE (SELECT chunkX = ? AND chunkZ = ?)";
        Boolean bool = false;
        try {
            Connection conn = SQLManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, chunkX);
            pstmt.setInt(2, chunkZ);
            ResultSet rs = pstmt.executeQuery();
            bool = rs.next();
            pstmt.close();
            return bool;
        } catch (SQLException e) { e.printStackTrace(); }
        return bool;
    }

}

class MailboxGUI implements InventoryHolder {

    private Inventory inv;

    public MailboxGUI() {
        inv = Bukkit.createInventory(this, 54, "----------[Mailbox]---------");
        init();
    }

    private void init() {
        ItemStack item;

        // Row 1
        item = createButton("Inbox", Material.ENDER_CHEST,false);
        inv.setItem(0, item);
        for (int i = 1; i < 9; i++) {
            item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
            inv.setItem(i, item);}

        //Row 2
        item = createButton("Outbox", Material.CHEST,false);
        inv.setItem(9, item);
        item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
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
        item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
        inv.setItem(44, item);

        // Row 6
        for (int i = 45; i < 54; i++) {
            item = createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
            inv.setItem(i, item); }

        item = createButton("Compose", Material.WRITABLE_BOOK,true);
        inv.setItem(45, item);
        item = createButton("Page Up", Material.PAPER,false);
        inv.setItem(48, item);
        item = createButton("Page Down", Material.PAPER,false);
        inv.setItem(50, item);

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

    public ItemStack setSlot(Integer slotNumber, String name , Material material) {
        ItemStack item;
        item = createButton(name, material,false);
        inv.setItem(slotNumber, item);
        return item;
    }

    @Override
    public Inventory getInventory() { return inv; }

}
