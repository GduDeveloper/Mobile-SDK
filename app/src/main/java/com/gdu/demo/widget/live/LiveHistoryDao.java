package com.gdu.demo.widget.live;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Woo on 2019-1-7.
 */

public class LiveHistoryDao
{
    private final DbHelper dbHelper;

    private final String HISTORY = "history";
    private final String _ID = "_id";

    private final SQLiteDatabase sqLiteDatabase;

    public LiveHistoryDao(Context context)
    {
        dbHelper = new DbHelper(context);
        sqLiteDatabase = dbHelper.getWritableDatabase();
    }

    public void insertHistory(String url)
    {
        ContentValues values = new ContentValues();
        values.put( HISTORY ,url);
        sqLiteDatabase.insert(DbHelper.TABLE_NAME, null, values);
    }

    public List<LiveHistory>  queueHistry()
    {
        Cursor cursor = sqLiteDatabase.query(DbHelper.TABLE_NAME,
                new String[]{_ID,HISTORY},
                null,
                null,
                null,
                null,
                _ID + " desc");// 注意空格！

        int id_Index = cursor.getColumnIndex(_ID);
        int url_Index = cursor.getColumnIndex(HISTORY);
        List<LiveHistory> data = new ArrayList<>();

        while (cursor.moveToNext())
        {
            LiveHistory history = new LiveHistory();
            history._ID = cursor.getInt(id_Index);
            history.History = cursor.getString(url_Index);
            data.add(history);
        }
        return data;
    }

    /*********
     * 删除操作
     * @param liveHistory
     */
    public void deleteHistory(LiveHistory liveHistory)
    {
        if( liveHistory._ID > -1 )
            sqLiteDatabase.delete(DbHelper.TABLE_NAME,HISTORY + " = ?",new String[]{ liveHistory.History } );
        else
            sqLiteDatabase.delete(DbHelper.TABLE_NAME,_ID + " = ?",new String[]{ liveHistory._ID+"" } );
    }

}
