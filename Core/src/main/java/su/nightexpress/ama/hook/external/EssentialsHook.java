package su.nightexpress.ama.hook.external;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.hook.HookId;

public class EssentialsHook {

    private static final Essentials ESSENTIALS = (Essentials) Bukkit.getPluginManager().getPlugin(HookId.ESSENTIALS);

    public static void disableGod(@NotNull Player player) {
        if (ESSENTIALS != null) {
            ESSENTIALS.getUser(player).setGodModeEnabled(false);
        }
    }
}
