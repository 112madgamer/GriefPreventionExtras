package _112.griefpreventionutilities.Commands;

import _112.griefpreventionutilities.GriefPreventionUtilities;
import com.boydti.fawe.object.schematic.Schematic;
import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaveClaims implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            GriefPreventionUtilities gpu = GriefPreventionUtilities.getPlugin();
            Player player = (Player) sender;
            if (args.length == 2 && args[0] != null && args[1] != null) {
                if (player.getServer().getWorld(args[0]) != null && player.getServer().getWorld(args[1]) != null) {
                    TaskManager.IMP.async(() -> {
                        Location corner1;
                        Location corner2;
                        World world1;
                        World world2;
                        world1 = Bukkit.getWorld(args[0]);
                        world2 = Bukkit.getWorld(args[1]);

                        int claims = GriefPrevention.instance.dataStore.getClaims().size();
                        int done = 0;

                        EditSession copyWorld = new EditSessionBuilder(world1.getName())
                                .allowedRegionsEverywhere()
                                .build();
                        EditSession pasteWorld = new EditSessionBuilder(world2.getName())
                                .allowedRegionsEverywhere()
                                .build();

                        for (Claim claim : GriefPrevention.instance.dataStore.getClaims()) {
                            corner1 = claim.getLesserBoundaryCorner();
                            corner2 = claim.getGreaterBoundaryCorner();


                            if (!corner1.getWorld().getName().equals(args[0])) {
                                GriefPreventionUtilities.getPlugin().logMessage((String.format("Skipping claim in %s doesn't match specified world %s", corner1.getWorld().getName(), world1.getName())));
                                continue;
                            }

                            GriefPreventionUtilities.getPlugin().logMessage(String.format("Copying claim %s:%s from %s %s %s", claim.getOwnerName(), claim.getID(), corner1.getBlockX(), corner1.getBlockY(), corner1.getBlockZ()));


                            BlockVector3 pos1 = BlockVector3.at(corner1.getX(), 0,corner1.getZ());
                            BlockVector3 pos2 = BlockVector3.at(corner2.getX(), world2.getMaxHeight(),corner2.getZ());

                            CuboidRegion copyRegion = new CuboidRegion(pos1, pos2);
                            BlockArrayClipboard lazyCopy = copyWorld.lazyCopy(copyRegion);

                            Schematic schem = new Schematic(lazyCopy);
                            BlockVector3 to =  BlockVector3.at(corner1.getBlockX(), 0, corner1.getBlockZ());
                            schem.paste(pasteWorld, to, true);

                            pasteWorld.flushQueue();
                            pasteWorld.fixLighting(copyRegion.getChunks());

                            GriefPreventionUtilities.getPlugin().logMessage(String.format("Copied claim %s", claim.getID()));
                            claims--;
                            done++;
                            if (done == 10) {
                                done = 0;
                                GriefPreventionUtilities.getPlugin().logMessage(String.format("%s claims left to copy", claims));

                            }

                        }
                    });

                } else {
                    gpu.sendMessage(sender, "A world you specified doesn't exist");
                    return true;
                }
            } else {
                gpu.sendMessage(sender, "Please specify two worlds /saveclaims <world1> <world2>");
                return true;
            }

        }
        return true;
    }
}
