package de.guntram.mcmod.chatorganizer;

import de.guntram.mcmod.fabrictools.ConfigChangedEvent;
import de.guntram.mcmod.fabrictools.Configuration;
import de.guntram.mcmod.fabrictools.ModConfigurationHandler;
import java.io.File;

public class ConfigurationHandler implements ModConfigurationHandler {

    private static ConfigurationHandler instance;

    private Configuration config;
    private String configFileName;
    
    private boolean debugChatText, debugChatMessages, resetOnRelog;

    public static ConfigurationHandler getInstance() {
        if (instance==null)
            instance=new ConfigurationHandler();
        return instance;
    }

    public void load(final File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            configFileName=configFile.getPath();
            loadConfig();
        }
    }
    
    @Override
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equalsIgnoreCase(ChatOrganizer.MODID)) {
            loadConfig();
        }
    }
    
    private void loadConfig() {
        debugChatText=config.getBoolean("Debug chat texts", Configuration.CATEGORY_CLIENT, false, "send all chat texts to the log (unformatted) for debugging");
        debugChatMessages=config.getBoolean("Debug chat messages", Configuration.CATEGORY_CLIENT, false, "send all chat messages to the log (formatted) for debugging");
        resetOnRelog=config.getBoolean("Reset when relogging", Configuration.CATEGORY_CLIENT, false, "clear all tab content on relog");
        
        if (config.hasChanged())
            config.save();
    }
    
    @Override
    public Configuration getConfig() {
        return config;
    }

    public static String getConfigFileName() {
        return getInstance().configFileName;
    }

    public static boolean getDebugChatText() {
        return getInstance().debugChatText;
    }

    public static boolean getDebugChatMessages() {
        return getInstance().debugChatMessages;
    }
    
    public static boolean resetOnRelog() {
        return getInstance().resetOnRelog;
    }
}
