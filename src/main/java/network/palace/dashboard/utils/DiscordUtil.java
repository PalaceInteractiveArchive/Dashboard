package network.palace.dashboard.utils;

import network.palace.dashboard.handlers.Player;

import java.util.Random;

public class DiscordUtil {

    public static int generatePin(final Player player, int min, int max) {
        String username = player.getUsername();

        Random random = new Random();
        int pin = random.nextInt(max - min + 1) + min;

        System.out.println(username + "'s discord verification pin is " + pin);

        return pin;
    }

//    public static int generatePin(int min, int max) {
//        Random random = new Random();
//
//        int pin = random.nextInt(max - min + 1) + min;
//
//        userPin = pin;
//
//        return pin;
//    }
}
