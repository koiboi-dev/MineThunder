package me.kaiyan.realisticvehicles.DamageModel;

import me.kaiyan.realisticvehicles.DamageModel.Dimensions.Rect;
import me.kaiyan.realisticvehicles.DamageModel.Dimensions.VectorD;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.ArmourPlate;
import me.kaiyan.realisticvehicles.DamageModel.Hitboxes.Component;
import me.kaiyan.realisticvehicles.DamageModel.Projectiles.Shell;
import me.kaiyan.realisticvehicles.DataTypes.Enums.ComponentType;
import me.kaiyan.realisticvehicles.DataTypes.ImpactOutData;
import me.kaiyan.realisticvehicles.DataTypes.Interfaces.VehicleInterface;
import me.kaiyan.realisticvehicles.RealisticVehicles;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class DamageModel implements Cloneable{
    private List<ArmourPlate> armour;
    private List<Component> components;

    private final HashMap<ComponentType, List<Integer>> compMap = new HashMap<>();

    public static final double randSpeed = 0.05;

    @Nonnull
    private final Rect lowerHalf;
    @Nullable
    private final Rect upperHalf;

    private final float collisionSphere;

    private boolean finished = false;

    /**
     * Creates a damagemodel class
     * @param lowerHalf Lower hit box
     * @param upperHalf Upper hit box, Null if no turret
     */
    public DamageModel(@Nonnull Rect lowerHalf, @Nullable Rect upperHalf, float collisionSphere){
        armour = new ArrayList<>();
        components = new ArrayList<>();
        this.lowerHalf = lowerHalf;
        this.upperHalf = upperHalf;
        this.collisionSphere = collisionSphere;
    }

    public void finish(){
        setQuickComps();
        finished = true;
    }

    public void setQuickComps(){
        int loops = 0;
        for (Component comp : components){
            if (compMap.containsKey(comp.type)){
                compMap.get(comp.type).add(loops);
            } else {
                List<Integer> ints = new ArrayList<>();
                ints.add(loops);
                compMap.put(comp.type, ints);
            }
            loops++;
        }
    }
    public static final int maxLoops = 80;
    public ImpactOutData shellImpact(Shell shell, Location centerloc, Location shellloc, float vehicleyaw, float turretyaw, float yaw, float pitch, World world, int override, boolean sendResult, Player player){
        return shellImpact(shell, centerloc.getX(), centerloc.getY(), centerloc.getZ(), shellloc.getX(), shellloc.getY(), shellloc.getZ(), vehicleyaw, turretyaw, yaw, pitch, world, override, sendResult, player);
    }
    Random rand = new Random();
    /**
     * Simulates a shell hitting the target @class DamageModel
     * @return Returns the ammo index of the target destroyed.
     */
    public ImpactOutData shellImpact(Shell shell, final double x, final double y, final double z, final double vx, final double vy, final double vz, final float vehicleyaw, final float turretyaw, float yaw, float pitch, @Nonnull World world, int override, boolean sendResult, Player player){
        if (!finished){
            Bukkit.getLogger().severe("DAMAGED MODEL CALLED WITHOUT BEING FINISHED, ABORTING OPERATION.\nThis could be due to a improperly generated damage model\nFOR DEVELOPERS: try calling DamageModel.finished() on the damagemodel");
            return new ImpactOutData(-1, -1);
        }
        ImpactOutData data = new ImpactOutData(-1, -1);

        double mx = vx-x;
        //System.out.println(mx + " | "+x+" - "+vx);
        double my = vy-y;
        //System.out.println(my + " | "+y+" - "+vy);
        double mz = vz-z;
        //System.out.println(mz + " | "+z+" - "+vz);

        boolean isUpperHalf = isInUpperHalf(new VectorD(mx, my, mz));

        double myaw;
        double vyaw;

        if (override == 0) {
            if (isUpperHalf) {
                myaw = yaw - turretyaw;
                vyaw = turretyaw;
            } else {
                myaw = yaw - vehicleyaw;
                vyaw = vehicleyaw;
            }
        } else if (override == 1){
            myaw = yaw - turretyaw;
            vyaw = turretyaw;
        } else {
            myaw = yaw - vehicleyaw;
            vyaw = vehicleyaw;
        }

        vyaw = Math.toRadians(vyaw);

        //double dist = Math.sqrt(mx*mx+mz*mz);

        Vector startPoint = new Vector(mx, my, mz);
        startPoint.rotateAroundY(vyaw);

        //System.out.println("Prev : "+mx+","+my+","+mz);

        RealisticVehicles.debugLog(myaw + " |Ve "+vehicleyaw+" |Tu "+turretyaw + " | Org " + yaw);
        RealisticVehicles.debugLog(pitch);

        double vert = -Math.sin(Math.toRadians(pitch));
        double mult = 1-(Math.abs(pitch)/90);
        double yawx = -Math.sin(Math.toRadians(myaw))*mult;
        double yawz = Math.cos(Math.toRadians(myaw))*mult;

        RealisticVehicles.debugLog("Mult! "+mult);

        VectorD coords = new VectorD (startPoint.getX(), startPoint.getY(), startPoint.getZ());
        RealisticVehicles.debugLog(coords);
        VectorD modCoords = coords.clone();

        VectorD castBy = new VectorD(yawx, vert, yawz);
        RealisticVehicles.debugLog(castBy);
        castBy.multiply(0.1);
        RealisticVehicles.debugLog("Mod: "+castBy);
        RealisticVehicles.debugLog("IsUpper: "+isUpperHalf);

        boolean penningArmor = false;
        boolean pennedArmour = false;
        boolean hitComponent = false;
        boolean destroyComponent = false;
        boolean heatFired = false;
        Random rand = new Random();
        int finLoops = 0;
        //START
        for (int loops = 0; loops < maxLoops; loops++) {
            boolean hitPlate = false;
            VectorD point = modCoords.add(castBy);
            RealisticVehicles.debugLog("Shell: "+point);

            Vector loc = point.toVector();
            loc.rotateAroundY(-vyaw);
            loc.add(new Vector(x, y, z));

            if (loc.toLocation(world).getBlock().getType().isSolid()){
                world.createExplosion(loc.toLocation(world), 2);
                finLoops = loops;
                break;
            }

            for (ArmourPlate plate : armour){
                if (plate.isPointInsideBox(point.x, point.y, point.z)){
                    RealisticVehicles.debugLog("Hit Plate");
                    penningArmor = true;
                    hitPlate = true;
                    world.playSound(loc.toLocation(world), Sound.ENTITY_ITEM_BREAK, 2f, 0.65f);
                    // 70% chance to apply damage
                    if (!shell.heat) {
                        if (!shell.sabot) {
                            if (Math.round(rand.nextDouble() * 10) < 7) {
                                shell.penScore -= plate.penDef - plate.weakness;
                            }
                            plate.weakness += shell.weaknessDamage * plate.weaknessMult;
                            shell.weaknessDamage -= shell.weaknessDamage/2;

                            //Apply Slight Rotation
                            if (!shell.nudged){
                                pitch += (rand.nextDouble()-0.5)*20;
                                yaw += (rand.nextDouble()-0.5)*20;

                                vert = -Math.sin(Math.toRadians(pitch));
                                mult = 1-(Math.abs(pitch)/90);
                                yawx = -Math.sin(Math.toRadians(myaw))*mult;
                                yawz = Math.cos(Math.toRadians(myaw))*mult;

                                castBy = new VectorD(yawx, vert, yawz);
                                castBy.multiply(0.1);

                                shell.nudged = true;
                            }
                        } else {
                            shell.penScore -= plate.penDef-plate.weakness*0.5;
                            if (!shell.nudged){
                                pitch += (rand.nextDouble()-0.5)*10;
                                yaw += (rand.nextDouble()-0.5)*10;

                                vert = -Math.sin(Math.toRadians(pitch));
                                mult = 1-(Math.abs(pitch)/90);
                                yawx = -Math.sin(Math.toRadians(myaw))*mult;
                                yawz = Math.cos(Math.toRadians(myaw))*mult;

                                castBy = new VectorD(yawx, vert, yawz);
                                castBy.multiply(0.1);

                                shell.nudged = true;
                            }
                        }
                        if (shell.explosive){
                            plate.weakness += shell.weaknessDamage * plate.weaknessMult * 2;
                            shell.weaknessDamage -= shell.weaknessDamage/2;
                        }
                    }
                    if (0 > plate.penDef-plate.weakness){
                        plate.weakness = plate.penDef;
                    }
                }
            }
            if (!hitPlate && penningArmor){
                RealisticVehicles.debugLog("Stopped Pen");
                penningArmor = false;
                pennedArmour = true;
                world.playSound(loc.toLocation(world), Sound.ENTITY_ITEM_BREAK, 2f, 0.8f);
                if (!heatFired && shell.heat){
                    loops = maxLoops-10;
                    heatFired = true;
                    RealisticVehicles.debugLog("FIRED");
                    world.playSound(loc.toLocation(world), Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.6f);
                }
            }
            for (Component comp : components){
                if (comp.isPointInsideBox(point.x, point.y, point.z)){
                    RealisticVehicles.debugLog("Hit Component");
                    shell.penScore -= comp.penDef;
                    if (comp.type == ComponentType.PLAYER){
                        data.addPlayerDamage((int) shell.shellDamage);
                    }
                    world.playSound(loc.toLocation(world), Sound.ENTITY_ITEM_BREAK, 2f, 0.7f);
                    if (shell.penScore > 0) {
                        comp.health -= shell.shellDamage;
                    } else if (shell.penScore <= 0){
                        comp.health -= shell.shellDamage/2;
                    }
                    shell.shellDamage *= 0.75;
                    hitComponent = true;
                    if (comp.health <= 0){
                        RealisticVehicles.debugLog("Component Destroyed.");
                        world.playSound(loc.toLocation(world), Sound.ENTITY_GENERIC_EXPLODE, 2f, 2f);
                        comp.destroyed = true;
                        destroyComponent = true;
                        if (comp.destroys && comp.isAmmo){
                            data.setDestroyedIndex(components.indexOf(comp));
                            return data;
                        } else if (comp.destroys){
                            data.setDestroyedIndex(components.indexOf(comp));
                            return data;
                        }
                    }
                }
            }
            if (shell.penScore < 0){
                finLoops = loops;
                break;
            }
            //Might Work
            if (upperHalf != null && override == 0) {
                if (isUpperHalf && !isInUpperHalf(point)) {
                    RealisticVehicles.debugLog("===== ENTER LOWER HALF =====");
                    return shellImpact(shell, x, y, z, vx, vy, vz, vehicleyaw, turretyaw, yaw, pitch, world, 2, true, player);
                } else if (!isUpperHalf && isInUpperHalf(point)) {
                    RealisticVehicles.debugLog("===== ENTER UPPER HALF =====");
                    return shellImpact(shell, x, y, z, vx, vy, vz, vehicleyaw, turretyaw, yaw, pitch, world, 1, true, player);
                }
            }

            /*
            Color color;
            if (i == 0){
                color = Color.GREEN;
            } else if (i == maxLoops-1){
                color = Color.RED;
            } else {
                color = Color.AQUA;
            }
            world.spawnParticle(Particle.REDSTONE, new Location(world,point.x+x, point.y+y, point.z+z), 0, new Particle.DustOptions(color, 2));
            */

            if (penningArmor && !pennedArmour){
                //First Strike Effects
                RealisticVehicles.debugLog("Striked!");
                if (!shell.heat){
                    world.spawnParticle(Particle.REDSTONE, loc.toLocation(world), 0, getRand(rand, randSpeed), getRand(rand, randSpeed), getRand(rand, randSpeed),0, new Particle.DustOptions(Color.GRAY, 3), true);
                    world.spawnParticle(Particle.FLAME, loc.toLocation(world), 0, getRand(rand, randSpeed+0.05), getRand(rand, randSpeed+0.05), getRand(rand, randSpeed+0.05),1, null, true);
                } else {
                    for (int j = 0; j < 20; j++) {
                        world.spawnParticle(Particle.FLAME, loc.toLocation(world), 0, getRand(rand, randSpeed), getRand(rand, randSpeed), getRand(rand, randSpeed),3, null, true);
                    }
                }
                if (shell.explosive){
                    world.spawnParticle(Particle.LAVA, loc.toLocation(world), 0, getRand(rand, randSpeed+0.05), getRand(rand, randSpeed+0.05), getRand(rand, randSpeed+0.05),1, null, true);
                }
            }
            RealisticVehicles.spawnParticle(loc.toLocation(world), Particle.CLOUD);

            finLoops = loops;
            if (!isInBoundingBox(point)){
                finLoops = -1;
                break;
            }
        }

        if (pennedArmour && finLoops == -1){
            player.sendMessage(ChatColor.RED+"Gone through enemy");
        } else if (finLoops == -1){
            player.sendMessage(ChatColor.RED+"Missed.");
        }
        if (pennedArmour){
        player.sendMessage(ChatColor.YELLOW+"Penned armour");
        } else {
            player.sendMessage(ChatColor.RED+"Non Penetration.");
        }
        if (hitComponent){
            player.sendMessage(ChatColor.GREEN+"Hit A Component!");
            if (destroyComponent){
                player.sendMessage(ChatColor.GREEN+"Destroyed A Component!");
            }
        } else {
            player.sendMessage(ChatColor.RED+"No Damage");
        }

        return data;
    }

    public ImpactOutData explosionImpact(double power, final double x, final double y, final double z, final double vx, final double vy, final double vz, Player player){
        if (!finished){
            Bukkit.getLogger().severe("DAMAGED MODEL CALLED WITHOUT BEING FINISHED, ABORTING OPERATION.\nThis could be due to a improperly generated damage model\nFOR DEVELOPERS: try calling DamageModel.finished() on the damagemodel");
            return new ImpactOutData(-1, -1);
        }
        ImpactOutData data = new ImpactOutData(-1, -1);

        double mx = vx-x;
        //System.out.println(mx + " | "+x+" - "+vx);
        double my = vy-y;
        //System.out.println(my + " | "+y+" - "+vy);
        double mz = vz-z;
        //System.out.println(mz + " | "+z+" - "+vz);

        double damage = 0;
        for (ArmourPlate plate : armour){
            double distance = (square((plate.x+(plate.xsize/2))-mx)+square((plate.y+(plate.ysize/2))-my)+square((plate.z+(plate.zsize/2))-mz));
            if (distance < power){
                double change = plate.penDef*0.75-plate.weakness;
                plate.weakness = plate.penDef*0.75;
                damage += change;
            }
        }

        double totalHealth = 0;
        List<Component> comps = new ArrayList<>();
        for (Component comp : components){
            double distance = (square((comp.x+(comp.xsize/2))-mx)+square((comp.y+(comp.ysize/2))-my)+square((comp.z+(comp.zsize/2))-mz));
            if (distance < power){
                comps.add(comp);
                totalHealth += comp.health;
            }
        }
        double damageEach = (totalHealth/damage)/comps.size();
        for (Component comp : comps){
            comp.health -= damageEach;
            if (comp.type == ComponentType.PLAYER){
                data.setPlayerDamage((int) getPosRand(rand, 0.5));
            }
            if (comp.health <= 0){
                comp.destroyed = true;
                if (comp.destroys) {
                    data.setDestroyedIndex(components.indexOf(comp));
                    return data;
                }
            }
        }

        player.sendMessage("Exploded Target!");


        //dist = ((x2 - x1)2 + (y2 - y1)2 + (z2 - z1)2)1/2
        /*for (ArmourPlate plate : armour){
            double damage = Math.max((strength/2)-(2*((square((plate.x+(plate.xsize/2))-mx)+square((plate.y+(plate.ysize/2))-my)+square((plate.z+(plate.zsize/2))-mz)))), 0);
            plate.weakness += damage;
            strength -= damage;
        }

        for (Component comp : components){
            comp.health -= getPosRand(rand, 0.2);
            double damage = Math.max((strength/8)-(((square((comp.x+(comp.xsize/2))-mx)+square((comp.y+(comp.ysize/2))-my)+square((comp.z+(comp.zsize/2))-mz)))), 0);
            comp.health -= damage;
            strength -= damage;

            if (comp.type == ComponentType.PLAYER){
                data.setPlayerDamage((int) getPosRand(rand, 0.5));
            }
            if (comp.health <= 0){
                comp.destroyed = true;
                if (comp.destroys) {
                    data.setDestroyedIndex(comp.destroyableIndex);
                    return data;
                }
            }
        }*/
        return data;
    }

    public double square(double inp){
        return inp*inp;
    }

    public double getRand(Random rand, double mult){
        return (rand.nextDouble()-0.5)*mult;
    }
    public double getPosRand(Random rand, double mult){
        return rand.nextDouble()*mult;
    }

    public boolean isInUpperHalf(VectorD vec){
        if (upperHalf == null){
            return false;
        }
        return vec.y > upperHalf.y;
    }

    public boolean isInUpperHalf(Location loc, Location vehicleLoc){
        return isInUpperHalf(new VectorD(loc.getX()-vehicleLoc.getX(), loc.getY()-vehicleLoc.getY(), loc.getZ()-vehicleLoc.getZ()));
    }

    public boolean isInBoundingBox(Location loc, Location vehicleLoc){
        if (upperHalf != null){
            return upperHalf.isPointInsideBox(loc.getX()-vehicleLoc.getX(), loc.getY()-vehicleLoc.getY(), loc.getZ()-vehicleLoc.getZ()) || lowerHalf.isPointInsideBox(loc.getX()-vehicleLoc.getX(), loc.getY()-vehicleLoc.getY(), loc.getZ()-vehicleLoc.getZ());
        }
        return lowerHalf.isPointInsideBox(loc.getX()-vehicleLoc.getX(), loc.getY()-vehicleLoc.getY(), loc.getZ()-vehicleLoc.getZ());
    }

    public boolean isInBoundingBox(VectorD vec){
        if (upperHalf != null){
            return upperHalf.isPointInsideBox(vec.x, vec.y, vec.z) || lowerHalf.isPointInsideBox(vec.x, vec.y, vec.z);
        }
        return lowerHalf.isPointInsideBox(vec.x, vec.y, vec.z);
    }

    public void addArmour(ArmourPlate plate){
        if (finished){
            Bukkit.getLogger().severe("DAMAGED MODEL CALLED AFTER BEING FINISHED, ABORTING ACTION .addArmour().\nThis cannot be called after the model is finished.");
            return;
        }
        armour.add(plate);
    }

    public void addComponent(Component comp){
        if (finished){
            Bukkit.getLogger().severe("DAMAGED MODEL CALLED AFTER BEING FINISHED, ABORTING ACTION .addComponent().\nThis cannot be called after the model is finished.\n"+comp);
            return;
        }
        components.add(comp);
    }

    public void showDamageParticles(World world, double bodyYaw, double turrYaw, Vector vPos){
        Random rand = new Random();
        for (Component comp : components){
            if (comp.upper) {
                comp.spawnDamageParticle(world, rand, turrYaw,vPos);
            } else {
                comp.spawnDamageParticle(world, rand, bodyYaw,vPos);
            }
        }
        for (ArmourPlate plate : armour){
            if (plate.weakness > plate.penDef/4) {
                if (plate.upper) {
                    plate.createRandomParticle(rand, world, turrYaw, vPos, Particle.ASH);
                } else {
                    plate.createRandomParticle(rand, world, bodyYaw, vPos, Particle.ASH);
                }
            }
        }
    }

    public void flashArmour(World world, Location loc){
        for (ArmourPlate plate : armour){
            // bottomCube
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x, plate.y, plate.z), 0, new Particle.DustOptions(Color.RED, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x+plate.xsize, plate.y, plate.z), 0, new Particle.DustOptions(Color.RED, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x, plate.y, plate.z+plate.zsize), 0, new Particle.DustOptions(Color.RED, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x+plate.xsize, plate.y, plate.z+plate.zsize), 0, new Particle.DustOptions(Color.RED, 2));
            //topCube
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x, plate.y+plate.ysize, plate.z), 0, new Particle.DustOptions(Color.RED, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x+plate.xsize, plate.y+plate.ysize, plate.z), 0, new Particle.DustOptions(Color.RED, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x, plate.y+plate.ysize, plate.z+plate.zsize), 0, new Particle.DustOptions(Color.RED, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x+plate.xsize, plate.y+plate.ysize, plate.z+plate.zsize), 0, new Particle.DustOptions(Color.RED, 2));
            //mid
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x+plate.xsize/2, plate.y+plate.ysize/2, plate.z+plate.zsize/2), 0, new Particle.DustOptions(Color.GREEN, 2));
        }
    }

    public void flashComponents(World world, Location loc){
        for (Component plate : components){
            // bottomCube
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x, plate.y, plate.z), 0, new Particle.DustOptions(Color.YELLOW, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x+plate.xsize, plate.y, plate.z), 0, new Particle.DustOptions(Color.YELLOW, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x, plate.y, plate.z+plate.zsize), 0, new Particle.DustOptions(Color.YELLOW, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x+plate.xsize, plate.y, plate.z+plate.zsize), 0, new Particle.DustOptions(Color.YELLOW, 2));
            //topCube
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x, plate.y+plate.ysize, plate.z), 0, new Particle.DustOptions(Color.YELLOW, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x+plate.xsize, plate.y+plate.ysize, plate.z), 0, new Particle.DustOptions(Color.YELLOW, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x, plate.y+plate.ysize, plate.z+plate.zsize), 0, new Particle.DustOptions(Color.YELLOW, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x+plate.xsize, plate.y+plate.ysize, plate.z+plate.zsize), 0, new Particle.DustOptions(Color.YELLOW, 2));
            //mid
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(plate.x+plate.xsize/2, plate.y+plate.ysize/2, plate.z+plate.zsize/2), 0, new Particle.DustOptions(Color.GREEN, 2));
        }
    }

    public void flashBoundingBoxes(World world, Location loc){
        // bottomCube
        world.spawnParticle(Particle.REDSTONE, loc.clone().add(lowerHalf.x, lowerHalf.y, lowerHalf.z), 0, new Particle.DustOptions(Color.WHITE, 2));
        world.spawnParticle(Particle.REDSTONE, loc.clone().add(lowerHalf.x+lowerHalf.xsize, lowerHalf.y, lowerHalf.z), 0, new Particle.DustOptions(Color.WHITE, 2));
        world.spawnParticle(Particle.REDSTONE, loc.clone().add(lowerHalf.x, lowerHalf.y, lowerHalf.z+lowerHalf.zsize), 0, new Particle.DustOptions(Color.WHITE, 2));
        world.spawnParticle(Particle.REDSTONE, loc.clone().add(lowerHalf.x+lowerHalf.xsize, lowerHalf.y, lowerHalf.z+lowerHalf.zsize), 0, new Particle.DustOptions(Color.WHITE, 2));
        //topCube
        world.spawnParticle(Particle.REDSTONE, loc.clone().add(lowerHalf.x, lowerHalf.y+lowerHalf.ysize, lowerHalf.z), 0, new Particle.DustOptions(Color.WHITE, 2));
        world.spawnParticle(Particle.REDSTONE, loc.clone().add(lowerHalf.x+lowerHalf.xsize, lowerHalf.y+lowerHalf.ysize, lowerHalf.z), 0, new Particle.DustOptions(Color.WHITE, 2));
        world.spawnParticle(Particle.REDSTONE, loc.clone().add(lowerHalf.x, lowerHalf.y+lowerHalf.ysize, lowerHalf.z+lowerHalf.zsize), 0, new Particle.DustOptions(Color.WHITE, 2));
        world.spawnParticle(Particle.REDSTONE, loc.clone().add(lowerHalf.x+lowerHalf.xsize, lowerHalf.y+lowerHalf.ysize, lowerHalf.z+lowerHalf.zsize), 0, new Particle.DustOptions(Color.WHITE, 2));

        if (upperHalf != null){
            // bottomCube
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(upperHalf.x, upperHalf.y, upperHalf.z), 0, new Particle.DustOptions(Color.WHITE, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(upperHalf.x+upperHalf.xsize, upperHalf.y, upperHalf.z), 0, new Particle.DustOptions(Color.WHITE, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(upperHalf.x, upperHalf.y, upperHalf.z+upperHalf.zsize), 0, new Particle.DustOptions(Color.WHITE, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(upperHalf.x+upperHalf.xsize, upperHalf.y, upperHalf.z+upperHalf.zsize), 0, new Particle.DustOptions(Color.WHITE, 2));
            //topCube
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(upperHalf.x, upperHalf.y+upperHalf.ysize, upperHalf.z), 0, new Particle.DustOptions(Color.WHITE, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(upperHalf.x+upperHalf.xsize, upperHalf.y+upperHalf.ysize, upperHalf.z), 0, new Particle.DustOptions(Color.WHITE, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(upperHalf.x, upperHalf.y+upperHalf.ysize, upperHalf.z+upperHalf.zsize), 0, new Particle.DustOptions(Color.WHITE, 2));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(upperHalf.x+upperHalf.xsize, upperHalf.y+upperHalf.ysize, upperHalf.z+upperHalf.zsize), 0, new Particle.DustOptions(Color.WHITE, 2));
        }

        for (int i = 0; i < 20; i++) {
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(new Vector(0, 0, 1).rotateAroundY(Math.toRadians(i*18))), 0, new Particle.DustOptions(Color.ORANGE, 1));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(new Vector(0, 1, 0).rotateAroundX(Math.toRadians(i*18))), 0, new Particle.DustOptions(Color.ORANGE, 1));
            world.spawnParticle(Particle.REDSTONE, loc.clone().add(new Vector(0, 1, 0).rotateAroundZ(Math.toRadians(i*18))), 0, new Particle.DustOptions(Color.ORANGE, 1));
        }
    }
    
    public void flashAll(World world, Location loc){
        if (!finished){
             Bukkit.getLogger().severe("DAMAGED MODEL CALLED WITHOUT BEING FINISHED, ABORTING .flashAll().\nThis could be due to a improperly generated damage model\nFOR DEVELOPERS: try calling DamageModel.finished() on the damagemodel");
             return;
        }
        flashArmour(world, loc);
        flashComponents(world, loc);
        flashBoundingBoxes(world, loc);
    }

    public List<ArmourPlate> getArmour() {
        return armour;
    }

    public List<Component> getComponents() {
        return components;
    }

    public Component getComponent(ComponentType type){
        for (Component comp : components){
            if (comp.type == type){
                return comp;
            }
        }
        return null;
    }

    public void setArmour(List<ArmourPlate> armour) {
        this.armour = armour;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    @Override
    public String toString() {
        return "DamageModel{" +
                "armour=" + armour +
                ", components=" + components +
                ", compMap="+compMap+
                ", lowerHalf=" + lowerHalf +
                ", upperHalf=" + upperHalf +
                ", finished=" + finished +
                '}';
    }


    @Override
    public DamageModel clone() {
        DamageModel clone;
        try {
            clone = (DamageModel) super.clone();
            clone.armour = new ArrayList<>();
            for (ArmourPlate armour : armour){
                clone.armour.add(armour.clone());
            }
            clone.components = new ArrayList<>();
            for (Component comp : components){
                clone.components.add(comp.clone());
            }
            return clone;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<ComponentType, List<Integer>> getCompMap() {
        return compMap;
    }

    /**
     * Gets how much damage all components have sustained.
     * @param type Component type to check
     * @return Amount of damage as totalMaxHealth-totalHealth
     */
    public float getComponentDamage(ComponentType type){
        float maxHealth = 0;
        float health = 0;
        for (Integer i : compMap.get(type)){
            Component comp = components.get(i);
            maxHealth += comp.maxHealth;
            health += comp.health;
        }
        return maxHealth-health;
    }

    /**
     * Checks if all of the components are active.
     * @param type Component type to check
     * @return If any components are inactive
     */
    public boolean areAllCompsActive(ComponentType type) {
        for (Integer i : compMap.get(type)) {
            Component comp = components.get(i);
            if (comp.destroyed){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a value between 0 and 1 of how much damage all the components have sustained (as a average)
     * @param type Component Type to check
     * @return 0 - 1 of percent health
     */
    public float getComponentDamagePercent(ComponentType type){
        float health = 0;
        float maxHealth = 0;
        for (Integer i : compMap.get(type)) {
            Component comp = components.get(i);
            maxHealth += comp.maxHealth;
            health += comp.health;
        }
        return health/maxHealth;
    }

    /**
     * Returns a value between 0 and 1 of how many components are active.
     * @param type Component Type to check
     * @return 0 - 1 of percent health
     */
    public float getComponentActivePercent(ComponentType type){
        int activeCount = 0;
        for (Integer i : compMap.get(type)) {
            Component comp = components.get(i);
            if (!comp.destroyed){
                activeCount++;
            }
        }
        return (float) activeCount/compMap.get(type).size();
    }


    public List<Integer> getComponentIndices(ComponentType type){
        return compMap.get(type);
    }

    public float getAllArmourDamage(){
        float out = 0;
        for (ArmourPlate plate : armour){
            out += plate.weakness;
        }
        return out;
    }

    /**
     * Gets percent 0.0 - 1.0 of damage taken across all components
     * @return percentage of 0.0 - 1.0
     */
    public float getAllComponentDamage(){
        float damage = 0;
        float maxHealth = 0;
        for (Component comp : components){
            damage += comp.maxHealth-comp.health;
            maxHealth += comp.maxHealth;
        }
        return damage/maxHealth;
    }

    public float getCollisionSphere() {
        return collisionSphere;
    }
}
