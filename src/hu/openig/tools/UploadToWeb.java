/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.io.*;
import java.net.*;

import javax.net.ssl.HttpsURLConnection;

import hu.openig.utils.*;

/**
 * Utility class to upload files to the google-code site.
 * @author akarnokd, 2011.08.24.
 */
public final class UploadToWeb {
    /** Utility class. */
    private UploadToWeb() {

    }
    /**
     * Main program.
     * @param args first argument password, subsequent arguments are the files
     */
    public static void main(String[] args) {
    }
    /**
     * Upload a file from the local directory into the specified project.
     * @param username the username
     * @param password the SVN password
     * @param project the project name
     * @param filename the file
     */
    static void uploadFile(String username, String password, String project, String filename) {
        try {

            String boundary = "----------Googlecode_boundary_reindeer_flotilla";

            URL u = new URI(String.format("https://%s.googlecode.com/files", project)).toURL();
            HttpsURLConnection c = (HttpsURLConnection)u.openConnection();
            try {
                String up = Base64.encodeBytes(String.format("%s:%s", username, password).getBytes("UTF-8"));
                c.setRequestProperty("Authorization", "Basic " + up);
                c.setRequestProperty("Content-Type", String.format("multipart/form-data; boundary=%s", boundary));
                c.setRequestProperty("User-Agent", "Open-IG Google Code Upload 0.1");
                c.setRequestMethod("POST");
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setAllowUserInteraction(false);

                c.connect();

                try (OutputStream out = c.getOutputStream()) {

                    out.write(("--" + boundary + "\r\n").getBytes("ISO-8859-1"));
                    out.write("Content-Disposition: form-data; name=\"summary\"\r\n\r\nUpload.\r\n".getBytes("ISO-8859-1"));

                    out.write(("--" + boundary + "\r\n").getBytes("ISO-8859-1"));
                    out.write(String.format("Content-Disposition: form-data; name=\"filename\"; filename=\"%s1\"\r\n", filename).getBytes("ISO-8859-1"));
                    out.write("Content-Type: application/octet-stream\r\n".getBytes("ISO-8859-1"));
                    out.write("\r\n".getBytes("ISO-8859-1"));
                    out.write(IOUtils.load(filename));
                    out.write(("\r\n\r\n--" + boundary + "--\r\n").getBytes("ISO-8859-1"));
                    out.flush();
                }

                System.out.write(IOUtils.load(c.getInputStream()));

                System.out.println(c.getResponseCode() + ": " + c.getResponseMessage());
            } finally {
                c.disconnect();
            }
        } catch (Exception ex) {
            Exceptions.add(ex);
        }
    }
}
