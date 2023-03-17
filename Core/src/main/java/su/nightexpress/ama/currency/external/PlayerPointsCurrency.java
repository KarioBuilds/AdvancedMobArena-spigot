package su.nightexpress.ama.currency.external;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.ama.currency.AbstractCurrency;
import su.nightexpress.ama.api.currency.ICurrencyConfig;
import su.nightexpress.ama.hook.HookId;

public class PlayerPointsCurrency extends AbstractCurrency {

    private final PlayerPointsAPI api;

    public PlayerPointsCurrency(@NotNull ICurrencyConfig config) {
        super(config);
        PlayerPoints points = (PlayerPoints) Bukkit.getPluginManager().getPlugin(HookId.PLAYER_POINTS);
        if (points == null) throw new IllegalStateException("Plugin is null!");

        this.api = points.getAPI();
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return api.look(player.getUniqueId());
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        api.give(player.getUniqueId(), (int) amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        api.take(player.getUniqueId(), (int) amount);
    }
}
