public class TestMain {
    public static void main(String[] args) {
        System.out.println(AAAA.class.isAssignableFrom(AA.class));
    }

    public static abstract class AAAA {
    }

    public static class AA extends AAAA {
    }
}
