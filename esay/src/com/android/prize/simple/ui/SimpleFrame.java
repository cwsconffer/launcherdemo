package com.android.prize.simple.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.prize.simple.model.PagedDataModel;

/***
 * 老人主题 主UI
 * 
 * @author fanjunchen
 * 
 */
public class SimpleFrame extends FrameLayout implements 
		View.OnLongClickListener {

	private PagedDataModel mPageModel;

	//add by zhouerlong  meminfo out oom
	public PagedDataModel getPageModel() {
		return mPageModel;
	}

	public void setmPageModel(PagedDataModel mPageModel) {
		this.mPageModel = mPageModel;
	}
	//add by zhouerlong  meminfo out oom

	private Context mCtx;

	private Activity mAct;
	

    private WeakReference<Handler> h;

	public SimpleFrame(Context context) {
		this(context, null);
	}

	public SimpleFrame(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimpleFrame(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	private final Rect mInsets = new Rect();

	//add by zhouerlong  meminfo out oom
	private boolean speak=true;
	//add by zhouerlong  meminfo out oom

	public void enableHardwareLayer(boolean hasLayer) {
		this.setLayerType(hasLayer ? LAYER_TYPE_HARDWARE : LAYER_TYPE_NONE,
				null);
	}

	public boolean fitSystemWindowsByPrizeScrollView(Rect insets) {
		final int n = getChildCount();
		for (int i = 0; i < n; i++) {
			final View child = getChildAt(i);
			final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) child
					.getLayoutParams();
				flp.topMargin += (insets.top - mInsets.top);
				flp.leftMargin += (insets.left - mInsets.left);
				flp.rightMargin += (insets.right - mInsets.right);
				flp.bottomMargin += (insets.bottom - mInsets.bottom);
		}
		return true; // I'll take it from here
	}

	public SimpleFrame(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		mCtx = context;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		this.setOnLongClickListener(this);
	//add by zhouerlong  meminfo out oom
		if (mPageModel == null)
			mPageModel = new PagedDataModel(mCtx);
		if(mPageModel == null) {

			mPageModel = new PagedDataModel(mCtx);
		}
		mPageModel.setLauncher(mCtx,this);
	//add by zhouerlong  meminfo out oom
		if (mAct != null)
			mPageModel.setActivity(mAct);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		// if (mPageModel != null)
		// mPageModel.destroy();
		super.onDetachedFromWindow();
	}

	public void setActivity(Activity act) {
		mAct = act;
		if (mPageModel != null)
			mPageModel.setActivity(act);
	}

	/***
	 * 销毁
	 */
	public void destroy() {
//		unbindservice();
		mPageModel.destroy();

	//add by zhouerlong  meminfo out oom
	}

	public void onActivityResult(int reqCode, Intent data) {
		if (mPageModel != null) {
			mPageModel.onActivityResult(reqCode, data);
		}
	}

	/***
	 * 当menu键被click了
	 */
	public void onMenu() {
		if (mPageModel != null) {
			mPageModel.showSettings();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		heightSize = heightSize - mInsets.bottom;

		int childWidthSize = widthSize - (getPaddingLeft() + getPaddingRight());
		int childHeightSize = heightSize
				- (getPaddingTop() + getPaddingBottom());

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int childWidthMeasureSpec = 0;
			int childheightMeasureSpec = 0;
			childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize,
					MeasureSpec.AT_MOST);

			childheightMeasureSpec = MeasureSpec.makeMeasureSpec(
					childHeightSize, MeasureSpec.AT_MOST);

			child.measure(childWidthMeasureSpec, childheightMeasureSpec);
		}

		super.onMeasure(widthMeasureSpec,
				MeasureSpec.makeMeasureSpec(heightSize, heightMode));
	}

	/***
	 * 当用户按下返回键后
	 */
	public void onBackPressed() {
		if (mPageModel != null) {
			mPageModel.onBackPressed();
		}
	}
	

	/***
	 * 当用户按下返回键后
	 */
	public void onNewIntent() {
		if (mPageModel != null) {
			mPageModel.onNewIntent();
		}
	}

	@Override
	public boolean onLongClick(View arg0) {
			onMenu();
		return false;
	}


//	/* add baidu 语言 begin */
//
//	private static  PrizeTTSInterface mPrizeTTS;
//
//	private void bindservice() {
//		if (mPrizeTTS == null) {
//			Intent intent = new Intent("com.prize.tts.service");
//			intent.setPackage("com.prize.tts");
//			mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
//		}
//	}
//	
//	public  void speak(String s) {
//
//		if (mPrizeTTS != null) {
//			try {
//				if(s!=null)
//				mPrizeTTS.stop();
//				mPrizeTTS.speak(s);
//			} catch (RemoteException ex) {
//				ex.printStackTrace();
//			}
//		}
//	}
//	
//	/*mode 1为电话  2位短信 3音乐播报 4桌面播报	*/
//	public  void speakText(String s,int mode) {
//		if (mPrizeTTS != null) {
//			try {
//				if(s!=null)
//				mPrizeTTS.speakText(s,mode);
//			} catch (RemoteException ex) {
//				ex.printStackTrace();
//			}
//		}
//	}
//
//	private void unbindservice() {
//		if (mPrizeTTS != null) {
//			try{
//			mContext.unbindService(conn);
//			mPrizeTTS = null;
//			}catch(Exception e){}
//		}
//	}
//
//	private ServiceConnection conn = new ServiceConnection() {
//
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			mPrizeTTS = PrizeTTSInterface.Stub.asInterface(service);
//			((Activity) mContext).runOnUiThread(new Runnable() {
//
//				@Override
//				public void run() {
////					Toast.makeText(mContext, "bindservice ok",
////							Toast.LENGTH_LONG).show();
//				}
//			});
//
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			// TODO Auto-generated method stub
//
//		}
//
//	};
//
//	public static PrizeTTSInterface getmPrizeTTS() {
//		return mPrizeTTS;
//	}

	/* add baidu 语言 end */

}