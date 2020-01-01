package de.guntram.mcmod.chatorganizer;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.util.Texts;
import net.minecraft.util.math.MathHelper;

public class Tab {
    final public String name;
    final public String regex;
    final public String tabCommand;
    final public String messageCommand;
    final public boolean visible;
    final public int colorCode;

    private Pattern pattern;
    private double xpos, ypos, width, height;
    private List<ChatHudLine> messages;
    private String groupMatch;
    private boolean closable;
    private boolean shouldAddAnyway;

    private static final Logger LOGGER = LogManager.getLogger();

    Tab(String name, String regex, String tabCommand, String messageCommand, boolean visible, int color) {
        this.name = name;
        this.regex = regex;
        this.tabCommand = tabCommand;
        this.messageCommand = messageCommand;
        this.visible = visible;
        this.colorCode = color;

        this.compileRegex();
        this.messages = new ArrayList<>();
        this.closable = false;
        this.shouldAddAnyway = false;
    }

    public void setRenderedPos(double x, double y, double width, double height) {
        this.xpos = x;
        this.ypos = y;
        this.width = width;
        this.height = height;
    }

    public boolean containsPos(double x, double y) {
        LOGGER.debug("check if "+x+"/"+y+" is in rect "+xpos+"/"+ypos+ " width "+width+" height "+height);
        return x > xpos && x <= xpos+width && y > ypos && y < ypos+height;
    }

    public boolean addMessage(Text message, int id, int timestamp, boolean doNotAdd) {
        return addMessage(message, id, timestamp, doNotAdd, false);
    }

    public boolean addMessage(Text message, int id, int timestamp, boolean doNotAdd, boolean forced) {
        if (!doNotAdd) {
            boolean ok;
            String text = "";
            Matcher matcher=null;
            if (pattern == null) {
                ok = true;
            } else if (forced) {
                // If we are forced to accept the string (which means we're called from the regex tab that created us)
                // we should return "added" the next time the TabManager calls us, even when we don't add then.
                shouldAddAnyway = true;
                ok = true;
            } else {
                // I don't understand this, but LiteralText chat messages return empty in asString and text in asFormattedString.
                // this should really be text=message.asString()
                text=message.asFormattedString();
                text=text.replaceAll("§.", "");
                matcher=pattern.matcher(text);
                ok = matcher.find();
            }
            if (ok) {
                if (matcher != null && matcher.groupCount() > 0 && this.name.contains("\\")) {
                    String newTabName = insertMatcherGroups(this.name, matcher);
                    String newMessageCommand = insertMatcherGroups(this.messageCommand, matcher);
                    Tab realTab = TabManager.getTab(newTabName);
                    if (realTab == null) {
                        realTab = new Tab(newTabName, "!!!easter egg!!!", this.tabCommand, newMessageCommand, true, this.colorCode);
                        TabManager.addTabLater(realTab);
                    }
                    LOGGER.info("forwarded "+message.asFormattedString()+" to "+realTab.name);
                    realTab.addMessage(message, id, timestamp, doNotAdd, true);
                } else {
                    LOGGER.info("added "+message.asFormattedString()+" to "+this.name);
                    
                    MinecraftClient client = MinecraftClient.getInstance();
                    ChatHud hud = client.inGameHud.getChatHud();
                    int i = MathHelper.floor((double)hud.getWidth() / hud.getChatScale());
                    List<Text> list = Texts.wrapLines(message, i, client.textRenderer, false, false);
                    for (Text part: list) {
                        messages.add(0, new ChatHudLine(timestamp, part, id));
                    }
                }
                return true;
            } else {
                LOGGER.info("ignored "+text+"/"+message.asString()+"/"+message.asFormattedString()+" in "+this.name);
                LOGGER.debug("class is "+message.getClass().getName());
                if (shouldAddAnyway) {
                    shouldAddAnyway = false;
                    return true;
                }
                return false;
            }
        }
        LOGGER.info("not added "+message.asString()+" in "+this.name);
        return true;
    }
    
    public void reset() {
        this.messages.clear();
    }

    public void replaceMessages(List<ChatHudLine>newMessages) {
        reset();
        this.messages.addAll(newMessages);
    }

    public List<ChatHudLine> getMessages() {
        // Never return empty; we want to show some text to make sure the hud gets displayed
        if (messages.size() == 0) {
            messages.add(new ChatHudLine(0, new LiteralText("Messages for "+name), 0 ));
        }
        return messages;
    }
    
    public void setClosable(boolean b) {
        this.closable = b;
    }
    
    public boolean isClosable() {
        return closable;
    }

    private void compileRegex() {
        if (this.regex == null) {
            this.pattern = null;
        } else try {
            this.pattern = Pattern.compile(this.regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException ex) {
            this.pattern = null;
        }
    }
    
    private String insertMatcherGroups(String name, Matcher matcher) {
        StringBuilder result=new StringBuilder();
        for (int i=0; i<name.length(); i++) {
            if (name.charAt(i) == '\\' && i < name.length()-1 && name.charAt(i+1) >= '0' && name.charAt(i+1) <= '9') {
                int groupIndex = name.charAt(i+1) - '0';
                if (groupIndex <= matcher.groupCount()) {
                    result.append(matcher.group(groupIndex));
                    i++;
                } // no else here; groups that don't exist in the regex are assumed to be empty
            } else {
                result.append(name.charAt(i));
            }
        }
        return result.toString();
    }
}
