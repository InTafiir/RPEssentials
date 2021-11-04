package net.finalbarrage.RPEssentials.RPItems;

import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.*;
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

import java.util.ArrayList;
import java.util.List;

public class Compass {

    public static ItemStack compass;

    public static void init(RPEssentials rpEssentials) {
        rpEssentials.getServer().getPluginManager().registerEvents(new CompassEvents(rpEssentials), rpEssentials);
        Boolean toggleDescription = rpEssentials.config.getConfig().getBoolean("Items.Compass.toggle-description");
        Boolean isCraftable = rpEssentials.config.getConfig().getBoolean("Items.Compass.craftable");
        loadCompass(toggleDescription, isCraftable);
    }

    private static void loadCompass(Boolean toggleDescription, Boolean isCraftable) {
        ItemStack item = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Compass");

        List<String> lore = new ArrayList<>();
        if (!toggleDescription) {
            lore.add(""); } else {
            lore.add("§3Tells you what direction you're facing."); }
        meta.setLore(lore);

        meta.addEnchant(Enchantment.LUCK, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        compass = item;

        // Shaped Crafting Recipe
        if (isCraftable) {
            ShapedRecipe sr = new ShapedRecipe(NamespacedKey.minecraft("comp"), item);
            sr.shape(
                    " H ",
                    " C ",
                    " B ");
            sr.setIngredient('H', Material.STICK);
            sr.setIngredient('C', Material.COMPASS);
            sr.setIngredient('B', Material.WATER_BUCKET);
            Bukkit.getServer().addRecipe(sr);
        }
    }

    static void printFacing(Player player) {
        Location location = player.getLocation();
        float angle = location.getYaw();


        // North
        //      West       >    North           East        >    North
        if (angle >= 170 && angle <= 180 || angle <= -168.5 && angle >= -180) { player.sendMessage("I'm currently facing north.."); }

        // Northeast
        if (angle <= -100 && angle >= -168.4) { player.sendMessage("I'm currently facing northeast.."); }

        // Northwest
        if (angle >= 100 && angle <= 170) { player.sendMessage("I'm currently facing northwest.."); }



        // SOUTH
        //      West         >    South           East    >    South
        if (angle >= 0 && angle <= 11.4 || angle <= -11.5 && angle >= -0) { player.sendMessage("I'm currently facing south.."); }

        // Southeast
        if (angle <= -11 && angle >= -78.1) { player.sendMessage("I'm currently facing southeast.."); }

        // Southwest
        if (angle >= 11.5 && angle <= 79) { player.sendMessage("I'm currently facing southwest.."); }




        // EAST
        if (angle <= -100.5 && angle >= -78.2) { player.sendMessage("I'm currently facing east.."); }

        // WEST
        if (angle >= 90 && angle <= 135) { player.sendMessage("I'm currently facing west.."); }

    }
    //https://stackoverflow.com/questions/35566359/working-out-which-out-of-two-numbers-are-closer-to-another-number-java

}

class CompassEvents implements Listener {

    private RPEssentials rpEssentials;

    public CompassEvents(RPEssentials rpEssentials) {
        this.rpEssentials = rpEssentials;
    }

    @EventHandler
    private void useCompass(PlayerInteractEvent interactEvent) {
        Player player = interactEvent.getPlayer();
        Action action = interactEvent.getAction();
        ItemStack item = interactEvent.getItem();
        if (item == null) { return; }
        if (!player.hasPermission("RPEssentials.Compass.Use")) { interactEvent.setCancelled(true); }
        if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {
            if (item.getItemMeta().getDisplayName().equals("§6Compass")) {
                player.sendMessage(String.valueOf(player.getLocation().getYaw()));
                //Compass.printFacing(player);
            }
        }
    }
}
