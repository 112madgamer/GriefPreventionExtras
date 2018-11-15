package _112.griefpreventionextras.Commands;

import _112.griefpreventionextras.Util.Region;
import com.boydti.fawe.object.FawePlayer;
import com.boydti.fawe.util.TaskManager;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DeleteClaims implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if(sender instanceof Player) {
            FawePlayer fawePlayer = FawePlayer.wrap(sender);
            World world = Bukkit.getWorld(fawePlayer.getWorld().getName());
            Region region = new Region(fawePlayer.getSelection().getMinimumPoint(), fawePlayer.getSelection().getMaximumPoint(), world);
            //TODO fix async WE warning?
            TaskManager.IMP.async(new BukkitRunnable() {
                @Override
                public void run() {
                    for(Claim claim : GriefPrevention.instance.dataStore.getClaims()){
                        if(region.locationIsInRegion(claim.getLesserBoundaryCorner()) && region.locationIsInRegion(claim.getGreaterBoundaryCorner())){
                            fawePlayer.sendMessage("Deleting claim " + claim.getID());
                            GriefPrevention.instance.dataStore.deleteClaim(claim);
                        }
                    }
                }
            });


        }
        return true;
    }
}