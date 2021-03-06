package quarris.enchantability.mod.common.enchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import quarris.enchantability.api.EnchantabilityApi;
import quarris.enchantability.api.IEffectComponent;
import quarris.enchantability.api.IEffectSupplier;
import quarris.enchantability.api.enchants.IEnchantEffect;
import quarris.enchantability.mod.Enchantability;
import quarris.enchantability.mod.ModConfig;
import quarris.enchantability.mod.common.enchants.impl.*;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Enchants {

    public static void registerEffect() {
        ModConfig config = ModConfig.get();

        // Effects
        if (config.enableFarReach.get())
            registerEffect(FarReachEnchantEffect.NAME, Enchantments.KNOCKBACK, FarReachEnchantEffect::new);

        if (config.enableFastBreak.get())
            registerEffect(FastBreakEnchantEffect.NAME, Enchantments.PUNCH, FastBreakEnchantEffect::new);

        if (config.enableGravity.get())
            registerEffect(GravityEnchantEffect.NAME, Enchantments.FEATHER_FALLING, GravityEnchantEffect::new);

        if (config.enableDeflection.get())
            registerEffect(DeflectionEnchantEffect.NAME, Enchantments.PROJECTILE_PROTECTION, DeflectionEnchantEffect::new);

        if (config.enableVoid.get())
            registerEffect(VoidEnchantEffect.NAME, Enchantments.INFINITY, VoidEnchantEffect::new);

        if (config.enableBlastResist.get())
            registerEffect(BlastResistanceEnchantEffect.NAME, Enchantments.BLAST_PROTECTION, BlastResistanceEnchantEffect::new);


        if (config.enableFirePraise.get())
            registerEffect(FirePraiseEnchantEffect.NAME, Enchantments.FIRE_ASPECT, FirePraiseEnchantEffect::new);


        if (config.enableAirWalker.get())
            registerEffect(AirWalkerEnchantEffect.NAME, Enchantments.FROST_WALKER, AirWalkerEnchantEffect::new);


        if (config.enableGluttony.get())
            registerEffect(GluttonyEnchantEffect.NAME, Enchantments.MENDING, GluttonyEnchantEffect::new);

        if (config.enableDexterity.get())
            registerEffect(DexterityEnchantEffect.NAME, Enchantments.EFFICIENCY, DexterityEnchantEffect::new);

        if (config.enableSmite.get())
            registerEffect(SmiteEnchantEffect.NAME, Enchantments.SMITE, SmiteEnchantEffect::new);

        if (config.enableStrike.get())
            registerEffect(StrikeEnchantEffect.NAME, Enchantments.POWER, StrikeEnchantEffect::new);

        if (config.enableSwiftCharge.get())
            registerEffect(SwiftChargeEnchantEffect.NAME, Enchantments.QUICK_CHARGE, SwiftChargeEnchantEffect::new);

        if (config.enableHeat.get())
            registerEffect(HeatEnchantEffect.NAME, Enchantments.FLAME, HeatEnchantEffect::new);


        // Components
        Enchantability.proxy.registerClientComponents();

        if (config.enableFastBreak.get())
            registerComponent(FastBreakEnchantEffect.NAME, PlayerEvent.BreakSpeed.class, FastBreakEnchantEffect::handBreak, e -> Collections.singleton(e.getPlayer()));

        if (config.enableDeflection.get())
            registerComponent(DeflectionEnchantEffect.NAME, ProjectileImpactEvent.class, DeflectionEnchantEffect::deflect,
                    e -> {
                        if (e.getRayTraceResult() instanceof EntityRayTraceResult) {
                            if (((EntityRayTraceResult) e.getRayTraceResult()).getEntity() instanceof PlayerEntity) {
                                return Collections.singleton((PlayerEntity) ((EntityRayTraceResult) e.getRayTraceResult()).getEntity());
                            }
                        }
                        return null;
                    });

        if (config.enableDexterity.get()) {
            registerComponent(DexterityEnchantEffect.NAME, PlayerEvent.ItemCraftedEvent.class, DexterityEnchantEffect::craft, e -> Collections.singleton(e.getPlayer()));
        }

        if (config.enableVoid.get()) {
            registerComponent(VoidEnchantEffect.NAME, TickEvent.PlayerTickEvent.class, VoidEnchantEffect::voidTeleport, e -> Collections.singleton(e.player));
        }

        if (config.enableSmite.get()) {
            registerComponent(SmiteEnchantEffect.NAME, AttackEntityEvent.class, SmiteEnchantEffect::smite, e -> Collections.singleton(e.getPlayer()));
        }

        if (config.enableBlastResist.get()) {
            registerComponent(BlastResistanceEnchantEffect.NAME, ExplosionEvent.Detonate.class, BlastResistanceEnchantEffect::resistBlast,
                    evt -> evt.getAffectedEntities().stream()
                            .filter(entity -> entity instanceof PlayerEntity)
                            .map(entity -> (PlayerEntity) entity)
                            .collect(Collectors.toList()));
        }

        if (config.enableFirePraise.get()) {
            registerComponent(FirePraiseEnchantEffect.NAME, TickEvent.PlayerTickEvent.class, FirePraiseEnchantEffect::praiseTheSun,
                    e -> Collections.singleton(e.player));
        }

        if (config.enableAirWalker.get()) {
            registerComponent(AirWalkerEnchantEffect.NAME, TickEvent.PlayerTickEvent.class, AirWalkerEnchantEffect::airWalk,
                    e -> Collections.singleton(e.player));
        }

        if (config.enableGluttony.get()) {
            registerComponent(GluttonyEnchantEffect.NAME, LivingEntityUseItemEvent.Finish.class, GluttonyEnchantEffect::consume,
                    e -> e.getEntity() instanceof PlayerEntity ?
                            Collections.singleton((PlayerEntity) e.getEntity()) :
                            Collections.emptyList()
            );
            GluttonyFoods.initMendingFoods();
        }


        if (config.enableSwiftCharge.get()) {
            registerComponent(SwiftChargeEnchantEffect.NAME, LivingEntityUseItemEvent.Start.class, SwiftChargeEnchantEffect::itemUse, e -> {
                if (e.getEntity() instanceof PlayerEntity) {
                    return Collections.singleton((PlayerEntity) e.getEntity());
                }
                return Collections.emptyList();
            });
            registerComponent(SwiftChargeEnchantEffect.NAME, TickEvent.PlayerTickEvent.class, SwiftChargeEnchantEffect::transition,
                    e -> Collections.singleton(e.player));
        }

        if (config.enableStrike.get()) {
            registerComponent(StrikeEnchantEffect.NAME, LivingHurtEvent.class, StrikeEnchantEffect::strike,
                    e -> {
                        if (e.getSource().damageType.equals("player")) {
                            return Collections.singleton((PlayerEntity) e.getSource().getTrueSource());
                        }
                        return Collections.emptyList();
                    });
        }

        if (config.enableHeat.get()) {
            registerComponent(HeatEnchantEffect.NAME, TickEvent.PlayerTickEvent.class, HeatEnchantEffect::heat, e -> Collections.singleton(e.player));
        }
    }

    private static void registerEffect(ResourceLocation name, Enchantment enchantment, IEffectSupplier effectSupplier) {
        EnchantabilityApi.getInstance().registerEnchantEffect(name, enchantment, effectSupplier);
    }

    private static <F extends IEnchantEffect, T extends Event> void registerComponent(ResourceLocation name, Class<T> eventClass, IEffectComponent<F, T> component, Function<T, Collection<PlayerEntity>> playerGetter) {
        EnchantabilityApi.getInstance().registerEffectComponent(name, eventClass, component, playerGetter);
    }
}
