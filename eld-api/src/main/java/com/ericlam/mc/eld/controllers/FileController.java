package com.ericlam.mc.eld.controllers;

import com.ericlam.mc.eld.components.Configuration;

import java.io.IOException;

public interface FileController {

    boolean reload();

    void save() throws IOException;

}
