package com.stealth.morphing;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.ipaulpro.afilechooser.utils.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import kellinwood.security.zipsigner.ZipSigner;
import org.xml.sax.SAXException;

/**
 * Created by Alex on 31-3-14.
 */
public class AppMorph {
	public enum ProgressStep {
		Start,
		Extracting,
		SettingLabel,
		SettingIcons,
		Repackaging,
		Signing,
	}

	public interface MorphProgressListener {
		void onProgress(ProgressStep progress);

		void onMorphFailed(ProgressStep atPoint, Exception failure);

		void onFinished(File newApk);
	}

	//Static initializer of the Icon size hashmap, needed to construct our bitmaps
	private static final HashMap<String, Integer> ICON_SIZES;

	private static final String MORPH_DIR = "Morph";
	private static final String TEMP_JAR_DIR = "morphjar";
	private static final String UN_SIGNED_EXTENSION = ".unsigned.unaligned.apk";
	private static final String SIGNED_EXTENSION = ".apk";
	private static final int DEFAULT_COMPRESSION_LEVEL = 2;
	private static final String DEFAULT_ICON_SIZE = "mdpi";
	private static final String DRAWABLE_NAME = "@drawable/";

	static {
		ICON_SIZES = new HashMap<String, Integer>();
		ICON_SIZES.put("mdpi", 48);
		ICON_SIZES.put("hdpi", 72);
		ICON_SIZES.put("xhdpi", 96);
		ICON_SIZES.put("xxhdpi", 144);
		ICON_SIZES.put("xxxhdpi", 192);
	}

	private Context mAppContext;
	private String mIconResName = "ic_launcher";
	private MorphProgressListener mListener;
	private ProgressStep mProgressStep;

	public AppMorph(Context context) {
		mAppContext = context.getApplicationContext();
	}

	/**
	 * Sets a new MorphProgressListener which will be notified of the progress and any failure that might occur
	 *
	 * @param listener
	 */
	public void setMorphProgressListener(MorphProgressListener listener) {
		mListener = listener;
	}

	/**
	 * Sets the resource file name to look for when replacing the icons
	 *
	 * @param iconResName
	 */
	public void setIconResName(String iconResName) {
		if (iconResName != null) {
			mIconResName = iconResName;
		}
	}

	/**
	 * Creates a new signed Apk file from this application with the new label and icon. Calling this function clears
	 * any
	 * previously generated apk files.
	 *
	 * @param label The new app title to be used for the Apk
	 * @param icon  The new icon to be used for the Apk
	 */
	public void morphApp(String label, Uri icon) {
		File signedApkFile = null;

		try {
			deleteContent(getMorphCacheDir());

			setProgressStep(ProgressStep.Extracting);
			File jarDir = extractApk();

			setProgressStep(ProgressStep.SettingLabel);
			setManifestLabel(jarDir, label);

			setProgressStep(ProgressStep.SettingIcons);
			setIcons(jarDir, icon, mIconResName);

			setProgressStep(ProgressStep.Repackaging);
			File unSignedApkFile = createJar(jarDir, label);
			deleteRecursively(jarDir);

			setProgressStep(ProgressStep.Signing);
			signedApkFile = signApk(unSignedApkFile, label);
			unSignedApkFile.delete();

		}
		catch (Exception e) {
			if (mListener != null) {
				mListener.onMorphFailed(mProgressStep, e);
			}
		}

		finished(signedApkFile);
	}

	/**
	 * Sets the new progress step of the current morph and notifies the listener if it has been set
	 *
	 * @param progressStep
	 */
	private void setProgressStep(ProgressStep progressStep) {
		mProgressStep = progressStep;
		if (mListener != null) {
			mListener.onProgress(mProgressStep);
		}
	}

	/**
	 * Resets progress and returns the final apk to the listener if it has been set
	 *
	 * @param newApk
	 */
	private void finished(File newApk) {
		mProgressStep = ProgressStep.Start;

		if (mListener != null) {
			mListener.onFinished(newApk);
		}
	}

	/**
	 * Extracts this application's Apk file to a new content directory
	 *
	 * @return the directory to which all the content has been written
	 * @throws ZipException
	 */
	private File extractApk() throws ZipException {
		File jarDir = new File(getMorphCacheDir(), TEMP_JAR_DIR);
		if (jarDir.exists() && jarDir.isDirectory()) {
			deleteContent(jarDir);
		}
		else if (!jarDir.mkdirs()) {
			return null;
		}

		ZipFile zipFile = new ZipFile(mAppContext.getPackageResourcePath());
		zipFile.extractAll(jarDir.getPath());

		File[] unneeded = jarDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().equals("META-INF");
			}
		});

		if (unneeded.length > 0) {
			for (File del : unneeded) {
				deleteRecursively(del);
			}
		}

		return jarDir;
	}

	/**
	 * Extracts the manifest from the content directory, sets a new application label, and stores the manifest again
	 *
	 * @param jarDir
	 * @param label
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	private void setManifestLabel(File jarDir, String label) throws
			ParserConfigurationException,
			SAXException,
			IOException,
			TransformerException {
		File manifest = new File(jarDir, "AndroidManifest.xml");
		if (!manifest.exists()) {
			throw new IOException("Couldn't find android manifest!");
		}

		ManifestTransformer.writeLabel(manifest, label);
	}

	/**
	 * Sets the icons in all the necessary resource directories
	 *
	 * @param jarDir
	 * @param icon
	 * @param iconResName
	 * @return
	 * @throws FileNotFoundException
	 */
	private boolean setIcons(File jarDir, final Uri icon, String iconResName) throws FileNotFoundException {

		Bitmap original = BitmapFactory.decodeStream(mAppContext.getContentResolver().openInputStream(icon));

		File resDir = new File(jarDir, "res");
		if (!resDir.exists()) {
			return false;
		}

		File[] drawables = resDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().startsWith("drawable-");
			}
		});

		boolean singleIconFailed = false;

		String newFileName = iconResName + "." + getExtension(FileUtils.getFile(mAppContext, icon));

		for (File file : drawables) {
			if (!file.isDirectory()) {
				continue;
			}

			try {
				replaceIcon(original, file, iconResName, newFileName);
			}
			catch (FileNotFoundException e) {
				singleIconFailed = true;
			}
		}

		return !singleIconFailed;
	}

	/**
	 * Replaces the icon in this dir with the new icon, if it can be found
	 *
	 * @param replacement The new icon
	 * @param currentDir  current resource dir
	 * @param iconResName name of the icon
	 * @throws FileNotFoundException
	 */
	private void replaceIcon(Bitmap replacement, File currentDir, final String iconResName,
			String newIconName) throws FileNotFoundException {
		File[] iconFilesInDir = currentDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String s) {
				s = s.replaceFirst("[.][^.]+$", "");
				return s.equalsIgnoreCase(iconResName);
			}
		});

		if (iconFilesInDir.length == 0) {
			return;
		}
		else {
			iconFilesInDir[0].delete();
		}

		String filename = currentDir.getName();
		String size = filename.substring(filename.indexOf('-') + 1);

		File iconFile = new File(currentDir, newIconName);

		if (ICON_SIZES.containsKey(size)) {
			int iconSize = ICON_SIZES.get(size);
			createIcon(replacement, iconFile, iconSize);
		}
		else {
			int iconSize = ICON_SIZES.get(DEFAULT_ICON_SIZE);
			createIcon(replacement, iconFile, iconSize);
		}
	}

	/**
	 * Creates a new, scaled down icon in the given file
	 *
	 * @param original
	 * @param iconFile
	 * @param size
	 * @throws FileNotFoundException if the file does not match the requirements as given by {@link
	 *                               java.io.OutputStream#OutputStream()}
	 */
	private void createIcon(Bitmap original, File iconFile, int size) throws FileNotFoundException {
		Bitmap icon = Bitmap.createScaledBitmap(original, size, size, true);

		OutputStream iconFileStream = new FileOutputStream(iconFile);

		icon.compress(Bitmap.CompressFormat.PNG, 100, iconFileStream);
	}

	/**
	 * @param contentDir
	 * @param unsignedName
	 * @return
	 * @throws IOException
	 */
	private File createJar(File contentDir, String unsignedName) throws IOException {
		File unSignedApkFile = new File(getMorphCacheDir(), unsignedName + UN_SIGNED_EXTENSION);

		JarWriter writer = new JarWriter();
		writer.setIncludeManifest(false);
		writer.setFiles(getPaths(contentDir));
		writer.createJar(unSignedApkFile, DEFAULT_COMPRESSION_LEVEL);

		return unSignedApkFile;
	}

	/**
	 * Uses the ZipSigner library to sign the apk in the given file.
	 *
	 * @param unsignedApk
	 * @param signedName
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private File signApk(File unsignedApk, String signedName) throws
			IllegalAccessException,
			InstantiationException,
			ClassNotFoundException,
			IOException,
			GeneralSecurityException {
		File signedApkFile = new File(getMorphCacheDir(), signedName + SIGNED_EXTENSION);
		ZipSigner zipSigner = new ZipSigner();
		zipSigner.setKeymode("auto-testkey");
		zipSigner.signZip(unsignedApk.getPath(), signedApkFile.getPath());

		return signedApkFile;
	}

	/**
	 * Returns the cache directory where all the morphing is managed
	 *
	 * @return
	 */
	private File getMorphCacheDir() {
		return new File(mAppContext.getExternalCacheDir(), MORPH_DIR);
	}

	/**
	 * retrieves all file paths from the contents inside a directory
	 *
	 * @param dir
	 * @return
	 */
	private static String[] getPaths(File dir) {
		File[] files = dir.listFiles();
		String[] paths = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			paths[i] = files[i].getPath();
		}

		return paths;
	}

	/**
	 * Deletes the content inside a folder
	 *
	 * @param folder
	 */
	private static void deleteContent(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { //some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteRecursively(f);
				}
				else {
					f.delete();
				}
			}
		}
	}

	/**
	 * Deletes a folder and all its content
	 *
	 * @param folder
	 */
	private static void deleteRecursively(File folder) {
		if (folder.isDirectory()) {
			deleteContent(folder);
		}
		folder.delete();
	}

	/**
	 * Retrieves the extension from a file
	 *
	 * @param f The file to retrieve the extension from
	 * @return The extension
	 */
	private String getExtension(File f) {
		String extension = "";
		String name = f.getName();

		int i = name.lastIndexOf('.');
		if (i > 0) {
			extension = name.substring(i + 1);
		}

		return extension;
	}
}
