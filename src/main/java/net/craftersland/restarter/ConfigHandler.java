/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ninja.leaping.configurate.ConfigurationNode
 *  ninja.leaping.configurate.yaml.YAMLConfigurationLoader
 *  ninja.leaping.configurate.yaml.YAMLConfigurationLoader$Builder
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.asset.Asset
 *  org.spongepowered.api.text.Text
 *  org.spongepowered.api.text.serializer.TextSerializers
 */
package net.craftersland.restarter;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import net.craftersland.restarter.RR;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class ConfigHandler {
    private RR rr;
    private ConfigurationNode config;
    Function<Object, String> stringTransformer = new Function<Object, String>(){

        @Override
        public String apply(Object input) {
            if (input instanceof String) {
                return (String)input;
            }
            return null;
        }
    };

    public ConfigHandler(RR rr) {
        this.rr = rr;
        this.loadConfig();
    }

    public void loadConfig() {
        File configFile;
        File pluginFolder;
        File plugins = new File("plugins");
        if (!plugins.exists()) {
            plugins.mkdir();
        }
        if (!(pluginFolder = new File("plugins" + System.getProperty("file.separator") + this.rr.getPluginContainer().getName())).exists()) {
            pluginFolder.mkdir();
        }
        if (!(configFile = new File("plugins" + System.getProperty("file.separator") + this.rr.getPluginContainer().getName() + System.getProperty("file.separator") + "config.yml")).exists()) {
            RR.log.info("No config file found! Creating new one...");
            try {
                ((Asset)Sponge.getAssetManager().getAsset((Object)this.rr, "config.yml").get()).copyToDirectory(Paths.get("plugins" + System.getProperty("file.separator") + this.rr.getPluginContainer().getName(), new String[0]));
            }
            catch (Exception e) {
                RR.log.error("Failed to deploy config file! Error: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            RR.log.info("Config file generated.");
        }
        try {
            RR.log.info("Loading the config file...");
            this.config = ((YAMLConfigurationLoader.Builder)YAMLConfigurationLoader.builder().setPath(Paths.get("plugins" + System.getProperty("file.separator") + this.rr.getPluginContainer().getName() + System.getProperty("file.separator") + "config.yml", new String[0]))).build().load();
            RR.log.info("Config loaded successuffly!");
        }
        catch (Exception e) {
            RR.log.error("Could not load the config file! You need to regenerate the config! Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Text getTextWithColor(Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (data.isVirtual()) {
            RR.log.error("Could not locate " + key.toString() + " in the config.yml inside of the " + this.rr.getPluginContainer().getName() + " folder! (Try generating a new one by deleting the current)");
            return Text.of();
        }
        return TextSerializers.FORMATTING_CODE.deserialize(data.getString());
    }

    public Text getTextWithColorAndPlaceholders(String max, String placed, Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (data.isVirtual()) {
            RR.log.error("Could not locate " + key.toString() + " in the config.yml inside of the " + this.rr.getPluginContainer().getName() + " folder! (Try generating a new one by deleting the current)");
            return Text.of();
        }
        String text = data.getString();
        if (max != null) {
            text = text.replace("<max>", max);
        }
        if (placed != null) {
            text = text.replace("<placed>", placed);
        }
        return TextSerializers.FORMATTING_CODE.deserialize(text);
    }

    public List<String> getStringList(Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (data.isVirtual()) {
            RR.log.error("Could not locate " + key.toString() + " in the config.yml inside of the " + this.rr.getPluginContainer().getName() + " folder! (Try generating a new one by deleting the current)");
            return null;
        }
        return data.getList(this.stringTransformer);
    }

    public String getString(Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (data.isVirtual()) {
            RR.log.error("Could not locate " + key.toString() + " in the config.yml inside of the " + this.rr.getPluginContainer().getName() + " folder! (Try generating a new one by deleting the current)");
            return "errorCouldNotLocateInConfigYml:" + key.toString();
        }
        return data.getString();
    }

    public Integer getInteger(Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (data.isVirtual()) {
            RR.log.error("Could not locate " + key.toString() + " in the config.yml inside of the " + this.rr.getPluginContainer().getName() + " folder! (Try generating a new one by deleting the current)");
            return null;
        }
        return data.getInt();
    }

    public Double getDouble(Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (data.isVirtual()) {
            RR.log.error("Could not locate " + key.toString() + " in the config.yml inside of the " + this.rr.getPluginContainer().getName() + " folder! (Try generating a new one by deleting the current)");
            return null;
        }
        return data.getDouble();
    }

    public Boolean getBoolean(Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (data.isVirtual()) {
            RR.log.error("Could not locate " + key.toString() + " in the config.yml inside of the " + this.rr.getPluginContainer().getName() + " folder! (Try generating a new one by deleting the current)");
            return null;
        }
        return data.getBoolean();
    }
}

