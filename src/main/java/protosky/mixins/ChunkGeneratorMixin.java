package protosky.mixins;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    @Shadow
    private Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;
    @Shadow
    protected BiomeSource biomeSource;
    @Shadow
    private static BlockBox getBlockBoxForChunk(Chunk chunk) {
        return null;
    }
    @Shadow
    private Function<RegistryEntry<Biome>, GenerationSettings> generationSettingsGetter;

    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        //System.out.println("Generating Features!");
        ChunkPos chunkPos2 = chunk.getPos();
        if (SharedConstants.isOutsideGenerationArea(chunkPos2)) {
            return;
        }
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos2, world.getBottomSectionCoord());
        BlockPos blockPos = chunkSectionPos.getMinPos();
        Registry<Structure> registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        Map<Integer, List<Structure>> map = registry.stream().collect(Collectors.groupingBy(structureType -> structureType.getFeatureGenerationStep().ordinal()));
        List<PlacedFeatureIndexer.IndexedFeatures> list = this.indexedFeaturesListSupplier.get();
        ChunkRandom chunkRandom = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
        long l = chunkRandom.setPopulationSeed(world.getSeed(), blockPos.getX(), blockPos.getZ());
        ObjectArraySet set = new ObjectArraySet();
        ChunkPos.stream(chunkSectionPos.toChunkPos(), 1).forEach(chunkPos -> {
            Chunk chunk1 = world.getChunk(chunkPos.x, chunkPos.z);
            for (ChunkSection chunkSection : chunk1.getSectionArray()) {
                chunkSection.getBiomeContainer().forEachValue(set::add);
            }
        });
        set.retainAll(this.biomeSource.getBiomes());
        int i = list.size();
        try {
            Registry<PlacedFeature> registry2 = world.getRegistryManager().get(RegistryKeys.PLACED_FEATURE);
            int j = Math.max(GenerationStep.Feature.values().length, i);
            for (int k = 0; k < j; ++k) {
                int m = 0;
                if (structureAccessor.shouldGenerateStructures()) {
                    List<Structure> list2 = map.getOrDefault(k, Collections.emptyList());
                    for (Structure structure : list2) {
                        chunkRandom.setDecoratorSeed(l, m, k);
                        Supplier<String> supplier = () -> registry.getKey(structure).map(Object::toString).orElseGet(structure::toString);
                        try {
                            world.setCurrentlyGeneratingStructureName(supplier);
                            structureAccessor.getStructureStarts(chunkSectionPos, structure).forEach(start -> start.place(world, structureAccessor, (ChunkGenerator) (Object)this, chunkRandom, this.getBlockBoxForChunk(chunk), chunkPos2));
                        } catch (Exception exception) {
                            CrashReport crashReport = CrashReport.create(exception, "Feature placement");
                            crashReport.addElement("Feature").add("Description", supplier::get);
                            throw new CrashException(crashReport);
                        }
                        ++m;
                    }
                }
                //if (k >= i) continue;
//                IntArraySet intSet = new IntArraySet();
//                for (RegistryEntry registryEntry : set) {
//                    List<RegistryEntryList<PlacedFeature>> list3 = this.generationSettingsGetter.apply(registryEntry).getFeatures();
//                    if (k >= list3.size()) continue;
//                    RegistryEntryList<PlacedFeature> registryEntryList = list3.get(k);
//                    PlacedFeatureIndexer.IndexedFeatures indexedFeatures = list.get(k);
//                    registryEntryList.stream().map(RegistryEntry::value).forEach(placedFeature -> intSet.add(indexedFeatures.indexMapping().applyAsInt((PlacedFeature)placedFeature)));
//                }
//                int n = intSet.size();
//                int[] is = intSet.toIntArray();
//                Arrays.sort(is);
//                PlacedFeatureIndexer.IndexedFeatures indexedFeatures2 = list.get(k);
//                for (int o = 0; o < n; ++o) {
//                    int p = is[o];
//                    PlacedFeature placedFeature2 = indexedFeatures2.features().get(p);
//                    Supplier<String> supplier2 = () -> registry2.getKey(placedFeature2).map(Object::toString).orElseGet(placedFeature2::toString);
//                    chunkRandom.setDecoratorSeed(l, p, k);
//                    try {
//                        world.setCurrentlyGeneratingStructureName(supplier2);
//                        placedFeature2.generate(world, this, chunkRandom, blockPos);
//                        continue;
//                    } catch (Exception exception2) {
//                        CrashReport crashReport2 = CrashReport.create(exception2, "Feature placement");
//                        crashReport2.addElement("Feature").add("Description", supplier2::get);
//                        throw new CrashException(crashReport2);
//                    }
//                }
            }
            world.setCurrentlyGeneratingStructureName(null);
        } catch (Exception exception3) {
            CrashReport crashReport3 = CrashReport.create(exception3, "Biome decoration");
            crashReport3.addElement("Generation").add("CenterX", chunkPos2.x).add("CenterZ", chunkPos2.z).add("Seed", l);
            throw new CrashException(crashReport3);
        }
    }


}
