package su.nightexpress.ama.command.currency;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.Perms;
import su.nightexpress.ama.Placeholders;
import su.nightexpress.ama.config.Lang;
import su.nightexpress.ama.currency.internal.ArenaCoinsCurrency;

public class BalanceCommand extends AbstractCommand<AMA> {

    private final ArenaCoinsCurrency currency;

    public BalanceCommand(@NotNull AMA plugin, @NotNull ArenaCoinsCurrency currency) {
        super(plugin, new String[]{"balance"}, Perms.COMMAND_BALANCE);
        this.currency = currency;
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.COMMAND_BALANCE_USAGE).getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_BALANCE_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() >= 2 && !sender.hasPermission(Perms.COMMAND_BALANCE_OTHERS)) {
            this.errorPermission(sender);
            return;
        }

        String pName = result.length() >= 2 ? result.getArg(1) : sender.getName();
        Player player = this.plugin.getServer().getPlayer(pName);
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        plugin.getMessage(sender.equals(player) ? Lang.COMMAND_BALANCE_DONE_SELF : Lang.COMMAND_BALANCE_DONE_OTHERS)
            .replace(Placeholders.forPlayer(player))
            .replace(Placeholders.GENERIC_AMOUNT, this.currency.format(this.currency.getBalance(player)))
            .send(sender);
    }
}
