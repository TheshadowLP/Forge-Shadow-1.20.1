package net.shadowbeast.frostbound.registries;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowbeast.frostbound.Frostbound;
import net.shadowbeast.frostbound.particle.FeatherParticles;

import static net.shadowbeast.frostbound.Frostbound.MOD_ID;

@Mod.EventBusSubscriber(modid = Frostbound.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MOD_ID);
    public static final RegistryObject<SimpleParticleType> FEATHER_PARTICLES = PARTICLES.register("feather_particles", () -> new SimpleParticleType(false));

    @SubscribeEvent public static void registerFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.FEATHER_PARTICLES.get(), FeatherParticles.Provider::new);
    }
}
