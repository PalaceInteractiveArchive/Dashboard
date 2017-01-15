package network.palace.dashboard.scheduler;

import network.palace.dashboard.Dashboard;

/**
 * Created by Marc on 1/15/17.
 */
public class ShowReminder implements Runnable {
    private String msg = "";

    public ShowReminder(String msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        Dashboard.moderationUtil.sendMessage(msg);
    }
}
