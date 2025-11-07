package dev.ogblackdiamond.proxymessages.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Processor;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class ConfigUtil {

    public GeneralConfig generalConfig;
    public ResourcePackConfig resourcePackConfig;
    public DiscordConfig discordConfig;
    public DiscordTextConfig discordTextConfig;
    public DiscordChatSyncConfig discordChatSyncConfig;

    public List<String> colorMap;

    private Path database;

    public ConfigUtil(Path dataDirectory) throws IOException {

        if (Files.notExists(dataDirectory)) {
            Files.createDirectory(dataDirectory);
        }

        final Path config = dataDirectory.resolve("config.yml");
        if (Files.notExists(config)) {
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(stream, config);
            }
        }
        
        boolean newFile = false;

        database = dataDirectory.resolve("database.txt");
        if (Files.notExists(database)) {
            newFile = true;
            try (InputStream dbstream = this.getClass().getClassLoader().getResourceAsStream("database.txt")) {
                Files.copy(dbstream, database);
            }
        }

        colorMap = Files.readAllLines(database);

        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(config)
            .build();

        final CommentedConfigurationNode rootNode = loader.load(
            ConfigurationOptions.defaults()
                .serializers(
                    TypeSerializerCollection.builder()
                    .registerAll(ConfigurationOptions.defaults().serializers())
                    .registerAnnotatedObjects(
                        ObjectMapper.factoryBuilder()
                        .addProcessor(
                            Comment.class,
                            Processor.comments()
                        )
                        .build()
                    )
                    .build()
                )
        );


        generalConfig = rootNode
            .get(GeneralConfig.class);
        resourcePackConfig = rootNode.node("network-resource-pack")
            .get(ResourcePackConfig.class);
        CommentedConfigurationNode discordNode = rootNode.node("discord");
        discordConfig = discordNode
            .get(DiscordConfig.class);
        CommentedConfigurationNode discordTextConfigNode = discordNode.node("text-configuration");
        discordTextConfig = discordTextConfigNode
            .get(DiscordTextConfig.class);
        CommentedConfigurationNode discordChatSyncNode = discordTextConfigNode.node("player-chat-sync");
        discordChatSyncConfig = discordChatSyncNode 
            .get(DiscordChatSyncConfig.class);

        loader.save(rootNode);

        if (newFile) 
        colorMap.add(0, "default " + generalConfig.defaultPlayerColor);

    }

    public void saveData() throws IOException {

        OutputStream out = Files.newOutputStream(database);

        for (String string : colorMap) {
            out.write((string + "\n").getBytes());
        }

        out.close();

    }


    public List<String> getColorMap() {
        return colorMap;
    }

    public String getColor(String playerName) {
        for (String color : colorMap) {
            if (color.substring(0, color.indexOf(" ")).equals(playerName)){
                return color.substring(color.indexOf(" ") + 1);
            } 
        }
        return getColor("default");
    }

    public void appendColorMap(String colorPair) {
        for (int i = 0; i < colorMap.size(); i++) {
            if (colorMap.get(i).substring(0, colorMap.get(i).indexOf(" "))
                .equals(colorPair.substring(0, colorPair.indexOf(" ")))) {
                colorMap.remove(i);
            }
        }
        colorMap.add(colorPair);
    }

}
