package network.palace.library;

import lombok.Getter;

/**
 * @author Innectic
 * @since 4/29/2017
 */
public class Start {

    @Getter private static DashboardClassLoader classLoader;

    public static void main(String[] args) throws Exception {
        System.out.println("Initializing class loader...");
        classLoader = new DashboardClassLoader();
        System.out.println("Loading Dashboard library...");
        classLoader.addURL(Start.class.getProtectionDomain().getCodeSource().getLocation());
        System.out.println("Loading external libraries...");
        LibraryHandler.loadLibraries(Start.class);
        System.out.println("Launching Dashboard...");
        Class<?> temp = classLoader.loadClass("network.palace.dashboard.Launcher");
        temp.getDeclaredConstructor().newInstance();
    }
}