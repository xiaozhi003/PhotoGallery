package com.example.photogallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

public class FlickrFetchr {

	private static final String TAG = "FlickrFetchr";
	
	public static final String PREF_SEARCH_QUERY = "searchQuery";
	public static final String PREF_LAST_RESULT_ID = "lastResultId";

	private static final String ENDPOINT = "http://image.baidu.com/channel/listjson";
	private static final String PN = "pn";
	private static final String RN = "rn";
	private static final String TAG1 = "tag1";
	private static final String TAG2 = "tag2";

	byte[] getUrlBytes(String urlSpec) throws IOException {
		URL url = new URL(urlSpec);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}

			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			return out.toByteArray();
		} finally {
			connection.disconnect();
		}
	}

	public String getUrl(String urlSpec) throws IOException {
		return new String(getUrlBytes(urlSpec));
	}

	/**
	 * 下载图片模型数据
	 * 
	 * @param url
	 * @return
	 */
	public ArrayList<GalleryItem> downloadGalleryItems(String url) {
		ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();

		try {
			String jsonString = getUrl(url);
			Log.i(TAG, "Received json: ");

			JSONObject jsonObject = new JSONObject(jsonString);
			parseItems(items, jsonObject);
		} catch (IOException e) {
			Log.e(TAG, "Failed to fetch items", e);
		} catch (JSONException e) {
			Log.e(TAG, "Failed to parse items", e);
		}
		return items;
	}

	/**
	 * 获取图片模型列表
	 * 
	 * @return
	 */
	public ArrayList<GalleryItem> fetchItems(int pn, String tag2) {
		ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();

		String url = Uri.parse(ENDPOINT).buildUpon()
				.appendQueryParameter(PN, String.valueOf(pn))
				.appendQueryParameter(RN, "14")
				.appendQueryParameter(TAG1, "美女")
				.appendQueryParameter(TAG2, tag2).build().toString();
		return downloadGalleryItems(url);
	}

	/**
	 * 解析json数据
	 * 
	 * @param items
	 * @param jo
	 */
	void parseItems(ArrayList<GalleryItem> items, JSONObject jo) {
		try {
			JSONArray jsonArray = jo.getJSONArray("data");
			int jsonLength = jsonArray.length();
			for (int i = 0; i < jsonLength - 1; i++) {
				JSONObject dataObject = (JSONObject) jsonArray.opt(i);
				if (dataObject != null) {
					String id = dataObject.getString("id");
					String caption = dataObject.getString("desc");
					String smalUrl = dataObject.getString("share_url");

					GalleryItem item = new GalleryItem();
					item.setId(id);
					item.setCaption(caption);
					item.setUrl(smalUrl);

					items.add(item);
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "Failed to parse items", e);
		}
	}

}
