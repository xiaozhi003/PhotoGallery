package com.example.photogallery;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class HttpUtil {

	public static HttpResponse getHttpResponse(String url)
			throws ClientProtocolException, IOException {

		// get请求
		HttpGet get = new HttpGet(url);
		// 请求客户
		DefaultHttpClient client = new DefaultHttpClient();
		// 请求超时
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
		// 读取超时
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
		return client.execute(get);
	}

	/***
	 * 获取logo
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getImageFromUrl(String action) {
		Bitmap icon = null;
		try {

			URL url = new URL(action);
			HttpURLConnection hc = (HttpURLConnection) url.openConnection();
			icon = BitmapFactory.decodeStream(hc.getInputStream());
			hc.disconnect();
		} catch (Exception e) {
		}
		return icon;

	}
}