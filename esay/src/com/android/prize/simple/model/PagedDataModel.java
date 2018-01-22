package com.android.prize.simple.model;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.prize.simple.LauncherApplication;
import com.android.prize.simple.R;
import com.android.prize.simple.activity.AppUpateListener;
import com.android.prize.simple.activity.SettingsActivity;
import com.android.prize.simple.table.ItemTable;
import com.android.prize.simple.ui.RoundImageView;
import com.android.prize.simple.ui.SimplePage;
import com.android.prize.simple.ui.SimplePageView;
import com.android.prize.simple.utils.SimplePrefUtils;

import org.xutils.common.util.LogUtil;
import org.xutils.ex.DbException;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

/***
 * 简单桌面所有页面的数据处理类
 * @author fanjunchen
 *
 */
public class PagedDataModel {
	/**正常状态*/
	public static final int STATUS_NORMAL = 0;
	/**编辑状态*/
	public static final int STATUS_EDIT = STATUS_NORMAL + 1;
	/**默认的图片KEY*/
	public final static String DEFAULT = "default";
	
	private int mStatus = STATUS_NORMAL;
	
	private AppUpateListener update;
	
	public void setAppUpdateListener(AppUpateListener listener) {
		update = listener;
	}

	private SimplePageView mPagedView;
	
	private View topView;
	/**当前的页数*/
	private int mPageNum = 0;
	/**总数据*/
	private SparseArray<List<ItemTable>> mAllDatas = new SparseArray<List<ItemTable>>();
	/**初始化数据处理*/
	private DataDeals dataDeal = null;
	
	private Context mCtx;
	
	private SimpleHandler mHandler;
	
	private static PagedDataModel mInstance = null;
	/*** 图片缓存 clsName, drawable */
	public static HashMap<ComponentName, Drawable> iconCache = new HashMap<ComponentName, Drawable>();
	/**联系人背景资源id*/
	private List<Integer> mContactBgs = new ArrayList<Integer>(7);
	/**应用背景资源id*/
	private List<Integer> mAppBgs = new ArrayList<Integer>(7);
	
	private Intent mSettingsIntent;

	// TODO: 2018/1/22 0022 添加测试数据
	private LayoutInflater mInflate = null;
	
	//add by zhouerlong  meminfo out oom
	public PagedDataModel(Context ctx) {
		mCtx = ctx;
		// TODO Auto-generated constructor stub
		// TODO: 2018/1/22 0022 添加测试数据
		mInflate = LayoutInflater.from(mCtx);
	}
	//add by zhouerlong  meminfo out oom
	

	//add by zhouerlong  meminfo out oom
	public void setLauncher(Context c,View view) {
		mCtx = c;
			topView = view;

		if(mHandler!=null) {
			mHandler.removeMessages(MSG_SYNC_PAGE_NUM);
			mHandler.removeMessages(MSG_SYNC_PAGE_DATA);
			mHandler = null;
		}
		mHandler = new SimpleHandler();
			if (AllAppsModel.allList != null) {
				synchronized (AllAppsModel.allList) {
				AllAppsModel.allList.clear();
			}
		}
		mInstance = this;
		initView();
		
	}
	//add by zhouerlong  meminfo out oom
	
	public static PagedDataModel getInstance() {
		return mInstance;
	}
	
	private void initView() {
		
		mSettingsIntent = new Intent(mCtx, SettingsActivity.class);
		
		mPagedView = (SimplePageView) topView.findViewById(R.id.paged_view);
		
		mPagedView.setClickListener(mClick);
		
		mPagedView.setLongClickListener(mLongClick);
		
		dataDeal = new DataDeals(mCtx);
		
		initArrays();
		initIcons();
		// new InitGetData().execute(0);
		AllAppsModel app = new AllAppsModel();
		app.setContext(mCtx);
		app.getDataOnly();
		app = null;
	}
	
	private void initIcons() {
		
//		iconCache.put(DEFAULT, mCtx.getDrawable(R.drawable.ic_launcher));
	//add by zhouerlong  meminfo out oom
		if(iconCache == null) {
			iconCache = new HashMap<ComponentName, Drawable>();
		}
	//add by zhouerlong  meminfo out oom
		iconCache.put(new ComponentName("com.android.dialer","com.android.dialer.DialtactsActivity"), mCtx.getDrawable(R.drawable.simple_icon_dial));
		iconCache.put(new ComponentName("com.android.mms","com.android.mms.ui.BootActivity"), mCtx.getDrawable(R.drawable.simple_icon_mms));
		iconCache.put(new ComponentName("com.mediatek.camera","com.android.camera.CameraLauncher"), mCtx.getDrawable(R.drawable.simple_icon_camera));
		iconCache.put(new ComponentName("com.android.gallery3d","com.android.gallery3d.app.GalleryActivity"), mCtx.getDrawable(R.drawable.simple_icon_photos));
		iconCache.put(new ComponentName("com.android.calendar","com.android.calendar.AllInOneActivity"), mCtx.getDrawable(R.drawable.simple_icon_calendar));
		iconCache.put(new ComponentName("com.android.contacts","com.android.contacts.activities.PeopleActivity"), mCtx.getDrawable(R.drawable.simple_icon_contacts));
		
		iconCache.put(new ComponentName("com.android.settings","com.android.settings.Settings"), mCtx.getDrawable(R.drawable.simple_icon_settings));
		iconCache.put(new ComponentName("com.nqmobile.live.base","com.nqmobile.livesdk.commons.ui.StoreControlACT"), mCtx.getDrawable(R.drawable.simple_icon_beautify));
		iconCache.put(new ComponentName("com.android.deskclock","com.android.deskclock.DeskClock"), mCtx.getDrawable(R.drawable.simple_icon_deskclock));
		iconCache.put(new ComponentName("com.android.email","com.android.email.activity.Welcome"), mCtx.getDrawable(R.drawable.simple_icon_email));
		iconCache.put(new ComponentName("com.andreader.prein","com.cmread.bplusc.bookshelf.LocalMainActivity"), mCtx.getDrawable(R.drawable.simple_icon_reader));
		iconCache.put(new ComponentName("com.android.music","com.android.music.MusicBrowserActivity"), mCtx.getDrawable(R.drawable.simple_icon_music));
		iconCache.put(new ComponentName("com.mediatek.filemanager","com.mediatek.filemanager.main.MainActivity"), mCtx.getDrawable(R.drawable.simple_icon_filemanager));
		iconCache.put(new ComponentName("com.android.soundrecorder","com.android.soundrecorder.SoundRecorder"), mCtx.getDrawable(R.drawable.simple_icon_recorder));
		
		iconCache.put(new ComponentName("com.tencent.qqlive","com.tencent.qqlive.ona.activity.WelcomeActivity"), mCtx.getDrawable(R.drawable.simple_icon_video));
//		iconCache.put(new ComponentName("","com.mediatek.fmradio.FmRadioActivity"), mCtx.getDrawable(R.drawable.simple_icon_radio));
		iconCache.put(new ComponentName("com.koobee.koobeecenter02","com.koobee.koobeecenter.MainActivity"), mCtx.getDrawable(R.drawable.simple_icon_koobee));
		iconCache.put(new ComponentName("com.prize.appcenter","com.prize.appcenter.PlayPlusClientActivity"), mCtx.getDrawable(R.drawable.simple_icon_market));
//		iconCache.put(new ComponentName("","com.prize.cloud.StartActivity"), mCtx.getDrawable(R.drawable.simple_icon_cloud));
		iconCache.put(new ComponentName("com.pr.scuritycenter","com.pr.scuritycenter.activity.MainActivity"), mCtx.getDrawable(R.drawable.simple_icon_safe));
		iconCache.put(new ComponentName("com.android.browser","com.tencent.mtt.SplashActivity"), mCtx.getDrawable(R.drawable.simple_icon_browser));
		
		iconCache.put(new ComponentName("com.android.stk","com.android.stk.StkMain"), mCtx.getDrawable(R.drawable.simple_icon_sim));
		iconCache.put(new ComponentName("com.android.calculator2","com.android.calculator2.Calculator"), mCtx.getDrawable(R.drawable.simple_icon_calc));
		iconCache.put(new ComponentName("com.assistant.icontrol","com.tiqiaa.icontrol.WelcomeActivity"), mCtx.getDrawable(R.drawable.simple_icon_control));
		iconCache.put(new ComponentName("com.android.notepad","com.android.notepad.NotePadActivity"), mCtx.getDrawable(R.drawable.simple_icon_noted));
		iconCache.put(new ComponentName("com.prize.compass","com.prize.compass.CompassActivity"), mCtx.getDrawable(R.drawable.simple_icon_compass));
		
		iconCache.put(new ComponentName("com.autonavi.minimap","com.autonavi.map.activity.SplashActivity"), mCtx.getDrawable(R.drawable.simple_icon_map));
//		iconCache.put(new ComponentName("","com.tianqiyubao2345.activity.CoveryActivity"), mCtx.getDrawable(R.drawable.simple_icon_weather));
		iconCache.put(new ComponentName("com.prize.weather","com.prize.weather.WeatherHomeActivity"), mCtx.getDrawable(R.drawable.simple_icon_weather));
//		iconCache.put(new ComponentName("","com.tencent.news.activity.SplashActivity"), mCtx.getDrawable(R.drawable.simple_icon_news));
		
//		iconCache.put(new ComponentName("","com.yidian.dk.ui.guide.UserGuideActivity"), mCtx.getDrawable(R.drawable.simple_icon_info));
	}

	/***
	 * 获取总页数
	 */
	public void getPages() {
		try {
			ItemTable item = LauncherApplication.getDbManager().selector(ItemTable.class).orderBy("screen", true).findFirst();
			if (item != null)
				mPageNum = item.screen + 1;
		} catch (DbException e) {
			e.printStackTrace();
		}
	}
	/***
	 * 初始化数据
	 */
	private void initDatas() {
		//若为第一次调用, 则需要
		if (!SimplePrefUtils.getBoolean(mCtx, IConstant.KEY_INIT)) {
			int id = R.xml.simple_default_data;
			int nums = dataDeal.loadFavorites(id);
			LogUtil.i("===simple init nums===" + nums);
			if (nums > 0)
				SimplePrefUtils.putBoolean(mCtx, IConstant.KEY_INIT, true);
		}
	}
	/***
	 * 获取某页数据
	 * @param page
	 */
	private void getPageData(int page) {
		List<ItemTable> as = dataDeal.queryPageDatas(page);
		int sz = null == as ? 0 : as.size();
		for (int i=0; i<sz; i++) {
			ItemTable t = as.get(i);
			if (t != null && AllAppsModel.existMap != null
					&& !TextUtils.isEmpty(t.clsName)) {
				ItemTable tts = AllAppsModel.existMap.get(new ComponentName(t.pkgName,t.clsName));
				if (tts != null)
					t.title = tts.title;
			}
		}
	//add by zhouerlong  meminfo out oom
		if(mAllDatas ==null) {
			mAllDatas =  new SparseArray<List<ItemTable>>();
		}
//		Log.i("EasyActivity",)
	//add by zhouerlong  meminfo out oom
		mAllDatas.put(page, as);
	}
	/***
	 * 更新某页数据 从DB中
	 * @param page
	 * @return
	 */
	boolean updatePage(int page) {
		
		return true;
	}
	/***
	 * 进入某个状态
	 * @param status
	 */
	public void enterMode(int status) {
		if (STATUS_EDIT == status) {
			if (mPagedView.enterEdit())
				mStatus = STATUS_EDIT;
		}
		else {
			mPagedView.exitEdit();
			mStatus = STATUS_NORMAL;
		}
	}
	
	
	/***
	 * 安心锁进入到不可编辑状态
	 */
	public void safeLock(boolean isLock) {
		mPagedView.exitEdit();
		mStatus = STATUS_NORMAL;
	//add by zhouerlong  meminfo out oom
		if(mAllDatas ==null) {
			mAllDatas =  new SparseArray<List<ItemTable>>();
		}
	//add by zhouerlong  meminfo out oom
		if (isLock) {
			int sz = mAllDatas.size() - 1;
			
			if (sz > 1) {
				List<ItemTable> aps = mAllDatas.get(sz);
				if (aps != null && aps.size() < 2) {
					mPagedView.setPageNum(sz, isLock);
				}
			}
		}
		else {
			int sz = mAllDatas.size() - 1;
			if (sz > 1) {
				List<ItemTable> aps = mAllDatas.get(sz);
				if (aps != null && aps.size() < 2) {
					mPagedView.setPageNum(sz + 1);
					mPagedView.syncPages();
					mPagedView.syncPageItems(sz, false);
				}
			}
		}
	}
	
	/***
	 * 显示设置
	 */
	public void showSettings() {
		
		mCtx.startActivity(mSettingsIntent);
	}
	
	public void onBackPressed() {
		// 若处理编辑模块则退出编辑模式
		if (mStatus == STATUS_EDIT) {
			enterMode(STATUS_NORMAL);
		}
		return;
	}
	
	
	public void onNewIntent() {
		// 若处理编辑模块则退出编辑模式
		mPagedView.snapToPage(1,750);
	}
	/***
	 * 获取数据
	 */
	public void getDatas() {
//bugId 36646 开启老人桌面开关，按home键进入老人桌面后launcher无响应		
//		new InitGetData().execute();
		new InitGetData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	/***
	 * 初始化数据或取数据
	 * @author fanjunchen
	 *
	 */
	class InitGetData extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... args) {
			try {
				initDatas();
				
				getPages();
				
				for (int i=0; i<mPageNum; i++) {
					getPageData(i);
				}
			} catch (Exception e) {
				this.cancel(true);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// 刷新smoothPageView

	//add by zhouerlong  meminfo out oom
			if(mAllDatas ==null) {
				mAllDatas =  new SparseArray<List<ItemTable>>();
			}
	//add by zhouerlong  meminfo out oom

// bugID 36643  add by bxh
			if(mPagedView != null){
				mPagedView.setDatas(mAllDatas);
				mPagedView.setPageNum(mPageNum);
				mPagedView.syncPages();
				if (mHandler != null)
					mHandler.sendEmptyMessageDelayed(MSG_SYNC_PAGE_DATA, 500);
			}
// bugID 36643  add by bxh 				
		}
	}
	/**绑定页数据*/
	private final int MSG_SYNC_PAGE_DATA = 1;
	/**同步页数及UI*/
	private final int MSG_SYNC_PAGE_NUM = MSG_SYNC_PAGE_DATA + 1;
	/**同步页数及UI*/
	private final int MSG_CAN_CLICK = MSG_SYNC_PAGE_NUM + 1;
	/***
	 * 处理UI方法
	 * @author fanjunchen
	 *
	 */
	class SimpleHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
	//add by zhouerlong  meminfo out oom
			if(mAllDatas ==null) {
				mAllDatas =  new SparseArray<List<ItemTable>>();
			}
	//add by zhouerlong  meminfo out oom
			switch (msg.what) {
				case MSG_SYNC_PAGE_NUM:
					mPagedView.syncPages();
					break;
				case MSG_SYNC_PAGE_DATA:
					if (mPagedView != null) {
						for (int i=0; i<mPageNum; i++) {
							mPagedView.syncPageItems(i, false);
						}
						
						if (SimplePrefUtils.getBoolean(mCtx, IConstant.KEY_LOCK)) {
							int sz = mAllDatas.size() - 1;
							
							if (sz > 1) {
								List<ItemTable> aps = mAllDatas.get(sz);
								if (aps != null && aps.size() < 2) {
									mPagedView.setPageNum(sz, true);
								}
							}
						}
						mPagedView.setCurrentPage(1);
					}
					// 注册广播
					registerBroadcast();
					initWeather();
					break;
				case MSG_CAN_CLICK:
					isCanClick = true;
					break;
			}
			
		}
	}
	/**是否已经注册过广播*/
	private boolean isRigstered = false;
	
	private WeatherTimeReceiver mTimerReceiver = null;
	/***
	 * 注册广播,若已经注册则不管
	 */
	private void registerBroadcast() {
		
// 36656 老人桌面天气显示不出来  begin
//		if (isRigstered)
//			return ;
//		isRigstered = true;
		try {
			if (/*isRigstered &&*/ mActivity != null)  {
				mActivity.unregisterReceiver(mTimerReceiver);
			mTimerReceiver=null;
			}
		} catch (Exception e) {
			mTimerReceiver=null;
			e.printStackTrace();
		}
// 36656 老人桌面天气显示不出来  end		
		if (mTimerReceiver == null)
			mTimerReceiver = new WeatherTimeReceiver();
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(REC_WEATHER_BRD);
		filter.addAction(REC_APP_ADD);
		filter.addAction(REC_APP_DEL);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		if (mActivity != null)
			mActivity.registerReceiver(mTimerReceiver, filter);
	}
	/***
	 * 销毁
	 */
	public void destroy() {
		if(mHandler!=null) {
			mHandler.removeMessages(MSG_SYNC_PAGE_NUM);
			mHandler.removeMessages(MSG_SYNC_PAGE_DATA);
			mHandler = null;
		}
		try {
			if (isRigstered && mActivity != null) 
				mActivity.unregisterReceiver(mTimerReceiver);
			mTimerReceiver=null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		isRigstered = false;
		if(iconCache!=null)
		iconCache.clear();
	//add by zhouerlong  meminfo out oom
		iconCache =null;
	//add by zhouerlong  meminfo out oom
		if(weatherBgs!=null)
		weatherBgs.clear();
		if(dataDeal!=null)
		dataDeal=null;
		weatherBgs =null;
		if(weatherIcs!=null)
		weatherIcs.clear();
		weatherIcs = null;
		mCtx=null;
		mActivity=null;
//		mLongClick=null;
//		mClick = null;
		mAnimatorSet=null;
		if(SimpleDeviceProfile.getInstance()!=null)
		SimpleDeviceProfile.getInstance().destory();
	//add by zhouerlong  meminfo out oom
		topView=null;
		mPagedView=null;

		if(mAllDatas ==null) {
			mAllDatas =  new SparseArray<List<ItemTable>>();
		}
	//add by zhouerlong  meminfo out oom
		if(mAllDatas!=null) {
			int sz = mAllDatas.size();
			for (int i=0; i<sz; i++) {
				try {
					LauncherApplication.getDbManager().update(mAllDatas.get(i));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		AllAppsModel.reset();
		mAllDatas=null;
	}
	/**默认为可点击*/
	private boolean isCanClick = true;
	
	private final int CLICK_DELAY = 300;
	//add by zhouerlong  meminfo out oom
	
	class ViewOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (!isCanClick)
				return;
			isCanClick = false;
			Calendar c = Calendar.getInstance();
			switch (v.getId()) {
				case R.id.lay_time:
					//进入时间设置
					Intent it = new Intent(Intent.ACTION_MAIN, null);
					it.addCategory(Intent.CATEGORY_LAUNCHER);
					ComponentName cn = new ComponentName("com.android.deskclock", "com.android.deskclock.DeskClock");
					it.setComponent(cn);
					
//					String formatTimeToChina = SimplePrefUtils.formatTimeToChina(c.getTime());
//					((SimpleFrame)topView).speakText(mCtx.getResources().getString(R.string.simple_widget_clock),4);
		            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		            mCtx.startActivity(it);
					mHandler.sendEmptyMessageDelayed(MSG_CAN_CLICK, CLICK_DELAY);
					cn = null;
					it = null;
					break;
				case R.id.txt_old_day:
				case R.id.txt_day:
					it = new Intent(Intent.ACTION_MAIN, null);
					it.addCategory(Intent.CATEGORY_LAUNCHER);
					cn = new ComponentName("com.android.calendar", "com.android.calendar.AllInOneActivity");
//					int weekday = c.get(Calendar.DAY_OF_WEEK) - 1;
//					String str = SimplePrefUtils.formatCalendreToChina(c.getTime()) + "  " + weekDays[weekday];
					
//					((SimpleFrame)topView).speakText(mCtx.getResources().getString(R.string.simple_widget_calender),4);
					it.setComponent(cn);
		            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		            mCtx.startActivity(it);
					mHandler.sendEmptyMessageDelayed(MSG_CAN_CLICK, CLICK_DELAY);
					cn = null;
					it = null;
					break;
				case R.id.txt_temp:
				case R.id.txt_city:
					it = new Intent(Intent.ACTION_MAIN, null);
					it.addCategory(Intent.CATEGORY_LAUNCHER);
					//add by zhouerlong monkey
					if(mPagedView==null) {
						return;
					}
					//add by zhouerlong monkey
					View parentView = mPagedView.getSpecialView();
					if (parentView == null)
						return;
//					TextView tv = (TextView)parentView.findViewById(R.id.txt_temp);
//					((SimpleFrame)topView).speakText(mCtx.getResources().getString(R.string.simple_widget_weather),4);
//launcher:className="com.prize.weather.WeatherHomeActivity"
//launcher:packageName="com.prize.weather"
					cn = new ComponentName("com.tianqiwhite", "com.tianqiyubao2345.activity.CoveryActivity");
					it.setComponent(cn);
		            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		            try {
			            mCtx.startActivity(it);
					} catch (Exception e) {

						try {
							cn = new ComponentName("com.prize.weather", "com.prize.weather.WeatherHomeActivity");

							it.setComponent(cn);
//						TextView 	t = (TextView) v.findViewById(R.id.txt_title);
//						String tx = t.getText().toString();

				            mCtx.startActivity(it);
						} catch (Exception e2) {
							// TODO: handle exception
						}
					}
					mHandler.sendEmptyMessageDelayed(MSG_CAN_CLICK, CLICK_DELAY);
					cn = null;
					it = null;
					break;
				default:
					Object o = v.getTag();
					if (null == o) {
						isCanClick = true;
						return;
					}
					if (!(o instanceof ItemTable)) {
						isCanClick = true;
						return;
					}
					ItemTable itab = (ItemTable)o;
					
					switch (itab.type) {
						case IConstant.TYPE_APP:
						case IConstant.TYPE_APP_PRD:
							if (mStatus == STATUS_EDIT) {
								if (itab.canDel) {
									delItem(itab, true);
									return;
								}
							}
							else {
								try {
									Intent i = Intent.parseUri(itab.intent, 0); 
									if(i.getComponent().equals(new ComponentName("com.android.settings", "com.android.settings.Settings"))) {
											/*Launcher l = (Launcher) mCtx;
											l.getSimple_main().onMenu();*/
										showSettings();
										
											isCanClick=true;
//											mContextonMenu();
											return;
									}
									
//add by zhouerlong  start activity error bug ID 30791
									Intent ifs=	Intent.parseUri(itab.intent, 0);
									ifs.setAction(Intent.ACTION_MAIN);
									ifs.setComponent(new ComponentName(ifs.getComponent().getPackageName(),ifs.getComponent().getClassName()));
									ifs.addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
									mCtx.startActivity(ifs);
//add by zhouerlong  start activity error bug ID 30791
//									mCtx.startActivity(Intent.parseUri(itab.intent, 0));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							break;
						case IConstant.TYPE_CONTACT:
							if (mStatus == STATUS_EDIT && !TextUtils.isEmpty(itab.intent)) {
								if (itab.canDel) {
									updateContactPage(itab); // 更新
									return;
								}
							}
							else if (!TextUtils.isEmpty(itab.intent)) {
								try {
									
									mCtx.startActivity(Intent.parseUri(itab.intent, 0));
								} catch (URISyntaxException e) {
									e.printStackTrace();
								}
							}
							else {
								if (mStatus == STATUS_EDIT)
									enterMode(STATUS_NORMAL);
								// 中转到A中去
								it = new Intent(Intent.ACTION_CREATE_SHORTCUT);
								cn = new ComponentName("com.android.contacts", "com.android.contacts.ContactShortcut");
								it.setComponent(cn);

								TextView 	t = (TextView) v.findViewById(R.id.txt_title);
								String tx = t.getText().toString();
								mActivity.startActivityForResult(it, 100);
							}
							break;
						case IConstant.TYPE_WIDGET:
							break;
						case IConstant.TYPE_ADD:
							onBackPressed();
							String tx = mCtx.getResources().getString(R.string.str_add_to);
							it = new Intent(mCtx, com.android.prize.simple.activity.AllAppsActivity.class);
							
							mActivity.startActivityForResult(it, 0);
							break;
					}
					mHandler.sendEmptyMessageDelayed(MSG_CAN_CLICK, CLICK_DELAY);
					break;
			}
		}
		
	}
	
	private ViewOnClickListener mClick = new ViewOnClickListener();
	
	class ViewLongClick implements View.OnLongClickListener {

		@Override
		public boolean onLongClick(View arg0) {
			// TODO Auto-generated method stub
			if (!isCanClick)
				return false;
			isCanClick = false;
			
			boolean isLock = SimplePrefUtils.getBoolean(mCtx, IConstant.KEY_LOCK);
			
			if (!isLock && mStatus == STATUS_NORMAL) {
				enterMode(STATUS_EDIT);
			}
			else if (!isLock){
				enterMode(STATUS_NORMAL);
			}
			mHandler.sendEmptyMessageDelayed(MSG_CAN_CLICK, CLICK_DELAY);
			return true;
		}
		
	}
	/**长按事件处理*/
	private View.OnLongClickListener mLongClick = new ViewLongClick();
	
	
	private Activity mActivity;
	
	public void setActivity(Activity act) {
		mActivity = act;
	}
	/***
	 * 处理联系人返回结果
	 * @param data
	 */
	public void onActivityResult(int reqCode, Intent data) {
		if (reqCode == 101) {
			Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
	        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
	        //Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
	        //if (bitmap != null && bitmap instanceof Bitmap);
	        updateContactItem(intent, name);
		}
	}
	/***
	 * 更新Simple DB及UI
	 * @param it
	 * @param name
	 */
	private void updateContactItem(Intent it, String name) {
		if(mAllDatas ==null) {
			mAllDatas =  new SparseArray<List<ItemTable>>();
		}
		List<ItemTable> items = mAllDatas.get(0);
		if (items == null || items.size() < 1) {
			return;
		}
		for (int i=0; i<items.size(); i++) {
			ItemTable itc = items.get(i);
			
			if (TextUtils.isEmpty(itc.intent)) {
				itc.title = name;
				itc.intent = it.toUri(0);
				try {
					LauncherApplication.getDbManager().saveOrUpdate(itc);
				} catch (DbException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		// 更新页面
		mPagedView.syncPageItems(0, true);
	}
	/***
	 * 更新联系人页
	 * @param item
	 */
	private void updateContactPage(ItemTable item) {
		List<ItemTable> items = mAllDatas.get(0);
		if (items == null || items.size() < 1) {
			return;
		}
		
		int pos = items.indexOf(item);
		
		if (pos < 0) {
			return;
		}
		mOpt = 1;
		updateContactData(items, item, pos);
		
		// 更新页面
		//mPagedView.syncPageItems(0, true);
	}
	/***
	 * 更新数据, 包括DB
	 * @param items
	 * @param item
	 * @param pos
	 */
	private void updateContactData(final List<ItemTable> items, final ItemTable item, final int pos) {
		
		items.remove(item);
		int preX = item.x;
		int preY = item.y;
		int x = preX, y = preY;
		int sz = items.size();
		
		SimplePage p = (SimplePage)mPagedView.getChildAt(0);
		mRemovedView = p.getChildAt(pos);
		
		int preLeft, preTop, tTop, tLeft;
		
		SimplePage.LayoutParams params = (SimplePage.LayoutParams)mRemovedView.getLayoutParams();
		/*preTop = mRemovedView.getTop();
		preLeft = mRemovedView.getLeft();*/
		preTop = params.getY();
		preLeft = params.getX();
		p.removeViewAt(pos); // 删除
		
		List<Animator> animators = new ArrayList<Animator>();
		int i;
		for (i=pos; i<sz; i++) {
			ItemTable itc = items.get(i);
			if (!TextUtils.isEmpty(itc.intent)) {
				View aView = p.getChildAt(i);
				
				tTop = aView.getTop();
				tLeft = aView.getLeft();
				
				x = itc.x;
				y = itc.y;
				
				itc.x = preX;
				itc.y = preY;
				
				// 比较x, y来确定移动位置
				ValueAnimator a = ValueAnimator.ofFloat(0, 1);
				AnimatorUpdateXYListener selfUpdate = new AnimatorUpdateXYListener();
				selfUpdate.targetView = aView;
				selfUpdate.mParams = (SimplePage.LayoutParams)aView.getLayoutParams();
				selfUpdate.mParams.cellX = preX;
				selfUpdate.mParams.cellY = preY;
				if (y > preY) {
					selfUpdate.xLen = preLeft - tLeft;
					selfUpdate.yLen = preTop - tTop;
				}
				else {
					selfUpdate.xLen = preLeft - tLeft;
				}
				a.addUpdateListener(selfUpdate);
				a.setDuration(DURATION_TIME);
				animators.add(a);
				
				preX = x;
				preY = y;
				
				preLeft = tLeft;
				preTop = tTop;
			}
			else {
				break;
			}
		}
		
		item.x = x;
		item.y = y;
		item.intent = "";
		item.title = "";
		item.bgResId = 0;
		items.add(i, item);
		
		params.cellX = x;
		params.cellY = y;
		params.setX(preLeft);
		params.setY(preTop);
		
		resetContactView(mRemovedView, item);
		mRemovedView.setVisibility(View.INVISIBLE);
		p.addView(mRemovedView, i, params);
		
		ValueAnimator b = ValueAnimator.ofFloat(0, 1);
		b.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator va) {
				// TODO Auto-generated method stub
				Float scale = (Float)va.getAnimatedValue();
				mRemovedView.setScaleX(scale);
				mRemovedView.setScaleY(scale);
			}
			
		});
		b.setDuration(DURATION_TIME);
		b.addListener(mAnimListener);
		animators.add(b);
		
		if (mAnimatorSet != null) {
			mAnimatorSet.end();
		}
		mAnimatorSet = new AnimatorSet();
		mAnimatorSet.playSequentially(animators);
		mAnimatorSet.start();
		try {
			LauncherApplication.getDbManager().update(items);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**操作是哪个, 1: 联系人, 2:　应用*/
	private int mOpt = 0;
	
	private View mRemovedView = null;
	
	private AnimatorSet mAnimatorSet = null;
	/***
	 * 动画监听器
	 */
	private Animator.AnimatorListener mAnimListener = new Animator.AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator arg0) {
			
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			if (mOpt == 1) // 更新页面
				mPagedView.syncPageItems(0, true);
			else if (mOpt == 2) {
				//mPagedView.delLast2Item(mStartPage);
				mPageNum = mAllDatas.size();
				for (int pp = mStartPage; pp<mPageNum; pp++) {
					mPagedView.syncPageItems(pp, false);
				}
			}
			isCanClick = true; // can click
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
			// no repeat
		}

		@Override
		public void onAnimationStart(Animator arg0) {
			// no repeat
			if (mOpt == 1 && mRemovedView != null)
				mRemovedView.setVisibility(View.VISIBLE);
		}
		
	};
	/***
	 * 还原成默认数据
	 * @param v
	 * @param item
	 */
	private void resetContactView(final View v, final ItemTable item) {
		v.setTag(item);
		TextView title = (TextView)v.findViewById(R.id.txt_title);
		title.setText(R.string.simple_add_contact);
		
		ImageView imgDel = (ImageView)v.findViewById(R.id.img_del);
		if (imgDel != null)
			imgDel.setVisibility(View.INVISIBLE);
		v.setBackgroundResource(R.drawable.simple_contact_bg);
	}
	
	/**200ms 动画时长*/
	private static final int DURATION_TIME = 200;
	
	/**请求天气广播  原先为 com.cooee.weather.Weather.action.REQUEST_REFRESH_DATA*/
	final String REQ_WEATHER_BRD = "com.cooee.weather.Weather.action.REQUEST_REFRESH_DATA_FOR_3TH";
	/**接收天气广播 原先为 com.cooee.weather.Weather.action.REFRESH_UPDATE_LAUNCHER*/
	final String REC_WEATHER_BRD = "com.cooee.weather.Weather.action.REFRESH_UPDATE_LAUNCHER_FOR_3TH";
	/**新安装一个应用*/
	public static final String REC_APP_ADD = "package_add_new";
	/**删除一个应用*/
	public static final String REC_APP_DEL = "package_del_app";
	/**包名参数*/
	public static final String P_PKG = "package";
	/***
	 * 请求天气数据
	 */
	private void reqWeatherInfo() {
		Intent it = new Intent();
		it.setAction(REQ_WEATHER_BRD);
		String post = SimplePrefUtils.getString(mCtx, IConstant.KEY_POSTAL);
		if (!TextUtils.isEmpty(post))
			post = "none";
		it.putExtra("postCode", post);//postCode为null
		
		mActivity.sendBroadcast(it);
		it = null;
	}
	private Time mCalendar = new Time();
	/***
	 * 天气时间广播
	 * @author fanjunchen
	 *
	 */
	class WeatherTimeReceiver extends BroadcastReceiver {

		

		@Override
		public void onReceive(Context ctx, Intent data) {
			// TODO Auto-generated method stub
			String act = data.getAction();
//			Log.d("addddddd-----Receiver",  data.getStringExtra(P_PKG));
			if (REC_WEATHER_BRD.equals(act)) {
				// 接收到天气
				// ERROR时为错误
				String result = data.getStringExtra("result");
				
				if ("ERROR".equals(result))
					return;
				
				//该数据为当前城市名称
				String cityName = data.getStringExtra("postalCode");
				SimplePrefUtils.putString(ctx, IConstant.KEY_POSTAL, cityName);
				//天气信息  雷阵雨、晴、多云、雾霾、雪、阴、雨
				String cn = data.getStringExtra("T0_condition");
				//当前温度
				String tempc_now = String.valueOf(data.getIntExtra("T0_tempc_now", 0));
//				//最高温度
//				String tmp_h = String.valueOf(data.getIntExtra("T0_tempc_high", 0));
//				//最低温度
//				String tmp_l = String.valueOf(data.getIntExtra("T0_tempc_low", 0));
				
//				changeWeatherView(cn, tmp_h, tmp_l, cityName);
				changeWeatherView(cn, tempc_now, cityName);
				//当前温度
				//data.getStringExtra("T0_tempc_now");
			}
			else if (Intent.ACTION_DATE_CHANGED.equals(act)) { // 日期变化
				changeDateView();
			}else if (Intent.ACTION_TIME_CHANGED.equals(act) || Intent.ACTION_TIME_TICK.equals(act)) { // 时间变化
				changeTimeView();
			}
			else if(Intent.ACTION_TIMEZONE_CHANGED.equals(act)){
				String tz = data.getStringExtra("time-zone");
				mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
				changeTimeView();
			}
			else if (REC_APP_ADD.equals(act)) { // 新安装应用
				addApp(data.getStringExtra(P_PKG));
			}
			else if (REC_APP_DEL.equals(act)) { // 删除应用
				delApp(data.getStringExtra(P_PKG));
			}
			
		}
	}
	/***
	 * 更新天气信息
	 * @param info
	 * @param cityName
	 */
	private void changeWeatherView(String info, String tmpN, String cityName) {
		if(mPagedView == null) return;
		View parentView = mPagedView.getSpecialView();
		
		if (parentView == null)
			return;
		
		TextView tv = (TextView)parentView.findViewById(R.id.txt_temp);
//		tv.setText(mCtx.getString(R.string.simple_temp_l_h, tmpL, tmpH));
		
		if (info!=null) {
			tv.setText(mCtx.getString(R.string.simple_temp_l_h, tmpN, info));
		}
		//tv.setCompoundDrawables(arg0, arg1, arg2, arg3);
		if (weatherIcs.get(info) != null) {
			tv.setCompoundDrawablesWithIntrinsicBounds(null, mCtx.getDrawable(weatherIcs.get(info)), null, null);
		}
		else
			tv.setCompoundDrawablesWithIntrinsicBounds(null, mCtx.getDrawable(R.drawable.simple_weather_default), null, null);
		
		tv = (TextView)parentView.findViewById(R.id.txt_city);
		tv.setText(cityName);
		
		RoundImageView roundView = (RoundImageView)parentView.findViewById(R.id.img_weather_bg);
		if (weatherBgs.get(info) != null) {
			roundView.setImageResource(weatherBgs.get(info));
		}
		else
			roundView.setImageResource(R.drawable.simple_weather_bg);
	}
	
	private String[] weekDays = null;
	
	private HashMap<String, Integer> weatherBgs = new HashMap<String, Integer>();
	
	private HashMap<String, Integer> weatherIcs = new HashMap<String, Integer>();
	
	private LunarCalendarUtil mLunar = null;
	/***
	 * 初始化数据
	 */
	private void initArrays() {
		
		mLunar = LunarCalendarUtil.getInstance(mCtx);
		
		weekDays = mCtx.getResources().getStringArray(R.array.prize_week_day);
		if(weatherBgs==null) {
			weatherBgs = new HashMap<>();
		}
		// "雷阵雨"、"晴"、"多云"、"雾霾"、"雪"、"阴"、"雨"
		weatherBgs.put("雷阵雨", R.drawable.simple_wbg_lzy);
		weatherBgs.put("晴", R.drawable.simple_wbg_qt);
		weatherBgs.put("多云", R.drawable.simple_wbg_dy);
		weatherBgs.put("雾霾", R.drawable.simple_wbg_wm);
		weatherBgs.put("雪", R.drawable.simple_wbg_xx);
		weatherBgs.put("阴", R.drawable.simple_wbg_yint);
		weatherBgs.put("雨", R.drawable.simple_wbg_xy);

		if(weatherIcs==null) {
			weatherIcs = new HashMap<>();
		}
		weatherIcs.put("雷阵雨", R.drawable.simple_weather_leiyu);
		weatherIcs.put("晴", R.drawable.simple_weather_qin);
		weatherIcs.put("多云", R.drawable.simple_weather_douyun);
		weatherIcs.put("雾霾", R.drawable.simple_weather_wumai);
		weatherIcs.put("雪", R.drawable.simple_weather_xie);
		weatherIcs.put("阴", R.drawable.simple_weather_yin);
		weatherIcs.put("雨", R.drawable.simple_weather_yu);
		
		mContactBgs.add(R.drawable.simple_contact_bg1);
		mContactBgs.add(R.drawable.simple_contact_bg2);
		mContactBgs.add(R.drawable.simple_contact_bg3);
		mContactBgs.add(R.drawable.simple_contact_bg4);
		mContactBgs.add(R.drawable.simple_contact_bg5);
		mContactBgs.add(R.drawable.simple_contact_bg6);

		mAppBgs.add(R.drawable.simple_app_bg1);
		mAppBgs.add(R.drawable.simple_app_bg2);
		mAppBgs.add(R.drawable.simple_app_bg3);
		mAppBgs.add(R.drawable.simple_app_bg4);
		mAppBgs.add(R.drawable.simple_app_bg5);
		mAppBgs.add(R.drawable.simple_app_bg6);
		
	}
	
	/***
	 * 日期发生了变化
	 */
	private void changeDateView() {
		// prize_week_day;
		Calendar c = Calendar.getInstance();
		// 得到星期几
		int weekday = c.get(Calendar.DAY_OF_WEEK) - 1;
		
		String str = SimplePrefUtils.formatDate(c.getTime()) + "  " + weekDays[weekday];
		if(mPagedView == null) return;
		
		View parentView = mPagedView.getSpecialView();
		
		if (parentView == null)
			return;
		
		TextView tv = (TextView)parentView.findViewById(R.id.txt_day);
		tv.setText(str);
		
		tv = (TextView)parentView.findViewById(R.id.txt_old_day);
		
		ArrayList<String> arr = mLunar.getLunarYMD(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
		
		String sxYear = mLunar.animalsYear(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
		
		tv.setText(mCtx.getString(R.string.simple_lu_year_date, sxYear, arr.get(1) + arr.get(2)));
	}
	/**
	 * 时间发生变化了
	 */
	private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	
	@SuppressWarnings("deprecation")
	private void changeTimeView(){
		
	/**add by bxh 2017年4月20日17:24:50 start */
		
		mCalendar.setToNow();
		int hour = mCalendar.hour;
		int minute = mCalendar.minute;
		String str1 = hour+":"+minute; 
		try {
		Date date = TIME_FORMAT.parse(str1);
		String str = SimplePrefUtils.formatTime(date);
		
	/**add by bxh 2017年4月20日17:24:50 end */
		if(mPagedView == null) return;
		View parentView = mPagedView.getSpecialView();
		if (parentView == null || str.length() < 5)
			return;
		for (int i=0; i<5; i++) {
			ImageView img = null;
			
			switch (i) {
				case 0:
					img = (ImageView)parentView.findViewById(R.id.img_1);
					break;
				case 1:
					img = (ImageView)parentView.findViewById(R.id.img_2);
					break;
				case 3:
					img = (ImageView)parentView.findViewById(R.id.img_4);
					break;
				case 4:
					img = (ImageView)parentView.findViewById(R.id.img_5);
					break;
			}
			if (img == null)
				continue;
			int imgResId = 0;
			switch(str.charAt(i)) {
				case '0':
					imgResId = R.drawable.simple_time_0;
					break;
				case '1':
					imgResId = R.drawable.simple_time_1;
					break;
				case '2':
					imgResId = R.drawable.simple_time_2;
					break;
				case '3':
					imgResId = R.drawable.simple_time_3;
					break;
				case '4':
					imgResId = R.drawable.simple_time_4;
					break;
				case '5':
					imgResId = R.drawable.simple_time_5;
					break;
				case '6':
					imgResId = R.drawable.simple_time_6;
					break;
				case '7':
					imgResId = R.drawable.simple_time_7;
					break;
				case '8':
					imgResId = R.drawable.simple_time_8;
					break;
				case '9':
					imgResId = R.drawable.simple_time_9;
					break;
			}
			if (imgResId != 0)
				img.setImageResource(imgResId);
		}
		/*TextView tv = (TextView)parentView.findViewById(R.id.txt_time);
		tv.setText(str);*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/***
	 * 初始化数据
	 * @return 返回成功与否
	 */
	private void initWeather() {
		changeDateView();
		changeTimeView();
		reqWeatherInfo();
	}
	/***
	 * 添加一条记录到DB中,并同步现在的数据集合
	 * @param item
	 * @return 成功与否
	 */
	public boolean addToDB(ItemTable item) {

		if(mAllDatas ==null) {
			mAllDatas =  new SparseArray<List<ItemTable>>();
		}
		int lastPage = mAllDatas.size() - 1;
		
		int r = SimpleDeviceProfile.getInstance().getRows();
		int c = SimpleDeviceProfile.getInstance().getCols();
		
		List<ItemTable> apps = mAllDatas.get(lastPage);
		
		if (apps == null || apps.size() < 1)
			return false;
		
		int last = apps.size() - 1;
		ItemTable tmp = apps.get(last);
		
		if (tmp.y + 1 < r) { // 至少还可以增加一行
			if (tmp.x + 1 < c) {
				item.y = tmp.y;
				item.x = tmp.x;
				tmp.x = tmp.x + 1;
				item.screen = tmp.screen;
			}
			else { // 换行
				item.y = tmp.y;
				item.x = tmp.x;
				tmp.x = 0;
				tmp.y = tmp.y + 1;
				item.screen = tmp.screen;
			}
			
			if (!saveAndGetId(item, tmp)) {
				return false;
			}
			apps.set(last, item);
			apps.add(tmp);
		}
		else if (tmp.x + 1 < c) { //至少还可以增加一列
			item.screen = tmp.screen;
			item.y = tmp.y;
			item.x = tmp.x;
			tmp.x = tmp.x + 1;
			
			if (!saveAndGetId(item, tmp)) {
				return false;
			}
			
			apps.set(last, item);
			apps.add(tmp);
		}
		else { // 增加一页
			item.y = tmp.y;
			item.x = tmp.x;
			item.screen = tmp.screen;
			
			tmp.x = 0;
			tmp.y = 0;
			tmp.screen = lastPage + 1;
			
			if (!saveAndGetId(item, tmp)) {
				return false;
			}
			
			apps.set(last, item);
			
			List<ItemTable> newPage = new ArrayList<ItemTable>(r * c);
			newPage.add(tmp);
			mAllDatas.put(tmp.screen, newPage);
		}
		
		// 刷新页面
		int sz = mAllDatas.size();
		if (sz > lastPage + 1) {
			mPageNum = sz;
			mPagedView.setPageNum(sz);
			mPagedView.syncPages();
		}
		for (int i=lastPage; i<sz; i++) {
			mPagedView.syncPageItems(i, false);
		}
		return true;
	}
	/***
	 * 保存且获取到ID并返回
	 * @param t
	 * @param updateItem 要更新的项
	 * @return 成功与否
	 */
	private boolean saveAndGetId(ItemTable t, ItemTable updateItem) {
		boolean rs = false;
		try {
			LauncherApplication.getDbManager().update(updateItem, "screen", "x", "y");
			rs = LauncherApplication.getDbManager().saveBindingId(t);
		}
		catch (Exception e) {
			updateItem.x = t.x;
			updateItem.y = t.y;
			updateItem.screen = t.screen;
			e.printStackTrace();
			return false;
		}
		
		return rs;
	}
	
	private int mStartPage = -1;
	/***
	 * 删除某个应用项
	 * @param item
	 * @param isLauncher是否直接从桌面上删除
	 * @return 删除是否成功
	 */
	public boolean delItem(ItemTable item, final boolean isLauncher) {
		int pos = -1;
		List<ItemTable> apps = null;

		if(mAllDatas ==null) {
			mAllDatas =  new SparseArray<List<ItemTable>>();
		}
		if (!isLauncher) { // 先找出位置
			pos = findItemPos(item);
			apps = mAllDatas.get(item.screen);
		}
		else {
			apps = mAllDatas.get(item.screen);
			if (apps == null || apps.size() < 0)
				pos = -1;
			pos = apps.indexOf(item);
		}
		mStartPage = item.screen;
		if (pos < 0)
			return false;
		
		// 先对本页数据做处理, 再对后面页处理
		ItemTable delItem = apps.remove(pos);
		ItemTable midItem;
		int j = pos;
		int sz = apps.size();
		
		// 取要删除的view
		int preLeft = 0, preTop = 0, tTop = 0, tLeft = 0;
		SimplePage p = null;
		List<Animator> animators = null;
		View removedView = null;
		SimplePage.LayoutParams params = null;
		if (isLauncher) {
			mOpt = 2;
			animators = new ArrayList<Animator>();
			p = (SimplePage)mPagedView.getChildAt(mStartPage);
			removedView = p.getChildAt(pos);
			/*preTop = removedView.getTop();
			preLeft = removedView.getLeft();*/
			params = (SimplePage.LayoutParams)removedView.getLayoutParams();
			params.setup1(SimpleDeviceProfile.getInstance().getCellW(), SimpleDeviceProfile.getInstance().getCellH());
			preTop = params.getY();
			preLeft = params.getX();
			p.removeViewAt(pos); // 删除
		}
		
		int preScreen, preX, preY;
		int tmpX = delItem.x;
		int tmpScreen = delItem.screen;
		int tmpY = delItem.y;
		for (; j < sz; j++) { // 交换
			midItem = apps.get(j);

			preScreen = midItem.screen;
			preX = midItem.x;
			preY = midItem.y;
			
			midItem.x = tmpX;
			midItem.screen = tmpScreen;
			midItem.y = tmpY;
			
			if (isLauncher) {
				View aView = p.getChildAt(j);
				
				// 比较x, y来确定移动位置
				ValueAnimator a = ValueAnimator.ofFloat(0, 1);
				AnimatorUpdateXYListener selfUpdate = new AnimatorUpdateXYListener();
				selfUpdate.targetView = aView;
				// params = (SimplePage.LayoutParams)aView.getLayoutParams();
				selfUpdate.mParams = (SimplePage.LayoutParams)aView.getLayoutParams();
				selfUpdate.mParams.setup1(SimpleDeviceProfile.getInstance().getCellW(), SimpleDeviceProfile.getInstance().getCellH());
				//tTop = aView.getTop();
				//tLeft = aView.getLeft();
				
				tTop = selfUpdate.mParams.getY();
				tLeft = selfUpdate.mParams.getX();
				
				selfUpdate.mParams.cellX = tmpX;
				selfUpdate.mParams.cellY = tmpY;
				if (preY > tmpY) {
					selfUpdate.xLen = preLeft - tLeft;
					selfUpdate.yLen = preTop - tTop;
				}
				else {
					selfUpdate.xLen = preLeft - tLeft;
				}
				a.addUpdateListener(selfUpdate);
				a.setDuration(DURATION_TIME);
				animators.add(a);
				
				preLeft = tLeft;
				preTop = tTop;
			}
			
			tmpX = preX;
			tmpY = preY;
			tmpScreen = preScreen;
			
		}
		
		int startScreen = item.screen + 1;
		
		int scrCount = mAllDatas.size();
		
		List<ItemTable> tmpItems;
		for (int i=startScreen; i<scrCount; i++) {
			tmpItems = mAllDatas.get(i);
			
			int size = tmpItems.size();
			for (int jj=0; jj<size; jj++) {
				midItem = tmpItems.get(jj);

				preScreen = midItem.screen;
				preX = midItem.x;
				preY = midItem.y;
				
				midItem.x = tmpX;
				midItem.screen = tmpScreen;
				midItem.y = tmpY;
				
				tmpX = preX;
				tmpY = preY;
				tmpScreen = preScreen;
				
				if (jj == 0) {
					apps.add(midItem);
					
					if (isLauncher && i==startScreen) {
						SimplePage tp = (SimplePage)mPagedView.getChildAt(i);
						// 做动画事情
						if (removedView != null) {
							params.cellX = midItem.x;
							params.cellY = midItem.y;
							params.setup1(SimpleDeviceProfile.getInstance().getCellW(), SimpleDeviceProfile.getInstance().getCellH());
							if (midItem.type == IConstant.TYPE_ADD) {
								removedView = tp.getChildAt(jj);
								tp.removeViewAt(jj);
							}
							else {
								int childSz = mPagedView.getChildCount();
								SimplePage pageView = (SimplePage)mPagedView.getChildAt(childSz - 1);
								int childCount = pageView.getChildCount();
								if (childCount > 1) {
									pageView.removeViewAt(childCount - 1);
									pageView.removeViewAt(childCount - 2);
								}
							}
							
							int x1 = params.getX();
							int y1 = params.getY();
							params.setX(x1 + (SimpleDeviceProfile.getInstance().getLeftPadding() + SimpleDeviceProfile.getInstance().getCellW()));
							params.setY(SimpleDeviceProfile.getInstance().getTopPadding());
							removedView.setLayoutParams(params);
							initAppItem(midItem, removedView);
							p.addView(removedView, apps.size() - 1, params);
							ValueAnimator b = ValueAnimator.ofFloat(0, 1);
							AnimatorUpdateXYListener selfUpdate = new AnimatorUpdateXYListener();
							selfUpdate.targetView = removedView;
							selfUpdate.mParams = params;
							selfUpdate.xLen =  - (SimpleDeviceProfile.getInstance().getLeftPadding() + SimpleDeviceProfile.getInstance().getCellW()); //params.getX()
							selfUpdate.yLen = y1 - SimpleDeviceProfile.getInstance().getTopPadding();
							b.addUpdateListener(selfUpdate);
							b.setDuration(DURATION_TIME);
							animators.add(b);
						}
					}
				}
			}
			
			if (tmpItems.size() > 0)
				tmpItems.remove(0);
			// save
			try {
				LauncherApplication.getDbManager().update(apps, "screen", "x", "y");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			apps = tmpItems;
		}
		
		//mPagedView.delItemAt(startPage, pos);
		try {
			LauncherApplication.getDbManager().delete(delItem);
			item.setId(0);
			if (apps.size()>0)
				LauncherApplication.getDbManager().update(apps, "screen", "x", "y");
			else {
				mAllDatas.remove(mAllDatas.size() - 1);
				mPageNum = mAllDatas.size();
				mPagedView.setPageNum(mPageNum);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		if (!isLauncher) {
			// 刷新UI
			mPagedView.delLast2Item(mStartPage);
			mPageNum = mAllDatas.size();
			for (int pp=mStartPage; pp<mPageNum; pp++) {
				mPagedView.syncPageItems(pp, false);
			}
		}
		else {
			// 执行动画
			int sss = animators.size();
			Animator amt = animators.get(sss - 1);
			amt.addListener(mAnimListener);
			
			if (mAnimatorSet != null) {
				mAnimatorSet.end();
			}
			mAnimatorSet = new AnimatorSet();
			mAnimatorSet.playSequentially(animators);
			mAnimatorSet.start();
		}
		return true;
	}
	/***
	 * 初始化APP控件
	 * @param item
	 * @param v
	 */
	private void initAppItem(ItemTable item, final View v) {
		switch(item.type) {
			case IConstant.TYPE_APP:
				
				if (mClick == null) {
					mClick = new ViewOnClickListener();
				}
					v.setOnClickListener(mClick);
				if(mLongClick ==null) {
					mLongClick = new ViewLongClick();
				}
				v.setOnLongClickListener(mLongClick);
				v.setTag(item);
				ImageView imgHead = (ImageView)v.findViewById(R.id.img_head);
				
				Drawable d = PagedDataModel.getInstance().iconCache.get(new ComponentName(item.pkgName,item.clsName));
				if (d == null) {
					PagedDataModel.getInstance().getAppIcon(item.pkgName, item.clsName, imgHead);
				}
				imgHead.setImageDrawable(d);
				TextView title = (TextView)v.findViewById(R.id.txt_title);
				title.setText(item.title);
				
				ImageView imgDel = (ImageView)v.findViewById(R.id.img_del);
				if (imgDel != null && 
						item.canDel)
					imgDel.setVisibility(View.VISIBLE);
				
				if (item.bgResId > 0)
					v.setBackgroundResource(item.bgResId);
				else {
					item.bgResId = PagedDataModel.getInstance().getAppBgId();
					v.setBackgroundResource(item.bgResId);
				}
				break;
			case IConstant.TYPE_ADD:
				if (mClick == null) {
					mClick = new ViewOnClickListener();
				}
					v.setOnClickListener(mClick);
				v.setOnLongClickListener(mLongClick);
				v.setTag(item);
				boolean b = SimplePrefUtils.getBoolean(mCtx, IConstant.KEY_LOCK);
				if (b) {
					v.setVisibility(View.GONE);
				}
				else
					v.setVisibility(View.VISIBLE);
				break;
		}
	}
	/***
	 * 找出对应的位置
	 * @param item
	 * @return
	 */
	private int findPos(ItemTable item, List<ItemTable> datas) {
		
		if (item.screen < 0)
			return -1;
		
		if (datas == null || datas.size() < 0)
			return -1;
		int sz = datas.size();
		
		for (int i=0; i<sz; i++) {
			ItemTable it = datas.get(i);
			if (it.getId() == item.getId())
				return i;
		}
		
		return -1;
	}
	
	/***
	 * 找出对应的真实项
	 * @param item
	 * @return
	 */
	private int findItemPos(ItemTable item) {

		if(mAllDatas ==null) {
			mAllDatas =  new SparseArray<List<ItemTable>>();
		}
		int sz = mAllDatas.size();
		
		for (int i=2; i<sz; i++) {
			List<ItemTable> apps = mAllDatas.get(i);
			int asz = apps.size();
			for (int j=0; j<asz; j++) {
				ItemTable it = apps.get(j);
				if (it.getId() == item.getId()) {
					item.screen = it.screen;
					return j;
				}
			}
		}
		
		return -1;
	}
	
	private Random mRandom = new Random();
	
	public int getContactBgId() {
		return mContactBgs.get(mRandom.nextInt(mContactBgs.size()));
	}
	
	public int getAppBgId() {
		return mAppBgs.get(mRandom.nextInt(mAppBgs.size()));
	}
	
	
	/**包管理器*/
	private PackageManager mPackageManager;
	
	public Drawable getFullResIcon(ResolveInfo info) {
		Drawable d =getFullResIcon(info.activityInfo);
		if(d==null) {
			return null;
		}
	
		return d;
	}
	
	
	 public Bitmap drawableToBitmap1(Drawable drawable,int w) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						w,
						w,
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.save();
		drawable.setBounds(0, 0, w,
				w);
		drawable.draw(canvas);
		canvas.restore();
		if (drawable!=null&&drawable instanceof BitmapDrawable) {
			BitmapDrawable b = (BitmapDrawable) drawable;
			 if (b != null&&b.getBitmap()!=bitmap) {
		            b.setCallback(null);
		            b = null;
		            System.gc();
			 }}
		
		return bitmap;

	}
	
	public Drawable getFullResIcon(ActivityInfo info) {

		Resources resources;
		try {
			if (null == mPackageManager)
				mPackageManager = mCtx.getPackageManager();
			resources = mPackageManager
					.getResourcesForApplication(info.applicationInfo);
		} catch (Exception e) {
			resources = null;
		}
		if (resources != null) {
			int iconId = info.getIconResource();
			if (iconId != 0) {
				return getFullResIcon(resources, iconId);
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	private Drawable getFullResIcon(Resources resources, int iconId) {
		Drawable d;
		try {
			//d = resources.getDrawableForDensity(iconId, mIconDpi);
			d = resources.getDrawable(iconId, null);
		} catch (Resources.NotFoundException e) {
			d = null;
		}

		return (d != null) ? d : mCtx.getResources().getDrawable(
				R.drawable.ic_launcher, null);
	}
	/***
	 * 异步获取应用图片
	 * @param pkg
	 * @param clsName
	 * @param img
	 */
	public void getAppIcon(String pkg, String clsName, ImageView img) {
		if (TextUtils.isEmpty(pkg) || TextUtils.isEmpty(clsName))
			return;
		FetchIcon f = new FetchIcon(img);
		f.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new ComponentName(pkg,clsName));
	}
	/***
	 * 获取图标
	 * @author fanjunchen
	 *
	 */
	class FetchIcon extends AsyncTask<ComponentName, Void, Void> {

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		
		private Drawable d;
		
		private WeakReference<ImageView> refImg;
		
		public FetchIcon(ImageView v) {
			refImg = new WeakReference<ImageView>(v);
		}
		/***
		 * args[0]:pkgName, args[1]: clsName
		 */
		@Override
		protected Void doInBackground(ComponentName... args) {
			// TODO
			if (null == mPackageManager)
				mPackageManager = mCtx.getPackageManager();
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			mainIntent.setComponent(args[0]);
			List<ResolveInfo> ls = mPackageManager.queryIntentActivities(mainIntent, 0);
			if (ls != null)
			for (ResolveInfo app : ls) {
				d = getFullResIcon(app);
				iconCache.put(args[0], d);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			ImageView img = refImg.get();
			if (img != null) {
				img.setImageDrawable(d);
			}
			
			d = null;
			refImg.clear();
			refImg = null;
		}
	}
	
	
	/***
	 * 删除某个应用项,并重新更新DB
	 * @param item
	 */
	public static void delItem(ItemTable item) {
		int pos = -1;
		List<ItemTable> apps = null;
		
		SparseArray<List<ItemTable>> allDatas = new SparseArray<List<ItemTable>>();
		int start = item.screen;
		for (; ; start ++) {
			try {
				List<ItemTable> as = LauncherApplication.getDbManager().selector(ItemTable.class).where("screen", "=", start)
				.orderBy("y").orderBy("x").findAll();
				if (as == null || as.size() <= 0)
					break;
				
				if (start == item.screen) {
					pos = findItemPos(as, item);
				}
				allDatas.put(start, as);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		apps = allDatas.get(item.screen);
		
		// 先对本页数据做处理, 再对后面页处理
		ItemTable delItem = apps.remove(pos);
		ItemTable midItem;
		int j = pos;
		int sz = apps.size();
		
		int preScreen, preX, preY;
		int tmpX = delItem.x;
		int tmpScreen = delItem.screen;
		int tmpY = delItem.y;
		for (; j < sz; j++) { // 交换
			midItem = apps.get(j);

			preScreen = midItem.screen;
			preX = midItem.x;
			preY = midItem.y;
			
			midItem.x = tmpX;
			midItem.screen = tmpScreen;
			midItem.y = tmpY;
			
			tmpX = preX;
			tmpY = preY;
			tmpScreen = preScreen;
			
		}
		
		int startScreen = item.screen + 1;
		
		List<ItemTable> tmpItems;
		for (int i=startScreen; i<start; i++) {
			tmpItems = allDatas.get(i);
			
			int size = tmpItems.size();
			for (int jj=0; jj<size; jj++) {
				midItem = tmpItems.get(jj);

				preScreen = midItem.screen;
				preX = midItem.x;
				preY = midItem.y;
				
				midItem.x = tmpX;
				midItem.screen = tmpScreen;
				midItem.y = tmpY;
				
				tmpX = preX;
				tmpY = preY;
				tmpScreen = preScreen;
				
				if (jj == 0) {
					apps.add(midItem);
				}
			}
			
			if (tmpItems.size() > 0)
				tmpItems.remove(0);
			try {
				LauncherApplication.getDbManager().update(apps, "screen", "x", "y");
			} catch (Exception e) {
				e.printStackTrace();
			}
			apps = tmpItems;
		}
		
		try {
			LauncherApplication.getDbManager().delete(delItem);
			item.setId(0);
			if (apps.size()>0)
				LauncherApplication.getDbManager().update(apps, "screen", "x", "y");
			else {
				allDatas.remove(allDatas.size() - 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * 找出对应的真实项
	 * @param item
	 * @return
	 */
	private static int findItemPos(List<ItemTable> apps, ItemTable item) {
		
		int asz = apps.size();
		for (int j=0; j<asz; j++) {
			ItemTable it = apps.get(j);
			if (it.getId() == item.getId()) {
				item.screen = it.screen;
				return j;
			}
		}
		return -1;
	}
	/***
	 * 判断是否可以添加到增加列表, 若能测添加否则不添加
	 */
	private void addApp(String pkg) {
		
		if (TextUtils.isEmpty(pkg))
			return;
		
		if (null == AllAppsModel.allList || AllAppsModel.allList.size()<1)
			return;
		
		addNewAppToList(pkg);
	}
	
	private void delApp(final String pkg) {
		if (TextUtils.isEmpty(pkg))
			return;
		
		if (null == AllAppsModel.allList || AllAppsModel.allList.size()<1)
			return;
		
		synchronized (AllAppsModel.allList) {
			int sz = AllAppsModel.allList.size();
			for(int i=sz - 1; i>=0; i--) {
				ItemTable itb = AllAppsModel.allList.get(i);
				if (itb != null && itb.pkgName.equals(pkg)) {
						AllAppsModel.allList.remove(i);
						if(update!=null)
						update.OnRemove(i);
				}
			}
		}
	}
	/***
     * 添加新应用到应用列表中
     * @param pkgName
     */
    private void addNewAppToList(final String pkgName) {
    	
    	final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mainIntent.setPackage(pkgName);
    	PackageManager mPackageManager = mCtx.getPackageManager();
    	
		List<ResolveInfo> apps = mPackageManager.queryIntentActivities(mainIntent, 0);

		if (AllAppsModel.allList == null || AllAppsModel.allList.size() < 1)
			return;
		for(ItemTable i :AllAppsModel.allList) {
			if(i.pkgName.equals(pkgName)) {
				return ;
			}
		}
		for (ResolveInfo app : apps) {
			ItemTable a = new ItemTable();
			
			a.title = (String) app.loadLabel(mPackageManager);
            a.pkgName = app.activityInfo.applicationInfo.packageName;
            a.clsName = app.activityInfo.name;
            a.spanX = 1;
            a.spanY = 1;
            a.type = IConstant.TYPE_APP;
            mainIntent.setComponent(new ComponentName(a.pkgName, a.clsName));
            a.intent = mainIntent.toUri(0);
            a.isExist = false;
            if (iconCache.get(new ComponentName(a.pkgName,a.clsName)) == null)
            	iconCache.put(new ComponentName(a.pkgName,a.clsName), getFullResIcon(app));
            synchronized (AllAppsModel.allList) {
        		AllAppsModel.allList.add((ItemTable) a);
                if(update!=null)
                update.onAdd(a);
			}
//            Log.d("addddddd---------",  a.pkgName);
		}
    }
	//add by zhouerlong  meminfo out oom

}
