package de.guntram.mcmod.chatorganizer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TabManager {
    static int currentTabIndex;

    private static List<Tab> tabs;
    private static Tab tabToAddLater;   // This is a kludge to avoid any ConcurrentModificationExceptions.

    static {
        tabs = new ArrayList<>();
        tabs.add(new Tab("*", null, "/ch g", "", true, 0xffffff));
        tabs.add(new Tab("Local", "\\[L\\]", "/ch l", "", true, 0xffff55));
        tabs.add(new Tab("Global", "\\[G\\]", "/ch g", "", true, 0x00aa00));
        tabs.add(new Tab("Discord", "\\[Discord\\]", "/ch g", "", true, 0x00aaaa));
        tabs.add(new Tab("DM \\2", "^(From|To) <(.*?)>", "", "/msg \\2", false, 0x800080));
    }

    public static boolean addMessage(Text message, int id, int timestamp, boolean doNotAdd) {
        boolean currentAccepted = false;
        tabToAddLater = null;
        for (Tab tab: tabs) {
            if (tab.addMessage(message, id, timestamp, doNotAdd) && tabIsFocused(tab)) {
                currentAccepted = true;
            }
        }
        if (tabToAddLater != null) {
            tabs.add(tabToAddLater);
        }
        return currentAccepted;
    }

    public static List<String> getTabNames() {
        List<String> names = new ArrayList<>(tabs.size());
        for (Tab tab:tabs) {
            names.add(tab.name);
        }
        return names;
    }

    public static List<Tab> getTabs() {
        return tabs;
    }

    public static Tab getTab(String name) {
        for (Tab tab:tabs) {
            if (tab.name.equals(name)) {
                return tab;
            }
        }
        return null;
    }

    public static void addTabLater(Tab tab) {
        tabToAddLater = tab;
    }

    public static int getCurrentTabIndex() {
        return currentTabIndex;
    }

    public static Tab getCurrentTab() {
        return tabs.get(currentTabIndex);
    }

    public static boolean tabIsFocused(Tab tab) {
        return tabs.get(currentTabIndex) == tab;
    }

    public static boolean mouseClicked(double x, double y, int button) {
        if (button == 0) {
            for (int i=0; i<tabs.size(); i++) {
                if (tabs.get(i).containsPos(x, y) && i != currentTabIndex) {
                    ChatHudExtension switcher = (ChatHudExtension) MinecraftClient.getInstance().inGameHud.getChatHud();
                    Tab tab = tabs.get(currentTabIndex);
                    tab.replaceMessages(switcher.getMessages());
                    switcher.replaceMessages(tabs.get(i).getMessages());
                    String tabCommand = tabs.get(i).tabCommand;
                    if (tabCommand != null && !tabCommand.isEmpty()) {
                        // TODO this is commented out for testing
                        // if ((!tabCommand.startsWith("/"))) {
                        //    tabCommand = "/"+tabCommand;
                        //}
                        MinecraftClient.getInstance().player.sendChatMessage(tabCommand);
                    }
                    currentTabIndex = i;
                    return true;
                }
            }
        }
        return false;
    }

    public static void clearAllTabs() {
        for (Tab tab:tabs) {
            tab.reset();
        }
    }
}
