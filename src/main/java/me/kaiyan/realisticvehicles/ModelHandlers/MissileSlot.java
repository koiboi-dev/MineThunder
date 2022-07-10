package me.kaiyan.realisticvehicles.ModelHandlers;

import me.kaiyan.realisticvehicles.DataTypes.MissileSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Objects;

public class MissileSlot {
    private final Vector pos;
    private ArmorStand stand;
    private MissileSettings settings;

    public MissileSlot(Vector pos, ArmorStand stand, MissileSettings settings) {
        this.pos = pos;
        this.stand = stand;
        this.settings = settings;
    }

    public void generateArmourStand(Location loc){
        if (stand != null){
            stand.setInvulnerable(false);
            stand.damage(stand.getHealth());
        }
        stand = (ArmorStand) Objects.requireNonNull(loc.getWorld()).spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        ItemStack item = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setCustomModelData(settings.getTexID());
        item.setItemMeta(meta);
        Objects.requireNonNull(stand.getEquipment()).setHelmet(item);
    }

    public Vector getPos() {
        return pos;
    }

    public ArmorStand getStand() {
        return stand;
    }

    public void setStand(ArmorStand stand) {
        this.stand = stand;
    }

    public MissileSettings getSettings() {
        return settings;
    }

    public void setSettings(MissileSettings settings) {
        this.settings = settings;
    }
}
