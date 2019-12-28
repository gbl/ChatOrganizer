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
            Matcher matcher=null;
            if (pattern == null) {
                ok = true;
            } else if (forced) {
                ok = true;
            } else {
                matcher=pattern.matcher(message.asString());
                ok = matcher.find();
            }
            if (ok) {
                messages.add(0, new ChatHudLine(timestamp, message, id));
                LOGGER.info("added "+message.asString()+" to "+this.name);
                if (matcher != null && matcher.groupCount() > 0 && this.name.contains("\\1")) {
                    String newTabName = this.name.replace("\\1", matcher.group(1));
                    String newMessageCommand = this.messageCommand.replace("\\1", matcher.group(1));
                    Tab realTab = TabManager.getTab(newTabName);
                    if (realTab == null) {
                        realTab = new Tab(newTabName, "!!!easter egg!!!", this.tabCommand, newMessageCommand, true, this.colorCode);
                        TabManager.addTabLater(realTab);
                    }
                    realTab.addMessage(message, id, timestamp, doNotAdd, true);
                }
                return true;
            } else {
                LOGGER.info("ignored "+message.asString()+" in "+this.name);
                return false;
            }
        }
        LOGGER.info("not added "+message.asString()+" in "+this.name);
        return true;
    }

    public void replaceMessages(List<ChatHudLine>messages) {
        this.messages.clear();
        this.messages.addAll(messages);
    }

    public List<ChatHudLine> getMessages() {
        // Never return empty; we want to show some text to make sure the hud gets displayed
        if (messages.size() == 0) {
            messages.add(new ChatHudLine(0, new LiteralText("Messages for "+name), 0 ));
        }
        return messages;
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

}
