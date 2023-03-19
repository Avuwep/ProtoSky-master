package protosky.mixins;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
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
import net.minecraft.world.chunk.ReadableContainer;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
//    @Inject(method = "generateFeatures", at = @At(value="INVOKE", target = "Lnet/minecraft/util/math/random/ChunkRandom;setDecoratorSeed(JII)V", ordinal = 1),cancellable = true)
//    private void onGenerateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, CallbackInfo ci) {
//        if (world.toServerWorld().getRegistryKey() != World.END) {
//            System.out.println("Structures cancelled! " + world.toServerWorld().getRegistryKey().toString());
//            world.setCurrentlyGeneratingStructureName((Supplier)null);
//            ci.cancel();
//        }
//        //System.out.println("Hello");
//    }

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

    //Overwrite copied exactly from source code, modified to prevent features from generating while allowing entities to generate
    @Overwrite
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        ChunkPos chunkPos = chunk.getPos();
        //for testing
        System.out.println("Generating features!");
        if (!SharedConstants.isOutsideGenerationArea(chunkPos)) {
            ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, world.getBottomSectionCoord());
            BlockPos blockPos = chunkSectionPos.getMinPos();
            Registry<Structure> registry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
            Map<Integer, List<Structure>> map = (Map)registry.stream().collect(Collectors.groupingBy((structureType) -> {
                return structureType.getFeatureGenerationStep().ordinal();
            }));
            List<PlacedFeatureIndexer.IndexedFeatures> list = (List)this.indexedFeaturesListSupplier.get();
            ChunkRandom chunkRandom = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
            long l = chunkRandom.setPopulationSeed(world.getSeed(), blockPos.getX(), blockPos.getZ());
            Set<RegistryEntry<Biome>> set = new ObjectArraySet();
            ChunkPos.stream(chunkSectionPos.toChunkPos(), 1).forEach((chunkPosx) -> {
                Chunk chunk1 = world.getChunk(chunkPosx.x, chunkPosx.z);
                ChunkSection[] var4 = chunk1.getSectionArray();
                int var5 = var4.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    ChunkSection chunkSection = var4[var6];
                    ReadableContainer<RegistryEntry<Biome>> var10000 = chunkSection.getBiomeContainer();
                    Objects.requireNonNull(set);
                    var10000.forEachValue(set::add);
                }

            });
            set.retainAll(this.biomeSource.getBiomes());
            int i = list.size();

            try {
                Registry<PlacedFeature> registry2 = world.getRegistryManager().get(RegistryKeys.PLACED_FEATURE);
                int j = Math.max(GenerationStep.Feature.values().length, i);

                for(int k = 0; k < j; ++k) {
                    int m = 0;
                    CrashReportSection var10000;
                    Iterator var20;
                    if (structureAccessor.shouldGenerateStructures()) {
                        List<Structure> list2 = (List)map.getOrDefault(k, Collections.emptyList());

                        for(var20 = list2.iterator(); var20.hasNext(); ++m) {
                            Structure structure = (Structure)var20.next();
                            chunkRandom.setDecoratorSeed(l, m, k);
                            Supplier<String> supplier = () -> {
                                Optional var10001 = registry.getKey(structure).map(Object::toString);
                                Objects.requireNonNull(structure);
                                return (String)var10001.orElseGet(structure::toString);
                            };

                            try {
                                world.setCurrentlyGeneratingStructureName(supplier);
                                structureAccessor.getStructureStarts(chunkSectionPos, structure).forEach((start) -> {
                                    start.place(world, structureAccessor, (ChunkGenerator)(Object) this, chunkRandom, getBlockBoxForChunk(chunk), chunkPos);
                                });
                            } catch (Exception var29) {
                                CrashReport crashReport = CrashReport.create(var29, "Feature placement");
                                var10000 = crashReport.addElement("Feature");
                                Objects.requireNonNull(supplier);
                                var10000.add("Description", supplier::get);
                                throw new CrashException(crashReport);
                            }
                        }
                    }

                    if (k < i) {
                        IntSet intSet = new IntArraySet();
                        var20 = set.iterator();

                        while(var20.hasNext()) {
                            RegistryEntry<Biome> registryEntry = (RegistryEntry)var20.next();
                            List<RegistryEntryList<PlacedFeature>> list3 = ((GenerationSettings)this.generationSettingsGetter.apply(registryEntry)).getFeatures();
                            if (k < list3.size()) {
                                RegistryEntryList<PlacedFeature> registryEntryList = (RegistryEntryList)list3.get(k);
                                PlacedFeatureIndexer.IndexedFeatures indexedFeatures = (PlacedFeatureIndexer.IndexedFeatures)list.get(k);
                                registryEntryList.stream().map(RegistryEntry::value).forEach((placedFeaturex) -> {
                                    //Commenting out this line prevents features from being added to a list of things to be generated, effectively preventing them from generating
                                    //intSet.add(indexedFeatures.indexMapping().applyAsInt(placedFeaturex));
                                });
                            }
                        }

                        int n = intSet.size();
                        int[] is = intSet.toIntArray();
                        Arrays.sort(is);
                        PlacedFeatureIndexer.IndexedFeatures indexedFeatures2 = (PlacedFeatureIndexer.IndexedFeatures)list.get(k);

                        for(int o = 0; o < n; ++o) {
                            int p = is[o];
                            PlacedFeature placedFeature = (PlacedFeature)indexedFeatures2.features().get(p);
                            Supplier<String> supplier2 = () -> {
                                Optional var10001 = registry2.getKey(placedFeature).map(Object::toString);
                                Objects.requireNonNull(placedFeature);
                                return (String)var10001.orElseGet(placedFeature::toString);
                            };
                            chunkRandom.setDecoratorSeed(l, p, k);

                            try {
                                world.setCurrentlyGeneratingStructureName(supplier2);
                                placedFeature.generate(world, (ChunkGenerator)(Object) this, chunkRandom, blockPos);
                            } catch (Exception var30) {
                                CrashReport crashReport2 = CrashReport.create(var30, "Feature placement");
                                var10000 = crashReport2.addElement("Feature");
                                Objects.requireNonNull(supplier2);
                                var10000.add("Description", supplier2::get);
                                throw new CrashException(crashReport2);
                            }
                        }
                    }
                }

                world.setCurrentlyGeneratingStructureName((Supplier)null);
            } catch (Exception var31) {
                CrashReport crashReport3 = CrashReport.create(var31, "Biome decoration");
                crashReport3.addElement("Generation").add("CenterX", chunkPos.x).add("CenterZ", chunkPos.z).add("Seed", l);
                throw new CrashException(crashReport3);
            }
        }
    }

}
