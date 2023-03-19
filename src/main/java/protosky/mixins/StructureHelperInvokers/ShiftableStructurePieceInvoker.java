package protosky.mixins.StructureHelperInvokers;

import net.minecraft.structure.ShiftableStructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShiftableStructurePiece.class)
public interface ShiftableStructurePieceInvoker {
    @Accessor("hPos")
    int gethPos();

    @Accessor("hPos")
    void sethPos(int hPos);

    @Invoker("adjustToAverageHeight")
    boolean invokeAdjustToAverageHeight(WorldAccess world, BlockBox boundingBox, int deltaY);
    @Invoker("adjustToMinHeight")
    boolean invokeAdjustToMinHeight(WorldAccess world, int i);
}
