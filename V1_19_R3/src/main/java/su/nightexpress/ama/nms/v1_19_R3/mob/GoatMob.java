package su.nightexpress.ama.nms.v1_19_R3.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.api.arena.IArena;
import su.nightexpress.ama.api.type.MobFaction;
import su.nightexpress.ama.nms.ArenaMob;
import su.nightexpress.ama.nms.v1_19_R3.brain.MobAI;
import su.nightexpress.ama.nms.v1_19_R3.brain.MobBrain;
import su.nightexpress.ama.nms.v1_19_R3.brain.behavior.MobCoreBehaviors;
import su.nightexpress.ama.nms.v1_19_R3.brain.behavior.MobFightBehaviors;
import su.nightexpress.ama.nms.v1_19_R3.brain.behavior.MobIdleBehaviors;
import su.nightexpress.ama.nms.v1_19_R3.brain.behavior.impl.PrepareRamTarget;

import javax.annotation.Nullable;

public class GoatMob extends Goat implements ArenaMob {

    public static final  UniformInt          TIME_BETWEEN_RAMS     = UniformInt.of(100, 300);
    private static final TargetingConditions RAM_TARGET_CONDITIONS = TargetingConditions.forCombat().selector((entity) -> {
        return true;
    });
    public static final  int                 RAM_MIN_DISTANCE      = 4;

    private final IArena arena;
    private final MobFaction faction;

    public GoatMob(@NotNull ServerLevel level, @NotNull IArena arena, @NotNull MobFaction faction) {
        super(EntityType.GOAT, level);
        this.arena = arena;
        this.faction = faction;
        this.getBrain().setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, TIME_BETWEEN_RAMS.sample(this.level.random));
    }

    @NotNull
    @Override
    public IArena getArena() {
        return arena;
    }

    @NotNull
    @Override
    public MobFaction getFaction() {
        return faction;
    }

    @Override
    protected Brain.Provider<Goat> brainProvider() {
        return MobBrain.brainProvider(this);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return this.refreshBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @NotNull
    public Brain<Goat> refreshBrain(@NotNull Goat pet, @NotNull Brain<Goat> brain) {
        BehaviorControl<LivingEntity> cooldownTicks = new CountDownCooldownTicks(MemoryModuleType.RAM_COOLDOWN_TICKS);

        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
            MobCoreBehaviors.lookAtTarget(),
            MobCoreBehaviors.moveToTarget(),
            MobCoreBehaviors.swim(),
            cooldownTicks,
            MobFightBehaviors.stopAngryIfTargetDead())
        );

        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
            MobIdleBehaviors.followOwner(),
            MobFightBehaviors.autoTargetAndAttack())
        );

        brain.addActivity(Activity.FIGHT, 10, ImmutableList.of(
            MobFightBehaviors.stopAttackIfTargetInvalid(pet),
            MobFightBehaviors.reachTargetWhenOutOfRange(),
            MobFightBehaviors.meleeAttack(20))
        );

        brain.addActivityWithConditions(Activity.RAM,
            ImmutableList.of(
                Pair.of(0, new RamTarget(
                    goat -> TIME_BETWEEN_RAMS,
                    RAM_TARGET_CONDITIONS,
                    3.0F,
                    goat -> goat.isBaby() ? 1.0D : 2.5D,
                    goat -> goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_RAM_IMPACT : SoundEvents.GOAT_RAM_IMPACT,
                    goat -> goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_HORN_BREAK : SoundEvents.GOAT_HORN_BREAK)
                ),
                Pair.of(1, new PrepareRamTarget<>(
                    4, 10, 1.5F, 20,
                    goat -> goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_PREPARE_RAM : SoundEvents.GOAT_PREPARE_RAM)
                )
            ),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)
            )
        );

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();

        this.brain = brain;
        return brain;
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("goatBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("goatActivityUpdate");
        this.updateActivity();
        this.level.getProfiler().pop();
    }

    protected void updateActivity() {
        Brain<Goat> brain = this.getBrain();
        if (MobAI.getAngerTarget(this).isPresent()) {
            brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.RAM, Activity.FIGHT));
        }
        else {
            brain.setActiveActivityIfPossible(Activity.IDLE);
        }
        this.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    @Override
    public boolean hurt(DamageSource damagesource, float damage) {
        if (super.hurt(damagesource, damage)) {
            if (damagesource.getEntity() instanceof LivingEntity target) {
                MobAI.setAngerTarget(this, target, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player entityhuman, InteractionHand enumhand) {
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    @Override
    public boolean dropHorn() {
        return false;
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    public void setInLove(@Nullable Player entityhuman) {

    }

    @Override
    public void setInLoveTime(int i) {

    }

    @Override
    public boolean isInLove() {
        return false;
    }

    @Override
    public boolean canMate(Animal entityanimal) {
        return false;
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel worldserver, Animal entityanimal) {

    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return false;
    }
}
