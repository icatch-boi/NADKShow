/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.icatchtek.nadk.show.kvsarchivedmedialibextend;

import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;
import com.icatch.smarthome.am.utils.DebugLogger;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * JSON unmarshaller for response GetClipResult
 */
public class GetClipResultJsonUnmarshaller implements
        Unmarshaller<GetClipResult, JsonUnmarshallerContext> {
    private static final String TAG = "GetClip";
    private static final int BUFFER_SIZE = 1024 * 4;
    private DownloadingProgressListen downloadingProgressListen = null;
    private String downloadPath;
    private long contentLength = 0;
    private long downloadSize = 0;

    public GetClipResultJsonUnmarshaller() {
    }

    public GetClipResultJsonUnmarshaller(DownloadingProgressListen downloadingProgressListen) {
        this.downloadingProgressListen = downloadingProgressListen;
    }

    public GetClipResultJsonUnmarshaller(String downloadPath, DownloadingProgressListen downloadingProgressListen) {
        this.downloadPath = downloadPath;
        this.downloadingProgressListen = downloadingProgressListen;
    }

    public GetClipResult unmarshall(JsonUnmarshallerContext context)
            throws Exception {
        GetClipResult getClipResult = new GetClipResult();
        DebugLogger.d(TAG, "context.getHttpResponse().getHeaders(): " + context.getHttpResponse().getHeaders().toString());
        DebugLogger.d(TAG, "context.getHttpResponse().getStatusCode(): " + context.getHttpResponse().getStatusCode());
        DebugLogger.d(TAG, "context.getHttpResponse().getStatusText(): " + context.getHttpResponse().getStatusText());
        DebugLogger.d(TAG, "context.getHttpResponse().getContent(): " + context.getHttpResponse().getContent().toString());
        DebugLogger.d(TAG, "context.getHttpResponse().getContent().available(): " + context.getHttpResponse().getContent().available());

        if (context.getHeader("Content-Type") != null)
            getClipResult.setContentType(context.getHeader("Content-Type"));

        if (context.getHeader("Content-Length") != null) {
            contentLength = Long.parseLong(context.getHeader("Content-Length"));
            downloadSize = 0;
            getClipResult.setContentLength(contentLength);
            if (downloadingProgressListen != null) {
                downloadingProgressListen.updateTotalSize(contentLength);
            }
        }

        getClipResult.setStatusCode(context.getHttpResponse().getStatusCode());
        getClipResult.setStatusText(context.getHttpResponse().getStatusText());

        InputStream is = context.getHttpResponse().getContent();
        if (is != null) {
            byte[] bytes = toByteArray(is);

            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bytes);
            if (downloadPath != null && !downloadPath.isEmpty() && downloadSize == contentLength) {
                writeToFile(bytes, downloadPath);
            }
            getClipResult.setPayload(bis);
            getClipResult.setDownloadSize(downloadSize);
        }

        return getClipResult;
    }

    private static GetClipResultJsonUnmarshaller instance;

    public static GetClipResultJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new GetClipResultJsonUnmarshaller();
        return instance;
    }


    private  byte[] toByteArray(InputStream is) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            final byte[] b = new byte[BUFFER_SIZE];
            int n = 0;
            while ((n = is.read(b)) != -1) {
//                DebugLogger.e(TAG, "n = is.read(b): " + n);
                output.write(b, 0, n);
                output.flush();
                downloadSize += n;
                if (downloadingProgressListen != null) {
                    downloadingProgressListen.updateDownloadSize(contentLength, downloadSize);
                }
            }
            return output.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            DebugLogger.e(TAG, "toByteArray: " + e.getClass().getSimpleName() + ", " + e.getMessage());
            throw e;
        }finally {
            output.close();
        }
    }

    private void writeToFile(byte[] bytes, String path) throws IOException {
        final FileOutputStream downloadFile = new FileOutputStream(path);
        try {
//            byte[] bytes = new byte[1024 * 1024 * 2];
//            int n = 0;
//            while ((n = is.read(bytes)) != -1) {
//                downloadFile.write(bytes, 0, n);
//                downloadFile.flush();
//                downloadSize += n;
//                if (downloadingProgressListen != null) {
//                    downloadingProgressListen.update(contentLength, downloadSize);
//                }
//            }

            downloadFile.write(bytes);
            downloadFile.flush();

        } catch (Exception e) {
            DebugLogger.e(TAG, "writeToFile: " + e.getClass().getSimpleName() + ", " + e.getMessage());
            throw e;
        } finally {
            downloadFile.close();
        }
    }
 }
