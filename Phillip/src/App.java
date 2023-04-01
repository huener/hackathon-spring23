public class App {
    Data data = new Data();
    public static void main(String[]args)
    {
        String url = "jdbc:postgresql://db.lroakeegvjazrfrsdccb.supabase.co:5432/postgres?user=postgres&password=Ie0prHyD9JZS0hM7";
        Data.initializeData(url);
    }
}
