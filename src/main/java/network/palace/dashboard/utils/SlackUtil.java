package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;
import network.palace.dashboard.slack.SlackService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marc on 9/12/16
 */
public class SlackUtil {
    public SlackService s = new SlackService();

    public void sendDashboardMessage(SlackMessage msg) {
        sendDashboardMessage(msg, new ArrayList<>());
    }

    public void sendDashboardMessage(SlackMessage msg, List<SlackAttachment> attachments) {
        sendDashboardMessage(msg, attachments, true);
    }

    public void sendDashboardMessage(SlackMessage msg, List<SlackAttachment> attachments, boolean status) {
        if (Dashboard.isTestNetwork() && status) {
            return;
        }
        for (SlackAttachment a : attachments) {
            a.addMarkdownIn("text");
        }
        String webhook;
        if (status) {
            webhook = "https://hooks.slack.com/services/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";
        } else {
            webhook = "https://hooks.slack.com/services/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";
        }
        try {
            s.push(webhook, msg, attachments);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}