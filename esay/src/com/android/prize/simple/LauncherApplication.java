/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.prize.simple;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;

/**
 * @author Administrator
 */
public class LauncherApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initXUtils();
    }

    private final int DB_VERSION = 4;

    private String getDbFile() {
        String filePath = Environment.getExternalStorageDirectory() + File.separator + "launcher" + File.separator + "db" + File.separator;
        return filePath;
    }

    private DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
            .setDbName("leftPage.db")
            .setDbDir(new File(getDbFile()))
            .setDbVersion(DB_VERSION)
            .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                @Override
                public void onUpgrade(DbManager db, int oldVersion,
                                      int newVersion) {
                    // TODO: upgrade
                    Log.v("LK", "oldVersion==" + oldVersion + "newVersion=="
                            + newVersion);
                    if (newVersion > oldVersion) {
                        try {
                            db.dropDb();
                            db.getDaoConfig();
                        } catch (DbException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }
            });
    /**
     * 单例数据库管理类
     */
    private static DbManager xDbManager = null;

    public static DbManager getDbManager() {
        return xDbManager;
    }

    private void initXUtils() {
        x.Ext.init(this);
        // x.Ext.setDebug(true);
        try {
            xDbManager = x.getDb(daoConfig);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public SQLiteDatabase getDatabase() {
        return xDbManager.getDatabase();
    }

}