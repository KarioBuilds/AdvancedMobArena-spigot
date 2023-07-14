package su.nightexpress.ama.hook.mob.impl;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.hook.HookId;
import su.nightexpress.ama.hook.external.MythicMobsHook;
import su.nightexpress.ama.hook.mob.MobProvider;

import java.util.List;
import java.util.Optional;

public class MythicMobProvider implements MobProvider {

    @NotNull
    @Override
    public String getName() {
        return HookId.MYTHIC_MOBS;
    }

    @NotNull
    @Override
    public Optional<LivingEntity> spawn(@NotNull String mobId, @NotNull Location location, int level) {
        if (MythicMobsHook.getMobConfig(mobId) == null) return Optional.empty();

        return Optional.ofNullable((LivingEntity) MythicMobsHook.spawnMythicMob(mobId, location, level));
    }

    @NotNull
    @Override
    public List<String> getMobNames() {
        return MythicMobsHook.getMobConfigIds();
    }
}
