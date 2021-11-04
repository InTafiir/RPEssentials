package net.finalbarrage.RPEssentials.LiM;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;

public class Settlement {
}

class Outpost {

    private String name;

    private Integer tier = 1;
    private Integer maxMembers = 5;

    private Player leader;
    private Player officer;

    private ArrayList<Player> members;

    private Map<Integer, Map<Integer, Integer>> claimedChunks;

}
/*
Outpost:

    Level:              Tier 1
    Size:               3 Chunks * 3 Chunks
    Max Members:        5 Players
    Points-To-Upgrade:  10,000

    Ranks:
        Leader:         "Founder"   [x1 Player(s)]
        Officer:        "Officer"   [x1 Player(s)]
        Member:         "Campmate"  [x4 Player(s)]
 */