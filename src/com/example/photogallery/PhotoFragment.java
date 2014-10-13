package com.example.photogallery;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * 显示大图的Fragment
 * 
 * @author asus
 * 
 */
public class PhotoFragment extends Fragment {

	private static final String TAG = "PhotoFragment";

	public static final String EXTRA_PHOTO_ID = "com.example.photogallery.photo_id";

	private GalleryItem mGalleryItem;

	private ImageView mImageView;

	private ImageLoaderTask imageLoaderTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// 获取id值
		String id = getArguments().getString(EXTRA_PHOTO_ID);
		Log.i(TAG, "id: " + id);

		// 获取GalleryItem实例
		for (GalleryItem item : PhotoGalleryFragment.mItems) {
			if (item.getId().equals(id)) {
				mGalleryItem = item;
				break;
			}
		}
		imageLoaderTask = new ImageLoaderTask(mGalleryItem.getUrl());
		imageLoaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_photo, container, false);

		mImageView = (ImageView) v.findViewById(R.id.photo_imageView);
		mImageView.setImageResource(R.drawable.empty_photo);
		return v;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (imageLoaderTask != null)
			imageLoaderTask.cancel(true);
	}

	/*
	 * 附加argument的frament
	 */
	public static PhotoFragment newInstance(String id) {
		Bundle args = new Bundle();
		args.putString(EXTRA_PHOTO_ID, id);

		PhotoFragment fragment = new PhotoFragment();
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * 加载大图片任务
	 */
	private class ImageLoaderTask extends AsyncTask<Void, Void, Bitmap> {
		private String url;

		public ImageLoaderTask(String url) {
			this.url = url;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Log.i(TAG, "url: " + url);
			Bitmap bitmap = ImageLoader.getInstance().loadImage(url);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			mImageView.setImageBitmap(result);
		}
	}
	

}
