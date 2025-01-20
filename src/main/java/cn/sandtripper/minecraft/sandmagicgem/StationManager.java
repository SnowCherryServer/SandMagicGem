package cn.sandtripper.minecraft.sandmagicgem;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class BlockManager {

    private final String GEM_SETTING_NAME = "§d§l宝石镶嵌台";
    private final String[] GEM_SETTING_LORES = {
            "§7右键放置",
    };

    ItemStack gemSettingItemStack;

    private SandMagicGem plugin;

    BlockManager(SandMagicGem plugin) {
        this.plugin = plugin;

        gemSettingItemStack = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta itemMeta = gemSettingItemStack.getItemMeta();
        itemMeta.setDisplayName(GEM_SETTING_NAME);
        itemMeta.setLore(Arrays.asList(GEM_SETTING_LORES));
        gemSettingItemStack.setItemMeta(itemMeta);
    }

    ItemStack getGemSettingItemStack() {
        return gemSettingItemStack.clone();
    }

    boolean isGemSettingItemStack(ItemStack itemStack) {
        return itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(GEM_SETTING_NAME) && itemStack.getItemMeta().getLore().equals(GEM_SETTING_LORES);
    }
}
