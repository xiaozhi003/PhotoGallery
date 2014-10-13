package com.example.photogallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;
import android.view.WindowManager;
/**
 * 显示大图托管Activity
 * @author asus
 *
 */
public class PhotoActivity extends SingleFragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 隐藏标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 隐藏状态栏
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected Fragment createFragment() {
		String url = getIntent().getStringExtra(PhotoFragment.EXTRA_PHOTO_ID);
		return PhotoFragment.newInstance(url);
	}

}
