package spikes.morphing;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;

import kellinwood.security.zipsigner.ZipSigner;
import kellinwood.security.zipsigner.optional.CustomKeySigner;

/**
 * Created by Alex on 31-3-14.
 */
public class AppMorph {
    private Context mAppContext;
    private static final String TempJarDir = "morphjar";
    private static final String UnSignedApkName = "MorphTest.unsigned.unaligned.apk";
    private static final String SignedApkName = "MorphTest.apk";
    private static final int CompressionLevel = 2;

    public AppMorph(Context context){
        mAppContext = context.getApplicationContext();
    }

    public File morphApp(String label, Uri icon) throws ZipException, IOException, ClassNotFoundException, GeneralSecurityException, InstantiationException, IllegalAccessException {

        File jarDir = extractApk();

        //TODO cool stuff with unzipped files!

        File unSignedApkFile = createJar(jarDir);
        File signedApkFile = signApk(unSignedApkFile);

        Log.d("AppMorph", "Finalized app morphing!");

        return signedApkFile;
    }

    private File extractApk() throws ZipException {
        File jarDir = new File(mAppContext.getExternalCacheDir(), TempJarDir);
        if(jarDir.exists() && jarDir.isDirectory()){
            deleteContent(jarDir);
        }
        else if(!jarDir.mkdirs())
            return null;

        ZipFile zipFile = new ZipFile(mAppContext.getPackageResourcePath());
        zipFile.extractAll(jarDir.getPath());

        File[] unneeded = jarDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().equals("META-INF") || file.getName().equals("keys");
            }
        });

        if(unneeded.length > 0){
            for(File del : unneeded)
                deleteRecursively(del);
        }

        return jarDir;
    }

    private File createJar(File contentDir) throws IOException {
        File unSignedApkFile = new File(mAppContext.getExternalCacheDir(), UnSignedApkName);

        JarWriter writer = new JarWriter();
        writer.setIncludeManifest(false);
        writer.setFiles(getPaths(contentDir));
        writer.createJar(unSignedApkFile, CompressionLevel);

        return unSignedApkFile;
    }

    private File signApk(File unsignedApk) throws
            IllegalAccessException,
            InstantiationException,
            ClassNotFoundException,
            IOException,
            GeneralSecurityException {
        File signedApkFile = new File(mAppContext.getExternalCacheDir(), SignedApkName);
        ZipSigner zipSigner = new ZipSigner();
        zipSigner.setKeymode("auto-testkey");
        zipSigner.signZip(unsignedApk.getPath(), signedApkFile.getPath());

        return signedApkFile;
    }

    private static String[] getPaths(File dir){
        File[] files = dir.listFiles();
        String[] paths = new String[files.length];
        for (int i = 0; i< files.length; i++){
            paths[i] = files[i].getPath();
        }

        return paths;
    }

    private static void deleteContent(File folder){
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteRecursively(f);
                } else {
                    f.delete();
                }
            }
        }
    }

    private static void deleteRecursively(File folder) {
        if(folder.isDirectory())
            deleteContent(folder);
        folder.delete();
    }
}
