package dev.ogblackdiamond.proxymessages.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import dev.ogblackdiamond.proxymessages.config.ConfigUtil;
import dev.ogblackdiamond.proxymessages.util.HexUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;


public final class SetColor implements SimpleCommand {

    private ConfigUtil configUtil;

    public SetColor(ConfigUtil configUtil) {
        this.configUtil= configUtil;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();

        if (source instanceof Player player) {

            String[] args = invocation.arguments();

            String playerName = "";

            if (args.length == 0){
                source.sendMessage(
                    Component.text("You must provide the color you want to set as an argument!", NamedTextColor.RED)
                );

                return;
            }                    
                
            if (!HexUtil.isValidHex(args[0])) {
                source.sendMessage(
                    Component.text("You must provide a valid hex code!", NamedTextColor.RED)
                );
                return;
            }
            
            if (args.length > 1) {
                playerName = args[1];
            } else {
                playerName = player.getUsername();
            }

            configUtil.appendColorMap(playerName + " " + args[0]);

            source.sendMessage(
                Component.text(playerName + " name color set to ", NamedTextColor.WHITE).append(
                    Component.text(args[0], TextColor.fromHexString(args[0]))
                )
            );
            
        }
    }


    /*
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("operator");
    }
    */
}
