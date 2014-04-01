package spikes.morphing;

/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 *
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 *
 * END_COPYRIGHT_BLOCK*/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarBuilder {
    private JarOutputStream _output;

    /** Creates a file file without a manifest
     *
     * @param file the file to write the jar to
     * @throws java.io.IOException thrown if the file cannot be opened for writing
     */
    public JarBuilder(File file) throws IOException {
        _output = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(file)), ManifestWriter.DEFAULT);
    }

    /** Creates an empty jar file with the given manifest
     *
     * @param jar      the file to write the jar to
     * @param manifest the file that is the manifest for the archive
     * @throws java.io.IOException thrown if either file cannot be opened for reading
     */
    public JarBuilder(File jar, File manifest) throws IOException {
        _output = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jar)), new Manifest(new FileInputStream(manifest)));
    }

    /** Creates an empty jar file with the given manifest
     *
     * @param jar      the file to write the jar to
     * @param manifest the manifest file for the jar
     * @see ManifestWriter
     */
    public JarBuilder(File jar, Manifest manifest) {
        try {
            _output = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jar)), manifest);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Takes a parent name and a field name and returns the concatenation of them correctly
     *
     * @param parent The parent directory
     * @param name   The name of the file or directory
     * @return the string concatenation of the parent and the name
     */
    private String makeName(String parent, String name) {
        String sep = "/"; // NOTE: This can be a '/' since it is a path in the jar file itself
        if( parent.equals("") )
            return name;
        if (parent.endsWith(sep))
            return parent + name;
        return parent + sep + name;
    }

    /** Adds the file to the given path and name
     *
     * @param file     the file to be added
     * @param parent   the directory to the path in which the file is to be added
     * @param fileName the name of the file in the archive
     */
    public void addFile(File file, String parent, String fileName) throws IOException {
        byte data[] = new byte[2048];

        FileInputStream fi = new FileInputStream(file.getAbsolutePath());
        BufferedInputStream origin = new BufferedInputStream(fi, 2048);

        JarEntry entry = new JarEntry(makeName(parent, fileName));
        _output.putNextEntry(entry);

        int count = origin.read(data, 0, 2048);
        while (count != -1) {
            _output.write(data, 0, count);
            count = origin.read(data, 0, 2048);
        }

        origin.close();
    }

    /** Add the directory into the directory specified by parent
     * @param dir the directory to add
     * @param parent the path inside the jar that the directory should be added to
     */
    public void addDirectoryRecursive(File dir, String parent) {
        addDirectoryRecursiveHelper(dir, parent, new byte[2048], new FileFilter() {
            public boolean accept(File pathname) { return true; }
        });
    }

    /** Add the directory into the directory specified by parent
     * @param dir the directory to add
     * @param parent the path inside the jar that the directory should be added to
     * @param filter the filter used to filter the files
     */
    public void addDirectoryRecursive(File dir, String parent, FileFilter filter) {
        addDirectoryRecursiveHelper(dir, parent, new byte[2048], filter);
    }

    /** Add the contents of a directory that match a filter to the archive
     * @param dir the directory to add
     * @param parent the directory to add into
     * @param buffer a buffer that is 2048 bytes
     * @param filter the FileFilter to filter the files by
     * @return true on success, false on failure
     */
    private boolean addDirectoryRecursiveHelper(File dir, String parent, byte[] buffer, FileFilter filter) {
        try {
            File[] files = dir.listFiles(filter);
            BufferedInputStream origin = null;

            if( files == null ) // listFiles may return null if there's an IO error
                return true;
            for (int i = 0; i < files.length; i++) {
                if( files[i].isFile() ) {
                    origin = new BufferedInputStream(new FileInputStream(files[i]), 2048);

                    JarEntry entry = new JarEntry(makeName(parent, files[i].getName()));
                    _output.putNextEntry(entry);

                    int count;
                    while((count = origin.read(buffer, 0, 2048)) != -1) {
                        _output.write(buffer, 0, count);
                    }
                    origin.close();
                }
                else if( files[i].isDirectory() ) {
                    addDirectoryRecursiveHelper(files[i], makeName(parent, files[i].getName()),buffer,filter);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /** Makes a directory in the jar file
     *
     * @param parent  The name of the parent that the directory is to be created in
     * @param dirName The name of the directory to be created
     * @return Returns true on success, false on failure
     */
    public boolean makeDirectory(String parent, String dirName) {
        JarEntry entry = new JarEntry(makeName(parent, dirName));
        try {
            _output.putNextEntry(entry);
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }

    /** Close writing on the jar file
     */
    public void close() throws IOException {
        _output.flush();
        _output.close();
    }
}

class ManifestWriter {
    private List<String> _classPaths;
    private String _mainClass;
    private String _rawManifest;
    public static final Manifest DEFAULT = new ManifestWriter().getManifest();

    /** Create a new manifest file */
    public ManifestWriter() {
        _classPaths = new LinkedList<String>();
        _mainClass = null;
        _rawManifest = null;
    }

    /** Add a class path to the Manifest
     * @param path the path to be added
     */
    public void addClassPath(String path) {
        _classPaths.add(_classPaths.size(), path);
    }

    /** Set the main class of the Manifest
     * @param mainClass
     */
    public void setMainClass(String mainClass) {
        _mainClass = mainClass;
        _rawManifest = null;
    }

    public void setManifestContents(String rawManifest) {
        _rawManifest =  rawManifest;
        _mainClass = null;
    }

    /** Get an input stream to the contents of the manifest file
     * @return an InputStream whose contents are the contents of the Manifest file
     */
    protected InputStream getInputStream() {
        // NOTE: All significant lines in the manifest MUST end in the end of line character

        final StringBuilder sbuf = new StringBuilder();
        sbuf.append(Attributes.Name.MANIFEST_VERSION.toString());
        sbuf.append(": 1.0" + "\n");
        if( !_classPaths.isEmpty() ) {
            Iterator<String> iter = _classPaths.iterator();
            sbuf.append(Attributes.Name.CLASS_PATH.toString());
            sbuf.append(":");
            while (iter.hasNext()) {
                sbuf.append(" ");
                sbuf.append(iter.next());
            }
            sbuf.append("\n");
        }
        if( _mainClass != null ) {
            sbuf.append(Attributes.Name.MAIN_CLASS.toString());
            sbuf.append(": ");
            sbuf.append(_mainClass);
            sbuf.append("\n");
        }

        if(_rawManifest != null) {
            sbuf.append(_rawManifest);

            if(!_rawManifest.endsWith("\n"))
                sbuf.append("\n");
        }

        try { return new ByteArrayInputStream(sbuf.toString().getBytes("UTF-8")); }
        catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
    }

    /** Get the Manifest object that this object created.
     * @return the Manifest that this builder created
     */
    public Manifest getManifest() {
        try {
            Manifest m = new Manifest();
            m.read(getInputStream());
            return m;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
