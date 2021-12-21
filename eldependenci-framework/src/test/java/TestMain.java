import com.ericlam.mc.eld.services.ScheduleService;

import java.util.List;

public class TestMain {

    private static ScheduleService scheduleService;

    public static void main(String[] args) {
        scheduleService.callAllAsync(null, List.of(
                scheduleService.callAsync(null, () -> "string"),
                scheduleService.callAsync(null, () -> 123)
        )).thenRunAsync(results -> {
            String string = (String) results[0];
            int number = (Integer) results[1];
            //...
        }).join();
    }

}
