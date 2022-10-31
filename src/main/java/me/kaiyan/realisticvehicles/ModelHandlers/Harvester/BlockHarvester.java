package me.kaiyan.realisticvehicles.ModelHandlers.Harvester;

import me.kaiyan.realisticvehicles.RealisticVehicles;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BlockHarvester implements Cloneable{
    private final List<Vector> breakPoints = new ArrayList<>();
    private final List<Vector> placePoints = new ArrayList<>();

    private final List<Material> breakAbles = new ArrayList<>();
    private final List<Material> placeAbles = new ArrayList<>();

    private final Vector depositPoint;

    private Inventory inv;
    private List<Inventory> trailerInventories = null;

    public BlockHarvester(Vector depositPoint) {
        this.depositPoint = depositPoint;
        this.inv = Bukkit.createInventory(null, 54, "Vehicle Inventory");
    }

    public void generateInventory(Inventory... invs){
        trailerInventories = new ArrayList<>();
        trailerInventories.addAll(Arrays.stream(invs).toList());
    }

    public void onExit(Location loc){
        for (ItemStack item : inv.getContents()) {
            loc.getWorld().dropItem(loc, item);
        }
    }

    public void flashPoints(Location loc, float yaw){
        for (Vector vec : breakPoints){
            RealisticVehicles.spawnParticle(loc.clone().add(vec.clone().rotateAroundY(Math.toRadians(yaw))), new Particle.DustOptions(Color.RED, 3));
        }
        for (Vector vec : placePoints){
            RealisticVehicles.spawnParticle(loc.clone().add(vec.clone().rotateAroundY(Math.toRadians(yaw))), new Particle.DustOptions(Color.GREEN, 3));
        }
        RealisticVehicles.spawnParticle(loc.clone().add(depositPoint.clone().rotateAroundY(Math.toRadians(yaw))), new Particle.DustOptions(Color.ORANGE, 3));
    }

    public void addBreakPoints(Vector... points){
        for (Vector vec : points){
            vec.add(new Vector(0.5, 0.5, 0.5));
        }
        breakPoints.addAll(Arrays.stream(points).toList());
    }

    public void addPlacePoints(Vector... points){
        for (Vector vec : points){
            vec.add(new Vector(0.5, 0.5, 0.5));
        }
        placePoints.addAll(Arrays.stream(points).toList());
    }

    public void addBreakables(Material... mats){
        breakAbles.addAll(Arrays.stream(mats).toList());
    }

    public void addPlaceables(Material... mats){
        placeAbles.addAll(Arrays.stream(mats).toList());
    }

    public void update(Location loc, float yaw, boolean placing){
        World world = loc.getWorld();
        for (Vector vec : breakPoints){
            Block block = loc.clone().add(vec.clone().rotateAroundY(Math.toRadians(yaw))).getBlock();
            if (breakAbles.contains(block.getType())){
                if (inv == null){
                    world.dropItem(loc.clone().add(depositPoint.clone().rotateAroundY(Math.toRadians(yaw))), new ItemStack(block.getType(), 1));
                    break;
                }
                boolean found = false;
                if (trailerInventories == null) {
                    for (int i = 0; i < inv.getContents().length; i++) {
                        if (inv.getContents()[i] == null) {
                            ItemStack item = new ItemStack(block.getType(), 1);
                            inv.setItem(i, item);
                            found = true;
                            block.setType(Material.AIR);
                            world.playSound(loc, block.getBlockData().getSoundGroup().getBreakSound(), 1, 1);
                            world.spawnParticle(Particle.BLOCK_CRACK, block.getBoundingBox().getCenter().toLocation(world), 30, 1, 1, 1, block.getBlockData());
                            System.out.println("Broke Other");
                            break;
                        }
                        if (inv.getContents()[i].getType() == block.getType() && !(inv.getContents()[i].getAmount() + 1 > 64)) {
                            ItemStack item = inv.getContents()[i];
                            item.setAmount(item.getAmount() + 1);
                            inv.setItem(i, item);
                            block.setType(Material.AIR);
                            world.playSound(loc, block.getBlockData().getSoundGroup().getBreakSound(), 1, 1);
                            world.spawnParticle(Particle.BLOCK_CRACK, block.getBoundingBox().getCenter().toLocation(world), 30, 1, 1, 1, block.getBlockData());
                            found = true;
                            System.out.println("Broke Inv");
                            break;
                        }
                    }
                } else {
                    for (Inventory inv : trailerInventories){
                        for (int i = 0; i < inv.getContents().length; i++) {
                            if (inv.getContents()[i] == null) {
                                ItemStack item = new ItemStack(block.getType(), 1);
                                inv.setItem(i, item);
                                found = true;
                                block.setType(Material.AIR);
                                world.playSound(loc, block.getBlockData().getSoundGroup().getBreakSound(), 1, 1);
                                world.spawnParticle(Particle.BLOCK_CRACK, block.getBoundingBox().getCenter().toLocation(world), 30, 1, 1, 1, block.getBlockData());
                                System.out.println("Broke Other");
                                break;
                            }
                            if (inv.getContents()[i].getType() == block.getType() && !(inv.getContents()[i].getAmount() + 1 > 64)) {
                                ItemStack item = inv.getContents()[i];
                                item.setAmount(item.getAmount() + 1);
                                inv.setItem(i, item);
                                block.setType(Material.AIR);
                                world.playSound(loc, block.getBlockData().getSoundGroup().getBreakSound(), 1, 1);
                                world.spawnParticle(Particle.BLOCK_CRACK, block.getBoundingBox().getCenter().toLocation(world), 30, 1, 1, 1, block.getBlockData());
                                found = true;
                                System.out.println("Broke Inv");
                                break;
                            }
                        }
                    }
                }
                if (!found){
                    System.out.println("Dropping");
                    world.dropItem(loc.clone().add(depositPoint.clone().rotateAroundY(Math.toRadians(yaw))), new ItemStack(block.getType(), 1));
                }
            }
        }
        if (!placing){
            return;
        }
        for (Vector vec : placePoints){
            Block block = loc.clone().add(vec.clone().rotateAroundY(Math.toRadians(yaw))).getBlock();
            Optional<ItemStack> opitem = Arrays.stream(inv.getContents()).filter((oitem) -> placeAbles.contains(oitem.getType())).findFirst();
            if (opitem.isEmpty()){
                continue;
            }
            ItemStack item = opitem.get();
            if (placeAbles.contains(item.getType())) {
                if (item == null) {
                    return;
                }
                if (item.getAmount() - 1 != 0) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    item.setAmount(0);
                    inv.remove(item);
                }
                block.setType(item.getType());
                return;
            }
        }
    }

    public Inventory getInv(){
        return inv;
    }

    public void setInv(Inventory inv) {this.inv = inv;}

    public BlockHarvester clone(){
        try {
            BlockHarvester harvester = (BlockHarvester) super.clone();
            harvester.inv = Bukkit.createInventory(null, 54, "Vehicle Inventory");
            return harvester;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
