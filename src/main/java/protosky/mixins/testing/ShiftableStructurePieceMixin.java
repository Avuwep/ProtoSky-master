package protosky.mixins.testing;

import net.minecraft.structure.ShiftableStructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import protosky.mixins.StructureHelperInvokers.ShiftableStructurePieceInvoker;

@Mixin(ShiftableStructurePiece.class)
public class ShiftableStructurePieceMixin {
    @Inject(method = "adjustToAverageHeight(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockBox;I)Z", at = @At("HEAD"), cancellable = true)
    public void adjustToAverageHeight(WorldAccess world, BlockBox boundingBox, int deltaY, CallbackInfoReturnable<Boolean> cir) {
        ShiftableStructurePiece This = ((ShiftableStructurePiece)(Object)this);
        ShiftableStructurePieceInvoker invoker = (ShiftableStructurePieceInvoker) This;

        if (invoker.gethPos() >= 0) {
            cir.setReturnValue(true);
        } else {
            int i = 0;
            int j = 0;
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int k = This.getBoundingBox().getMinZ(); k <= This.getBoundingBox().getMaxZ(); ++k) {
                for (int l = This.getBoundingBox().getMinX(); l <= This.getBoundingBox().getMaxX(); ++l) {
                    mutable.set(l, 64, k);
                    if (boundingBox.contains(mutable)) {

                        System.out.println("ran1");
                        BlockPos worldTopPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, mutable);
                        System.out.println("ran2");
                        i += worldTopPos.getY();
                        System.out.println("ran3");
                        ++j;
                    }
                }
            }

            if (j == 0) {
                cir.setReturnValue(false);
            } else {
                invoker.sethPos(i / j);
                This.getBoundingBox().move(0, invoker.gethPos() - This.getBoundingBox().getMinY() + deltaY, 0);
                cir.setReturnValue(true);
            }
        }
        cir.setReturnValue(false);

    }

}
