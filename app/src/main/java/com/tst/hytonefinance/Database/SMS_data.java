package com.tst.hytonefinance.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SMS_data extends SQLiteOpenHelper {

    public SMS_data(@Nullable Context context) {
        super(context, "smsData", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table smsData(title TEXT,sender TEXT ,message TEXT,date_time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
        DB.execSQL("drop Table if exists smsData");
    }

    public Boolean Insert_Subject_data(String title,String sender,String message,String date_time)
    {
        SQLiteDatabase DB=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("title",title);
        contentValues.put("sender",sender);
        contentValues.put("message",message);
        contentValues.put("date_time",date_time);

        String Table_name="smsData";
        long result =DB.insert(Table_name,null,contentValues);
        return result != -1;
    }

//    public Boolean Delete_data()
//    {
//
//
//        SQLiteDatabase DB=this.getWritableDatabase();
//        long result =DB.delete("smsData",null,null);
//        if(result==-1)
//        {
//            return false;
//        }else
//        {
//            return true;
//        }
//
//    }

//    public Cursor Get_Subject_data()
//    {
//        SQLiteDatabase DB=this.getWritableDatabase();
//        String Table_name="smsData";
//        Cursor cursor=DB.rawQuery("Select * from  "+Table_name.replaceAll("\\s",""), null);
//        return cursor;
//
//    }
//
//    public void Drop_Subject_data()
//    {
//        SQLiteDatabase DB=this.getWritableDatabase();
//        DB.execSQL("drop Table if exists coordinates");
//
//    }
}
