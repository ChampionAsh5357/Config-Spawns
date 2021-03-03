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

package net.ashwork.configspawns;

import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.ashwork.configspawns.util.Helper;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ConfigSpawns.ID)
public class ConfigSpawns {

    public static final String ID = "configspawns";
    public static final Logger LOGGER = LogManager.getLogger("Config Spawns");

    public ConfigSpawns() {
        final IEventBus mod = FMLJavaModLoadingContext.get().getModEventBus(), forge = MinecraftForge.EVENT_BUS;

        ModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_SPECIFICATION);

        mod.addListener(this::configLoad);
        mod.addListener(this::configReload);
        forge.addListener(this::reloadListener);
        forge.addListener(this::tick);
        forge.addListener(this::attachToBiomes);
    }

    private void attachToBiomes(final BiomeLoadingEvent event) {
        // Test
        if (event.getName().equals(Biomes.PLAINS.getLocation()))
            Helper.addEntitySpawn(event, EntityClassification.MONSTER, EntityType.ILLUSIONER,
                    biome -> Config.SERVER.spawnWeight, 5, 10);
    }

    private void reloadListener(final AddReloadListenerEvent event) {
        event.addListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor,
                gameExecutor) -> CompletableFuture.allOf().thenCompose(stage::markCompleteAwaitingOthers)
                        .thenAcceptAsync(v -> Helper.invalidate()));
    }

    private void tick(final WorldTickEvent event) {
        if (event.side == LogicalSide.CLIENT || event.phase == Phase.START)
            return;
        if (Helper.isInvalid())
            Helper.update((ServerWorld) event.world);
    }

    private void configLoad(final ModConfig.Loading event) {
        if (event.getConfig().getType() == Type.SERVER) {
            Config.SERVER.spawnWeight = Config.SERVER.spawnWeightConfig.get();
            Helper.invalidate();
        }
    }

    private void configReload(final ModConfig.Reloading event) {
        if (event.getConfig().getType() == Type.SERVER) {
            Config.SERVER.spawnWeight = Config.SERVER.spawnWeightConfig.get();
            Helper.invalidate();
        }
    }

    public static class Config {
        public static final Config SERVER;
        public static final ForgeConfigSpec SERVER_SPECIFICATION;

        static {
            final Pair<Config, ForgeConfigSpec> specificationPair = new ForgeConfigSpec.Builder()
                    .configure(Config::new);
            SERVER_SPECIFICATION = specificationPair.getRight();
            SERVER = specificationPair.getLeft();
        }

        private final IntValue spawnWeightConfig;
        private int spawnWeight;

        private Config(final Builder builder) {
            this.spawnWeightConfig = builder.defineInRange("spawnWeight", 10, 0, Integer.MAX_VALUE);
        }
    }
}
