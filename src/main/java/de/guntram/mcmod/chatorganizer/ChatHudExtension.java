package de.guntram.mcmod.chatorganizer;

import net.minecraft.client.gui.hud.ChatHudLine;

import java.util.List;

public interface ChatHudExtension {
    public void replaceMessages(List<ChatHudLine> newMessages);
    public List<ChatHudLine> getMessages();

}
