package su.nightexpress.ama.mob.editor;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.Menu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.ama.AMA;
import su.nightexpress.ama.config.Lang;
import su.nightexpress.ama.editor.EditorHub;
import su.nightexpress.ama.editor.EditorLocales;
import su.nightexpress.ama.mob.config.MobConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MobMainEditor extends EditorMenu<AMA, MobConfig> {

    private MobStylesEditor mobStylesEditor;

    public MobMainEditor(@NotNull MobConfig mob) {
        super(mob.plugin(), mob, EditorHub.TITLE_MOB_EDITOR, 45);

        this.addReturn(40).setClick((viewer, event) -> {
            plugin.getEditor().getMobEditor().openNextTick(viewer, 1);
        });

        this.addItem(Material.NAME_TAG, EditorLocales.MOB_NAME, 10).setClick((viewer, event) -> {
            if (event.isRightClick()) {
                mob.setNameVisible(!mob.isNameVisible());
                this.save(viewer);
                return;
            }

            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_NAME, wrapper -> {
                mob.setName(wrapper.getText());
                mob.save();
                return true;
            });
        });

        this.addItem(Material.CREEPER_SPAWN_EGG, EditorLocales.MOB_ENTITY_TYPE, 11).setClick((viewer, event) -> {
            EditorManager.suggestValues(viewer.getPlayer(), Stream.of(EntityType.values())
                .filter(EntityType::isSpawnable).filter(EntityType::isAlive).map(Enum::name).toList(), true);

            this.handleInput(viewer, Lang.Editor_Mob_Enter_Type, wrapper -> {
                EntityType entityType = StringUtil.getEnum(wrapper.getTextRaw(), EntityType.class).orElse(null);
                if (entityType == null || !entityType.isSpawnable() || !entityType.isAlive()) {
                    return false;
                }
                mob.setEntityType(entityType);
                mob.save();
                return true;
            });
        });

        this.addItem(Material.EXPERIENCE_BOTTLE, EditorLocales.MOB_LEVEL, 12).setClick((viewer, event) -> {
            this.handleInput(viewer, Lang.Editor_Mob_Enter_Level, wrapper -> {
                if (event.isLeftClick()) {
                    mob.setLevelMin(wrapper.asInt(1));
                }
                else {
                    mob.setLevelMax(wrapper.asInt(1));
                }
                mob.save();
                return true;
            });
        });

        this.addItem(Material.MAP, EditorLocales.MOB_BOSSBAR, 13).setClick((viewer, event) -> {
            if (event.isShiftClick()) {
                if (event.isLeftClick()) {
                    mob.setBarColor(CollectionsUtil.next(mob.getBarColor()));
                }
                else if (event.isRightClick()) {
                    mob.setBarStyle(CollectionsUtil.next(mob.getBarStyle()));
                }
            }
            else {
                if (event.isLeftClick()) {
                    mob.setBarEnabled(!mob.isBarEnabled());
                }
                else if (event.isRightClick()) {
                    this.handleInput(viewer, Lang.Editor_Mob_Enter_BossBar_Title, wrapper -> {
                        mob.setBarTitle(wrapper.getText());
                        mob.save();
                        return true;
                    });
                    return;
                }
            }
            this.save(viewer);
        });

        this.addItem(Material.APPLE, EditorLocales.MOB_ATTRIBUTES, 14).setClick((viewer, event) -> {
            if (event.isShiftClick()) {
                if (event.isLeftClick()) {
                    mob.getAttributes().clear();
                    this.save(viewer);
                }
                return;
            }

            EditorManager.suggestValues(viewer.getPlayer(), Stream.of(Attribute.values()).map(Enum::name).toList(), false);
            this.handleInput(viewer, Lang.Editor_Mob_Enter_Attribute, wrapper -> {
                String[] split = wrapper.getTextRaw().split(" ");
                if (split.length != 2) return false;

                Attribute attribute = StringUtil.getEnum(split[0], Attribute.class).orElse(null);
                if (attribute == null) return false;

                double value = StringUtil.getDouble(split[1], 0D);
                double[] valuesHas = mob.getAttributes().computeIfAbsent(attribute, k -> new double[2]);
                int index = event.isLeftClick() ? 0 : 1;
                valuesHas[index] = value;
                mob.getAttributes().put(attribute, valuesHas);
                mob.save();
                return true;
            });
        });

        this.addItem(Material.ARMOR_STAND, EditorLocales.MOB_EQUIPMENT, 15).setClick((viewer, event) -> {
            new EquipmentEditor(mob).openNextTick(viewer, 1);
        });

        this.addItem(Material.ORANGE_DYE, EditorLocales.MOB_STYLES, 16).setClick((viewer, event) -> {
            this.getEditorMobStyles().openNextTick(viewer, 1);
        });

        this.getItems().forEach(menuItem -> {
            menuItem.getOptions().addDisplayModifier((viewer, item) -> ItemUtil.replace(item, mob.replacePlaceholders()));
        });
    }

    private void save(@NotNull MenuViewer viewer) {
        this.object.save();
        this.openNextTick(viewer, viewer.getPage());
    }

    @NotNull
    public MobStylesEditor getEditorMobStyles() {
        if (this.mobStylesEditor == null) {
            this.mobStylesEditor = new MobStylesEditor(plugin, this.object);
        }
        return mobStylesEditor;
    }

    private static class EquipmentEditor extends Menu<AMA> {

        private final MobConfig                   mob;
        private final Map<EquipmentSlot, Integer> equipmentSlots;

        public EquipmentEditor(@NotNull MobConfig mob) {
            super(mob.plugin(), EditorHub.TITLE_MOB_EDITOR, 9);
            this.mob = mob;
            this.equipmentSlots = new HashMap<>();
            this.equipmentSlots.put(EquipmentSlot.FEET, 0);
            this.equipmentSlots.put(EquipmentSlot.LEGS, 1);
            this.equipmentSlots.put(EquipmentSlot.CHEST, 2);
            this.equipmentSlots.put(EquipmentSlot.HEAD, 3);
            this.equipmentSlots.put(EquipmentSlot.HAND, 4);
            this.equipmentSlots.put(EquipmentSlot.OFF_HAND, 5);
        }

        @Override
        public boolean isPersistent() {
            return false;
        }

        @Nullable
        private EquipmentSlot getTypeBySlot(int slot) {
            return this.equipmentSlots.entrySet().stream().filter(entry -> entry.getValue() == slot).findFirst()
                .map(Map.Entry::getKey).orElse(null);
        }

        private void saveEquipment(@NotNull Player player, @NotNull Inventory inventory) {
            this.equipmentSlots.forEach((equipmentSlot, slot) -> {
                this.mob.setEquipment(equipmentSlot, inventory.getItem(slot));
            });
            this.mob.save();
        }

        @Override
        public void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
            super.onReady(viewer, inventory);
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                inventory.setItem(this.equipmentSlots.getOrDefault(equipmentSlot, 0), this.mob.getEquipment(equipmentSlot));
            }
        }

        @Override
        public void onClick(@NotNull MenuViewer viewer, @Nullable ItemStack item, @NotNull SlotType slotType, int slot, @NotNull InventoryClickEvent event) {
            super.onClick(viewer, item, slotType, slot, event);
            event.setCancelled(slotType != SlotType.PLAYER && slotType != SlotType.PLAYER_EMPTY && this.getTypeBySlot(event.getRawSlot()) == null);
        }

        @Override
        public void onDrag(@NotNull MenuViewer viewer, @NotNull InventoryDragEvent event) {
            super.onDrag(viewer, event);
            event.setCancelled(event.getRawSlots().stream().anyMatch(slot -> this.getTypeBySlot(slot) == null));
        }

        @Override
        public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
            this.saveEquipment(viewer.getPlayer(), event.getInventory());
            this.mob.getEditor().openNextTick(viewer, 1);
            super.onClose(viewer, event);
        }
    }
}
