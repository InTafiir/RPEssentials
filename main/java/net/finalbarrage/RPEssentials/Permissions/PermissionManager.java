package net.finalbarrage.RPEssentials.Permissions;

import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.UUID;

public class PermissionManager {

    public HashMap<UUID, PermissionAttachment> playerPermissions = new HashMap<>();
    private RPEssentials rpEssentials;
    private FileConfiguration config;

    public PermissionManager(RPEssentials rpEssentials) {
        this.rpEssentials = rpEssentials;
        this.config = rpEssentials.permissions.getConfig();
    }

    public void permissionSetter(Player player) {
        PermissionAttachment attachment = player.addAttachment(rpEssentials);
        this.playerPermissions.put(player.getUniqueId(), attachment);

        for (String groups : rpEssentials.permissions.getConfig().getConfigurationSection("Groups").getKeys(false)) {
            for (String permissions : config.getStringList("Groups." + groups + ".permissions")) {
                attachment.setPermission(permissions, true); }
        }
    }

}
