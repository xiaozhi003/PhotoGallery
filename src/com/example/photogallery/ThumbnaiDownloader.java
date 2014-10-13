package com.example.photogallery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

/**
 * 下载图片后台线程
 * 
 * @author asus
 * 
 */
public class ThumbnaiDownloader<Token> extends HandlerThread {
	private static final String TAG = "ThumbnaiDownloader";
	private static final int MESSAGE_DOENLOAD = 0;
	private ImageLoader mImageLoader = ImageLoader.getInstance();

	Handler mHandler;
	Map<Token, String> requestMap = Collections
			.synchronizedMap(new HashMap<Token, String>());
	Handler mResponseHandler;
	Listener<Token> mListener;

	public interface Listener<Token> {
		void onThumbnailDownloaded(Token token, Bitmap thumbnail);
	}

	public void setListener(Listener<Token> listener) {
		mListener = listener;
	}

	public ThumbnaiDownloader(Handler responseHandler) {
		super(TAG);
		mResponseHandler = responseHandler;
	}

	public void queueThumbnail(Token token, GalleryItem item) {
		requestMap.put(token, item.getUrl());
		Log.i(TAG, "Got an URL: " + item.getUrl());

		mHandler.obtainMessage(MESSAGE_DOENLOAD, token).sendToTarget();
	}

	@SuppressLint("HandlerLeak")
	@Override
	protected void onLooperPrepared() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MESSAGE_DOENLOAD) {
					@SuppressWarnings("unchecked")
					Token token = (Token) msg.obj;
					Log.i(TAG,
							"Got a request for url: " + requestMap.get(token));
					handleRequest(token);
				}
			}
		};
	}

	private void handleRequest(final Token token) {
		final String url = requestMap.get(token);
		if (url == null) {
			return;
		}
		// 获取图片
		// byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
		final Bitmap bitmap = mImageLoader.loadImage(url, 120);
		Log.i(TAG, "Bitmap created");

		mResponseHandler.post(new Runnable() {

			@Override
			public void run() {
				if (requestMap.get(token) != url) {
					return;
				}

				requestMap.remove(token);
				mListener.onThumbnailDownloaded(token, bitmap);
			}
		});
	}

	public void clearQueue() {
		mHandler.removeMessages(MESSAGE_DOENLOAD);
		requestMap.clear();
	}
}
