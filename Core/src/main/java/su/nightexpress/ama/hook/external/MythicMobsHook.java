package su.nightexpress.ama.hook.external;

import io.lumine.mythic.api.exceptions.InvalidMobTypeException;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MythicMobsHook {

    static MythicBukkit mythicMobs = MythicBukkit.inst();

    public static boolean isMythicMob(@NotNull Entity entity) {
        return mythicMobs.getAPIHelper().isMythicMob(entity);
    }

    @Nullable
    public static ActiveMob getMobInstance(@NotNull Entity entity) {
        return mythicMobs.getAPIHelper().getMythicMobInstance(entity);
    }

    @Nullable
    public static MythicMob getMobConfig(@NotNull Entity entity) {
        ActiveMob mob = getMobInstance(entity);
        return mob != null ? mob.getType() : null;
    }

    @Nullable
    public static MythicMob getMobConfig(@NotNull String mobId) {
        return mythicMobs.getAPIHelper().getMythicMob(mobId);
    }

    @NotNull
    public static String getMobInternalName(@NotNull Entity entity) {
        MythicMob mythicMob = getMobConfig(entity);
        return mythicMob != null ? mythicMob.getInternalName() : "null";
    }

    @NotNull
    public static String getMobDisplayName(@NotNull String mobId) {
        MythicMob mythicMob = getMobConfig(mobId);
        PlaceholderString string = mythicMob != null ? mythicMob.getDisplayName() : null;
        return string != null ? string.get() : mobId;
    }

    public static double getMobLevel(@NotNull Entity entity) {
        ActiveMob mob = getMobInstance(entity);
        return mob != null ? mob.getLevel() : 0;
    }

    @NotNull
    public static List<String> getMobConfigIds() {
        return new ArrayList<>(mythicMobs.getMobManager().getMobNames());
    }

    public static void killMob(@NotNull Entity entity) {
        ActiveMob mob = getMobInstance(entity);
        if (mob == null || mob.isDead()) return;

        mob.setDead();
        mob.remove();
        entity.remove();
    }

    public static boolean isValid(@NotNull String mobId) {
        return getMobConfig(mobId) != null;
    }

    @Nullable
    public static Entity spawnMythicMob(@NotNull String mobId, @NotNull Location location, int level) {
        return spawnMob(mobId, location, level);
    }

    @Nullable
    public static Entity spawnMob(@NotNull String mobId, @NotNull Location location, int level) {
        try {
            MythicMob mythicMob = getMobConfig(mobId);
            return mythicMobs.getAPIHelper().spawnMythicMob(mythicMob, location, level);
        }
        catch (InvalidMobTypeException e) {
            e.printStackTrace();
        }
        return null;
    }
}
