package dev.ogblackdiamond.proxymessages.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MessageUtil {

    public Component compileFormattedMessage(String type, String playerName, String previousServer, String newServer, String chosenMessage) {

        TextComponent.Builder finalMessage = Component.text().color(NamedTextColor.YELLOW);

        int messageLength = chosenMessage.length();
        int playerNameLength = "{player}".length(); 
        int previousServerNameLength = "{prev}".length();
        int newServerNameLength = "{cur}".length();

        

        // builds the final component, interpolating the correct strings when need 
        switch (type) {
            
            case "join": 
            case "leave": {
                
                for (int i = 0; i < messageLength; i++) {

                    boolean atLength = i + playerNameLength > messageLength;

                    if (!atLength && chosenMessage.substring(i, i + playerNameLength).equals("{player}")) {
                        finalMessage.append(Component.text(playerName).decoration(TextDecoration.BOLD, true));
                         i += playerNameLength - 1;
                    } else {
                        finalMessage.append(Component.text(chosenMessage.substring(i, i+1)).decoration(TextDecoration.BOLD, false));
                    }
                }
                break;
            }

            case "switch": {

                for (int i = 0; i < messageLength; i++) {

                    boolean playerAtLength = i + playerNameLength > messageLength;
                    boolean previousServerLength = i + previousServerNameLength > messageLength;
                    boolean newServerLength = i + newServerNameLength > messageLength;
                    
                    if (!playerAtLength && chosenMessage.substring(i, i + playerNameLength).equals("{player}")) {
                        finalMessage.append(Component.text(playerName).decoration(TextDecoration.BOLD, true));
                        i += playerNameLength - 1;
                    } else if (!previousServerLength && chosenMessage.substring(i, i + previousServerNameLength).equals("{prev}")) {
                        finalMessage.append(Component.text(previousServer).decoration(TextDecoration.BOLD, true));
                        i += previousServerNameLength - 1;
                    } else if (!newServerLength && chosenMessage.substring(i, i + newServerNameLength).equals("{cur}")) {
                        finalMessage.append(Component.text(newServer).decoration(TextDecoration.BOLD, true));
                        i += newServerNameLength - 1;
                    } else { 
                        finalMessage.append(Component.text(chosenMessage.substring(i, i+1)).decoration(TextDecoration.BOLD, false));
                    }
                }
                break;
            }

            default: {
                return Component.text("[ProxyMessages] Invalid type passed! Cannot render message.");
            }

        }

        return finalMessage.build(); 
    }
}
