package dev.ogblackdiamond.proxymessages.util;

import dev.ogblackdiamond.proxymessages.config.ConfigUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MessageUtil {

    private ConfigUtil configUtil;

    private final String playerStr = "{player}";
    private final int playerNameLength = playerStr.length(); 
    private final String previousServerNameStr = "{prev}";
    private final int previousServerNameLength = previousServerNameStr.length();
    private final String newSeverNameStr = "{cur}";
    private final int newServerNameLength = newSeverNameStr.length();

    private final int colorLength = "{#xxxxxx}".length();

    private final String boldStr = "{bold}";
    private final int boldLength = boldStr.length();
    private final String italicStr = "{italic}";
    private final int italicLength = italicStr.length();
    private final String strikeStr = "{strike}";
    private final int strikeLength = strikeStr.length();
    private final String underlineStr = "{underline}";
    private final int underlineLength = underlineStr.length();
    private final String obfuscateStr = "{obfuscate}";
    private final int obfuscatedLength = obfuscateStr.length();

    private final TextColor defaultTextColor = NamedTextColor.YELLOW;

    public MessageUtil(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    // compiles the message, interpolating correct strings when needed.
    public MessageReturns compileFormattedMessage(String type, String playerName, String previousServer, String newServer, String ogString, boolean fromDiscord) {

        String chosenMessage = ogString;
        String finalString = "";


        // inject the custom player name color
        int playerStrLocation = ogString.indexOf(playerStr);
        
        if (playerStrLocation != -1 || !fromDiscord) {

            int previousColorLocation = ogString.indexOf("{#");
            int prevPrevColorLoc = previousColorLocation;
            // find the closest color definition to the player name
            while (previousColorLocation != -1 && previousColorLocation < playerStrLocation) {
                prevPrevColorLoc = previousColorLocation;
                previousColorLocation = ogString.indexOf("{#", prevPrevColorLoc);
            }
            previousColorLocation = prevPrevColorLoc;
            String previousColor = "{" + defaultTextColor.asHexString() + "}";
            // return the string to the previously existing color
            if (previousColorLocation < playerStrLocation && previousColorLocation != -1){
                previousColor = ogString.substring(previousColorLocation, previousColorLocation + colorLength);
            }

            chosenMessage = 
                ogString.substring(0, playerStrLocation) 
                + "{" + configUtil.getColor(playerName) + "}"
                + ogString.substring(playerStrLocation, playerStrLocation + playerNameLength)
                + previousColor
                + ogString.substring(playerStrLocation + playerNameLength);
        }

        int messageLength = chosenMessage.length();

        // builds the final component, interpolating the correct strings when need  
        for (int i = 0; i < messageLength; i++) {

            boolean atLength = i + playerNameLength > messageLength;
            boolean newServerLength = i + newServerNameLength > messageLength;
            boolean previousServerLength = i + previousServerNameLength > messageLength;

            if (!atLength && chosenMessage.substring(i, i + playerNameLength).equals(playerStr)) {
                finalString += playerName;
                i += playerNameLength - 1;
            } else if (!previousServerLength && chosenMessage.substring(i, i + previousServerNameLength).equals(previousServerNameStr)) {
                finalString += previousServer;
                i += previousServerNameLength - 1;
            } else if (!newServerLength && chosenMessage.substring(i, i + newServerNameLength).equals(newSeverNameStr)) {
                finalString += newServer;
                i += newServerNameLength - 1;
            } else {
                finalString += chosenMessage.substring(i, i+1);
            }
        }

        System.out.println(finalString);

        return compileColoredMessage(finalString, type);
    }

    public MessageReturns compileFormattedMessage(String type, String playerName, String previousServer, String newServer, String ogString) {
        return compileFormattedMessage(
            type, playerName, previousServer, newServer, ogString, false
        );
    }




    public MessageReturns compileColoredMessage(String coloredString) {
        return compileColoredMessage(coloredString, "");
    }

    // removes and applies hex code coloring to the given string, returns a custom MessageReturns object
    public MessageReturns compileColoredMessage(String coloredString, String type) {

        TextComponent.Builder finalMessage = Component.text();
        TextColor textColor = defaultTextColor;

        boolean isBold = false;
        boolean isItalic = false;
        boolean isStrike = false;
        boolean isUnderline = false;
        boolean isObfuscated = false;

        String finalString = "";

        for (int i = 0; i < coloredString.length(); i++) {
    
            boolean atColorLength = i + colorLength > coloredString.length();

            boolean atBoldLengh = i + boldLength > coloredString.length();
            boolean atItalicLength = i + italicLength > coloredString.length();
            boolean atStrikeLength = i + strikeLength > coloredString.length();
            boolean atUnderlineLength = i + underlineLength > coloredString.length();
            boolean atObfuscateLength = i + obfuscatedLength > coloredString.length();

            if (!atColorLength && coloredString.substring(i, i + 2).equals("{#")) {
                textColor = TextColor.fromCSSHexString(coloredString.substring(i + 1, i + colorLength - 1));
                i += colorLength - 1;

            } else if (!atBoldLengh && coloredString.substring(i, i + boldLength).equals(boldStr)) {
                isBold = !isBold;
                i += boldLength - 1;
            } else if (!atItalicLength && coloredString.substring(i, i + italicLength).equals(italicStr)) {
                isItalic = !isItalic;
                i += italicLength - 1;
            } else if (!atStrikeLength && coloredString.substring(i, i + strikeLength).equals(italicStr)) {
                isStrike = !isStrike;
                i += strikeLength - 1;
            } else if (!atUnderlineLength && coloredString.substring(i, i + underlineLength).equals(underlineStr)) {
                isUnderline = !isUnderline;
                i += underlineLength - 1;
            } else if (!atObfuscateLength && coloredString.substring(i, i + obfuscatedLength).equals(obfuscateStr)) {
                isObfuscated = !isObfuscated;
                i += obfuscatedLength - 1;
            } else {
                finalMessage.append(Component.text(coloredString.substring(i, i + 1))
                    .color(textColor)
                    .decoration(TextDecoration.BOLD, isBold)
                    .decoration(TextDecoration.ITALIC, isItalic)
                    .decoration(TextDecoration.STRIKETHROUGH, isStrike)
                    .decoration(TextDecoration.UNDERLINED, isUnderline)
                    .decoration(TextDecoration.OBFUSCATED, isObfuscated)
                );
                finalString += coloredString.substring(i, i + 1);
            }

        }

        return new MessageReturns(finalMessage.build(), finalString, type); 
    }

    /* 
     * A simple class with two datatypes to return a string and it's component counterpart
     */
    public class MessageReturns {

        private Component component;
        private String string;
        private String type;

        public MessageReturns(Component component, String string, String type) {
            this.component = component;
            this.string = string;
            this.type = type;
        }

        public Component getComponent() {
            return component;
        }

        public String getString() {
            return string;
        }

        public String getType() {
            return type;
        }
    }
}


