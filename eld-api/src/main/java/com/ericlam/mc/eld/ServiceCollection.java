package com.ericlam.mc.eld;

import com.ericlam.mc.eld.services.Service;

public interface ServiceCollection {

    <T extends Service> ServiceCollection registerService(Class<T> service);


}
