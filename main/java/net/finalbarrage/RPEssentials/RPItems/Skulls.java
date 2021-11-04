package net.finalbarrage.RPEssentials.RPItems;

import dev.dbassett.skullcreator.SkullCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class Skulls implements Listener {

    private Boolean skullDrops;

    public Skulls(Boolean skullDrops) { this.skullDrops = skullDrops; }

    @EventHandler
    private void onDeath(PlayerDeathEvent deathEvent) {
        Player player = deathEvent.getEntity();
        if (skullDrops) {
            ItemStack skull = SkullCreator.itemFromUuid(player.getUniqueId());
            deathEvent.getDrops().add(skull); }
    }
}
