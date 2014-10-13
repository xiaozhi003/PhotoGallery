package com.example.photogallery;

/**
 * 图片模型对象
 * 
 * @author asus
 * 
 */
public class GalleryItem {
	private String mCaption;
	private String mId;
	private String mUrl;

	public String getCaption() {
		return mCaption;
	}

	public void setCaption(String caption) {
		mCaption = caption;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public String toString() {
		return mCaption;
	}
}
