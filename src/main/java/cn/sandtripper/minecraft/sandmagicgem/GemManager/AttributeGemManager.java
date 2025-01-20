package cn.sandtripper.minecraft.sandmagicgem.GemManager;

import cn.sandtripper.minecraft.sandmagicgem.SandMagicGem;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

import static cn.sandtripper.minecraft.sandmagicgem.GemManager.GemManager.GEM_LEVEL_DISPLAY;
import static cn.sandtripper.minecraft.sandmagicgem.GemManager.GemManager.GEM_LEVEL_NAME;

public class EnchantGemManager {
    private final String GEM_NAME_TEMP = "§7§l§k||{ENCHANT_COLOR}§l{LEVEL_DISPLAY}{ENCHANT_NAME}宝石§7§l§k||";
    private final String[] GEM_LORES_TEMP = {
            "§7品质: {LEVEL_NAME}",
            "§7在宝石镶嵌台内将宝石放到装备上",
            "§7即可将装备{ENCHANT_NAME}等级+1",
            "§7上限 {MAX_LEVEL} 级",
    };
    private SandMagicGem plugin;
    private HashMap<String, EnchantGemConfig> enchantGemConfigs;
    private HashMap<String, List<EnchantGemData>> enchantGemDatas;
    private HashMap<String, List<ItemStack>> enchantGemItemStacks;
    private HashMap<String, String> enchantGemDisplay2id;
    private FileConfiguration config;

    private Random random;

    public EnchantGemManager(SandMagicGem plugin) {
        this.plugin = plugin;
        this.random = new Random(System.currentTimeMillis());
        initData();
    }

    public void reload() {
        initData();
    }

    private void initData() {
        this.config = plugin.getConfig();
        this.enchantGemConfigs = new HashMap<>();
        this.enchantGemDatas = new HashMap<>();
        this.enchantGemDisplay2id = new HashMap<>();
        this.enchantGemItemStacks = new HashMap<>();
        ConfigurationSection enchantConfigsSection = config.getConfigurationSection("enchant-configs");
        if (enchantConfigsSection != null) {
            for (String gemId : enchantConfigsSection.getKeys(false)) {
                ConfigurationSection gemSection = enchantConfigsSection.getConfigurationSection(gemId);
                if (gemSection != null) {
                    EnchantGemConfig enchantGemConfig = new EnchantGemConfig();
                    enchantGemConfig.id = gemId;
                    enchantGemConfig.maxNormalLevel = gemSection.getInt("max-normal-level", 0);
                    enchantGemConfig.maxRareLevel = gemSection.getInt("max-rare-level", 0);
                    enchantGemConfig.maxEpicLevel = gemSection.getInt("max-epic-level", 0);
                    enchantGemConfig.display = gemSection.getString("display", "");
                    enchantGemConfig.color = colorFormat(gemSection.getString("color", "&f"));
                    enchantGemConfig.headUrl = gemSection.getString("head-url", "");
                    enchantGemConfigs.put(gemId, enchantGemConfig);

                    EnchantGemData enchantGemDataNormal = new EnchantGemData();
                    EnchantGemData enchantGemDataRare = new EnchantGemData();
                    EnchantGemData enchantGemDataEpic = new EnchantGemData();

                    enchantGemDataNormal.id = enchantGemDataRare.id = enchantGemDataEpic.id = enchantGemConfig.id;
                    enchantGemDataNormal.headUrl = enchantGemDataRare.headUrl = enchantGemDataEpic.headUrl = enchantGemConfig.headUrl;

                    enchantGemDataNormal.name = GEM_NAME_TEMP.replace("{LEVEL_DISPLAY}", GEM_LEVEL_DISPLAY[0]).replace("{ENCHANT_COLOR}", enchantGemConfig.color).replace("{ENCHANT_NAME}", enchantGemConfig.display);
                    enchantGemDataRare.name = GEM_NAME_TEMP.replace("{LEVEL_DISPLAY}", GEM_LEVEL_DISPLAY[1]).replace("{ENCHANT_COLOR}", enchantGemConfig.color).replace("{ENCHANT_NAME}", enchantGemConfig.display);
                    enchantGemDataEpic.name = GEM_NAME_TEMP.replace("{LEVEL_DISPLAY}", GEM_LEVEL_DISPLAY[2]).replace("{ENCHANT_COLOR}", enchantGemConfig.color).replace("{ENCHANT_NAME}", enchantGemConfig.display);

                    enchantGemDataNormal.lores = new ArrayList<>(Arrays.asList(GEM_LORES_TEMP));
                    enchantGemDataRare.lores = new ArrayList<>(Arrays.asList(GEM_LORES_TEMP));
                    enchantGemDataEpic.lores = new ArrayList<>(Arrays.asList(GEM_LORES_TEMP));

                    for (int i = 0; i < enchantGemDataNormal.lores.size(); i++) {
                        enchantGemDataNormal.lores.set(i, enchantGemDataNormal.lores.get(i).replace("{LEVEL_NAME}", GEM_LEVEL_NAME[0]).replace("{ENCHANT_NAME}", enchantGemConfig.display).replace("{MAX_LEVEL}", String.valueOf(enchantGemConfig.maxNormalLevel)));
                    }
                    for (int i = 0; i < enchantGemDataRare.lores.size(); i++) {
                        enchantGemDataRare.lores.set(i, enchantGemDataRare.lores.get(i).replace("{LEVEL_NAME}", GEM_LEVEL_NAME[1]).replace("{ENCHANT_NAME}", enchantGemConfig.display).replace("{MAX_LEVEL}", String.valueOf(enchantGemConfig.maxRareLevel)));
                    }
                    for (int i = 0; i < enchantGemDataEpic.lores.size(); i++) {
                        enchantGemDataEpic.lores.set(i, enchantGemDataEpic.lores.get(i).replace("{LEVEL_NAME}", GEM_LEVEL_NAME[2]).replace("{ENCHANT_NAME}", enchantGemConfig.display).replace("{MAX_LEVEL}", String.valueOf(enchantGemConfig.maxEpicLevel)));
                    }

                    enchantGemDataNormal.maxLevel = enchantGemConfig.maxNormalLevel;
                    enchantGemDataRare.maxLevel = enchantGemConfig.maxRareLevel;
                    enchantGemDataEpic.maxLevel = enchantGemConfig.maxEpicLevel;

                    enchantGemDatas.put(gemId, Arrays.asList(enchantGemDataNormal, enchantGemDataRare, enchantGemDataEpic));
                    enchantGemDisplay2id.put(enchantGemConfig.display, gemId);
                    enchantGemItemStacks.put(gemId, Arrays.asList(makeItemStack(enchantGemDataNormal), makeItemStack(enchantGemDataRare), makeItemStack(enchantGemDataEpic)));
                }
            }
        }
    }

    public EnchantGemData getEnchantGemData(ItemStack item) {
        if (item == null) {
            return null;
        }
        if (item.getType() != Material.PLAYER_HEAD) {
            return null;
        }

        for (Map.Entry<String, List<EnchantGemData>> gemDataList : enchantGemDatas.entrySet()) {
            for (EnchantGemData enchantGemData : gemDataList.getValue()) {
                if (item.getItemMeta().getDisplayName().equalsIgnoreCase(enchantGemData.name) && item.getItemMeta() != null && item.getItemMeta().getLore().equals(enchantGemData.lores)) {
                    return enchantGemData;
                }
            }
        }
        return null;
    }

    public ItemStack GemstoneSetEnchant(ItemStack input1, ItemStack input2) {
        EnchantGemData enchantGemData = getEnchantGemData(input2);
        if (enchantGemData == null) {
            return null;
        }
        for (Enchantment enchantment : input1.getEnchantments().keySet()) {
            String enchantId = enchantment.getKey().getKey();
            if (enchantGemData.id.equals(enchantId)) {
                int level = input1.getEnchantments().get(enchantment);
                if (level < enchantGemData.maxLevel) {
                    // 将附魔等级加1
                    int newLevel = level + 1;

                    // 创建新的 ItemStack，并应用修改后的附魔
                    ItemStack newStack = input1.clone();
                    ItemMeta itemMeta = newStack.getItemMeta();
                    itemMeta.addEnchant(enchantment, newLevel, true);
                    newStack.setItemMeta(itemMeta);
                    return newStack;
                }
            }
        }
        return null;
    }

    public ItemStack getEnchantGemItemStackByDisplay(String display, int level) {
        if (level >= 3) {
            return null;
        }
        String gemId = enchantGemDisplay2id.get(display);
        List<ItemStack> lst = enchantGemItemStacks.get(gemId);
        ItemStack itemStack = lst.get(level);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setCustomModelData(random.nextInt());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<String> getEnchantGemDisplays() {
        return new ArrayList<>(enchantGemDisplay2id.keySet());
    }

    private String colorFormat(String content) {
        if (content == null) {
            return "";
        }
        return content.replace("&", "§");
    }

    private ItemStack makeItemStack(EnchantGemData enchantGemData) {
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);

        // 获取SkullMeta
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();

        // 创建GameProfile对象
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", enchantGemData.headUrl));

        // 利用反射设置SkullMeta的GameProfile属性
        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        skullMeta.setDisplayName(enchantGemData.name);
        skullMeta.setLore(enchantGemData.lores);
        // 应用SkullMeta到ItemStack
        skullItem.setItemMeta(skullMeta);

        return skullItem;
    }

    private static class EnchantGemConfig {
        public String id;
        public int maxNormalLevel;

        public int maxRareLevel;

        public int maxEpicLevel;

        public String display;

        public String color;

        public String headUrl;
    }

    public static class EnchantGemData {
        public String id;

        public int maxLevel;

        public String name;

        public List<String> lores;

        public String headUrl;
    }
}
