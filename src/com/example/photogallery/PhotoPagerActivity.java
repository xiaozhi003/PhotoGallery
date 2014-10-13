package com.example.photogallery;

import java.util.ArrayList;

import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;

import android.R.color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

/**
 * 显示大图的可滑动的Activity
 * 
 * @author asus
 * 
 */
public class PhotoPagerActivity extends FragmentActivity {
	
	private String TAG = "PhotoPagerActivity";

	// 显示大图的ViewPager
	private ViewPager mViewPager;

	// 图片模型列表
	private ArrayList<GalleryItem> mGalleryItems;

	@Override
	protected void onCreate(Bundle arg0) {
		// 隐藏标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(arg0);

		// 实例化ViewPager
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.viewPager);
		try {
			// 设置ViewPager滑动的动画效果
			mViewPager.setPageTransformer(true,
					CubeOutTransformer.class.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		mViewPager.setBackgroundResource(android.R.color.background_dark);
		setContentView(mViewPager);

		// 获取图片模型列表
		mGalleryItems = PhotoGalleryFragment.mItems;

		FragmentManager fm = getSupportFragmentManager();

		// 为ViewPager创建适配器
		mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return mGalleryItems.size() - 1;
			}

			@Override
			public Fragment getItem(int pos) {
				// TODO Auto-generated method stub
				GalleryItem galleryItem = mGalleryItems.get(pos);
				return PhotoFragment.newInstance(galleryItem.getId());
			}
		});

		// 为ViewPager设置监听器
		mViewPager
				.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

					@Override
					public void onPageSelected(int arg0) {
						GalleryItem galleryItem = mGalleryItems.get(arg0);
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
					}

					@Override
					public void onPageScrollStateChanged(int arg0) {
					}
				});

		// 显示当前的图片
		String id = getIntent().getStringExtra(PhotoFragment.EXTRA_PHOTO_ID);
		for (int i = 0; i < mGalleryItems.size(); i++) {
			if (mGalleryItems.get(i).getId().equals(id)) {
				mViewPager.setCurrentItem(i);
				break;
			}
		}
	}

}
