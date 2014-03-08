package content;

/**
 * A Factory class to retrieve the IContentManager instance.
 * Created by Alex on 3/8/14.
 */
public class ContentManagerFactory {
    private static IContentManager Instance = new DummyManager();

    /**
     * @return Returns an instance of the ContentManager
     */
    public static IContentManager getInstance(){
        return Instance;
    }
}
