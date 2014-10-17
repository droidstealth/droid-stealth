package com.alexkolpa.appmorphing;

/*  JarWriter.java
 *
 *  Copyright (C) 2002-2003 Dominik Werthmueller
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;


/**
 * This class allows to easily build a jar file.
 *
 * @author Dominik Werthmueller
 *
 */
public class JarWriter {
    public interface JarProgressListener{
        public void onProgress(int filesWritten, int totalFileNumber);
    }

    public final static String FILESEPARATOR = System.getProperty("file.separator");

    private String[] files;
    private String manifest;
    private File manifestFile;
    private boolean includeManifest = true;
    private boolean aborted = false;
    private int writtenBytes;
    private int totalFileSize;
    private JarProgressListener mListener;

    private final int BUFFERSIZE = 32768;

    /**
     * construct an empty JarWriter class
     */
    public JarWriter() {
        manifest = "";
    }

    /**
     * construct an JarWriter class including the given files
     */
    public JarWriter(String[] f) {
        files = f;
        manifest = "";
    }

    /**
     * construct an JarWriter class including the given files and the given Manifest file
     */
    public JarWriter(String[] f, String m) {
        files = f;
        manifest = m;
    }

    public void setListener(JarProgressListener listener){
        mListener = listener;
    }

    /**
     * set the aborted variable
     * @param b true if the creation process should be stopped
     */
    public void setAborted(boolean b) {
        aborted = b;
    }

    /**
     * set the manifest include option
     * @param b true for including manifest, false for non-including manifest
     */
    public void setIncludeManifest(boolean b) {
        includeManifest = b;
    }

    /**
     * return the manifest include option
     * @return manifest include option
     */
    public boolean getIncludeManifest() {
        return includeManifest;
    }

    /**
     * set the files for the jar file
     * @param f files for the jar file
     */
    public void setFiles(String[] f) {
        files = f;
    }

    /**
     * return the files of the jar file
     * @return files of the jar file
     */
    public String[] getFiles() {
        return files;
    }

    /**
     * return the file at the given index of the jar file
     * @param i index
     * @return the file at the given index of the jar file
     */
    public String getFile(int i) {
        return files[i];
    }

    /**
     * set the manifest content for the jar file
     * @param m manifest content for the jar file
     */
    public void setManifest(String m) {
        manifest = m;
    }

    /**
     * return the manifest content of the jar file
     * @return manifest content of the jar file
     */
    public String getManifest() {
        return manifest;
    }

    /**
     * set the manifest file for the jar file
     * @param f manifest file for the jar file
     */
    public void setManifestFile(File f) {
        manifestFile = f;
    }

    /**
     * return the manifest file of the jar file
     * @return manifest file of the jar file
     */
    public File getManifestFile() {
        return manifestFile;
    }

    /**
     * return the number of files of the jar file
     * @return number of files of the jar file
     */
    public int getLength() {
        return files.length;
    }

    /**
     * calculate the size of one file (kb)
     * @param f file
     */
    private int getFileSize(File f) {
        File[] dContent;
        int dSize;

        if (!f.isDirectory()) {
            return (int)(f.length() / 1024f);
        }
        else {
            dContent = f.listFiles();
            dSize = 0;
            for (int a = 0; a < dContent.length; a++) {
                dSize += getFileSize(dContent[a]);
            }
            return dSize;
        }
    }


    /**
     * calculate the size of all files (kb)
     * @return the total file size
     */
    private int getTotalFileSize() {
        File f;
        int totalSize = 0;

        for (int i = 0; i < files.length; i++) {
            f = new File(files[i]);
            totalSize += getFileSize(f);
        }
        return totalSize;
    }


    /**
     * write the manifest file
     * @return the manifest file
     */
    private File writeManifest() throws IOException {
        File fManifest = File.createTempFile("Manifest",".tmp", null);
        BufferedWriter bout = new BufferedWriter(new FileWriter(fManifest));

        if (!manifest.equals("")) {
            bout.write("Manifest-Version: 1.0");
            bout.newLine();
            bout.write("Created-By: MorphTest 1.0");
            bout.newLine();
            bout.write(manifest);
            bout.newLine();
            bout.newLine();
        }

        bout.close();

        return fManifest;
    }


    /**
     * write the manifest entry in the jar file
     * @param f file to write
     * @param out current JarOutputStream
     */
    private void writeManifestEntry(File f, JarOutputStream out) throws IOException {
        // buffer
        byte[] buffer = new byte[BUFFERSIZE];

        // read bytes
        int bytes_read;


        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), BUFFERSIZE);
        String en = "META-INF" + "/" + "MANIFEST.MF";
        out.putNextEntry(new ZipEntry(en));
        while ((bytes_read = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytes_read);
        }

        in.close();
        out.closeEntry();
    }


    /**
     * write entries in the jar file
     * @param f file to write
     * @param out current JarOutputStream
     * @param depth depth of the directory structure
     * @return true if the file was successfully written, false if there was an error during the writing
     */
    private boolean writeEntry(File f, JarOutputStream out, int depth) {
        String en = "";
        File[] dContent;
        int i;
        String fPath;

        // buffer
        byte[] buffer = new byte[BUFFERSIZE];

        // read bytes
        int bytes_read;

        try {
            if (!f.isDirectory()) {
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), BUFFERSIZE);

                i = f.getPath().length();
                fPath = f.getPath();
                for (int a = 0; a <= depth; a++) {
                    i = fPath.lastIndexOf(FILESEPARATOR, i) - 1;
                }


                en = fPath.substring(i + 2, fPath.length());
                out.putNextEntry(new ZipEntry(en));

                while ((bytes_read = in.read(buffer)) != -1) {
                    // do the work
                    out.write(buffer, 0, bytes_read);

                    // check if the user has aborted the creation process
                    if (aborted) {
                        in.close();
                        out.closeEntry();
                        return false;
                    }

                    // update progress bar
                    writtenBytes += bytes_read;
                }

                in.close();
                out.closeEntry();
                return true;
            }
            else {
                dContent = f.listFiles();
                for (int a = 0; a < dContent.length; a++) {
                    writeEntry(dContent[a], out, depth + 1);
                    // check if the user has aborted the creation process
                    if (aborted) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }



    /**
     * the maximum length of the file's path (that will be displayed) should be 30
     * paste ... in the middle
     * @param s file path string to resize
     * @return resized string
     */
    private String resizeFilePathString(String s) {
        if (s.length() < 30) return s;
        else {
            String s1, s2;
            int index;

            s1 = s.substring(0, s.indexOf(FILESEPARATOR, 5) + 1);
            s2 = s.substring(s.lastIndexOf(FILESEPARATOR), s.length());

            return s1 + "..." + s2;
        }
    }



    /**
     * create a jar file including the files and the manifest file
     *
     * @param fj the jar file
     * @param compress true to compress, false to not compress
     * @return true if the file was successfully built, false if there was an error during the building process
     */
    public boolean createJar(File fj, int compress) throws IOException {
        boolean written;

        // target
        JarOutputStream out = new JarOutputStream(new FileOutputStream(fj));
        out.setComment("This file was created by JarBuilder\nCheck http://jarbuild.sourceforge.net");

        // set the compression rate
        out.setLevel(compress);

        // preparations
        totalFileSize = getTotalFileSize();

        aborted = false;
        writtenBytes = 0;

        // add files
        for (int i = 0; i < files.length; i++) {
            File entry = new File(files[i]);
            written = writeEntry(entry, out, 0);
            if (!written) {
                out.close();
                fj.delete();
                return false;
            }
            else {
                if(mListener != null)
                    mListener.onProgress(i, includeManifest? files.length+1:files.length);
            }
        }

        // add manifest
        if (includeManifest) {
            if (manifestFile == null) {
                File tempManifest = writeManifest();
                writeManifestEntry(tempManifest, out);
                tempManifest.delete();
            }
            else {
                writeManifestEntry(manifestFile, out);
            }
        }

        out.close();
        return true;
    }
}
