/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork2.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


/**
 * FileManager
 * <p/>
 * This class was brought in from oscore trunk revision 147.
 *
 * @author Jason Carreira
 *         Created May 7, 2003 8:44:26 PM
 */
public class FileManager {
    //~ Static fields/initializers /////////////////////////////////////////////

    private static Map<String, Revision> files = Collections.synchronizedMap(new HashMap<String, Revision>());
    protected static boolean reloadingConfigs = true;

    private static final String JAR_FILE_NAME_PREFIX = "jar:file:";
    private static final String JAR_FILE_NAME_SEPARATOR = "!/";


    //~ Constructors ///////////////////////////////////////////////////////////

    private FileManager() {
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public static void setReloadingConfigs(boolean reloadingConfigs) {
        FileManager.reloadingConfigs = reloadingConfigs;
    }

    public static boolean isReloadingConfigs() {
        return reloadingConfigs;
    }

    public static boolean fileNeedsReloading(String fileName, Class clazz) {
        URL fileUrl = ClassLoaderUtil.getResource(fileName, clazz);
        return fileUrl != null && fileNeedsReloading(fileUrl.toString());
    }
      
    public static boolean fileNeedsReloading(String fileName) {
        Revision revision = files.get(fileName);

        if (revision == null) {
            // no revision yet and we keep the revision history, so
            // return whether the file needs to be loaded for the first time
            return reloadingConfigs;
        }

        return revision.needsReloading();
    }

    /**
     * Loads opens the named file and returns the InputStream
     *
     * @param fileName - the name of the file to open
     * @return an InputStream of the file contents or null
     * @throws IllegalArgumentException if there is no file with the given file name
     */
    public static InputStream loadFile(String fileName, Class clazz) {
        URL fileUrl = ClassLoaderUtil.getResource(fileName, clazz);
        return loadFile(fileUrl);
    }

    /**
     * Loads opens the named file and returns the InputStream
     *
     * @param fileUrl - the URL of the file to open
     * @return an InputStream of the file contents or null
     * @throws IllegalArgumentException if there is no file with the given file name
     */
    public static InputStream loadFile(URL fileUrl) {
        return loadFile(fileUrl, true);
    }

    /**
     * Loads opens the named file and returns the InputStream
     *
     * @param fileUrl    - the URL of the file to open
     * @param openStream - if true, open an InputStream to the file and return it
     * @return an InputStream of the file contents or null
     * @throws IllegalArgumentException if there is no file with the given file name
     */
    public static InputStream loadFile(URL fileUrl, boolean openStream) {
        if (fileUrl == null) {
            return null;
        }

        String fileName =  fileUrl.toString();
        InputStream is = null;

        if (openStream) {
            try {
                is = fileUrl.openStream();

                if (is == null) {
                    throw new IllegalArgumentException("No file '" + fileName + "' found as a resource");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("No file '" + fileName + "' found as a resource");
            }
        }

        if (isReloadingConfigs()) {
            Revision revision;

            if (fileName.startsWith(JAR_FILE_NAME_PREFIX)) {
                revision = JarEntryRevision.build(fileUrl);
            } else {
                revision = FileRevision.build(fileUrl);
            }
            if (revision == null) {
                files.put(fileName, Revision.build(fileUrl));
            } else {
                files.put(fileName, revision);
            }
        }
        return is;
    }

    //~ Inner Classes //////////////////////////////////////////////////////////

    /**
     * Class represents common revsion resource, should be used as default class when no other option exists
     */
    private static class Revision {

        public Revision() {
        }

        public boolean needsReloading() {
            return false;
        }

        public static Revision build(URL fileUrl) {
            return new Revision();
        }
    }

    /**
     * Represents file resource revision, used for file://* resources
     */
    private static class FileRevision extends Revision {
        private File file;
        private long lastModified;

        public FileRevision(File file, long lastUpdated) {
            if (file == null) {
                throw new IllegalArgumentException("File cannot be null");
            }

            this.file = file;
            this.lastModified = lastUpdated;
        }

        public File getFile() {
            return file;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        public long getLastModified() {
            return lastModified;
        }

        public boolean needsReloading() {
            return this.lastModified < this.file.lastModified();
        }

        public static Revision build(URL fileUrl) {
            File file;
            try {
                file = new File(fileUrl.toURI());
            } catch (URISyntaxException e) {
                file = new File(fileUrl.getPath());
            }
            if (file.exists() && file.canRead()) {
                long lastModified = file.lastModified();
                return new FileRevision(file, lastModified);
            }
            return null;
        }
    }

    /**
     * Represents jar resurce revision, used for jar://* resource
     */
    private static class JarEntryRevision extends Revision {

        private String jarFileName;
        private String fileNameInJar;
        private long lastModified;

        public JarEntryRevision(String jarFileName, String fileNameInJar, long lastModified) {
            if ((jarFileName == null) || (fileNameInJar == null)) {
                throw new IllegalArgumentException("JarFileName and FileNameInJar cannot be null");
            }
            this.jarFileName = jarFileName;
            this.fileNameInJar = fileNameInJar;
            this.lastModified = lastModified;
        }

        public boolean needsReloading() {
            ZipEntry entry;
            try {
                JarFile jarFile = new JarFile(this.jarFileName);
                entry = jarFile.getEntry(this.fileNameInJar);
            }
            catch (IOException e) {
                entry = null;
            }

            if (entry != null) {
                return (this.lastModified < entry.getTime());
            } else {
                return false;
            }
        }

        public static Revision build(URL fileUrl) {
            // File within a Jar
            // Find separator index of jar filename and filename within jar
            try {
                String fileName = fileUrl.toString();
                int separatorIndex = fileName.indexOf(JAR_FILE_NAME_SEPARATOR);
                // Split file name
                String jarFileName = fileName.substring(JAR_FILE_NAME_PREFIX.length(), separatorIndex);
                String fileNameInJar = fileName.substring(separatorIndex + JAR_FILE_NAME_SEPARATOR.length()).replaceAll("%20", " ");

                JarFile jarFile = new JarFile(jarFileName);
                ZipEntry entry = jarFile.getEntry(fileNameInJar);
                return new JarEntryRevision(jarFileName.toString(), fileNameInJar, entry.getTime());
            } catch (IOException e) {
                return null;
            }
        }
    }

}
