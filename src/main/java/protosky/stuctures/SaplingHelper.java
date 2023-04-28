package protosky.stuctures;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.root.RootPlacer;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;

public class SaplingHelper extends TreeFeature {

    public SaplingHelper(Codec<TreeFeatureConfig> codec) {
        super(codec);
    }

    private static boolean isVine(TestableWorld world, BlockPos pos) {
        return world.testBlockState(pos, (state) -> {
            return state.isOf(Blocks.VINE);
        });
    }

    private boolean generate(StructureWorldAccess world, Random random, BlockPos pos, BiConsumer<BlockPos, BlockState> rootPlacerReplacer, BiConsumer<BlockPos, BlockState> trunkPlacerReplacer, BiConsumer<BlockPos, BlockState> foliagePlacerReplacer, TreeFeatureConfig config) {
        int i = config.trunkPlacer.getHeight(random);
        int j = config.foliagePlacer.getRandomHeight(random, i, config);
        int k = i - j;
        int l = config.foliagePlacer.getRandomRadius(random, k);
        BlockPos blockPos = (BlockPos)config.rootPlacer.map((rootPlacer) -> {
            return rootPlacer.trunkOffset(pos, random);
        }).orElse(pos);
        int m = Math.min(pos.getY(), blockPos.getY());
        int n = Math.max(pos.getY(), blockPos.getY()) + i + 1;
        if (m >= world.getBottomY() + 1 && n <= world.getTopY()) {
            OptionalInt optionalInt = config.minimumSize.getMinClippedHeight();
            int o = this.getTopPosition(world, i, blockPos, config);
            if (o < i && (optionalInt.isEmpty() || o < optionalInt.getAsInt())) {
                return false;
            } else if (config.rootPlacer.isPresent() && !((RootPlacer)config.rootPlacer.get()).generate(world, rootPlacerReplacer, random, pos, blockPos, config)) {
                return false;
            } else {
                List<FoliagePlacer.TreeNode> list = config.trunkPlacer.generate(world, trunkPlacerReplacer, random, o, blockPos, config);
                list.forEach((node) -> {
                    config.foliagePlacer.generate(world, foliagePlacerReplacer, random, config, o, node, j, l);
                });
                return true;
            }
        } else {
            return false;
        }
    }

    private int getTopPosition(TestableWorld world, int height, BlockPos pos, TreeFeatureConfig config) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int i = 0; i <= height + 1; ++i) {
            int j = config.minimumSize.getRadius(height, i);

            for(int k = -j; k <= j; ++k) {
                for(int l = -j; l <= j; ++l) {
                    mutable.set(pos, k, i, l);
                    if (!config.trunkPlacer.canReplaceOrIsLog(world, mutable) || !config.ignoreVines && isVine(world, mutable)) {
                        return i - 2;
                    }
                }
            }
        }

        return height;
    }
}
