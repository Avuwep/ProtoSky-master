package protosky.mixins.structures;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.VillagePlacedFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagePlacedFeatures.class)
public abstract class VillagePlacedFeaturesMixin {
    @Accessor("OAK")
    abstract void setOAK(RegistryKey<PlacedFeature> placedFeatureRegistryKey);
    @Accessor("ACACIA")
    abstract void setACACIA(RegistryKey<PlacedFeature> placedFeatureRegistryKey);
    @Accessor("SPRUCE")
    abstract void setSPRUCE(RegistryKey<PlacedFeature> placedFeatureRegistryKey);
    @Accessor("PINE")
    abstract void setPINE(RegistryKey<PlacedFeature> placedFeatureRegistryKey);

    //Make village trees not exist so they don't generate
    public void removeTrees() {
        setOAK(PlacedFeatures.of("empty"));
        setACACIA(PlacedFeatures.of("empty"));
        setSPRUCE(PlacedFeatures.of("empty"));
        setPINE(PlacedFeatures.of("empty"));
    }
    @Inject(method = "bootstrap", at = @At("HEAD"))
    private void onBootstrap(Registerable<PlacedFeature> featureRegisterable, CallbackInfo ci) {
        removeTrees();
        System.out.println("Trees blocked!");
    }
}
