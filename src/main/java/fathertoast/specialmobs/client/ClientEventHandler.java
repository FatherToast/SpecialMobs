package fathertoast.specialmobs.client;

import fathertoast.specialmobs.common.config.Config;
import fathertoast.specialmobs.common.core.SpecialMobs;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber( value = Dist.CLIENT, modid = SpecialMobs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE )
public final class ClientEventHandler {
    
    static void registerConfigGUIFactory() {
        ModLoadingContext.get().registerExtensionPoint( ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory( OpenConfigFolderScreen::new ) );
    }
    
    @SuppressWarnings( "ProtectedMemberInFinalClass" )
    @SubscribeEvent
    protected static void onGuiOpen( ScreenEvent.Init event ) {
        if( event.getScreen() instanceof OpenConfigFolderScreen ) {
            event.setCanceled( true );
            Util.getPlatform().openFile( Config.CONFIG_DIR );
        }
    }
    
    /**
     * This screen is effectively a redirect. It is opened when the "mod config" button is pressed with the goal of behaving
     * like the "mods folder" button; i.e. just opens the appropriate folder.
     */
    private static record ConfigGuiFactory(Minecraft minecraft, Screen s, Screen screen) {

    }

    private static class OpenConfigFolderScreen extends Screen {
        private OpenConfigFolderScreen( Minecraft game, Screen parent ) {
            // We don't need to localize the name or do anything since the opening of this screen is always canceled
            super(Component.literal("Opening mod config folder") );
        }
    }
}