package de.guntram.mcmod.chatorganizer;

import de.guntram.mcmod.fabrictools.ConfigurationProvider;
import net.fabricmc.api.ClientModInitializer;

public class ChatOrganizer implements ClientModInitializer
{
    static final String MODID="chatorganizer";
    static final String MODNAME="ChatOrganizer";
    static final String VERSION="@VERSION@";

    @Override
    public void onInitializeClient() {
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        ConfigurationProvider.register("ChatOrganizer", confHandler);
        confHandler.load(ConfigurationProvider.getSuggestedFile(MODID));
    }
}

