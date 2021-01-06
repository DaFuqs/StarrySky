package de.dafuqs.starrysky.spheroid.lists;

import de.dafuqs.starrysky.StarrySkyCommon;
import de.dafuqs.starrysky.dimension.SpheroidDistributionType;
import de.dafuqs.starrysky.dimension.SpheroidLoader;
import de.dafuqs.starrysky.spheroid.types.RainbowSpheroidType;
import de.dafuqs.starrysky.spheroid.types.ShellSpheroidType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

import static de.dafuqs.starrysky.dimension.SpheroidLoader.SpheroidDimensionType.OVERWORLD;

public class SpheroidListCinderscapes extends SpheroidList {

    private static final String MOD_ID = "cinderscapes";


    public static boolean shouldGenerate() {
        return FabricLoader.getInstance().isModLoaded(MOD_ID) && StarrySkyCommon.STARRY_SKY_CONFIG.generateCinderscapesSpheroids;
    }

    public static void setup(SpheroidLoader spheroidLoader) {
        StarrySkyCommon.LOGGER.info("Loading Cinderscapes integration...");

        BlockState polypite_nether_quartz = Registry.BLOCK.get(new Identifier(MOD_ID,"polypite_nether_quartz")).getDefaultState(); // size: 0, 1, ...
        BlockState polypite_rose_quartz = Registry.BLOCK.get(new Identifier(MOD_ID,"polypite_rose_quartz")).getDefaultState(); // size: 0, 1, ...
        BlockState polypite_smoky_quartz = Registry.BLOCK.get(new Identifier(MOD_ID,"polypite_smoky_quartz")).getDefaultState(); // size: 0, 1, ...
        BlockState polypite_sulfur_quartz = Registry.BLOCK.get(new Identifier(MOD_ID,"polypite_sulfur_quartz")).getDefaultState(); // size: 0, 1, ...
        BlockState sulfur_quartz_ore = Registry.BLOCK.get(new Identifier(MOD_ID,"sulfur_quartz_ore")).getDefaultState(); // size: 0, 1, ...
        BlockState rose_quartz_ore = Registry.BLOCK.get(new Identifier(MOD_ID,"rose_quartz_ore")).getDefaultState(); // size: 0, 1, ...
        BlockState smoky_quartz_ore = Registry.BLOCK.get(new Identifier(MOD_ID,"smoky_quartz_ore")).getDefaultState(); // size: 0, 1, ...

        BlockState scorched_stem = Registry.BLOCK.get(new Identifier(MOD_ID,"scorched_stem")).getDefaultState(); // size: 0, 1, ...
        BlockState scorched_hyphae = Registry.BLOCK.get(new Identifier(MOD_ID,"scorched_hyphae")).getDefaultState(); // size: 0, 1, ...

        // UMBRAL
        BlockState umbral_nylium = Registry.BLOCK.get(new Identifier(MOD_ID,"scorched_hyphae")).getDefaultState(); // size: 0, 1, ...
        BlockState umbral_stem = Registry.BLOCK.get(new Identifier(MOD_ID,"scorched_hyphae")).getDefaultState(); // size: 0, 1, ...
        BlockState umbral_hyphae = Registry.BLOCK.get(new Identifier(MOD_ID,"scorched_hyphae")).getDefaultState(); // size: 0, 1, ...
        BlockState umbral_wart_block = Registry.BLOCK.get(new Identifier(MOD_ID,"scorched_hyphae")).getDefaultState(); // size: 0, 1, ...
        BlockState umbral_flesh_block = Registry.BLOCK.get(new Identifier(MOD_ID,"scorched_hyphae")).getDefaultState(); // size: 0, 1, ...


    }

}