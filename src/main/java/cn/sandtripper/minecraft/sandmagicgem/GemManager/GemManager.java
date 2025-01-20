package cn.sandtripper.minecraft.sandmagicgem;

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


public class GemManager {
    private final String GEM_ENCHANT_NAME_TEMP = "§7§l§k||{ENCHANT_COLOR}§l{LEVEL_DISPLAY}{ENCHANT_NAME}宝石§7§l§k||";

    private final String[] GEM_ENCHANT_LORES_TEMP = {
            "§7品质: {LEVEL_NAME}",
            "§7在宝石镶嵌台内将宝石放到装备上",
            "§7即可将装备{ENCHANT_NAME}等级+1",
            "§7上限 {MAX_LEVEL} 级",
    };

    private final String GEM_UNBREAKABLE_NAME_TEMP = "§7§l§k||{OTHER_COLOR}§l{OTHER_NAME}宝石§7§l§k||";

    private final String[] GEM_UNBREAKABLE_LORES_TEMP = {
            "§7在宝石镶嵌台内将宝石放到装备上",
            "§7即可将装备设置成{OTHER_NAME}",
    };

    private final String[] GEM_LEVEL_NAME = {
            "§f普通",
            "§b稀有",
            "§d史诗",
    };

    private final String[] GEM_LEVEL_DISPLAY = {
            "",
            "幻·",
            "圣·",
    };

    private HashMap<String, GemConfig> gemConfigs;

    private HashMap<String, List<GemData>> gemDatas;

    private HashMap<String, List<ItemStack>> gemItemStacks;

    private HashMap<String, String> gemDisplay2id;

    private Random random;
    private SandMagicGem plugin;
    private FileConfiguration config;

    GemManager(SandMagicGem plugin) {
        this.plugin = plugin;
        this.random = new Random(System.currentTimeMillis());
        initData();
    }

    public void initData() {
        this.config = plugin.getConfig();
        gemConfigs = new HashMap<>();
        gemDatas = new HashMap<>();
        gemDisplay2id = new HashMap<>();
        gemItemStacks = new HashMap<>();
        ConfigurationSection enchantConfigsSection = config.getConfigurationSection("enchant-configs");
        if (enchantConfigsSection != null) {
            for (String gemId : enchantConfigsSection.getKeys(false)) {
                ConfigurationSection gemSection = enchantConfigsSection.getConfigurationSection(gemId);
                if (gemSection != null) {
                    GemConfig gemConfig = new GemConfig();
                    gemConfig.id = gemId;
                    gemConfig.maxNormalLevel = gemSection.getInt("max-normal-level", 0);
                    gemConfig.maxRareLevel = gemSection.getInt("max-rare-level", 0);
                    gemConfig.maxEpicLevel = gemSection.getInt("max-epic-level", 0);
                    gemConfig.display = gemSection.getString("display", "");
                    gemConfig.color = colorFormat(gemSection.getString("color", "&f"));
                    gemConfig.headUrl = gemSection.getString("head-url", "");
                    gemConfigs.put(gemId, gemConfig);

                    GemData gemDataNormal = new GemData();
                    GemData gemDataRare = new GemData();
                    GemData gemDataEpic = new GemData();

                    gemDataNormal.id = gemDataRare.id = gemDataEpic.id = gemConfig.id;
                    gemDataNormal.headUrl = gemDataRare.headUrl = gemDataEpic.headUrl = gemConfig.headUrl;
                    gemDataNormal.type = gemDataRare.type = gemDataEpic.type = GEM_TYPE.ENCHANT;

                    gemDataNormal.name = GEM_ENCHANT_NAME_TEMP.replace("{LEVEL_DISPLAY}", GEM_LEVEL_DISPLAY[0]).replace("{ENCHANT_COLOR}", gemConfig.color).replace("{ENCHANT_NAME}", gemConfig.display);
                    gemDataRare.name = GEM_ENCHANT_NAME_TEMP.replace("{LEVEL_DISPLAY}", GEM_LEVEL_DISPLAY[1]).replace("{ENCHANT_COLOR}", gemConfig.color).replace("{ENCHANT_NAME}", gemConfig.display);
                    gemDataEpic.name = GEM_ENCHANT_NAME_TEMP.replace("{LEVEL_DISPLAY}", GEM_LEVEL_DISPLAY[2]).replace("{ENCHANT_COLOR}", gemConfig.color).replace("{ENCHANT_NAME}", gemConfig.display);

                    gemDataNormal.lores = new ArrayList<>(Arrays.asList(GEM_ENCHANT_LORES_TEMP));
                    gemDataRare.lores = new ArrayList<>(Arrays.asList(GEM_ENCHANT_LORES_TEMP));
                    gemDataEpic.lores = new ArrayList<>(Arrays.asList(GEM_ENCHANT_LORES_TEMP));

                    for (int i = 0; i < gemDataNormal.lores.size(); i++) {
                        gemDataNormal.lores.set(i, gemDataNormal.lores.get(i).replace("{LEVEL_NAME}", GEM_LEVEL_NAME[0]).replace("{ENCHANT_NAME}", gemConfig.display).replace("{MAX_LEVEL}", String.valueOf(gemConfig.maxNormalLevel)));
                    }
                    for (int i = 0; i < gemDataRare.lores.size(); i++) {
                        gemDataRare.lores.set(i, gemDataRare.lores.get(i).replace("{LEVEL_NAME}", GEM_LEVEL_NAME[1]).replace("{ENCHANT_NAME}", gemConfig.display).replace("{MAX_LEVEL}", String.valueOf(gemConfig.maxRareLevel)));
                    }
                    for (int i = 0; i < gemDataEpic.lores.size(); i++) {
                        gemDataEpic.lores.set(i, gemDataEpic.lores.get(i).replace("{LEVEL_NAME}", GEM_LEVEL_NAME[2]).replace("{ENCHANT_NAME}", gemConfig.display).replace("{MAX_LEVEL}", String.valueOf(gemConfig.maxEpicLevel)));
                    }

                    gemDataNormal.maxLevel = gemConfig.maxNormalLevel;
                    gemDataRare.maxLevel = gemConfig.maxRareLevel;
                    gemDataEpic.maxLevel = gemConfig.maxEpicLevel;

                    gemDatas.put(gemId, Arrays.asList(gemDataNormal, gemDataRare, gemDataEpic));
                    gemDisplay2id.put(gemConfig.display, gemId);
                    gemItemStacks.put(gemId, Arrays.asList(makeItemStack(gemDataNormal), makeItemStack(gemDataRare), makeItemStack(gemDataEpic)));
                }
            }
        }
    }

    public void reload() {

        initData();
    }

    public ItemStack GemstoneSet(ItemStack input1, ItemStack input2) {
        GemData gemData = getGemData(input2);
        if (gemData == null) {
            return null;
        }
        switch (gemData.type) {
            case ENCHANT:
                for (Enchantment enchantment : input1.getEnchantments().keySet()) {
                    String enchantId = enchantment.getKey().getKey();
                    if (gemData.id.equals(enchantId)) {
                        int level = input1.getEnchantments().get(enchantment);
                        if (level < gemData.maxLevel) {
                            // 将附魔等级加1
                            int newLevel = level + 1;

                            // 创建新的 ItemStack，并应用修改后的附魔
                            ItemStack newStack = input1.clone();
                            ItemMeta itemMeta = newStack.getItemMeta();
                            itemMeta.addEnchant(enchantment, newLevel, true);
                            return newStack;
                        }
                    }
                }

                break;
        }
        return null;
    }

    public GemData getGemData(ItemStack item) {
        if (item == null) {
            return null;
        }
        if (item.getType() != Material.PLAYER_HEAD) {
            return null;
        }

        for (Map.Entry<String, List<GemData>> gemDataList : gemDatas.entrySet()) {
            for (GemData gemData : gemDataList.getValue()) {
                if (item.getItemMeta().getDisplayName().equalsIgnoreCase(gemData.name) && item.getItemMeta() != null && item.getItemMeta().getLore().equals(gemData.lores)) {
                    return gemData;
                }
            }
        }
        return null;
    }

    public ItemStack getGemItemStackByDisplay(String display, int level) {
        if (level >= 3) {
            return null;
        }
        String gemId = gemDisplay2id.get(display);
        List<ItemStack> lst = gemItemStacks.get(gemId);
        ItemStack itemStack = lst.get(level);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setCustomModelData(random.nextInt());
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private String colorFormat(String content) {
        if (content == null) {
            return "";
        }
        return content.replace("&", "§");
    }

    public List<String> getGemDisplays() {
        return new ArrayList<>(gemDisplay2id.keySet());
    }

    private ItemStack makeItemStack(GemData gemData) {

        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);

        // 获取SkullMeta
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();

        // 创建GameProfile对象
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", gemData.headUrl));

        // 利用反射设置SkullMeta的GameProfile属性
        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        skullMeta.setDisplayName(gemData.name);
        skullMeta.setLore(gemData.lores);
        // 应用SkullMeta到ItemStack
        skullItem.setItemMeta(skullMeta);

        return skullItem;
    }

    enum GEM_TYPE {
        ENCHANT,
        ATTRIBUTE,
        BUFF,
        OTHER,
    }

    private static class GemConfig {
        public String id;
        public int maxNormalLevel;

        public int maxRareLevel;

        public int maxEpicLevel;

        public String display;

        public String color;

        public String headUrl;
    }

    public static class GemData {
        public String id;

        public GEM_TYPE type;

        public int maxLevel;

        public String name;

        public List<String> lores;

        public String headUrl;
    }
}
