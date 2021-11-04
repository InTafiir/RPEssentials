package net.finalbarrage.RPEssentials.RPChat;

import net.finalbarrage.RPEssentials.RPEssentials;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {

    private RPEssentials rpEssentials;
    private FileConfiguration config;

    public Filter(RPEssentials rpEssentials) {
        this.rpEssentials = rpEssentials;
        this.config = rpEssentials.config.getConfig();
    }

    public void ProfanityFilter(String[] message) {
    }

    public void EmojiFilter(String message) {
        Pattern colonPattern = Pattern.compile(":[)(\\]\\[370TYUIOPSDFLXCV/\\|@#$^*()-<>]( |$)", Pattern.CASE_INSENSITIVE);
        Matcher colonMatcher = colonPattern.matcher(message);
        Boolean colonFaceFound = colonMatcher.find();

        Pattern equalsPattern = Pattern.compile("=[)(\\]\\[370TYUIOPSDFLXCV/\\|@#$^*()-<>]( |$)", Pattern.CASE_INSENSITIVE);
        Matcher equalsMatcher = equalsPattern.matcher(message);
        Boolean equalsFaceFound = equalsMatcher.find();

        Pattern semiColonPattern = Pattern.compile(";[)(\\]\\[370TYUIOPSDFLXCV/\\|@#$^*()-<>]( |$)", Pattern.CASE_INSENSITIVE);
        Matcher semiColonMatcher = semiColonPattern.matcher(message);
        Boolean semiColonFaceFound = equalsMatcher.find();
    }
}

// :[)(\]\[370TYUIOPSDFLXCV/\|@#$^*()-<>]( |$)
// =[)(\]\[370TYUIOPSDFLXCV/\|@#$^*()-<>]( |$)
// ;[)(\]\[370TYUIOPSDFLXCV/\|@#$^*()-<>]( |$)
// https://www.freeformatter.com/java-regex-tester.html#ad-output