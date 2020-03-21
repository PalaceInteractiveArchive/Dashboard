package network.palace.dashboard.commands.staff;

import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class MotionCaptureCommand extends DashboardCommand {

    public MotionCaptureCommand() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        StringBuilder str = new StringBuilder();
        for (String s : args) {
            str.append(s).append(" ");
        }
        player.chat("/motioncapture:mc " + str.toString().trim());
    }
}
