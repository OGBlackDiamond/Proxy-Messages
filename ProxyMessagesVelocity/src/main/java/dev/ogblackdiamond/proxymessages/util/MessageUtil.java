package dev.ogblackdiamond.proxymessages.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MessageUtil {

    // compiles the message, interpolating correct strings when needed.
    public MessageReturns compileFormattedMessage(String type, String playerName, String previousServer, String newServer, String chosenMessage) {

        TextComponent.Builder finalMessage = Component.text();

        String finalString = "";

        int messageLength = chosenMessage.length();
        int playerNameLength = "{player}".length(); 
        int previousServerNameLength = "{prev}".length();
        int newServerNameLength = "{cur}".length();

        // builds the final component, interpolating the correct strings when need  
        for (int i = 0; i < messageLength; i++) {

            boolean atLength = i + playerNameLength > messageLength;
            boolean newServerLength = i + newServerNameLength > messageLength;
            boolean previousServerLength = i + previousServerNameLength > messageLength;

            if (!atLength && chosenMessage.substring(i, i + playerNameLength).equals("{player}")) {
                finalMessage.append(Component.text(playerName).decoration(TextDecoration.BOLD, true));
                finalString += playerName;
                i += playerNameLength - 1;
            } else if (!previousServerLength && chosenMessage.substring(i, i + previousServerNameLength).equals("{prev}")) {
                finalMessage.append(Component.text(previousServer).decoration(TextDecoration.BOLD, true));
                finalString += previousServer;
                i += previousServerNameLength - 1;
            } else if (!newServerLength && chosenMessage.substring(i, i + newServerNameLength).equals("{cur}")) {
                finalMessage.append(Component.text(newServer).decoration(TextDecoration.BOLD, true));
                finalString += newServer;
                i += newServerNameLength - 1;
            } else {
               finalMessage.append(Component.text(chosenMessage.substring(i, i+1)).decoration(TextDecoration.BOLD, false));
               finalString += chosenMessage.substring(i, i+1);
            }
        }

        return compileColoredMessage(finalString, type);
    }

    public MessageReturns compileColoredMessage(String coloredString) {
        return compileColoredMessage(coloredString, "");
    }

    // removes and applies hex code coloring to the given string, returns a component object
    public MessageReturns compileColoredMessage(String coloredString, String type) {

        TextComponent.Builder finalMessage = Component.text();
        TextColor textColor = NamedTextColor.YELLOW;

        String finalString = "";

        int colorLength = "{#xxxxxx}".length();

        for (int i = 0; i < coloredString.length(); i++) {
    
            boolean atColorLength = i + colorLength > coloredString.length();

            if (!atColorLength && coloredString.substring(i, i + 2).equals("{#")) {
                textColor = TextColor.fromCSSHexString(coloredString.substring(i + 1, i + colorLength - 1));
                i += colorLength - 1;
            } else {
                finalMessage.append(Component.text(coloredString.substring(i, i + 1)).color(textColor).decoration(TextDecoration.BOLD, false));
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


