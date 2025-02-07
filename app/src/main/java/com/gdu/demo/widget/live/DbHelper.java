package com.gdu.demo.widget.live;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Woo on 2019-1-7.
 */

public class DbHelper extends SQLiteOpenHelper
{
    public static final String DB_NAME = "saga.db";



    // 数据库表名
    public static final String TABLE_NAME = "t_liveHistory";


    public DbHelper(Context context)
    {
        super(context,DB_NAME,null,1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " +
                TABLE_NAME +
                "(_id integer primary key autoincrement, " +
                "history" + " varchar "
                + ")";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
