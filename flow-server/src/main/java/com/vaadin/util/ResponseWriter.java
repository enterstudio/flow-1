/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The class that handles writing the response data into the response.
 *
 * @author Vaadin Ltd.
 */
public class ResponseWriter implements Serializable {
    private static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    private final int bufferSize;

    /**
     * Create a response writer with buffer size equal to
     * {@link ResponseWriter#DEFAULT_BUFFER_SIZE}.
     */
    public ResponseWriter() {
        this(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a response writer with custom buffer size.
     * 
     * @param bufferSize
     *            custom buffer size
     */
    public ResponseWriter(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Writes the contents and content type (if available) of the given
     * resourceUrl to the response.
     *
     * @param filenameWithPath
     *            the name of the file being sent
     * @param resourceUrl
     *            the URL to the file, reported by the servlet container
     * @param request
     *            the request object to read from
     * @param response
     *            the response object to write to
     * @throws IOException
     *             if the servlet container threw an exception while locating
     *             the resource
     */
    public void writeResponseContents(String filenameWithPath, URL resourceUrl,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        writeContentType(filenameWithPath, request, response);

        URLConnection connection = null;
        InputStream dataStream = null;

        if (acceptsGzippedResource(request)) {
            // try to serve a gzipped version if available
            String gzippedFilenameWithPath = filenameWithPath + ".gz";
            try {
                URL url = request.getServletContext()
                        .getResource(gzippedFilenameWithPath);
                if (url != null) {
                    connection = url.openConnection();
                    dataStream = connection.getInputStream();
                    response.setHeader("Content-Encoding", "gzip");
                }
            } catch (Exception e) {
                getLogger().log(Level.FINE,
                        "Unexpected exception looking for gzipped resource "
                                + gzippedFilenameWithPath,
                        e);
            }
        }
        if (dataStream == null) {
            // gzipped resource not available, get non compressed
            connection = resourceUrl.openConnection();
            dataStream = connection.getInputStream();
        }

        try {
            long length = connection.getContentLengthLong();
            if (length >= 0L) {
                response.setContentLengthLong(length);
            }
        } catch (Exception e) {
            getLogger().log(Level.FINE, "Error setting the content length", e);
        }

        try {
            writeStream(response.getOutputStream(), dataStream);
        } catch (IOException e) {
            getLogger().log(Level.FINE, "Error writing static file to user", e);
        } finally {
            try {
                dataStream.close();
            } catch (IOException e) {
                getLogger().log(Level.FINE,
                        "Error closing input stream for resource", e);
            }
        }
    }

    private void writeStream(ServletOutputStream outputStream,
            InputStream inputStream) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        int bytes;
        while ((bytes = inputStream.read(buffer)) >= 0) {
            outputStream.write(buffer, 0, bytes);
        }

    }

    /**
     * Returns whether it is ok to serve a gzipped version of the given
     * resource.
     * <p>
     * If this method returns true, the browser is ok with receiving a gzipped
     * version of the resource. In other cases, an uncompressed file must be
     * sent.
     *
     * @param request
     *            the request for the resource
     * @return true if the servlet should attempt to serve a precompressed
     *         version of the resource, false otherwise
     */
    protected boolean acceptsGzippedResource(HttpServletRequest request) {
        String accept = request.getHeader("Accept-Encoding");
        if (accept == null) {
            return false;
        }

        accept = accept.replace(" ", "");
        // Browser denies gzip compression if it reports
        // gzip;q=0
        //
        // Browser accepts gzip compression if it reports
        // "gzip"
        // "gzip;q=[notzero]"
        // "*"
        // "*;q=[not zero]"
        if (accept.contains("gzip")) {
            return !isQZero(accept, "gzip");
        }
        return accept.contains("*") && !isQZero(accept, "*");
    }

    void writeContentType(String filenameWithPath, ServletRequest request,
            ServletResponse response) {
        // Set type mime type if we can determine it based on the filename
        String mimetype = request.getServletContext()
                .getMimeType(filenameWithPath);
        if (mimetype != null) {
            response.setContentType(mimetype);
        }
    }

    private static boolean isQZero(String acceptEncoding, String encoding) {
        String qPrefix = encoding + ";q=";
        int qValueIndex = acceptEncoding.indexOf(qPrefix);
        if (qValueIndex == -1) {
            return false;
        }

        // gzip;q=0.123 or gzip;q=0.123,compress...
        String qValue = acceptEncoding
                .substring(qValueIndex + qPrefix.length());
        int endOfQValue = qValue.indexOf(',');
        if (endOfQValue != -1) {
            qValue = qValue.substring(0, endOfQValue);
        }
        return Objects.equals("0", qValue) || Objects.equals("0.0", qValue)
                || Objects.equals("0.00", qValue)
                || Objects.equals("0.000", qValue);
    }

    private Logger getLogger() {
        return Logger.getLogger(getClass().getName());
    }
}
