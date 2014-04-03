package content;

import android.content.Context;

import com.stealth.files.FileIndex;
import encryption.IContentManager;

/**
 * A Factory class to retrieve the IContentManager instance. Created by Alex on 3/8/14.
 */
public class ContentManagerFactory {
	private static IContentManager Instance;

	/**
	 * Making this constructor private makes sure Java doesn't offer a generated one, from which a CMF could be created
	 * which then could create a second ContentManager, which would be undesired
	 */
	private ContentManagerFactory() {
		throw new IllegalStateException("Using the ContentManagerFactory constructor is illegal");
	}

	/**
     * @param context Used to retrieve application info, such as data folder location for the ContentManager
     * @param index the file index to use by the content manager
	 * @return Returns an instance of the ContentManager
	 */
	public static IContentManager getInstance(Context context, FileIndex index) {
        if(Instance == null){
            Instance = new ContentManager(context, index);
        }
		return Instance;
	}
}
