package com.ericlam.mc.test.eld;

import com.ericlam.mc.eld.ELDenpendenci;
import com.ericlam.mc.test.eld.experinment.ELDependenciTest;

public class Main {

    public static void main(String[] args) {
        var main = new Main();
        main.run();
    }

    private Main(){
    }

    private void run(){
        ELDependenciTest.register(this, service -> {

        });
    }
}
