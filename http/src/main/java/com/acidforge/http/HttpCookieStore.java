/*
 * Copyright (C) 2017 The Acidforge Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.acidforge.http;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HttpCookieStore {
    private Map<URI, List<HttpCookie>> mapCookies = new HashMap<>();
    private final SharedPreferences spePreferences;

    @SuppressWarnings("unchecked")
    public HttpCookieStore(Context ctxContext) {
        spePreferences = ctxContext.getSharedPreferences("CookiePrefsFile", 0);
        Map<String, ?> prefsMap = spePreferences.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            for (String strCookie : (HashSet<String>) entry.getValue()) {
                if (!mapCookies.containsKey(entry.getKey())) {
                    List<HttpCookie> lstCookies = new ArrayList<>();
                    lstCookies.addAll(HttpCookie.parse(strCookie));
                    try {
                        mapCookies.put(new URI(entry.getKey()), lstCookies);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else {
                    List<HttpCookie> lstCookies = mapCookies.get(entry.getKey());
                    lstCookies.addAll(HttpCookie.parse(strCookie));
                    try {
                        mapCookies.put(new URI(entry.getKey()), lstCookies);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    /*
     * @see java.net.CookieStore#add(java.net.URI, java.net.HttpCookie)
     */
    public void add(URI uri, HttpCookie cookie) {
        System.out.println("add");
        System.out.println(cookie.toString());
        List<HttpCookie> cookies = mapCookies.get(uri);
        if (cookies == null) {
            cookies = new ArrayList<HttpCookie>();
            mapCookies.put(uri, cookies);
        }
        cookies.add(cookie);
        SharedPreferences.Editor ediWriter = spePreferences.edit();
        HashSet<String> setCookies = new HashSet<>();
        setCookies.add(cookie.toString());
        ediWriter.putStringSet(uri.toString(), spePreferences.getStringSet(uri.toString(), setCookies));
        ediWriter.commit();

    }


    /*
     * @see java.net.CookieStore#get(java.net.URI)
     */
    public List<HttpCookie> get(URI uri) {
        List<HttpCookie> lstCookies = mapCookies.get(uri);
        if (lstCookies == null)
            mapCookies.put(uri, new ArrayList<HttpCookie>());
        return mapCookies.get(uri);
    }

    /*
     * @see java.net.CookieStore#removeAll()
     */
    public boolean removeAll() {
        SharedPreferences.Editor ediWriter = spePreferences.edit();
        Map<String, ?> all = spePreferences.getAll();
        for (String k : all.keySet())
            ediWriter.remove(k);
        ediWriter.commit();
        mapCookies.clear();
        return true;
    }

    /*
     * @see java.net.CookieStore#getCookies()
     */
    public List<HttpCookie> getCookies() {
        Collection<List<HttpCookie>> values = mapCookies.values();
        List<HttpCookie> result = new ArrayList<>();
        for (List<HttpCookie> value : values) {
            result.addAll(value);
        }

        return result;

    }

    /*
     * @see java.net.CookieStore#getURIs()
     */
    public List<URI> getURIs() {
        Set<URI> keys = mapCookies.keySet();
        return new ArrayList<>(keys);
    }

    /*
     * @see java.net.CookieStore#remove(java.net.URI, java.net.HttpCookie)
     */
    public boolean remove(URI uri, HttpCookie cookie) {
        List<HttpCookie> lstCookies = mapCookies.get(uri);
        if (lstCookies == null)
            return false;
        return lstCookies.remove(cookie);
    }
}
