/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.reflect.TypeToken
 *  ninja.leaping.configurate.ConfigurationNode
 *  ninja.leaping.configurate.yaml.YAMLConfigurationLoader
 *  ninja.leaping.configurate.yaml.YAMLConfigurationLoader$Builder
 *  org.spongepowered.api.Sponge
 *  org.spongepowered.api.asset.Asset
 *  org.spongepowered.api.block.BlockState
 *  org.spongepowered.api.item.inventory.ItemStack
 */
package net.craftersland.restarter;

import com.google.common.reflect.TypeToken;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import net.craftersland.restarter.RR;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.item.inventory.ItemStack;

public class DataHandler {
    private RR plugin;
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

    public DataHandler(RR plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }

    public void loadConfig() {
        File configFile;
        File pluginFolder;
        File plugins = new File("plugins");
        if (!plugins.exists()) {
            plugins.mkdir();
        }
        if (!(pluginFolder = new File("plugins" + System.getProperty("file.separator") + this.plugin.getPluginContainer().getName())).exists()) {
            pluginFolder.mkdir();
        }
        if (!(configFile = new File("plugins" + System.getProperty("file.separator") + this.plugin.getPluginContainer().getName() + System.getProperty("file.separator") + "data.yml")).exists()) {
            RR.log.info("No data file found! Creating new one...");
            try {
                ((Asset)Sponge.getAssetManager().getAsset((Object)this.plugin, "data.yml").get()).copyToDirectory(Paths.get("plugins" + System.getProperty("file.separator") + this.plugin.getPluginContainer().getName(), new String[0]));
            }
            catch (Exception e) {
                RR.log.error("Failed to deploy data file! Error: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            RR.log.info("Data file generated.");
        }
        try {
            RR.log.info("Loading the data file...");
            this.config = ((YAMLConfigurationLoader.Builder)YAMLConfigurationLoader.builder().setPath(Paths.get("plugins" + System.getProperty("file.separator") + this.plugin.getPluginContainer().getName() + System.getProperty("file.separator") + "data.yml", new String[0]))).build().load();
            RR.log.info("Data loaded successuffly!");
        }
        catch (Exception e) {
            RR.log.error("Could not load the data file! Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Boolean hasNode(Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (!data.isVirtual()) {
            return true;
        }
        return false;
    }

    public String getString(Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (data.isVirtual()) {
            RR.log.error("Could not locate " + key + " in the data.yml inside of the " + this.plugin.getPluginContainer().getName() + " folder!");
            return "errorCouldNotLocateInConfigYml:" + key;
        }
        return data.getString();
    }

    public Integer getInteger(Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (data.isVirtual()) {
            RR.log.error("Could not locate " + key + " in the data.yml inside of the " + this.plugin.getPluginContainer().getName() + " folder!");
            return null;
        }
        return data.getInt();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String getIc2Type(BlockState item) {
        String string;
        try {
            this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).setValue(TypeToken.of(BlockState.class), (Object)item);
            string = (String)this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).getValue(TypeToken.of(String.class));
        }
        catch (Exception e) {
            try {
                e.printStackTrace();
            }
            catch (Throwable throwable) {
                if (this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).isVirtual()) throw throwable;
                this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).setValue(null);
                throw throwable;
            }
            if (this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).isVirtual()) return " ";
            this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).setValue(null);
            return " ";
        }
        if (this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).isVirtual()) return string;
        this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).setValue(null);
        return string;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Integer getBlockUnsafeDamage(BlockState item) {
        Integer n;
        try {
            this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).setValue(TypeToken.of(BlockState.class), (Object)item);
            n = (Integer)this.config.getNode(new Object[]{"UnsafeDamage", item.toString(), "UnsafeDamage"}).getValue(TypeToken.of(Integer.class));
        }
        catch (Exception e) {
            try {
                e.printStackTrace();
            }
            catch (Throwable throwable) {
                if (this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).isVirtual()) throw throwable;
                this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).setValue(null);
                throw throwable;
            }
            if (this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).isVirtual()) return 0;
            this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).setValue(null);
            return 0;
        }
        if (this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).isVirtual()) return n;
        this.config.getNode(new Object[]{"UnsafeDamage", item.toString()}).setValue(null);
        return n;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public ItemStack setItemUnsafeDamage(ItemStack item, Integer unsafeDamage) {
        ItemStack itemStack;
        try {
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(TypeToken.of(ItemStack.class), (Object)item);
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString(), "UnsafeDamage"}).setValue(TypeToken.of(Integer.class), (Object)unsafeDamage);
            itemStack = (ItemStack)this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).getValue(TypeToken.of(ItemStack.class));
        }
        catch (Exception e) {
            try {
                e.printStackTrace();
            }
            catch (Throwable throwable) {
                if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) throw throwable;
                this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
                throw throwable;
            }
            if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return null;
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
            return null;
        }
        if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return itemStack;
        this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
        return itemStack;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public ItemStack removeNbtTag(ItemStack item) {
        ItemStack itemStack;
        try {
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(TypeToken.of(ItemStack.class), (Object)item);
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString(), "UnsafeData", "Inventory"}).setValue(null);
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString(), "UnsafeData", "BlockEntityTag"}).setValue(null);
            itemStack = (ItemStack)this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).getValue(TypeToken.of(ItemStack.class));
        }
        catch (Exception e) {
            try {
                e.printStackTrace();
            }
            catch (Throwable throwable) {
                if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) throw throwable;
                this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
                throw throwable;
            }
            if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return null;
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
            return null;
        }
        if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return itemStack;
        this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
        return itemStack;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public ItemStack removeInventory(ItemStack item) {
        ItemStack itemStack;
        try {
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(TypeToken.of(ItemStack.class), (Object)item);
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString(), "UnsafeData", "Inventory"}).setValue(null);
            itemStack = (ItemStack)this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).getValue(TypeToken.of(ItemStack.class));
        }
        catch (Exception e) {
            try {
                e.printStackTrace();
            }
            catch (Throwable throwable) {
                if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) throw throwable;
                this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
                throw throwable;
            }
            if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return null;
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
            return null;
        }
        if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return itemStack;
        this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
        return itemStack;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Boolean isCreativeStrongbox(ItemStack item) {
        Boolean bl;
        try {
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(TypeToken.of(ItemStack.class), (Object)item);
            String data = (String)this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).getValue(TypeToken.of(String.class));
            bl = data.contains("Creative=1");
        }
        catch (Exception e) {
            try {
                e.printStackTrace();
            }
            catch (Throwable throwable) {
                if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) throw throwable;
                this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
                throw throwable;
            }
            if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return false;
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
            return false;
        }
        if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return bl;
        this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
        return bl;
    }

    public Boolean hasNbtTag(ItemStack item) {
        block9: {
            block8: {
                Boolean bl;
                try {
                    this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(TypeToken.of(ItemStack.class), (Object)item);
                    String data = (String)this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).getValue(TypeToken.of(String.class));
                    if (data == null) break block8;
                    bl = data.contains("+NBT");
                }
                catch (Exception e) {
                    try {
                        e.printStackTrace();
                    }
                    catch (Throwable throwable) {
                        if (!this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) {
                            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
                        }
                        throw throwable;
                    }
                    if (!this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) {
                        this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
                    }
                    break block9;
                }
                if (!this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) {
                    this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
                }
                return bl;
            }
            if (!this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) {
                this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
            }
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Boolean hasInventory(ItemStack item) {
        Boolean bl;
        try {
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(TypeToken.of(ItemStack.class), (Object)item);
            String data = (String)this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).getValue(TypeToken.of(String.class));
            bl = data.contains("Inventory=");
        }
        catch (Exception e) {
            try {
                e.printStackTrace();
            }
            catch (Throwable throwable) {
                if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) throw throwable;
                this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
                throw throwable;
            }
            if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return false;
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
            return false;
        }
        if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return bl;
        this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
        return bl;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String getDebugData(ItemStack item) {
        String string;
        try {
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(TypeToken.of(ItemStack.class), (Object)item);
            string = (String)this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).getValue(TypeToken.of(String.class));
        }
        catch (Exception e) {
            try {
                e.printStackTrace();
            }
            catch (Throwable throwable) {
                if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) throw throwable;
                this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
                throw throwable;
            }
            if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return null;
            this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
            return null;
        }
        if (this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).isVirtual()) return string;
        this.config.getNode(new Object[]{"UnsafeDamage", item.getItem().toString()}).setValue(null);
        return string;
    }

    public void setData(Object data, Object ... key) {
        this.config.getNode(key).setValue(data);
        try {
            ((YAMLConfigurationLoader.Builder)YAMLConfigurationLoader.builder().setPath(Paths.get("plugins" + System.getProperty("file.separator") + this.plugin.getPluginContainer().getName() + System.getProperty("file.separator") + "data.yml", new String[0]))).build().save(this.config);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getStringList(Object ... key) {
        ConfigurationNode data = this.config.getNode(key);
        if (data.isVirtual()) {
            RR.log.error("Could not locate " + key + " in the config.yml inside of the " + this.plugin.getPluginContainer().getName() + " folder! (Try generating a new one by deleting the current)");
            return null;
        }
        return data.getList(this.stringTransformer);
    }
}

