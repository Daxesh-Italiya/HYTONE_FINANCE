package com.tst.hytonefinance.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Location_DB extends SQLiteOpenHelper {

    public Location_DB(@Nullable Context context) {
        super(context, "coordinates", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table coordinates(Longitude TEXT,Latitude TEXT ,Date_Time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
        DB.execSQL("drop Table if exists coordinates");
    }

//    public Boolean Insert_Subject_data(String Longitude,String Latitude,String Date_Time)
//    {
//        SQLiteDatabase DB=this.getWritableDatabase();
//        ContentValues contentValues=new ContentValues();
//        contentValues.put("Longitude",Longitude);
//        contentValues.put("Latitude",Latitude);
//        contentValues.put("Date_Time",Date_Time);
//        String Table_name="coordinates";
//        long result =DB.insert(Table_name,null,contentValues);
//        if(result==-1)
//        {
//            return false;
//        }else
//        {
//            return true;
//        }
//    }

    public void Delete_data()
    {
        SQLiteDatabase DB=this.getWritableDatabase();
        DB.delete("coordinates",null,null);
    }

    public Cursor Get_Subject_data()
    {
        SQLiteDatabase DB=this.getWritableDatabase();
        String Table_name="coordinates";
        return DB.rawQuery("Select * from  "+Table_name.replaceAll("\\s",""), null);
    }

//    public void Drop_Subject_data()
//    {
//        SQLiteDatabase DB=this.getWritableDatabase();
//        DB.execSQL("drop Table if exists coordinates");
//
//    }
}
