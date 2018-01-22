package com.android.prize.simple.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.xutils.ex.DbException;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.prize.simple.LauncherApplication;
import com.android.prize.simple.R;
import com.android.prize.simple.table.ItemTable;

/***
 * 用来查询所有的桌面可显示activity
 * @author fanjunchen
 *
 */
public class AllAppsModel {

	private Context mContext;
	/**包管理器*/
	private PackageManager mPackageManager;
	/**获取图标的大小*/
	private int mIconDpi;
	/**已经在表中存在的 应用 (key: 类名)**/
	static HashMap<ComponentName, ItemTable> existMap = new HashMap<ComponentName, ItemTable>();
	/**所有应用数据*/
	public static List<ItemTable> allList = null;
	/**已经添加应用类名*/
	private List<ComponentName> addedCls = new ArrayList<ComponentName>(30);
	
	private AllAppAdapter mAdapter;
	
	private ListView mListView;
	
	private PagedDataModel pModel;
	
	private GetData mTask = null;
	/***
	 * 用这个构造方法必须要调用setContext
	 */
	public AllAppsModel() {
	}
	/***
	 * 设置上下文环境
	 * @param ctx
	 */
	public void setContext(Context ctx) {
		mContext = ctx;
		pModel = PagedDataModel.getInstance();
	}
	
	public AllAppsModel(Context ctx) {
		mContext = ctx;
		pModel = PagedDataModel.getInstance();
	}
	/***
	 * 设置控件并获取数据
	 * @param listView
	 */
	public void setListView(ListView listView) {
		mListView = listView;
		
		if (mAdapter == null)
			mAdapter = new AllAppAdapter(mContext);
		
		mListView.setAdapter(mAdapter);
		
		mListView.setOnItemClickListener(mItemClick);
		
		mTask = new GetData();
		mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	public void  notifyDataSetChanged(List<ItemTable> list) {
		if(mAdapter!= null) {
			mAdapter.setData(list);
			mAdapter.notifyDataSetChanged();
		}
	}
	/**是否在处理中*/
	private boolean isDealing = false;
	
	/**点击事件*/
	private OnItemClickListener mItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View v, int pos,
				long id) {
			// TODO Auto-generated method stub
			if (isDealing)
				return ;
			isDealing = true;
			int sz = allList.size();
			if (pos < 0 || pos >= sz || sz < 1 
					|| null == pModel) {
				isDealing = false;
				return;
			}
			// 判断是添加还是删除
			ItemTable item = allList.get(pos);
			if (!item.canDel) {
				isDealing = false;
				return;
			}
			if (addedCls.contains(new ComponentName(item.pkgName,item.clsName))) { //删除
				
				if (pModel.delItem(item, false)) {
					addedCls.remove(new ComponentName(item.pkgName,item.clsName));
					item.isExist = false;
					ImageView img = (ImageView)v.findViewById(R.id.img_ico);
					if (img != null)
						img.setImageResource(R.drawable.simple_icon_add);
				}
			}
			else { //添加
				if (pModel.addToDB(item)) {
					addedCls.add(new ComponentName(item.pkgName,item.clsName));
					item.isExist = true;
					ImageView img = (ImageView)v.findViewById(R.id.img_ico);
					if (img != null)
						img.setImageResource(R.drawable.simple_icon_del);
				}
			}
			isDealing = false;
		}
	};
	/**
	 * 获取系统所有的应用
	 */
	private void getAllIntents() { // 获取系统所有的应用
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		mIconDpi = activityManager.getLauncherLargeIconDensity();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<ResolveInfo> apps = null;
		
		mPackageManager = mContext.getPackageManager();
		
		apps = mPackageManager.queryIntentActivities(mainIntent, 0);
		
		mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		
		PagedDataModel pModel = PagedDataModel.getInstance();
		for (ResolveInfo app : apps) {
			ComponentName comp = new ComponentName(app.activityInfo.packageName,app.activityInfo.name);
			ItemTable a = (null == existMap ? null : existMap.get(comp));
			// LogUtils.i("===clsName==" + app.activityInfo.name);
			if (null == a) {
				
				a = new ItemTable();
				
				a.title = (String) app.loadLabel(mPackageManager);
	            a.pkgName = app.activityInfo.applicationInfo.packageName;
	            a.clsName = app.activityInfo.name;
	            a.spanX = 1;
	            a.spanY = 1;
	            a.type = IConstant.TYPE_APP;
	            
	            a.isExist = false;
	            
	            mainIntent.setComponent(new ComponentName(app.activityInfo.applicationInfo.packageName, app.activityInfo.name));
	            a.intent = mainIntent.toUri(0);
	            // componentNames.add(new ComponentName(packageName, app.activityInfo.name));
	            if (pModel != null && pModel.iconCache!=null&&pModel.iconCache.get(new ComponentName(a.pkgName,a.clsName)) == null)
	            	pModel.iconCache.put(new ComponentName(a.pkgName,a.clsName), pModel.getFullResIcon(app));
			}
			else
			{
				a.title = (String) app.loadLabel(mPackageManager);
			}
			if (allList != null && !a.pkgName.equals("com.android.music")&& !a.pkgName.equals("com.android.prize.simple")
					&& !a.pkgName.equals("com.prize.factorytest") &&!a.pkgName.equals("com.prize.prizethemecenter"))
				synchronized (allList) {
					allList.add(a);
				}
		}
	}
	/***
	 * 从DB中获取应用数据
	 */
	private void getDataFromDb() {
		try {
			List<ItemTable> dbData = LauncherApplication.getDbManager().selector(ItemTable.class)
					.where("clsName", "!=", null).findAll();
			
			if (dbData == null)
				return;
			if (null == existMap)
				existMap = new HashMap<ComponentName, ItemTable>();
			existMap.clear();
			addedCls.clear();
			for (ItemTable t : dbData) {
				if (TextUtils.isEmpty(t.clsName))
					continue;
				
				existMap.put(new ComponentName(t.pkgName,t.clsName), t);
				addedCls.add(new ComponentName(t.pkgName,t.clsName));
			}
			
		} catch (DbException e) {
			e.printStackTrace();
		}
	}
	
	public void destroy() {
		if (mTask != null) {
			mTask.cancel(true);
		}
		mAdapter = null;
	}
	/**语言是否发生了变化*/
	private static boolean mIsChange = false;
	
	private static String mPreLang = null;
	/***
	 * 是否语言变化了
	 * @return
	 */
	private void isChange() {
		Locale locale = mContext.getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.equals(mPreLang))
			mIsChange = false;
		else
			mIsChange = true;
		mPreLang = language;
	}
	/***
	 * 异步获取数据并更新
	 * @author fanjunchen
	 *
	 */
	class GetData extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... args) {
			if (null == allList)
				allList = new ArrayList<ItemTable>();
			synchronized (allList) {
				getDataFromDb();
				isChange();
				if (mIsChange)
					allList.clear();
				if (allList.size() < 1) {
					getAllIntents();
				}
				else {
					// 与存在的比较
					int sz = allList.size();
					for (int i=0; i<sz; i++) {
						ItemTable a = allList.get(i);
						ItemTable b = existMap.get(new ComponentName(a.pkgName,a.clsName));
						if (b != null) {
							b.title = a.title;
							allList.set(i, b);
						}
						else
							a.isExist = false;
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// 刷新UI
			if (mAdapter != null)
				mAdapter.setData(allList);
		}
	}
	
	public void getDataOnly() {
		new GetDataOnly().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	/***
	 * 异步获取数据并更新
	 * @author fanjunchen
	 *
	 */
	class GetDataOnly extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... args) {
			
			if (null == allList)
				allList = new ArrayList<ItemTable>();
			synchronized (allList) {
				getDataFromDb();
				isChange();
				if (mIsChange)
					allList.clear();
// add by bxh				
				if(allList == null)
					return null;
// add by bxh				
				if (allList.size() < 1) {
					getAllIntents();
				}
				else {
					// 与存在的比较
					int sz = allList.size();
					for (int i=0; i<sz; i++) {
						ItemTable a = allList.get(i);
						ItemTable b = existMap.get(new ComponentName(a.pkgName,a.clsName));
						if (b != null) {
							allList.set(i, b);
						}
						else
							a.isExist = false;
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (pModel != null)
				pModel.getDatas();
		}
	}
	
	public static void reset() {
		if (allList != null)
			synchronized (allList) {
				
				allList.clear();
				if(existMap!=null)
				existMap.clear();
				existMap = null; 
				allList = null;
			}
	}
}
