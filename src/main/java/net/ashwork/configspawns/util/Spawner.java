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

import java.lang.reflect.Field;
import java.util.function.ToIntFunction;

import net.ashwork.configspawns.ConfigSpawns;
import net.minecraft.entity.EntityType;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class Spawner extends MobSpawnInfo.Spawners {

    private static final Field ITEM_WEIGHT = ObfuscationReflectionHelper.findField(WeightedRandom.Item.class,
            "field_76292_a");
    private final ToIntFunction<Biome> weight;

    public Spawner(final EntityType<?> type, final ToIntFunction<Biome> weight, final int minCount,
            final int maxCount) {
        super(type, 0, minCount, maxCount);
        this.weight = weight;
    }

    public void update(final Biome biome) {
        try {
            Spawner.ITEM_WEIGHT.set(this, this.weight.applyAsInt(biome));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            ConfigSpawns.LOGGER.error("The weight for {} cannot be updated: {}", this, e.getLocalizedMessage());
        }
    }
}
