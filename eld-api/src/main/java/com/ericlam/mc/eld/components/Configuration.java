package com.ericlam.mc.eld.components;

import com.ericlam.mc.eld.controllers.FileController;

public abstract class Configuration {
    private FileController controller;

    public final FileController getController() {
        return controller;
    }
}
