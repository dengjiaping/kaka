/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.zmdx.kaka.locker.network;

import cn.zmdx.kaka.locker.content.DiskImageHelper;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class DownloadRequest extends Request<String> {
    private final Listener<String> mListener;

    private final String mUrl;

    /**
     * Creates a new request with the given method.
     * 
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param download_apth path to save the file to
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public DownloadRequest(String url, Listener<String> listener,
            ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mUrl = url;
        mListener = listener;
    }

    @Override
    protected void deliverResponse(String response) {
        if (null != mListener) {
            mListener.onResponse(response);
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        DiskImageHelper.put(mUrl, response.data);
        return Response.success(DiskImageHelper.getFileByUrl(mUrl).getAbsolutePath(), HttpHeaderParser.parseCacheHeaders(response));
    }
}