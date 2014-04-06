package content;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.stealth.files.IndexedFile;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;

/**
 * This class performs the necessary steps to create/read and de/encrypt thumbnails for images
 * Created by OlivierHokke on 06-Apr-14.
 */
public class ThumbnailManager {

	/**
	 * Creates the thumbnail for an item, encrypts it and saves it in the thumbnail folder
	 * @param item the file to generate the thumbnail of
	 */
	public static void createThumbnail(final IndexedFile item, final IOnResult<Boolean> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// generate thumbnail
					Bitmap thumb = FileUtils.getThumbnail(Utils.getContext(), item.getUnlockedFile());
					if (thumb == null) return;

					// save thumbnail to cache
					File cache = Utils.getRandomCacheFile(".jpg");
					FileOutputStream out = new FileOutputStream(cache);
					thumb.compress(Bitmap.CompressFormat.JPEG, 90, out);
					out.close();

					// encrypt
					File thumbFile = item.getThumbFile();
					Utils.getMainCrypto().encrypt(thumbFile, cache, item.getName());
					Utils.delete(cache);

					if (callback != null) {
						callback.onResult(true);
					}

				} catch (Exception e) {
					Utils.d("Failed to create thumbnail of item '" + item.getName() + "'. " + e.getMessage());
					e.printStackTrace();

					if (callback != null) {
						callback.onResult(false);
					}
				}
			}
		}).start();
	}

	/**
	 * Gets the encrypted thumbnail for an item, decrypts it and returns the bitmap to the callback
	 * @param item the file to generate the thumbnail of
	 * @param callback the callback to get the result (a bitmap)
	 */
	public static void getThumbnail(final IndexedFile item, final IOnResult<Bitmap> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					// decrypt
					File cache = Utils.getRandomCacheFile();
					File thumbFile = item.getThumbFile();
					Utils.getMainCrypto().decrypt(thumbFile, cache, item.getName());

					// read the bitmap
					Bitmap bm = BitmapFactory.decodeFile(cache.getPath());
					Utils.delete(cache);

					if (callback != null) {
						callback.onResult(bm);
					}

				} catch (Exception e) {
					Utils.d("Failed to read thumbnail of item '" + item.getName() + "'. " + e.getMessage());
					e.printStackTrace();

					if (callback != null) {
						callback.onResult(null);
					}
				}
			}
		}).start();
	}
}
