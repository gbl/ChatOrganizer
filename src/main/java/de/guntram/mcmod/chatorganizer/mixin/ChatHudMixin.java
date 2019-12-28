package de.guntram.mcmod.chatorganizer.mixin;

import de.guntram.mcmod.chatorganizer.ChatHudExtension;
import de.guntram.mcmod.chatorganizer.Tab;
import de.guntram.mcmod.chatorganizer.TabManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public class ChatHudMixin extends DrawableHelper implements ChatHudExtension {

    private static final Logger LOGGER = LogManager.getLogger();

    @Shadow public double getChatScale() {return 0;};
    @Shadow public int getWidth() { return 0; }
    @Shadow public boolean isChatFocused() {return true;};
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private List<ChatHudLine> visibleMessages, messages;

    @Inject(method="render", at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/RenderSystem;popMatrix()V"))
    public void renderTabs(CallbackInfo cb) {
        if (!(this.isChatFocused())) {
            return;
        }
        Matrix4f matrix4f = Matrix4f.method_24021(0.0F, 0.0F, -100.0F);
        int ypos = this.visibleMessages.size()*9;
        int xpos = 0;
        for (Tab tab: TabManager.getTabs()) {
            if (!(tab.visible)) {
                continue;
            }
            String s = tab.name;
            int width = this.client.textRenderer.getStringWidth(s);
            fill(matrix4f, xpos, -ypos-13, xpos+width+20, -ypos-2, 0xffc0c0c0);
            fill(matrix4f, xpos-2, -ypos-13, xpos+2, -ypos-4, 0xff000000 );
            this.client.textRenderer.draw(s, xpos+10, -ypos-11, TabManager.tabIsFocused(tab) ? 0xffff0000 : 0xff000000 | tab.colorCode);
            // RenderSystem.translate has Y set here so we need to adjust...
            tab.setRenderedPos(xpos+3, -ypos-13+this.client.getWindow().getScaledHeight()-38, width+20-6, 11);
            xpos += width + 20;
        }
    }

    @Inject(method="addMessage(Lnet/minecraft/text/Text;IIZ)V", at=@At("HEAD"), cancellable = true)
    public void exportMessage(Text message, int id, int timestamp, boolean doNotAdd, CallbackInfo ci) {
        if (!TabManager.addMessage(message, id, timestamp, doNotAdd)) {
            ci.cancel();        // this is if the current tab shouldn't receive this message
        }
    }

    public void replaceMessages(List<ChatHudLine> newMessages) {
        visibleMessages.clear();
        visibleMessages.addAll(newMessages);
    }

    public List<ChatHudLine> getMessages() {
        return visibleMessages;
    }
}
