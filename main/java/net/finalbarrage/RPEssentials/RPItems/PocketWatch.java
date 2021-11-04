package net.finalbarrage.RPEssentials.RPItems;

import dev.jcsoftware.jscoreboards.JGlobalMethodBasedScoreboard;
import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PocketWatch {
    public static ItemStack pocketWatch;

    public static void init(RPEssentials rpEssentials) {
        rpEssentials.getServer().getPluginManager().registerEvents(new PocketWatchEvents(rpEssentials), rpEssentials);
        Boolean toggleDescription = rpEssentials.config.getConfig().getBoolean("Items.PocketWatch.toggle-description");
        Boolean isCraftable = rpEssentials.config.getConfig().getBoolean("Items.PocketWatch.craftable");
        loadWatch(toggleDescription, isCraftable);
    }

    private static void loadWatch(Boolean toggleDescription, Boolean isCraftable) {
        ItemStack item = new ItemStack(Material.CLOCK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Pocket Watch");

        List<String> lore = new ArrayList<>();
        if (!toggleDescription) {
            lore.add("");
        } else {
            lore.add("§3Helps determine what time is in your world.");
            lore.add("§3(And server-side)");
        }
        meta.setLore(lore);

        meta.addEnchant(Enchantment.LUCK, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        pocketWatch = item;

        // Shaped Crafting Recipe
        if (isCraftable) {
            ShapedRecipe sr = new ShapedRecipe(NamespacedKey.minecraft("pocketwatch"), item);
            sr.shape(
                    "CHG",
                    "IcP",
                    "RIG");
            sr.setIngredient('C', Material.CHAIN);
            sr.setIngredient('H', Material.TRIPWIRE_HOOK);
            sr.setIngredient('G', Material.GOLD_INGOT);
            sr.setIngredient('I', Material.IRON_INGOT);
            sr.setIngredient('c', Material.CLOCK);
            sr.setIngredient('P', Material.GLASS_PANE);
            sr.setIngredient('R', Material.REDSTONE);
            Bukkit.getServer().addRecipe(sr);
        }
    }

}

class PocketWatchScoreboard {

    private RPEssentials rpEssentials;
    JGlobalMethodBasedScoreboard scoreboard;
    public List<Player> playersViewingPocketWatch = new ArrayList<>();

    public PocketWatchScoreboard(RPEssentials rpEssentials) {
        this.rpEssentials = rpEssentials;
        this.scoreboard = new JGlobalMethodBasedScoreboard();
    }

    private void updateScoreboard() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(rpEssentials, () -> scoreboardInfo(), 0L, 20L);
    }

    private void scoreboardInfo() {
        String pWatchHeader = ChatColor.GOLD + "[ Pocket Watch ]";
        String newLine = "";
        String icTimeStamp = "         " + ChatColor.UNDERLINE + "[ IC  Timestamp ]";
        String inGameTimeStamp = "     [ Days ]: " + rpEssentials.getServer().getWorlds().get(0).getGameTime() / 24000 + " | [ Time ]: " + rpEssentials.getServer().getWorlds().get(0).getFullTime() / 1000;
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("     MM/dd/yyyy | HH:mm:ss");
        String formatDateTime = currentDateTime.format(dateTimeFormat);
        String oocTimeStamp = "        " + ChatColor.UNDERLINE + "[ OOC Timestamp ]";
        String irlTimestamp = formatDateTime;

        scoreboard.setTitle(pWatchHeader);
        scoreboard.setLines(newLine, icTimeStamp, newLine, inGameTimeStamp, newLine, oocTimeStamp, newLine, irlTimestamp);
    }

    public void addToScoreboard(Player player) {
        updateScoreboard();
        scoreboard.addPlayer(player);
        playersViewingPocketWatch.add(player);
        scoreboardInfo();
    }

    public void removeFromScoreboard(Player player) {
        scoreboard.removePlayer(player);
        playersViewingPocketWatch.remove(player);
    }
}

class PocketWatchEvents implements Listener {

    private RPEssentials rpEssentials;
    private PocketWatchScoreboard pocketWatchScoreboard;

    public PocketWatchEvents(RPEssentials rpEssentials) {
        this.rpEssentials = rpEssentials;
        this.pocketWatchScoreboard = new PocketWatchScoreboard(rpEssentials);
    }

    @EventHandler
    private void usePocketWatch(PlayerInteractEvent interactEvent) {
        Player player = interactEvent.getPlayer();
        Action action = interactEvent.getAction();
        ItemStack item = interactEvent.getItem();
        if (item == null) { return; }
        if (!player.hasPermission("RPEssentials.Watch.Use")) { return; }
        if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {
            if (item.getItemMeta().equals(PocketWatch.pocketWatch.getItemMeta())) {
                rpEssentials.playersViewingChatSettings.remove(player);
                if (pocketWatchScoreboard.playersViewingPocketWatch.contains(player)) {
                    pocketWatchScoreboard.removeFromScoreboard(player); } else {
                    pocketWatchScoreboard.addToScoreboard(player); }
            }

        }
    }
}


