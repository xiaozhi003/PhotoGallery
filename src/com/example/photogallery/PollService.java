package com.example.photogallery;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * 后台服务类
 * 
 * @author asus
 * 
 */
public class PollService extends IntentService {
	private static final String TAG = "PollService";

	private static final int POLL_INTERVAL = 1000 * 15;// 15 秒

	public PollService() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// 确认网络连接是否可用
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		@SuppressWarnings("deprecation")
		boolean isNetworkAvailable = cm.getBackgroundDataSetting()
				&& cm.getActiveNetworkInfo() != null;
		if (!isNetworkAvailable)
			return;

		// 获取首选项
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String query = prefs.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
		String lastResultId = prefs.getString(FlickrFetchr.PREF_LAST_RESULT_ID,
				null);

		ArrayList<GalleryItem> items;
		if (query != null) {
			items = new FlickrFetchr().fetchItems(0, query);
		} else {
			items = new FlickrFetchr().fetchItems(0, "性感美女");
		}

		if (items.size() == 0)
			return;

		String resultId = items.get(0).getId();

		if (!resultId.equals(lastResultId)) {
			Log.i(TAG, "Got a new result: " + resultId);

			// 当图片有更新时添加通知信息
			Resources r = getResources();
			PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(
					this, PhotoGalleryActivity.class), 0);

			// 创建Notification通知消息
			Notification notification = new NotificationCompat.Builder(this)
					.setTicker(r.getString(R.string.new_picture_title))
					.setSmallIcon(android.R.drawable.ic_menu_report_image)
					.setContentTitle(r.getString(R.string.new_picture_title))
					.setContentText(r.getString(R.string.new_picture_text))
					.setContentIntent(pi).setAutoCancel(true).build();
			
			// 通知新结果信息给用户
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			notificationManager.notify(0,notification);
		} else {
			Log.i(TAG, "Got an old result: " + resultId);
		}

		prefs.edit().putString(FlickrFetchr.PREF_LAST_RESULT_ID, resultId)
				.commit();

		Log.i(TAG, "Received an intent: " + intent);
	}

	/**
	 * 使用AlarmManager定时服务运行
	 * 
	 * @param context
	 * @param isOn
	 */
	public static void setServiceAlarm(Context context, boolean isOn) {
		Intent i = new Intent(context, PollService.class);
		// 创建用来启动PollService的PendingIntent
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

		// 创建AlarmManager实例
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		if (isOn) {
			/*
			 * 设置定时器有四个参数： param: 1.描述定时器时间基准的常量 param: 2.定时器运行开始时间 param:
			 * 3.定时器循环时间间隔 param: 4.到时要发送的PendingIntent
			 */
			alarmManager.setRepeating(AlarmManager.RTC,
					System.currentTimeMillis(), POLL_INTERVAL, pi);
		} else {
			alarmManager.cancel(pi);
			pi.cancel();
		}
	}

	/**
	 * 判断定时器激活与否
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isServiceAlarmOn(Context context) {
		Intent i = new Intent(context, PollService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i,
				PendingIntent.FLAG_NO_CREATE);
		return pi != null;
	}

}
