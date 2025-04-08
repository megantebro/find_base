package lobby.find_base;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class find_base extends JavaPlugin implements Listener {

    private static find_base instance;
    public NamespacedKey find_base_key = new NamespacedKey(this,"find_base");

    public static HashMap<String,Location>bases = new HashMap<>();
    public static List<String> baseKeys;


    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        registerFind_base();
        getServer().getPluginManager().registerEvents(this,this);
        getCommand("set_base").setExecutor(new Commands());
        getCommand("get_findbase").setExecutor(new Commands());


        loadData();
        updateKeyList();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveData(bases);
    }

    private void registerFind_base(){
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "Find_base");

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(find_base_key, PersistentDataType.STRING,"find_base");
        compass.setItemMeta(meta);
    }

    @EventHandler
    public void find_base_interact(PlayerInteractEvent event){
        ItemStack find_base = event.getPlayer().getInventory().getItemInMainHand();
        if(find_base == null||!find_base.hasItemMeta()) return;
        ItemMeta meta = find_base.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if(data.has(find_base_key,PersistentDataType.STRING)
        && "find_base".equals(data.get(find_base_key, PersistentDataType.STRING))){
            CompassMeta compassMeta = (CompassMeta) meta;

            Location currentCompassLoc = compassMeta.getLodestone();
            compassMeta.setLodestoneTracked(false);

            compassMeta.setLodestone(getNextLocation(currentCompassLoc));
            find_base.setItemMeta(compassMeta);


            meta.setDisplayName(ChatColor.GREEN + getBaseNeme(compassMeta.getLodestone()));
            find_base.setItemMeta(meta);


        }
    }

    public Location getNextLocation(Location currentCompassLoc){

        if(currentCompassLoc == null) return bases.get(baseKeys.get(0));

        for(Map.Entry<String,Location> entry: bases.entrySet()){
            if(entry.getValue().getBlockX() == currentCompassLoc.getBlockX() &&
                    entry.getValue().getBlockY() == currentCompassLoc.getBlockY() &&
                    entry.getValue().getBlockZ() == currentCompassLoc.getBlockZ() &&
                    entry.getValue().getWorld().equals(currentCompassLoc.getWorld())){
                int index = baseKeys.indexOf(entry.getKey());
                return bases.get(baseKeys.get((index + 1) % bases.size()));
            }
        }
        return bases.get(baseKeys.get(0));
    }

    public static void addBase(String name,Location loc){
        bases.put(name,loc);
        updateKeyList();
    }

    public String getBaseNeme(Location baseLoc){
        for(String base : bases.keySet()){
            if(bases.get(base).getBlockX() == baseLoc.getBlockX() &&
                    bases.get(base).getBlockY() == baseLoc.getBlockY() &&
                    bases.get(base).getBlockZ() == baseLoc.getBlockZ() &&
                    bases.get(base).getWorld().equals(baseLoc.getWorld())){
                return base;
            }
        }
        return null;
    }

    private static void updateKeyList(){
        baseKeys = new ArrayList<>(bases.keySet());
    }

    public static find_base getInstance(){
        return instance;
    }

    public void saveData(HashMap<String,Location>bases){
        File basesDataFile = new File(getDataFolder(),"bases_data.yml");
        YamlConfiguration customConfig = YamlConfiguration.loadConfiguration(basesDataFile);
        for (Map.Entry<String, Location> entry : bases.entrySet()) {
            String key = entry.getKey();
            Location location = entry.getValue();

            customConfig.set("bases." + key + ".world", location.getWorld().getName());
            customConfig.set("bases." + key + ".x", location.getX());
            customConfig.set("bases." + key + ".y", location.getY());
            customConfig.set("bases." + key + ".z", location.getZ());
        }

        try {
            customConfig.save(basesDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData(){
        File basesDataFile = new File(getDataFolder(),"bases_data.yml");
        if(!basesDataFile.exists()){
            getLogger().warning("base_data.yml が見つかりませんでした。新しいファイルを作成します");
            return;
        }
        FileConfiguration customConfig = YamlConfiguration.loadConfiguration(basesDataFile);

        // "bases"セクションを取得
        if (customConfig.contains("bases")) {
            for (String key : customConfig.getConfigurationSection("bases").getKeys(false)) {
                String worldName = customConfig.getString("bases." + key + ".world");
                double x = customConfig.getDouble("bases." + key + ".x");
                double y = customConfig.getDouble("bases." + key + ".y");
                double z = customConfig.getDouble("bases." + key + ".z");

                Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                bases.put(key, location); // HashMapに追加
            }
        }

        getLogger().info("データを読み込みました: " + bases.size() + " 個のベースがロードされました。");
    }
}
