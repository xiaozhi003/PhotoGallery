package com.example.photogallery;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

/*
 * 获取网络图片加载器
 */
public class ImageLoader {

	private static int index = 0;

	private static final String TAG = "ImageLoader";

	/**
	 * 图片缓存技术的核心类，用于缓存所有下载好的图片，
	 * 
	 * 在程序内存达到设定值时会将最少最近使用的图片移除。
	 */
	private static LruCache<String, Bitmap> mLruCache;

	/**
	 * ImageLoader的实例。
	 */
	private static ImageLoader mImageLoader;

	private ImageLoader() {
		// 获取应用程序最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		Log.d(TAG, "最大可用内存maxMemory=" + maxMemory);
		// 设置缓存量为程序最大可用内存的1/8
		int cacheSize = maxMemory / 8;
		Log.d(TAG, "cacheSize=" + cacheSize);
		mLruCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
	}

	/**
	 * 获取ImageLoader的实例。
	 * 
	 * @return ImageLoader的实例。
	 */
	public static ImageLoader getInstance() {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader();
		}
		return mImageLoader;
	}

	/**
	 * 将一张图片存储到LruCache中
	 * 
	 * @param key
	 *            LruCache的键，是图片的URL地址
	 * @param bitmap
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemoryCache(key) == null) {
			Log.d(TAG, "mLruCache=" + mLruCache.size());
			mLruCache.put(key, bitmap);
		}
	}

	/**
	 * 从LruCache中获取一张图片，如果不存在就返回null
	 * 
	 * @param key
	 *            LruCache的键，是图片的URL地址
	 * @return 返回Bitmap对象，或null
	 */
	public Bitmap getBitmapFromMemoryCache(String key) {
		// TODO Auto-generated method stub
		return mLruCache.get(key);
	}

	// 通过reWidth计算图片压缩比例
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth) {
		// 源图片的宽度
		final int width = options.outWidth;
		int inSampleSize = 1;
		Log.d(TAG, "压缩比例为options=" + inSampleSize + "width=" + width
				+ ";reqWidth=" + reqWidth);
		if (width > reqWidth) {
			// 计算出实际宽度和目标宽度的比例
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = widthRatio;
		}
		return 2;
	}

	// 将图片压缩成适合reqWidth的比例
	public static Bitmap decodeSampleBitmapFromResource(String pathName,
			int reqWidth) {
		/*
		 * 第一次解析将inJustDecodeBounds设置为true,来获取图片大小 但是并不会把图片加载到内存中
		 */
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		// 调用上面定义图片压缩比例的方法calculateInSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth);
		Log.d(TAG, "压缩比例为options=" + options.inSampleSize);
		// 使用获取到的inSampleSize值再次解析图片即可得到压缩后的图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	// 返回图片原格式
	public static Bitmap decodeSampleBitmapFromResource(String pathName) {
		return BitmapFactory.decodeFile(pathName);
	}

	/**
	 * 获取图片进行压缩处理
	 * 
	 * @param imageUrl
	 * @param reqWidth
	 * @return
	 */
	public Bitmap loadImage(String imageUrl, int reqWidth) {
		// 从缓存中获取图片
		Log.d(TAG, "加载图片...imageUrl=" + imageUrl + ";reqWidth=" + reqWidth);
		Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
		if (bitmap == null) {
			if (imageUrl != null) {
				File imageFile = getImageFile(imageUrl);
				if (imageFile == null)
					return null;
				bitmap = ImageLoader.decodeSampleBitmapFromResource(
						imageFile.getPath(), reqWidth);
				if (bitmap != null) {
					Log.d(TAG, "将图片添加到了缓存中index=" + (++index));
					mImageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
					return bitmap;
				}
			}
		}
		return bitmap;
	}

	/**
	 * 获取高清大图不对图片进行压缩处理
	 * 
	 * @param imageUrl
	 * @return
	 */
	public Bitmap loadImage(String imageUrl) {
		Bitmap bitmap = null;
		Log.d(TAG, "加载图片...imageUrl=" + imageUrl);
		if (imageUrl != null) {
			bitmap = ImageLoader.decodeSampleBitmapFromResource(getImageFile(
					imageUrl).getPath());
		}
		return bitmap;
	}

	/**
	 * 从本地磁盘获取图片文件
	 * 
	 * @param imageUrl
	 * @return
	 */
	private File getImageFile(String imageUrl) {
		File imageFile = null;
		imageFile = getFromSD(imageUrl);
		if (imageFile == null)
			imageFile = getFromHttp(imageUrl);
		return imageFile;
	}

	/**
	 * 从SD卡中获取图片文件
	 * 
	 * @param imageUrl
	 * @param reqWidth
	 * @return
	 */
	private File getFromSD(String imageUrl) {
		File imageFile = new File(getImagePath(imageUrl));
		if (!imageFile.exists()) {
			return null;
		}
		return imageFile;
	}

	/**
	 * 将图片下载到SD卡缓存起来，并返回磁盘文件
	 * 
	 * @param imageUrl
	 */
	private File getFromHttp(String imageUrl) {
		Log.d(TAG, "图片下载imageUrl=" + imageUrl);
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		File imageFile = null;
		HttpResponse httpResponse = null;
		try {
			httpResponse = HttpUtil.getHttpResponse(imageUrl);
			// 检测访问是否成功
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				bis = new BufferedInputStream(httpResponse.getEntity()
						.getContent());
				Log.d(TAG, "图片下载InputStream=" + bis);
				imageFile = new File(getImagePath(imageUrl));
				fos = new FileOutputStream(imageFile);
				bos = new BufferedOutputStream(fos);
				byte[] b = new byte[1024];
				int length;
				while ((length = bis.read(b)) != -1) {
					bos.write(b, 0, length);
					bos.flush();
				}
			}
			return imageFile;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bis != null)
					bis.close();
				if (bos != null)
					bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return imageFile;
	}

	/**
	 * 获取图片的本地存储路径。
	 * 
	 * @param imageUrl
	 *            图片的URL地址。
	 * @return 图片的本地存储路径。
	 */
	private String getImagePath(String imageUrl) {
		int lastSlashIndex = imageUrl.lastIndexOf("/");
		int lastQuestionMarkIndex = imageUrl.lastIndexOf("?");
		String imageName = imageUrl.substring(lastSlashIndex + 1,
				lastQuestionMarkIndex);
		String imageDir = Environment.getExternalStorageDirectory().getPath()
				+ "/photogallery/";
		File file = new File(imageDir);
		if (!file.exists()) {
			file.mkdirs();
		}
		String imagePath = imageDir + imageName;
		return imagePath;
	}

}
