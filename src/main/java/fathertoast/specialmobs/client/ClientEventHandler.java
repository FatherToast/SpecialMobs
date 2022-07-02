package fathertoast.specialmobs.client;

import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.core.SpecialMobs;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mod.EventBusSubscriber( value = Dist.CLIENT, modid = SpecialMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE )
public class ClientEventHandler {
    
    static void registerConfigGUIFactory() {
        ModLoadingContext.get().registerExtensionPoint( ExtensionPoint.CONFIGGUIFACTORY,
                () -> ClientEventHandler.OpenConfigFolderScreen::new );
    }
    
    @SubscribeEvent
    public static void onGuiOpen( GuiOpenEvent event ) {
        if( event.getGui() instanceof OpenConfigFolderScreen ) {
            event.setCanceled( true );
            Util.getPlatform().openFile( Config.CONFIG_DIR );
        }
    }
    
    /**
     * This screen is effectively a redirect. It is opened when the "mod config" button is pressed with the goal of behaving
     * like the "mods folder" button; i.e. just opens the appropriate folder.
     */
    private static class OpenConfigFolderScreen extends Screen {
        private OpenConfigFolderScreen( Minecraft game, Screen parent ) {
            // We don't need to localize the name or do anything since the opening of this screen is always canceled
            super( new StringTextComponent( "Opening mod config folder" ) );
        }
    }
}