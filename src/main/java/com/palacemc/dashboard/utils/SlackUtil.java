package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.slack.SlackAttachment;
import com.palacemc.dashboard.slack.SlackMessage;
import com.palacemc.dashboard.slack.SlackService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marc on 9/12/16
 */
public class SlackUtil {
    public SlackService slackService = new SlackService();

    private Dashboard dashboard = Launcher.getDashboard();

    public void sendDashboardMessage(SlackMessage msg) {
        sendDashboardMessage(msg, new ArrayList<>());
    }

    public void sendDashboardMessage(SlackMessage msg, List<SlackAttachment> attachments) {
        if (dashboard.isTestNetwork()) return;

        String webhook = "https://hooks.slack.com/services/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";

        try {
            slackService.push(webhook, msg, attachments);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}