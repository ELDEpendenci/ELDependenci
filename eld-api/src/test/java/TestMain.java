import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class TestMain {

    private List<String> list;
    private String fake;

    public static void main(String[] args) {
        try{
            var f = TestMain.class.getDeclaredField("list");
            var ff = TestMain.class.getDeclaredField("fake");
            var list = new ArrayList<String>();
            if (f.getGenericType() instanceof ParameterizedType){
                var t = (ParameterizedType)f.getGenericType();
                System.out.println("is string list: "+(t.getRawType() == List.class && t.getActualTypeArguments()[0] == String.class));
            }
            System.out.println(ff.getGenericType());
            System.out.println(list.getClass());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
