package protosky;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import protosky.stuctures.StructureHelper;

public class fixWorldLoads implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonLifecycleEvents.TAGS_LOADED.register((arg1, arg2) -> {
            StructureHelper.ran = false;
        });
    }
}
