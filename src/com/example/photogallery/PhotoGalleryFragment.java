package com.example.photogallery;

import java.util.ArrayList;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

public class PhotoGalleryFragment extends Fragment {

	private static final String TAG = "PhotoGalleryFragment";

	GridView mGridView;
	public static ArrayList<GalleryItem> mItems = new ArrayList<GalleryItem>();
	ThumbnaiDownloader<ImageView> mThumbnaiDownloader;
	private GalleryItemAdapter adapter;
	private int pn;// 加载的图片数量
	private String tag2 = "性感美女";
	private boolean isLoadFinished;// 是否加载完成

	private String newQuery;
	private String oldQuery;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		// 设置Menu菜单
		setHasOptionsMenu(true);
		// 程序进入进行第一次加载json数据
		updateItems();

		mThumbnaiDownloader = new ThumbnaiDownloader<ImageView>(new Handler());
		mThumbnaiDownloader
				.setListener(new ThumbnaiDownloader.Listener<ImageView>() {
					@Override
					public void onThumbnailDownloaded(ImageView token,
							Bitmap thumbnail) {
						// TODO Auto-generated method stub
						if (isVisible()) {
							token.setImageBitmap(thumbnail);
						}
					}
				});
		mThumbnaiDownloader.start();
		mThumbnaiDownloader.getLooper();
		Log.i(TAG, "Background thread started");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_photo_gallery, container,
				false);

		mGridView = (GridView) v.findViewById(R.id.gridView);
		mGridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					if (view.getLastVisiblePosition() == (view.getCount() - 1)
							&& isLoadFinished
							&& adapter.getFooterView().getStatus() != FooterView.LOADING) {
						// loadMoreData();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});

		// 设置GridView的Item点击事件
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				// 获取图片模型实体类
				GalleryItem item = mItems.get(position);
				Log.i(TAG, "url: " + item.getUrl());
				Intent i = new Intent(getActivity(),PhotoPagerActivity.class);
				i.putExtra(PhotoFragment.EXTRA_PHOTO_ID, item.getId());
				startActivity(i);
			}
		});

		setupAdapter();

		return v;
	}

	// 设置gridview数据适配器
	void setupAdapter() {
		if (getActivity() == null || mGridView == null) {
			return;
		}
		if (mItems != null) {
			adapter = new GalleryItemAdapter(mItems);
			mGridView.setAdapter(adapter);
		} else {
			mGridView.setAdapter(null);
		}
	}

	// 加载GridView异步任务
	private class FetchItemsTask extends
			AsyncTask<Void, Void, ArrayList<GalleryItem>> {

		private int pn;
		private String tag2;

		public FetchItemsTask(int pn, String tag2) {
			this.pn = pn;
			this.tag2 = tag2;
		}

		@Override
		protected ArrayList<GalleryItem> doInBackground(Void... params) {
			Activity activity = getActivity();
			if (activity == null) {
				return new ArrayList<GalleryItem>();
			}

			String query = PreferenceManager.getDefaultSharedPreferences(
					activity).getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
			if (query != null) {
				// 将query字符赋值给newQuery
				newQuery = query;
				// 如果查询字符有变化则搜索新的GalleryItem，并初始化mItems和pn
				if (!newQuery.equals(oldQuery)) {
					oldQuery = newQuery;
					// 构造新的mItems
					mItems = new ArrayList<GalleryItem>();
					// 初始化为0
					pn = 0;
					return new FlickrFetchr().fetchItems(pn, query);
				} else {
					return new FlickrFetchr().fetchItems(pn, query);
				}
			} else {
				return new FlickrFetchr().fetchItems(pn, tag2);
			}
		}

		@Override
		protected void onPostExecute(ArrayList<GalleryItem> items) {
			if (mItems.size() == 0) {
				setupAdapter();
			}

			// 在添加数据之前删除最后的伪造item
			if (adapter.isFooterViewEnable()) {
				mItems.remove(mItems.get(mItems.size() - 1));
			}

			// 分页加载
			isLoadFinished = true;
			mItems.addAll(items);
			mItems.add(null);
			adapter.setFootreViewEnable(true);
			adapter.notifyDataSetChanged();

		}
	}

	private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {

		private FooterView mFooterView;// 底部视图
		private boolean footerViewEnable = false;// 底部视图是否可用

		public void setFootreViewEnable(boolean enable) {
			footerViewEnable = enable;
		}

		public boolean isFooterViewEnable() {
			return footerViewEnable;
		}

		public FooterView getFooterView() {
			return mFooterView;
		}

		public GalleryItemAdapter(ArrayList<GalleryItem> items) {
			super(getActivity(), 0, items);
		}

		private int getDisplayWidth(Activity activity) {
			Display display = activity.getWindowManager().getDefaultDisplay();
			int width = display.getWidth();
			return width;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// 伪造的空项可以根据id来确定。
			if (footerViewEnable && position == mItems.size() - 1) {
				if (mFooterView == null) {
					mFooterView = new FooterView(parent.getContext());
					mFooterView.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if (adapter != null
									&& adapter.getFooterView().getStatus() == FooterView.MORE) {
								loadMoreData();
							}
						}
					});

					GridView.LayoutParams pl = new GridView.LayoutParams(
							getDisplayWidth(getActivity()),
							LayoutParams.WRAP_CONTENT);
					mFooterView.setLayoutParams(pl);
				}

				setFooterViewStatus(FooterView.MORE);
				return mFooterView;
			}

			if (convertView == null
					|| (convertView != null && convertView == mFooterView)) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.gallery_item, parent, false);
			}

			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.gallery_item_imageView);
			imageView.setImageResource(R.drawable.empty_photo);
			GalleryItem item = getItem(position);
			imageView.setTag(item.getCaption());
			Log.i(TAG, "postion: " + position);
			mThumbnaiDownloader.queueThumbnail(imageView, item);
			return convertView;
		}

		public void setFooterViewStatus(int status) {
			if (mFooterView != null) {
				mFooterView.setStatus(status);
			}
		}

	}

	// 加载Gridview的列表项
	public void updateItems() {
		new FetchItemsTask(pn, tag2).execute();
		pn = pn + 14;
	}

	// 加载更多图片
	private void loadMoreData() {
		if (adapter != null) {
			adapter.setFooterViewStatus(FooterView.LOADING);
		}
		updateItems();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mThumbnaiDownloader.quit();
		Log.i(TAG, "Background thread destroyed");
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		mThumbnaiDownloader.clearQueue();
	}

	// 添加搜索菜单到视图中
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_photo_gallery, menu);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// 显示SearchView
			MenuItem searchItem = menu.findItem(R.id.menu_item_search);
			SearchView searchView = (SearchView) searchItem.getActionView();

			// 获取来自searchable.xml的数据
			SearchManager searchManager = (SearchManager) getActivity()
					.getSystemService(Context.SEARCH_SERVICE);
			ComponentName name = getActivity().getComponentName();
			SearchableInfo searchInfo = searchManager.getSearchableInfo(name);

			searchView.setSearchableInfo(searchInfo);
		}
	}

	/**
	 * 菜单的选中事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_search:
			getActivity().onSearchRequested();
			return true;
		case R.id.menu_item_clear:
			PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
					.putString(FlickrFetchr.PREF_SEARCH_QUERY, null).commit();
			updateItems();
			return true;
		case R.id.menu_item_toggle_polling:
			boolean shouldStartAlarm = !PollService
					.isServiceAlarmOn(getActivity());
			PollService.setServiceAlarm(getActivity(), shouldStartAlarm);

			// android3.0以后的版本实现更新选项菜单
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				// 用来回调onPrepareOptionsMenu
				getActivity().invalidateOptionsMenu();
			}

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 在android3.0以前的版本中： 除了菜单首次创建外，每次菜单需要配置都会调用该方法
	 */
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);

		MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
		if (PollService.isServiceAlarmOn(getActivity())) {
			toggleItem.setTitle(R.string.stop_polling);
		} else {
			toggleItem.setTitle(R.string.start_polling);
		}
	}

}
