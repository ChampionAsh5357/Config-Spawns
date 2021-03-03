/*
 * MIT License
 *
 * Copyright (c) 2021 ChampionAsh5357
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.ashwork.configspawns.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public class Helper {

    private static boolean invalidate = false;
    private static final Map<ResourceLocation, Set<EntityClassification>> BIOMES = new HashMap<>();

    public static void addEntitySpawn(final BiomeLoadingEvent event, final EntityClassification classification,
            final EntityType<?> type, final ToIntFunction<Biome> weight, final int minCount, final int maxCount) {
        final Spawner spawner = new Spawner(type, weight, minCount, maxCount);
        event.getSpawns().getSpawner(classification).add(spawner);
        Helper.BIOMES.computeIfAbsent(event.getName(), e -> new HashSet<>()).add(classification);
    }

    public static void invalidate() {
        Helper.invalidate = true;
    }

    public static boolean isInvalid() { return Helper.invalidate; }

    public static void update(final ServerWorld world) {
        world.getServer().func_244267_aX().func_230521_a_(Registry.BIOME_KEY)
                .ifPresent(registry -> registry.keySet().stream().filter(Helper.BIOMES::containsKey)
                        .map(registry::getOptional)
                        .forEach(opt -> opt.ifPresent(biome -> Helper.BIOMES.get(biome.getRegistryName())
                                .forEach(cls -> biome.getMobSpawnInfo().getSpawners(cls).stream()
                                        .filter(spn -> spn instanceof Spawner)
                                        .forEach(spn -> ((Spawner) spn).update(biome))))));
        Helper.invalidate = false;
    }
}
