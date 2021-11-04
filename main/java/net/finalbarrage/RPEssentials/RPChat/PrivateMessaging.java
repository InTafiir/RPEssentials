package net.finalbarrage.RPEssentials.RPChat;

import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.configuration.file.FileConfiguration;

public class PrivateMessaging {

    private RPEssentials rpEssentials;
    private FileConfiguration config;
    private String pmSetting;

    public PrivateMessaging(RPEssentials rpEssentials) {
        this.rpEssentials = rpEssentials;
        this.config = rpEssentials.config.getConfig();
        pmSetting = config.getString("Chat.Action.PrivateMessage.setting");
    }

    public void setPmSetting(String pmSetting) {
        String setting = null;

        if (pmSetting.equals(null) || pmSetting.equals("")) { return; }
        if (pmSetting.equalsIgnoreCase("orb")) { setting = "OrbOfCommunication"; }
        if (pmSetting.equalsIgnoreCase("vanilla")) { setting = "Vanilla"; }
        if (pmSetting.equalsIgnoreCase("pseudorp")) { setting = "PseudoRP"; }
        if (pmSetting.equalsIgnoreCase("off")) { setting = "Off"; }

        config.set("Chat.Action.PrivateMessage.setting", setting);
    }

    public String getPMSetting() { return this.pmSetting; }

}
