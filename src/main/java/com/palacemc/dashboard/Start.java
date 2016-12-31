package com.palacemc.dashboard;

import com.palacemc.dashboard.libraries.LibraryHandler;

/**
 * @author Innectic
 * @since 12/30/2016
 */
public class Start {

    private static Launcher launcher;

    public static void main(String[] args) {
        LibraryHandler.loadLibraries(Start.class);
        launcher = new Launcher();
    }
}
