package zedly.variabletime;

import java.util.*;
import static org.bukkit.Bukkit.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;

public class VariableTime extends JavaPlugin {

    public static Map<World, Double> worldTimeDay = new HashMap<>();
    public static Map<World, Double> worldSpeedDay = new HashMap<>();
    public static Map<World, Double> worldTimeNight = new HashMap<>();
    public static Map<World, Double> worldSpeedNight = new HashMap<>();
    public static final String LOGO = ChatColor.DARK_PURPLE + "[" + ChatColor.DARK_AQUA + "Variable Time" + ChatColor.DARK_PURPLE + "]" + ChatColor.DARK_PURPLE + " ";

    public void onEnable() {
        saveDefaultConfig();
        for (World world : getWorlds()) {
            if (!getConfig().contains(world.getName())) {
                getConfig().addDefault(world.getName() + ".Day", 1.0);
                getConfig().addDefault(world.getName() + ".Night", 1.0);
                getConfig().options().copyDefaults(true);
                saveConfig();
                worldSpeedDay.put(world, 1.0);
                worldTimeDay.put(world, 0.0);
                worldSpeedNight.put(world, 1.0);
                worldTimeNight.put(world, 0.0);
            } else {
                if (getConfig().contains(world.getName() + ".Day") && getConfig().contains(world.getName() + ".Night")) {
                    try {
                        worldSpeedDay.put(world, Math.abs(getConfig().getDouble(world.getName() + ".Day")));
                        worldTimeDay.put(world, 0.0);
                        worldSpeedNight.put(world, Math.abs(getConfig().getDouble(world.getName() + ".Night")));
                        worldTimeNight.put(world, 0.0);
                    } catch (Exception e) {
                        System.err.println("World " + world.getName() + " has a bad format!");
                    }
                } else {
                    System.err.println("World " + world.getName() + " has a bad format!");
                }
            }
        }
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getTime() > 12000 && worldTimeNight.containsKey(world)) {
                        world.setFullTime(world.getFullTime() - 1);
                        while (worldTimeNight.get(world) > 1) {
                            world.setFullTime(world.getFullTime() + 1);
                            worldTimeNight.put(world, worldTimeNight.get(world) - 1);
                            if (world.getTime() < 12000) {
                                worldTimeNight.put(world, 0.0);
                            }
                        }
                        worldTimeNight.put(world, worldSpeedNight.get(world) + worldTimeNight.get(world));
                    } else if (worldTimeDay.containsKey(world)) {
                        world.setFullTime(world.getFullTime() - 1);
                        while (worldTimeDay.get(world) > 1) {
                            world.setFullTime(world.getFullTime() + 1);
                            worldTimeDay.put(world, worldTimeDay.get(world) - 1);
                            if (world.getTime() > 12000) {
                                worldTimeDay.put(world, 0.0);
                            }
                        }
                        worldTimeDay.put(world, worldSpeedDay.get(world) + worldTimeDay.get(world));
                    }
                }
            }
        }, 0, 1);
    }

    public boolean onCommand(CommandSender sender, Command command, String commandlabel, String[] args) {
        if (commandlabel.equalsIgnoreCase("vt")) {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "night":
                    case "day":
                        boolean day = args[0].equals("day");
                        if (!sender.hasPermission("variabletime.set")) {
                            sender.sendMessage(LOGO + "You do not have permission to do this!");
                            return true;
                        } else if (args.length < 3) {
                            sender.sendMessage(LOGO + "/vt " + (day ? "day" : "night") + " <world> <multiplier>");
                            return true;
                        }
                        try {
                            World world = getWorld(args[1]);
                            if (world != null) {
                                (day ? worldSpeedDay : worldSpeedNight).put(world, Math.abs(Double.parseDouble(args[2])));
                                (day ? worldTimeDay : worldTimeNight).put(world, 0.0);
                                sender.sendMessage(LOGO + "Time value changed!");
                                updateConfig();
                            } else {
                                sender.sendMessage(LOGO + "Error, world entered is invalid!");
                            }
                        } catch (Exception e) {
                            sender.sendMessage(LOGO + "Error, value entered is invalid!");
                        }
                        break;
                    case "info":
                        if (!sender.hasPermission("variabletime.info")) {
                            sender.sendMessage(LOGO + "You do not have permission to do this!");
                            return true;
                        }
                        sender.sendMessage(LOGO + "Worlds: ");
                        for (World w : Bukkit.getWorlds()) {
                            if (worldSpeedNight.containsKey(w) && worldSpeedDay.containsKey(w)) {
                                sender.sendMessage(ChatColor.DARK_PURPLE + w.getName() + ChatColor.DARK_AQUA + ": Day speed is " + worldSpeedDay.get(w) + "x | Night Speed: " + worldSpeedNight.get(w) + "x");
                            }
                        }
                        break;
                    case "help":
                    default:
                        info(sender);
                        break;
                }
            } else {
                info(sender);
            }
        }
        return true;
    }

    public static void info(CommandSender sender) {
        sender.sendMessage(LOGO);
        sender.sendMessage(ChatColor.DARK_PURPLE + "-vt day:" + ChatColor.DARK_AQUA + " Sets the world's day setting");
        sender.sendMessage(ChatColor.DARK_PURPLE + "-vt night:" + ChatColor.DARK_AQUA + " Sets the world's night setting");
        sender.sendMessage(ChatColor.DARK_PURPLE + "-vt info:" + ChatColor.DARK_AQUA + " Returns info about world time settings");
    }

    public void updateConfig() {
        for (World world : getWorlds()) {
            getConfig().set(world.getName() + ".Day", worldSpeedDay.get(world));
            getConfig().set(world.getName() + ".Night", worldSpeedNight.get(world));
            saveConfig();
        }
    }
}
