package com.android.prize.simple.utils;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class SimpleUtils {
	private static int iconSize=110;

	 /**
     * Resizes an icon drawable to the correct icon size.
     */
    static public void resizeIconDrawable(Drawable icon) {
    	try {
    		BitmapDrawable b = (BitmapDrawable) icon;
    		
            icon.setBounds(0, 0, iconSize, iconSize);
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
}
