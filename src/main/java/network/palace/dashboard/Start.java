package network.palace.dashboard;

import network.palace.dashboard.library.LibraryHandler;

/**
 * @author Innectic
 * @since 4/29/2017
 */
public class Start {

    private static Launcher launcher;

    public static void main(String[] args) {
        LibraryHandler.loadLibraries(Start.class);
        launcher = new Launcher();
    }
}
