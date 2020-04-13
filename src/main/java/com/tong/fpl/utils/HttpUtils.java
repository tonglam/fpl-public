package com.tong.fpl.utils;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.log.InterfaceLog;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2018/2/8
 */
public class HttpUtils {

    private static CookieStore cookieStore = new BasicCookieStore();
    private static List<Cookie> cookieList = Lists.newArrayList();

    public static String httpGet(String url) throws IOException {
        long startTime = System.currentTimeMillis();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                InterfaceLog.info("url:{" + url + "}, args:{null}, " +
                        "response:{" + result + "}, elapsed time:{" + (System.currentTimeMillis() - startTime) + "}");
                return result;
            }
        } catch (Exception e) {
            throw new ExportException(e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
        return null;
    }

    public static String httpGetWithHeader(String url, String profile) throws IOException {
        long startTime = System.currentTimeMillis();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("PL_PROFILE", profile);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
//                InterfaceLog.info("url:{" + url + "}, args:{null}, " +
//                        "response:{" + result + "}, elapsed time:{" + (System.currentTimeMillis() - startTime) + "}");
                return result;
            }
        } catch (Exception e) {
            throw new ExportException(e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
        return null;
    }

    /**
     * http get(带参数)
     *
     * @param map 参数
     * @param url url
     * @return 返回信息
     * @throws IOException IO异常
     */
    public static String httpGetWithParams(Map<String, String> map, String url) throws IOException {
        long startTime = System.currentTimeMillis();
        url = attachHttpGetParams(map, url);
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                InterfaceLog.info("url:{" + url + "}, args:" + map.toString() +
                        ", response:" + result + ", elapsed time:{" + (System.currentTimeMillis() - startTime) + "}");
            }
        } catch (Exception e) {
            throw new ExportException(e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
        return null;
    }

    /**
     * http post
     *
     * @param map 参数
     * @param url url
     * @return 返回结果
     * @throws IOException IO异常
     */
    public static String httpPost(Map<String, String> map, String url) throws IOException {
        long startTime = System.currentTimeMillis();
        HttpClientContext httpClientContext = HttpClientContext.create();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> parameters = new ArrayList<>(0);
        for (String key :
                map.keySet()) {
            parameters.add(new BasicNameValuePair(key, map.get(key)));
        }
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
        httpPost.setEntity(formEntity);
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpclient.execute(httpPost, httpClientContext);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                InterfaceLog.info("url:{" + url + "}, args:" + map.toString() +
                        ", response:" + result + ", elapsed time:{" + (System.currentTimeMillis() - startTime) + "}");
                return result;
            }
        } catch (Exception e) {
            throw new ExportException(e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
        return null;
    }

    public static void httpLogin(String username, String password) throws IOException {
        String url = Constant.LOGIN;
        long startTime = System.currentTimeMillis();
        HttpClientContext httpClientContext = HttpClientContext.create();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> parameters = new ArrayList<>(0);
        parameters.add(new BasicNameValuePair("login", username));
        parameters.add(new BasicNameValuePair("password", password));
        parameters.add(new BasicNameValuePair("app", "plfpl-web"));
        parameters.add(new BasicNameValuePair("redirect_uri", "https://fantasy.premierleague.com/"));
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
        httpPost.setEntity(formEntity);
        // 伪装浏览器
        httpPost.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpclient.execute(httpPost, httpClientContext);
            String setCookie = response.getFirstHeader("Set-Cookie").getValue();
            String plProfile = setCookie.substring("pl_profile=".length(), setCookie.indexOf(";"));
            BasicClientCookie cookie = new BasicClientCookie("pl_profile", plProfile);
            cookieStore.addCookie(cookie);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
//                InterfaceLog.info("url:{" + url + "}, args:" + map.toString() +
//                        ", response:" + result + ", elapsed time:{" + (System.currentTimeMillis() - startTime) + "}");
            }
        } catch (Exception e) {
            throw new ExportException(e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }

    /**
     * 为http的get方法的url方便的添加多个键值参数
     *
     * @param url    原url
     * @param params 参数
     * @return 拼接好参数的url
     */
    private static String attachHttpGetParams(Map<String, String> params, String url) {
        Iterator<String> keys = params.keySet().iterator();
        Iterator<String> values = params.values().iterator();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("?");
        for (int i = 0; i < params.size(); i++) {
            String value = null;
            try {
                value = URLEncoder.encode(values.next(), "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            stringBuilder.append(keys.next());
            stringBuilder.append("=");
            stringBuilder.append(value);
            if (i != params.size() - 1) {
                stringBuilder.append("&");
            }
        }
        return url + stringBuilder.toString();
    }

}
