package com.ericlam.mc.test.eld;

import com.ericlam.mc.eld.ELDenpendenci;

public class Main {

    public static void main(String[] args) {
        var main = new Main();
        main.run();
    }

    private Main(){
    }

    private void run(){
        ELDenpendenci.register(this, injector -> {});
    }
}
