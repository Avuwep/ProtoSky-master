package protosky.mixins.structures;

import net.minecraft.structure.NetherFossilGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.structure.NetherFossilGenerator$Piece")
public abstract class NetherFossilGeneratorPieceMixin {

    //Prevent nether fossils from generating
    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void onGenerate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        //System.out.println("Fossils blocked!");
        ci.cancel();
    }
}
