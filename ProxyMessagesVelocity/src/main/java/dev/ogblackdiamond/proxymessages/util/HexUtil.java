package dev.ogblackdiamond.proxymessages.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexUtil {
    // checks to see if a string is a valid hex code
    public static boolean isValidHex(String str)
    {
        boolean ret = false;

        if(str != null) {
            String hexRegex = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
            Pattern hexPattern = Pattern.compile(hexRegex);
            Matcher hexMatcher = hexPattern.matcher(str);
            ret = hexMatcher.matches();
        }
        return ret;
    }
}
