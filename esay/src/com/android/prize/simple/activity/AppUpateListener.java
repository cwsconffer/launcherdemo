package com.android.prize.simple.activity;

public interface AppUpateListener {
	public void onAdd(Object o);
	public  void OnRemove(Object o);
	public <T> void OnUpdate(Object o);
}
