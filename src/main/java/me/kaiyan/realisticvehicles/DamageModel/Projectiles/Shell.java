package me.kaiyan.realisticvehicles.DamageModel.Projectiles;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Shell {
    public double penScore;
    public double shellDamage;
    public boolean explosive;
    public boolean capped;
    public boolean sabot;
    public boolean heat;

    public ItemStack item;
    public List<String> shellLore;
    public float power;
    public boolean tracer;

    public final float reloadTime;
    public float cost;

    public double weaknessDamage;
    public boolean nudged;

    public final int buyAmount;

    /**Creates a false shell, used to get pass JSON's NaN restrictions.
    */
    public Shell(){
        reloadTime = 1;
        buyAmount = 100;
    }

    /**
     * Creates a new shell
     * @param power Power to fire at (Speed)
     * @param tracer Red or White?
     * @param penScore Penetration score
     * @param shellDamage Damage Score
     * @param capped Capped?
     * @param sabot Sabot round?
     * @param explosive Explosive?
     * @param heat HEAT Round?
     * @param reloadTime Reload Time
     * @param cost Cost
     * @param weaknessDamage Damage to apply to plates
     * @param buyAmount Amount to buy
     */
    public Shell(double penScore, double shellDamage, boolean capped, boolean sabot, boolean explosive, boolean heat, Material itemIcon, List<String> shellLore, float power, boolean tracer, float reloadTime, float cost, double weaknessDamage, int buyAmount) {
        this.penScore = penScore;
        this.shellDamage = shellDamage;
        this.capped = capped;
        this.sabot = sabot;
        this.heat = heat;
        this.shellLore = shellLore;
        this.explosive = explosive;
        this.power = power;
        this.tracer = tracer;
        this.cost = cost;
        this.weaknessDamage = weaknessDamage;
        this.buyAmount = buyAmount;

        item = new ItemStack(itemIcon);
        this.reloadTime = reloadTime;
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD+getAbbreviation());
        List<String> out = new ArrayList<>();
        for (String str : shellLore) {
            out.add(ChatColor.translateAlternateColorCodes('&', str));
        }
        shellLore = out;
        meta.setLore(shellLore);

        item.setItemMeta(meta);
    }

    public String getAbbreviation(){
        StringBuilder out = new StringBuilder();
        if (penScore > 3){
            out.append("AP");
        } else {
            out.append("SAP");
        }
        if (explosive){
            out.append("HE");
        }
        if (capped){
            out.append("BC");
        }
        if (sabot){
            out.append("DS");
        }
        if (heat){
            out.append("HEAT");
        }
        return out.toString();
    }
}
