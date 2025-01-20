package cn.sandtripper.minecraft.sandmagicgem;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GlobalData {
    public static final int[] GemstoneSettingInvInputSlot = {11, 13};
    public static final int GemstoneSettingInvOutputSlot = 15;
    public static final Material[] GemstoneSettingInvMaterials = {
            Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE,
            Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.AIR, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.AIR, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE,
            Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE,
    };
    public static final String GemstoneSettingInvTitle = "§b§l宝石镶嵌台";

    public static class GemstoneSettingInventoryHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }


}
