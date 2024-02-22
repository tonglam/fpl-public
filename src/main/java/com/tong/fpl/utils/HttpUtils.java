package com.tong.fpl.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Create by tong on 2018/2/8
 */
public class HttpUtils {

	private static final CookieStore cookieStore = new BasicCookieStore();
	private static final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	private static final CloseableHttpClient httpclient = createHttpClient();

	private static CloseableHttpClient createHttpClient() {
		cm.setMaxTotal(10);
		cm.setDefaultMaxPerRoute(10);
		return HttpClients.custom()
				.setDefaultCookieStore(cookieStore)
				.setConnectionManager(cm)
				.setConnectionManagerShared(true)
				.build();
	}

	public static Optional<String> httpGet(String url) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
			if (response.getStatusLine().getStatusCode() == 200) {
				String result = EntityUtils.toString(response.getEntity(), "UTF-8");
				return Optional.of(result);
			}
		} catch (Exception e) {
			throw new ExportException(e.getMessage());
		} finally {
			httpclient.close();
		}
		return Optional.empty();
	}

	public static Optional<InputStream> httpGetStream(String url) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
			if (response.getStatusLine().getStatusCode() == 200) {
				return Optional.of(response.getEntity().getContent());
			}
		} catch (Exception e) {
			throw new ExportException(e.getMessage());
		} finally {
			httpclient.close();
		}
		return Optional.empty();
	}

	/**
	 * http post
	 *
	 * @param map param
	 * @param url url
	 * @return return
	 */
	public static Optional<String> httpPost(Map<String, String> map, String url) throws IOException {
		HttpClientContext httpClientContext = HttpClientContext.create();
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> parameters = new ArrayList<>(0);
		for (String key :
				map.keySet()) {
			parameters.add(new BasicNameValuePair(key, map.get(key)));
		}
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
		httpPost.setEntity(formEntity);
		try (CloseableHttpResponse response = httpclient.execute(httpPost, httpClientContext)) {
			if (response.getStatusLine().getStatusCode() == 200) {
				String result = EntityUtils.toString(response.getEntity(), "UTF-8");
				return Optional.of(result);
			}
		} catch (Exception e) {
			throw new ExportException(e.getMessage());
		} finally {
			httpclient.close();
		}
		return Optional.empty();
	}

	/**
	 * 获取真实ip
	 */
	public static String getRealIp(HttpServletRequest req) {
		String x_ip = req.getHeader("Cdn-Src-Ip");
		if (StringUtils.isEmpty(x_ip)) {
			x_ip = req.getHeader("X-Forwarded-For");
		}
		if (StringUtils.isEmpty(x_ip)) {
			x_ip = req.getHeader("X-Real-IP");
		}
		if (StringUtils.isEmpty(x_ip)) {
			x_ip = req.getRemoteAddr();
		}
		return x_ip;
	}

}
