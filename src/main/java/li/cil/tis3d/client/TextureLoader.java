package li.cil.tis3d.client;

import li.cil.tis3d.api.API;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class TextureLoader {
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_ERROR = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleExecutionError");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleExecutionRunning");
    public static final ResourceLocation LOCATION_MODULE_EXECUTION_OVERLAY_WAITING = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleExecutionWaiting");
    public static final ResourceLocation LOCATION_MODULE_RANDOM_OVERLAY = new ResourceLocation(API.MOD_ID, "blocks/overlay/moduleRandom");

    public static final TextureLoader INSTANCE = new TextureLoader();

    @SubscribeEvent
    public void onTextureStitchPre(final TextureStitchEvent.Pre event) {
        event.map.registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING);
        event.map.registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_WAITING);
        event.map.registerSprite(LOCATION_MODULE_EXECUTION_OVERLAY_ERROR);
        event.map.registerSprite(LOCATION_MODULE_RANDOM_OVERLAY);
    }

    // --------------------------------------------------------------------- //

    private TextureLoader() {
    }
}
