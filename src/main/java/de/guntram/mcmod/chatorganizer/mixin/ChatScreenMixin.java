package de.guntram.mcmod.chatorganizer.mixin;

import de.guntram.mcmod.chatorganizer.Tab;
import de.guntram.mcmod.chatorganizer.TabManager;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Shadow protected TextFieldWidget chatField;

    @Inject(method="mouseClicked", at=@At("HEAD"), cancellable = true)
    private void mouseClicked(double x, double y, int button, CallbackInfoReturnable cir) {
        if (TabManager.mouseClicked(x, y, button)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method="keyPressed", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/screen/ChatScreen;sendMessage(Ljava/lang/String;)V"), cancellable = true)
    private void beforeSendMessage(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable clr) {
        Tab tab=TabManager.getCurrentTab();
        if (tab.messageCommand == null || tab.messageCommand.isEmpty()) {
            return;
        }
        String string = this.chatField.getText().trim();
        if (string.startsWith("/")) {
            return;
        }
        LOGGER.info("prepended "+tab.messageCommand+" to "+string);
        this.sendMessage(tab.messageCommand+" "+string);
        this.minecraft.openScreen(null);
        clr.setReturnValue(true);
        clr.cancel();
    }
}
