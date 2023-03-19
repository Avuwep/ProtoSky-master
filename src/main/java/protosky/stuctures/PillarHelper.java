package protosky.stuctures;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.feature.EndSpikeFeature;

import java.util.ArrayList;
import java.util.Random;

import static protosky.ProtoSkySettings.LOGGER;

public class PillarHelper {

    //Copied from stack overflow
    //https://stackoverflow.com/questions/401847/circle-rectangle-collision-detection-intersection
    //This checks if a spike intersects with a chunk properly.
    private static boolean spikeChunkIntersects(EndSpikeFeature.Spike circle, ProtoChunk rect) {
        int rectXBlocks = (rect.getPos().x * 16) + 8;
        int rectYBlocks = (rect.getPos().z * 16) + 8;

        int circleDistance_x = Math.abs(circle.getCenterX() - rectXBlocks);
        int circleDistance_y = Math.abs(circle.getCenterZ() - rectYBlocks);

        if (circleDistance_x > (16/2 + circle.getRadius())) { return false; }
        if (circleDistance_y > (16/2 + circle.getRadius())) { return false; }

        if (circleDistance_x <= (16/2)) { return true; }
        if (circleDistance_y <= (16/2)) { return true; }

        int cornerDistance_sq = (circleDistance_x - 16/2)^2 +
                (circleDistance_y - 16/2)^2;

        return (cornerDistance_sq <= (circle.getRadius()^2));
    }

    public static void generate(StructureWorldAccess world, ProtoChunk chunk) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        //-6 Chunks is 96 blocks, all pillars should be within this
        //spike.isInChunk() Doesn't work because it checks the center of the pillar cutting it off.
        if (chunkX >= -6 && chunkX <= 6 && chunkZ >= -6 && chunkZ <= 6) {
            for (EndSpikeFeature.Spike spike : EndSpikeFeature.getSpikes(world)) {
                if(spikeChunkIntersects(spike, chunk)) PillarHelper.generateSpike(world, spike, chunk);
            }
        }
    }

    private static int i = 1;
    private static synchronized void incI() {
        i++;
        LOGGER.info(String.valueOf(i));
    }

    public static void generateSpike(ServerWorldAccess world, EndSpikeFeature.Spike spike, ProtoChunk chunk) {
        int i = spike.getRadius();
        for (BlockPos blockPos : BlockPos.iterate(
                new BlockPos(spike.getCenterX() - i, 0, spike.getCenterZ() - i),
                new BlockPos(spike.getCenterX() + i, spike.getHeight() + 10, spike.getCenterZ() + i)
        )) {
            if (blockPos.getSquaredDistance(spike.getCenterX(), blockPos.getY(), spike.getCenterZ()) <= (double) (i * i + 1) && blockPos.getY() < spike.getHeight()) {
                StructureHelper.setBlockInChunk(chunk, blockPos, Blocks.OBSIDIAN.getDefaultState());
                //.info("trying to generate");
            } else if (blockPos.getY() > 65) {
                StructureHelper.setBlockInChunk(chunk, blockPos, Blocks.AIR.getDefaultState());
            }

        }
        if (spike.isGuarded()) {
            int j = -2;
            int k = 2;
            int l = 3;
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int m = -2; m <= 2; ++m) {
                for (int n = -2; n <= 2; ++n) {
                    for (int o = 0; o <= 3; ++o) {
                        boolean bl = MathHelper.abs(m) == 2;
                        boolean bl2 = MathHelper.abs(n) == 2;
                        boolean bl3 = o == 3;
                        if (bl || bl2 || bl3) {
                            boolean bl4 = m == -2 || m == 2 || bl3;
                            boolean bl5 = n == -2 || n == 2 || bl3;
                            BlockState blockState = Blocks.IRON_BARS
                                    .getDefaultState()
                                    .with(PaneBlock.NORTH, bl4 && n != -2)
                                    .with(PaneBlock.SOUTH, bl4 && n != 2)
                                    .with(PaneBlock.WEST, bl5 && m != -2)
                                    .with(PaneBlock.EAST, bl5 && m != 2);
                            StructureHelper.setBlockInChunk(chunk, mutable.set(spike.getCenterX() + m, spike.getHeight() + o, spike.getCenterZ() + n), blockState);
                        }
                    }
                }
            }
        }

        //Check if where we want to put the bedrock is in the chunk we're in.
        if(Math.abs(((chunk.getPos().x * 16) + 8) - spike.getCenterX()) <= 8 && Math.abs(((chunk.getPos().z * 16) + 8) - spike.getCenterZ()) <= 8) {
            EndCrystalEntity endCrystalEntity = EntityType.END_CRYSTAL.create(world.toServerWorld());

            //endCrystalEntity.setBeamTarget(config.getPos());
            //endCrystalEntity.setInvulnerable(config.isCrystalInvulnerable());
            endCrystalEntity.refreshPositionAndAngles(
                    (double)spike.getCenterX() + 0.5, spike.getHeight() + 1, (double)spike.getCenterZ() + 0.5, new Random().nextFloat() * 360.0F, 0.0F
            );
            world.spawnEntity(endCrystalEntity);
            StructureHelper.setBlockInChunk(chunk, new BlockPos(spike.getCenterX(), spike.getHeight(), spike.getCenterZ()), Blocks.BEDROCK.getDefaultState());
        }
    }
}