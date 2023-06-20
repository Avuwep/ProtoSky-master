package protosky.mixins.StructureHelperInvokers;

import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureStart.class)
public interface StructureStartInvoker {
    /*@Shadow @Nullable private volatile BlockBox boundingBox;

    @Shadow public abstract BlockBox getBoundingBox();

    @Shadow @Final private Structure structure;

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    void place(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, CallbackInfo ci) {
        StructureStart This = ((StructureStart)(Object)this);

        List<StructurePiece> partsList = This.getChildren();
        if (!partsList.isEmpty()) {
            BlockBox boundingBox = partsList.get(0).getBoundingBox();
            BlockPos center = boundingBox.getCenter();
            BlockPos centerBottom = new BlockPos(center.getX(), boundingBox.getMinY(), center.getZ());

            if (findMovedStructure.searching && !findMovedStructure.inWait && This.getStructure() == findMovedStructure.structureToFind) {
                ArrayList<Pair<BlockBox, BlockBox>> partBoxes = new ArrayList<>();

                for(StructurePiece part : partsList) {
                    if (part.getBoundingBox().intersects(chunkBox)) {
                        BlockBox partBoundingBoxOld = cloneBlockBox(part.getBoundingBox());

                        part.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, centerBottom);

                        partBoxes.add(new Pair<>(partBoundingBoxOld, part.getBoundingBox()));
                    }
                }

                if(partBoxes.size() > 0) findMovedStructure.tickToBeFoundStructure(partBoxes);

            } else {
                for(StructurePiece part : partsList) {
                    if (part.getBoundingBox().intersects(chunkBox)) {
                        part.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, centerBottom);
                    }
                }
            }
            This.getStructure().postPlace(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, this.getChildren());
        }
        ci.cancel();
    }

    private static BlockBox cloneBlockBox(BlockBox blockBox) {
        return new BlockBox(blockBox.getMinX(), blockBox.getMinY(), blockBox.getMinZ(), blockBox.getMaxX(), blockBox.getMaxY(), blockBox.getMaxZ());
    }*/

    @Accessor("children")
    StructurePiecesList getRealChildren();
}
