package com.tst.hytonefinance.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Media_DB extends SQLiteOpenHelper {

    public Media_DB(@Nullable Context context) {
        super(context, "media", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table media(file_name TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
        DB.execSQL("drop Table if exists media");
    }

    public Boolean Insert_Subject_data(String name)
    {
        SQLiteDatabase DB=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("file_name",name);
        String Table_name="media";
        long result =DB.insert(Table_name,null,contentValues);
        return result != -1;
    }

    public Boolean Delete_data()
    {


        SQLiteDatabase DB=this.getWritableDatabase();
        long result =DB.delete("media",null,null);
        return result != -1;

    }

    public Cursor Get_Subject_data()
    {
        SQLiteDatabase DB=this.getWritableDatabase();
        String Table_name="media";
        return DB.rawQuery("Select * from  "+Table_name.replaceAll("\\s",""), null);

    }

    public void Drop_Subject_data()
    {
        SQLiteDatabase DB=this.getWritableDatabase();
        DB.execSQL("drop Table if exists media");

    }


}
