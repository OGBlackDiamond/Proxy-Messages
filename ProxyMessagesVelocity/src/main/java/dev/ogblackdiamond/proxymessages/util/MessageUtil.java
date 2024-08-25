package dev.ogblackdiamond.proxymessages.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MessageUtil {

    public MessageReturns compileFormattedMessage(String type, String playerName, String previousServer, String newServer, String chosenMessage) {

        TextComponent.Builder finalMessage = Component.text();
        TextColor textColor = NamedTextColor.YELLOW;

        String finalString = "";

        int messageLength = chosenMessage.length();
        int playerNameLength = "{player}".length(); 
        int previousServerNameLength = "{prev}".length();
        int newServerNameLength = "{cur}".length();
        int colorLength = "{#xxxxxx}".length();


        // builds the final component, interpolating the correct strings when need 
        switch (type) {
            
            case "join":
            case "leave": {
                
                for (int i = 0; i < messageLength; i++) {

                    boolean atLength = i + playerNameLength > messageLength;
                    boolean atColorLength = i + colorLength > messageLength;
                    boolean newServerLength = i + newServerNameLength > messageLength;
                    boolean previousServerLength = i + previousServerNameLength > messageLength;

                    if (!atColorLength && chosenMessage.substring(i, i + 2).equals("{#")) {
                        textColor = TextColor.fromCSSHexString(chosenMessage.substring(i + 1, i + colorLength - 1));
                        i += colorLength - 1;
                    } else if (!atLength && chosenMessage.substring(i, i + playerNameLength).equals("{player}")) {
                        finalMessage.append(Component.text(playerName).color(textColor).decoration(TextDecoration.BOLD, true));
                        finalString += playerName;
                        i += playerNameLength - 1;
                    } else if (!previousServerLength && chosenMessage.substring(i, i + previousServerNameLength).equals("{prev}")) {
                        finalMessage.append(Component.text(previousServer).color(textColor).decoration(TextDecoration.BOLD, true));
                        finalString += previousServer;
                        i += previousServerNameLength - 1;
                    } else if (!newServerLength && chosenMessage.substring(i, i + newServerNameLength).equals("{cur}")) {
                        finalMessage.append(Component.text(newServer).color(textColor).decoration(TextDecoration.BOLD, true));
                        finalString += newServer;
                        i += newServerNameLength - 1;
                    } else {
                        finalMessage.append(Component.text(chosenMessage.substring(i, i+1)).color(textColor).decoration(TextDecoration.BOLD, false));
                        finalString += chosenMessage.substring(i, i+1);
                    }
                }
                break;
            }

            case "switch": {

                for (int i = 0; i < messageLength; i++) {

                    boolean playerAtLength = i + playerNameLength > messageLength;
                    boolean previousServerLength = i + previousServerNameLength > messageLength;
                    boolean newServerLength = i + newServerNameLength > messageLength;
                    boolean atColorLength = i + colorLength > messageLength;
                    
                    if (!atColorLength && chosenMessage.substring(i, i + 2).equals("{#")) {
                        textColor = TextColor.fromCSSHexString(chosenMessage.substring(i + 1, i + colorLength - 1));
                        i += colorLength - 1;
                    } else if (!playerAtLength && chosenMessage.substring(i, i + playerNameLength).equals("{player}")) {
                        finalMessage.append(Component.text(playerName).color(textColor).decoration(TextDecoration.BOLD, true));
                        finalString += playerName;
                        i += playerNameLength - 1;
                    } else if (!previousServerLength && chosenMessage.substring(i, i + previousServerNameLength).equals("{prev}")) {
                        finalMessage.append(Component.text(previousServer).color(textColor).decoration(TextDecoration.BOLD, true));
                        finalString += previousServer;
                        i += previousServerNameLength - 1;
                    } else if (!newServerLength && chosenMessage.substring(i, i + newServerNameLength).equals("{cur}")) {
                        finalMessage.append(Component.text(newServer).color(textColor).decoration(TextDecoration.BOLD, true));
                        finalString += newServer;
                        i += newServerNameLength - 1;
                    } else { 
                        finalMessage.append(Component.text(chosenMessage.substring(i, i+1)).color(textColor).decoration(TextDecoration.BOLD, false));
                        finalString += chosenMessage.substring(i, i+1);
                    }
                }
                break;
            }

            default: {
                return new MessageReturns(
                    Component.text("[ProxyMessages] Invalid type passed! Cannot render message.").color(NamedTextColor.RED),
                    "[ProxyMessages] Invalid type passed! Cannot render message.",
                        "other"
                );
            }

        }

        return new MessageReturns(finalMessage.build(), finalString, type);
    }

    public class MessageReturns {

        Component component;
        String string;
        String type;

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
    }
}


