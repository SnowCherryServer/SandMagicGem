package cn.sandtripper.minecraft.sandmagicgem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.sandtripper.minecraft.sandmagicgem.InventoryClickListener.InvType.GEMSTONE_SETTING;
import static cn.sandtripper.minecraft.sandmagicgem.InventoryClickListener.InvType.UNKNOWN;


public class InventoryClickListener implements Listener {
    private final SandMagicGem plugin;

    public InventoryClickListener(SandMagicGem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) {
            return;
        }
        InvType invType = getInvType(inventory);
        if (invType != UNKNOWN) {
            switch (invType) {
                case GEMSTONE_SETTING:
                    clickGemstoneSetting(event);
                    break;
            }
        }
    }

    @EventHandler
    public void onContainerClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory == null) {
            return;
        }
        InvType invType = getInvType(inventory);
        if (invType != UNKNOWN) {
            Player player = (Player) event.getPlayer();
            List<ItemStack> itemList = new ArrayList<>();
            switch (invType) {
                case GEMSTONE_SETTING:
                    // 将的物品放回玩家背包
                    for (int i : GlobalData.GemstoneSettingInvInputSlot) {
                        ItemStack item = inventory.getItem(i);
                        if (item != null) {
                            itemList.add(item);
                            inventory.setItem(i, null); // 清空容器中的物品
                        }
                    }
                    break;
            }
            //添加物品到背包
            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(itemList.toArray(new ItemStack[0]));
            // 检查未能被添加的物品
            if (!remainingItems.isEmpty()) {
                // 处理未能被添加的物品
                for (Map.Entry<Integer, ItemStack> entry : remainingItems.entrySet()) {
                    ItemStack remainingItem = entry.getValue();
                    player.getWorld().dropItem(player.getLocation(), remainingItem);
                }
                // 告诉玩家背包已满
                player.sendMessage(plugin.messageManager.backpackFullThingsFallOnGround);
            }
        }
    }

    void clickGemstoneSetting(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (slot < 0 || slot >= GlobalData.GemstoneSettingInvMaterials.length) {
            return;
        }
        Inventory inventory = event.getClickedInventory();
        ItemStack cursorItem = event.getCursor();
        ItemStack clickedItem = inventory.getItem(slot);
        boolean isInputSlot = false;
        for (int i : GlobalData.GemstoneSettingInvInputSlot) {
            if (i == slot) {
                isInputSlot = true;
                break;
            }
        }
        if (isInputSlot) {
            // 将手中的物品放入槽位
            inventory.setItem(slot, cursorItem);
            // 将槽位上的物品放到手中
            event.setCursor(clickedItem);
            int cntInputItem = 0;
            for (int i : GlobalData.GemstoneSettingInvInputSlot) {
                if (inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR) {
                    cntInputItem++;
                }
            }
            if (cntInputItem == GlobalData.GemstoneSettingInvInputSlot.length) {
                ItemStack newItem = plugin.gemManager.GemstoneSet(inventory.getItem(GlobalData.GemstoneSettingInvInputSlot[0]), inventory.getItem(GlobalData.GemstoneSettingInvInputSlot[1]));
                inventory.setItem(GlobalData.GemstoneSettingInvOutputSlot, newItem);
            }
        } else if (slot == GlobalData.GemstoneSettingInvOutputSlot) {
            if ((cursorItem == null || cursorItem.getType() == Material.AIR) && (clickedItem != null && clickedItem.getType() != Material.AIR)) {
                // 将槽位上的物品放到手中
                event.setCursor(clickedItem);
                for (int i : GlobalData.GemstoneSettingInvInputSlot) {
                    inventory.setItem(i, null);
                }
                inventory.setItem(GlobalData.GemstoneSettingInvOutputSlot, null);
            } else {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }


    InvType getInvType(Inventory inventory) {
        if (inventory.getHolder() instanceof GlobalData.GemstoneSettingInventoryHolder) {
            return GEMSTONE_SETTING;
        }
        return UNKNOWN;
    }

    enum InvType {
        UNKNOWN,
        GEMSTONE_SETTING,
    }
}