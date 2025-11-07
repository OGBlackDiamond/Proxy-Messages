package dev.ogblackdiamond.proxymessages.config;

import java.util.List;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ResourcePackConfig {

    @Setting(value="enabled")
    @Comment(value="whether or not to use a resource pack")
    public boolean resourcePackEnabled = false;

    @Setting(value="url")
    @Comment(value="the url of the resource pack")
    public String resourcePackURL = "pack.zip";

    @Setting(value="sha1-hash")
    @Comment(value="the hash of the zip file\nthe hash currently does nothing, as I don't know how to properly parse this into a byte array\n\tif you know, please open a PR or issue ticket on my github. Thanks.")
    public String resourcePackHash = "hash";
    
    @Setting(value="prompt")
    @Comment(value="the prompt that users will recieve when connecting")
    public String resourcePackPrompt = "{#ff2200}This proxy reccomends the use of a datapack for a better player experience.";

    @Setting(value="required")
    @Comment(value="whether or not the resource pack is required to play on the network")
    public boolean resourcePackRequired = false;

    @Setting(value="except")
    @Comment(value="a list of servers that will not have a resource pack applied to it\nI recomend this for modded servers because resource packs can cause issues there")
    public List<String> resourcePackExcept = List.of("modded");

}


