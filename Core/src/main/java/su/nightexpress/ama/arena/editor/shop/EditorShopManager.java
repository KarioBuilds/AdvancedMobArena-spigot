package su.nightexpress.ama.arena.editor.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.AbstractEditorMenu;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.arena.shop.ArenaShopManager;
import su.nightexpress.ama.editor.ArenaEditorType;
import su.nightexpress.ama.editor.ArenaEditorUtils;

import java.util.Map;

public class EditorShopManager extends AbstractEditorMenu<AMA, ArenaShopManager> {

    private EditorShopCategoryList editorCategories;

    public EditorShopManager(@NotNull ArenaShopManager shopManager) {
        super(shopManager.plugin(), shopManager, ArenaEditorUtils.TITLE_SHOP_EDITOR, 45);

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    shopManager.getArenaConfig().getEditor().open(player, 1);
                }
            }
            else if (type instanceof ArenaEditorType type2) {
                switch (type2) {
                    case SHOP_OPEN_CATEGORIES -> {
                        this.getEditorCategories().open(player, 1);
                        return;
                    }
                    case SHOP_CHANGE_TRIGGERS_LOCKED, SHOP_CHANGE_TRIGGERS_UNLOCKED -> {
                        ArenaEditorUtils.handleTriggersClick(player, shopManager, type2, e.isRightClick());
                        if (e.isRightClick()) break;
                        return;
                    }
                    case SHOP_CHANGE_ACTIVE -> shopManager.setActive(!shopManager.isActive());
                    case SHOP_CHANGE_HIDE_OTHER_KIT_ITEMS -> shopManager.setHideOtherKitProducts(!shopManager.isHideOtherKitProducts());
                    default -> {
                        return;
                    }
                }
                shopManager.save();
                this.open(player, 1);
            }
        };

        this.loadItems(click);
    }

    @Override
    public void clear() {
        if (this.editorCategories != null) {
            this.editorCategories.clear();
            this.editorCategories = null;
        }
        super.clear();
    }

    @NotNull
    public EditorShopCategoryList getEditorCategories() {
        if (this.editorCategories == null) {
            this.editorCategories = new EditorShopCategoryList(this.object);
        }
        return this.editorCategories;
    }

    @Override
    public void setTypes(@NotNull Map<EditorButtonType, Integer> map) {
        map.put(ArenaEditorType.SHOP_CHANGE_ACTIVE, 4);
        map.put(ArenaEditorType.SHOP_OPEN_CATEGORIES, 22);
        map.put(ArenaEditorType.SHOP_CHANGE_TRIGGERS_LOCKED, 20);
        map.put(ArenaEditorType.SHOP_CHANGE_TRIGGERS_UNLOCKED, 24);
        map.put(MenuItemType.RETURN, 40);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        if (menuItem.getType() instanceof ArenaEditorType editorType) {
            if (editorType == ArenaEditorType.SHOP_CHANGE_ACTIVE) {
                item.setType(this.object.isActive() ? Material.LIME_DYE : Material.GRAY_DYE);
            }
        }
        ItemUtil.replace(item, this.object.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
