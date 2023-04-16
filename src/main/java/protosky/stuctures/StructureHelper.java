package protosky.stuctures;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.*;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.BlockRotStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import protosky.mixins.StructureHelperInvokers.*;
import protosky.mixins.StructurePieceAccessor;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StructureHelper {
    @FunctionalInterface
    public interface ManyArgumentFunction<ReturnType, Arg1Type, Arg2Type, Arg3Type, Arg4Type, Arg5Type, Arg6Type, Arg7Type, Arg8Type, Arg9Type> {
        ReturnType apply(Arg1Type arg1, Arg2Type arg2, Arg3Type arg3, Arg4Type arg4, Arg5Type arg5, Arg6Type arg6, Arg7Type arg7, Arg8Type arg8, Arg9Type arg9);
    }

    public static boolean ran = false;
    private static final List<Pair<Structure, ManyArgumentFunction<Boolean, StructurePiece, StructureWorldAccess, StructureAccessor, ChunkGenerator, Random, BlockBox, ChunkPos, BlockPos, Chunk>>> structures = new ArrayList<>();

    //This different from method_14829 because it uses the heightmap instead of using the physical blocks that get deleted.
    private static int oceanRuinYCalculator(BlockPos start, WorldAccess world, BlockPos end) {
        int yToMoveTo = start.getY();
        int initialY = start.getY() - 1;

        int lowestSpot = 512;
        int bottomOfWorld = 0;

        for(BlockPos blockPos : BlockPos.iterate(start, end)) {
            /*int x = blockPos.getX();
            int z = blockPos.getZ();
            int y = start.getY() - 1;

            BlockPos.Mutable blockToCheck = new BlockPos.Mutable(x, y, z);
            BlockState blockState = world.getBlockState(blockToCheck);*/

            //Keep going down until we run into something that's not water, air, or ice.
            /*for(FluidState fluidState = world.getFluidState(blockToCheck);
                (blockState.isAir() || fluidState.isIn(FluidTags.WATER) || blockState.isIn(BlockTags.ICE)) && y > world.getBottomY() + 1;
                fluidState = world.getFluidState(blockToCheck)
            ) {
                blockToCheck.set(x, --y, z);
                blockState = world.getBlockState(blockToCheck);
            }*/

            int y = world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, blockPos.getX(), blockPos.getZ());
            lowestSpot = Math.min(lowestSpot, y);

            if (y < initialY - 2) {
                bottomOfWorld++;
            }
        }

        int p = Math.abs(start.getX() - end.getX());
        if (initialY - lowestSpot > 2 && bottomOfWorld > p - 2) {
            yToMoveTo = lowestSpot + 1;
        }

        return yToMoveTo;
    }

    private static synchronized void fixRaceCondition(WorldAccess world) {
        if (!ran) {
            //Registry<StructureType<?>> structureRegistry = Registries.STRUCTURE_TYPE;
            Registry<Structure> structureRegistry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);

            structures.clear();
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("end_city")),
                    (structurePiece, worldAccess, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, blockPos2, chunk) -> {
                        return true;
                    })
            );
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("stronghold")),
                    (structurePiece, worldAccess, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, blockPos2, chunk) -> {
                        if (structurePiece instanceof StrongholdGenerator.PortalRoom) {
                            BlockState northFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
                            BlockState southFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
                            BlockState eastFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
                            BlockState westFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);

                            setBlockInStructure(structurePiece, chunk, northFrame, 4, 3, 8);
                            setBlockInStructure(structurePiece, chunk, northFrame, 5, 3, 8);
                            setBlockInStructure(structurePiece, chunk, northFrame, 6, 3, 8);
                            setBlockInStructure(structurePiece, chunk, southFrame, 4, 3, 12);
                            setBlockInStructure(structurePiece, chunk, southFrame, 5, 3, 12);
                            setBlockInStructure(structurePiece, chunk, southFrame, 6, 3, 12);
                            setBlockInStructure(structurePiece, chunk, eastFrame, 3, 3, 9);
                            setBlockInStructure(structurePiece, chunk, eastFrame, 3, 3, 10);
                            setBlockInStructure(structurePiece, chunk, eastFrame, 3, 3, 11);
                            setBlockInStructure(structurePiece, chunk, westFrame, 7, 3, 9);
                            setBlockInStructure(structurePiece, chunk, westFrame, 7, 3, 10);
                            setBlockInStructure(structurePiece, chunk, westFrame, 7, 3, 11);
                        }
                        return false;
                    })
            );
            //Structure Move handlers
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("swamp_hut")),
                    (structurePiece, worldAccess, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, blockPos2, chunk) -> {
                        if(structurePiece instanceof ShiftableStructurePiece) {
                            SwampHutGenerator This = ((SwampHutGenerator)(Object)structurePiece);
                            ShiftableStructurePieceInvoker shiftableStructurePieceInvoker = ((ShiftableStructurePieceInvoker) This);
                            shiftableStructurePieceInvoker.invokeAdjustToAverageHeight(worldAccess, chunkBox, 0);
                        }
                        return false;
                    })
            );
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("jungle_pyramid")),
                    (structurePiece, worldAccess, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, blockPos2, chunk) -> {
                        if(structurePiece instanceof ShiftableStructurePiece) {
                            JungleTempleGenerator This = ((JungleTempleGenerator)(Object)structurePiece);
                            ShiftableStructurePieceInvoker shiftableStructurePieceInvoker = ((ShiftableStructurePieceInvoker) This);
                            shiftableStructurePieceInvoker.invokeAdjustToAverageHeight(worldAccess, chunkBox, 0);
                        }
                        return false;
                    })
            );
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("desert_pyramid")),
                    (structurePiece, worldAccess, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, blockPos2, chunk) -> {
                        if(structurePiece instanceof ShiftableStructurePiece) {
                            DesertTempleGenerator This = ((DesertTempleGenerator)(Object)structurePiece);
                            ShiftableStructurePieceInvoker shiftableStructurePieceInvoker = ((ShiftableStructurePieceInvoker) This);
                            shiftableStructurePieceInvoker.invokeAdjustToMinHeight(worldAccess, -random.nextInt(3));
                        }
                        return false;
                    })
            );
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("igloo")),
                    (structurePiece, worldAccess, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot, chunk) -> {
                        SimpleStructurePieceInvoker simplePieceInvoker = ((SimpleStructurePieceInvoker) structurePiece);
                        IglooGeneratorPieceInvoker iglooGeneratorPieceInvoker = ((IglooGeneratorPieceInvoker) structurePiece);
                        StructurePieceInvoker pieceInvoker = (StructurePieceInvoker) structurePiece;


                        final Identifier TOP_TEMPLATE = new Identifier("igloo/top");
                        final Identifier MIDDLE_TEMPLATE = new Identifier("igloo/middle");
                        final Identifier BOTTOM_TEMPLATE = new Identifier("igloo/bottom");
                        final Map<Identifier, BlockPos> OFFSETS_FROM_TOP = ImmutableMap.of(
                                TOP_TEMPLATE, BlockPos.ORIGIN, MIDDLE_TEMPLATE, new BlockPos(2, -3, 4), BOTTOM_TEMPLATE, new BlockPos(0, -3, -2)
                        );

                        Identifier identifier = new Identifier(simplePieceInvoker.getTemplateIdString());
                        StructurePlacementData structurePlacementData = iglooGeneratorPieceInvoker.invokeCreatePlacementData(simplePieceInvoker.getPlacementData().getRotation(), identifier);

                        BlockPos blockPos = OFFSETS_FROM_TOP.get(identifier);
                        BlockPos blockPos2 = simplePieceInvoker.getPos().add(StructureTemplate.transform(structurePlacementData, new BlockPos(3 - blockPos.getX(), 0, -blockPos.getZ())));

                        int i = worldAccess.getTopY(Heightmap.Type.WORLD_SURFACE_WG, blockPos2.getX(), blockPos2.getZ());
                        BlockPos posCache = simplePieceInvoker.getPos();

                        simplePieceInvoker.setPos(simplePieceInvoker.getPos().add(0, i - 90 - 1, 0));

                        simplePieceInvoker.getPlacementData().setBoundingBox(chunkBox);
                        pieceInvoker.setBoundingBox(simplePieceInvoker.getTemplate().calculateBoundingBox(simplePieceInvoker.getPlacementData(), simplePieceInvoker.getPos()));

                        if (identifier.equals(TOP_TEMPLATE)) {
                            BlockPos blockPos4 = simplePieceInvoker.getPos().add(StructureTemplate.transform(structurePlacementData, new BlockPos(3, 0, 5)));
                        }

                        simplePieceInvoker.setPos(posCache);

                        //return true;
                        return false;
                    })
            );
            //FIX: Some structures don't work. See locations.txt
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("shipwreck")),
                    //This is looks different than the original, but all that happened to it is it was restructured and named.
                    (structurePiece, worldAccess, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot, chunk) -> {
                        SimpleStructurePieceInvoker simplePieceInvoker = ((SimpleStructurePieceInvoker) structurePiece);
                        ShipwreckGeneratorPieceInvoker iglooGeneratorPieceInvoker = ((ShipwreckGeneratorPieceInvoker) structurePiece);
                        StructurePieceInvoker pieceInvoker = (StructurePieceInvoker) structurePiece;

                        Heightmap.Type heightmap = iglooGeneratorPieceInvoker.getGrounded() ? Heightmap.Type.WORLD_SURFACE_WG : Heightmap.Type.OCEAN_FLOOR_WG;

                        Vec3i size = simplePieceInvoker.getTemplate().getSize();
                        int area2D = size.getX() * size.getZ();
                        int YtoMoveTo = 0;

                        //Beached Shipwrecks
                        if(iglooGeneratorPieceInvoker.getGrounded()) {
                            //This moves it down to the minium height of the world heightmap.
                            int minimumHeight = worldAccess.getTopY();
                            //Run through the 2D locations of the structure and get the average of their heightmaps
                            BlockPos otherBottomCorner = simplePieceInvoker.getPos().add(size.getX() - 1, 0, size.getZ() - 1);
                            //Run through the 2D locations of the structure and get the average of their heightmaps
                            for (BlockPos bottomLayerBlock : BlockPos.iterate(simplePieceInvoker.getPos(), otherBottomCorner)) {
                                minimumHeight = Math.min(minimumHeight, worldAccess.getTopY(heightmap, bottomLayerBlock.getX(), bottomLayerBlock.getZ()));
                            }
                             YtoMoveTo = minimumHeight - size.getY() / 2 - random.nextInt(3);

                        //Normal shipwrecks
                        } else {
                            //Basically this just averages all the heights of ocean floor heightmap and puts it there.
                            int accumulatedHeight = 0;
                            //Run through the 2D locations of the structure and get the average of their heightmaps
                            BlockPos otherBottomCorner = simplePieceInvoker.getPos().add(size.getX() - 1, 0, size.getZ() - 1);
                            //Run through the 2D locations of the structure and get the average of their heightmaps
                            for (BlockPos bottomLayerBlock : BlockPos.iterate(simplePieceInvoker.getPos(), otherBottomCorner)) {
                                accumulatedHeight += worldAccess.getTopY(heightmap, bottomLayerBlock.getX(), bottomLayerBlock.getZ());
                            }
                            YtoMoveTo = accumulatedHeight / area2D;
                        }

                        simplePieceInvoker.setPos(new BlockPos(simplePieceInvoker.getPos().getX(), YtoMoveTo, simplePieceInvoker.getPos().getZ()));
                        //super.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
                        simplePieceInvoker.getPlacementData().setBoundingBox(chunkBox);
                        pieceInvoker.setBoundingBox(simplePieceInvoker.getTemplate().calculateBoundingBox(simplePieceInvoker.getPlacementData(), simplePieceInvoker.getPos()));

                        return false;
                    })
            );
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("shipwreck_beached")),
                    structures.get(structures.size() - 1).getRight())
            );
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("ocean_ruin_cold")),
                    //This is looks different than the original, but all that happened to it is it was restructured and named.
                    (structurePiece, worldAccess, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot, chunk) -> {
                        SimpleStructurePieceInvoker simplePieceInvoker = ((SimpleStructurePieceInvoker) structurePiece);
                        OceanRuinGeneratorPieceInvoker oceanRuinGeneratorPieceInvoker = ((OceanRuinGeneratorPieceInvoker) structurePiece);
                        StructurePieceInvoker pieceInvoker = (StructurePieceInvoker) structurePiece;

                        simplePieceInvoker.getPlacementData()
                                .clearProcessors()
                                .addProcessor(new BlockRotStructureProcessor(oceanRuinGeneratorPieceInvoker.getIntegrity()))
                                .addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS);

                        int yToMoveTo = 0;

                        int i = worldAccess.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, simplePieceInvoker.getPos().getX(), simplePieceInvoker.getPos().getZ());
                        simplePieceInvoker.setPos(new BlockPos(simplePieceInvoker.getPos().getX(), i, simplePieceInvoker.getPos().getZ()));

                        BlockPos otherCorner = StructureTemplate.transformAround(
                                        new BlockPos(simplePieceInvoker.getTemplate().getSize().getX() - 1, 0, simplePieceInvoker.getTemplate().getSize().getZ() - 1),
                                        BlockMirror.NONE,
                                        simplePieceInvoker.getPlacementData().getRotation(),
                                        BlockPos.ORIGIN
                        ).add(simplePieceInvoker.getPos());

                        //yToMoveTo = oceanRuinGeneratorPieceInvoker.method_14829Invoker(simplePieceInvoker.getPos(), worldAccess, otherCorner);
                        yToMoveTo = oceanRuinYCalculator(simplePieceInvoker.getPos(), worldAccess, otherCorner);

                        simplePieceInvoker.setPos(new BlockPos(simplePieceInvoker.getPos().getX(), yToMoveTo, simplePieceInvoker.getPos().getZ()));
                        simplePieceInvoker.getPlacementData().setBoundingBox(chunkBox);
                        pieceInvoker.setBoundingBox(simplePieceInvoker.getTemplate().calculateBoundingBox(simplePieceInvoker.getPlacementData(), simplePieceInvoker.getPos()));
                        return false;
                    })
            );
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("ocean_ruin_warm")),
                    structures.get(structures.size() - 1).getRight())
            );
            structures.add(new MutablePair<>(
                    structureRegistry.get(Identifier.tryParse("buried_treasure")),
                    (structurePiece, worldAccess, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot, chunk) -> {
                        StructurePieceInvoker pieceInvoker = (StructurePieceInvoker) structurePiece;

                        int i = worldAccess.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, pieceInvoker.getBoundingBox().getMinX(), pieceInvoker.getBoundingBox().getMinZ());
                        BlockPos.Mutable mutable = new BlockPos.Mutable(pieceInvoker.getBoundingBox().getMinX(), i, pieceInvoker.getBoundingBox().getMinZ());

                        while(mutable.getY() > worldAccess.getBottomY()) {
                            BlockState blockState2 = worldAccess.getBlockState(mutable.down());
                            if (blockState2 == Blocks.SANDSTONE.getDefaultState()
                                    || blockState2 == Blocks.STONE.getDefaultState()
                                    || blockState2 == Blocks.ANDESITE.getDefaultState()
                                    || blockState2 == Blocks.GRANITE.getDefaultState()
                                    || blockState2 == Blocks.DIORITE.getDefaultState()) {
                                break;
                            }

                            mutable.move(0, -1, 0);
                        }
                        pieceInvoker.setBoundingBox(new BlockBox(mutable));
                        return false;
                    })
            );
            
            ran = true;
        }
    }

    private static void placeStructureStart(StructureStart structureStart, StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos,
                                           ManyArgumentFunction<Boolean, StructurePiece, StructureWorldAccess, StructureAccessor, ChunkGenerator, Random, BlockBox, ChunkPos, BlockPos, Chunk> handler, Chunk chunk) {
        List<StructurePiece> list2 = structureStart.getChildren();
        if (!list2.isEmpty()) {
            BlockBox blockBox = ((StructurePiece) list2.get(0)).getBoundingBox();
            BlockPos blockPos3 = blockBox.getCenter();
            BlockPos bottomCenterBlockBox = new BlockPos(blockPos3.getX(), blockBox.getMinY(), blockPos3.getZ());

            for(StructurePiece structurePiece : list2) {
                if (structurePiece.getBoundingBox().intersects(chunkBox)) {
                    if(handler.apply(structurePiece, world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, bottomCenterBlockBox, chunk)) {
                        structurePiece.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, bottomCenterBlockBox);
                    }
                }
            }

            structureStart.getStructure().postPlace(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, ((StructureStartInvoker) (Object) structureStart).getChildren());
        }
    }

    public static void handleStructures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, ChunkGenerator generator) {
        ChunkPos chunkPos = chunk.getPos();
        //Check if we should generate features
        if (!SharedConstants.isOutsideGenerationArea(chunkPos)) {
            //Get access to a bunch of private stuff by making a fake this
            ChunkGeneratorInvoker This = ((ChunkGeneratorInvoker) generator);

            //Find where to generate
            ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, world.getBottomSectionCoord());
            BlockPos blockPos = chunkSectionPos.getMinPos();

            //Get the structure registry
            Registry<Structure> registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
            Map<Integer, List<Structure>> map = registry.stream()
                    .collect(Collectors.groupingBy(structureType -> structureType.getFeatureGenerationStep().ordinal()));

            //Get the structure to place
            List<PlacedFeatureIndexer.IndexedFeatures> list = This.getIndexedFeaturesListSupplier().get();
            int i = list.size();

            //Make a new random
            ChunkRandom chunkRandom = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
            long l = chunkRandom.setPopulationSeed(world.getSeed(), blockPos.getX(), blockPos.getZ());

            //Stuff to do with features
            Set<RegistryEntry<Biome>> set = new ObjectArraySet();
            ChunkPos.stream(chunkSectionPos.toChunkPos(), 1).forEach(chunkPosx -> {
                Chunk chunkx = world.getChunk(chunkPosx.x, chunkPosx.z);

                for(ChunkSection chunkSection : chunkx.getSectionArray()) {
                    chunkSection.getBiomeContainer().forEachValue(set::add);
                }

            });
            set.retainAll(This.getBiomeSource().getBiomes());

            if(!ran) {
                fixRaceCondition(world);
            }

            try {
                int j = Math.max(GenerationStep.Feature.values().length, i);

                for(int k = 0; k < j; ++k) {
                    int m = 0;
                    if (structureAccessor.shouldGenerateStructures()) {
                        for(Structure structure : map.getOrDefault(k, Collections.emptyList())) {
                            chunkRandom.setDecoratorSeed(l, m, k);
                            Supplier<String> structureIDSupplier = () -> (String)registry.getKey(structure).map(Object::toString).orElseGet(structure::toString);

                            try {
                                world.setCurrentlyGeneratingStructureName(structureIDSupplier);
                                structureAccessor.getStructureStarts(chunkSectionPos, structure)
                                        .forEach(start -> {
                                            //System.out.println("Called on structure " + structure);
                                            for(Pair<Structure, ManyArgumentFunction<Boolean, StructurePiece, StructureWorldAccess, StructureAccessor, ChunkGenerator, Random, BlockBox, ChunkPos, BlockPos, Chunk>> structureFunctionPair : structures) {
                                                if (structure == structureFunctionPair.getLeft()) {
                                                    placeStructureStart(start, world, structureAccessor, generator, chunkRandom, This.getBlockBoxForChunkInvoker(chunk), chunkPos, structureFunctionPair.getRight(), chunk);
                                                    break;
                                                }
                                            }

                                            //start.place(world, structureAccessor, generator, chunkRandom, This.getBlockBoxForChunkInvoker(chunk), chunkPos);
                                        });
                            } catch (Exception e) {
                                CrashReport crashReport = CrashReport.create(e, "Feature placement");
                                crashReport.addElement("Feature").add("Description", structureIDSupplier::get);
                                throw new CrashException(crashReport);
                            }

                            ++m;
                        }
                    }

                    /*
                    if (k < i) {
                        IntSet intSet = new IntArraySet();

                        for(RegistryEntry<Biome> registryEntry : set) {
                            List<RegistryEntryList<PlacedFeature>> list3 = This.getGenerationSettingsGetter().apply(registryEntry)
                                    .getFeatures();
                            if (k < list3.size()) {
                                RegistryEntryList<PlacedFeature> registryEntryList = list3.get(k);
                                PlacedFeatureIndexer.IndexedFeatures indexedFeatures = list.get(k);
                                registryEntryList.stream()
                                        .map(RegistryEntry::value)
                                        .forEach(placedFeaturex -> intSet.add(indexedFeatures.indexMapping().applyAsInt(placedFeaturex)));
                            }
                        }

                        int n = intSet.size();
                        int[] is = intSet.toIntArray();
                        Arrays.sort(is);
                        PlacedFeatureIndexer.IndexedFeatures indexedFeatures2 = list.get(k);

                        for(int o = 0; o < n; ++o) {
                            int p = is[o];
                            PlacedFeature placedFeature = indexedFeatures2.features().get(p);
                            Supplier<String> supplier2 = () -> (String)registry2.getKey(placedFeature).map(Object::toString).orElseGet(placedFeature::toString);
                            chunkRandom.setDecoratorSeed(l, p, k);

                            try {
                                world.setCurrentlyGeneratingStructureName(supplier2);
                                placedFeature.generate(world, generator, chunkRandom, blockPos);
                            } catch (Exception var30) {
                                CrashReport crashReport2 = CrashReport.create(var30, "Feature placement");
                                crashReport2.addElement("Feature").add("Description", supplier2::get);
                                throw new CrashException(crashReport2);
                            }
                        }
                    }*/
                }

                world.setCurrentlyGeneratingStructureName(null);
            } catch (Exception var31) {
                CrashReport crashReport3 = CrashReport.create(var31, "Biome decoration");
                crashReport3.addElement("Generation").add("CenterX", chunkPos.x).add("CenterZ", chunkPos.z).add("Seed", l);
                throw new CrashException(crashReport3);
            }
        }
    }

    public static BlockPos getBlockInStructurePiece(StructurePiece piece, int x, int y, int z) {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        return new BlockPos(access.invokeApplyXTransform(x, z), access.invokeApplyYTransform(y), access.invokeApplyZTransform(x, z));
    }

    public static void setBlockInStructure(StructurePiece piece, Chunk chunk, BlockState state, int x, int y, int z) {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        BlockPos pos = getBlockInStructurePiece(piece, x, y, z);
        if (piece.getBoundingBox().contains(pos)) {
            BlockMirror mirror = access.getMirror();
            if (mirror != BlockMirror.NONE)
                state = state.mirror(mirror);
            BlockRotation rotation = piece.getRotation();
            if (rotation != BlockRotation.NONE)
                state = state.rotate(rotation);

            setBlockInChunk(chunk, pos, state);
        }
    }

    public static void setBlockInChunk(Chunk chunk, BlockPos pos, BlockState state) {
        if (chunk.getPos().equals(new ChunkPos(pos))) {
            chunk.setBlockState(pos, state, false);
        }
    }
}

