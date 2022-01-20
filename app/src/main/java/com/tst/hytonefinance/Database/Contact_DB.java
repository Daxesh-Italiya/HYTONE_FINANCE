package com.tst.hytonefinance.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Contact_DB extends SQLiteOpenHelper {

    public Contact_DB(@Nullable Context context) {
        super(context, "ContactDB", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table ContactDB(Name TEXT,Number TEXT, Location TEXT, Email TEXT, Company TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
        DB.execSQL("drop Table if exists ContactDB");
    }

    public Boolean Insert_Subject_data(String Name,String Number,String location ,String email,String company)
    {
        SQLiteDatabase DB=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("Name",Name);
        contentValues.put("Number",Number);
        contentValues.put("Location",location);
        contentValues.put("Email",email);
        contentValues.put("Company",company);

        String Table_name="ContactDB";
        long result =DB.insert(Table_name,null,contentValues);
        if(result==-1)
        {
            return false;
        }else
        {
            return true;
        }
    }

    public Boolean Delete_data()
    {


        SQLiteDatabase DB=this.getWritableDatabase();
        long result =DB.delete("ContactDB",null,null);
        if(result==-1)
        {
            return false;
        }else
        {
            return true;
        }

    }

    public Cursor Get_Subject_data()
    {
        SQLiteDatabase DB=this.getWritableDatabase();
        String Table_name="ContactDB";
        Cursor cursor=DB.rawQuery("Select * from  "+Table_name.replaceAll("\\s",""), null);
        return cursor;

    }

    public void Drop_Subject_data()
    {
        SQLiteDatabase DB=this.getWritableDatabase();
        DB.execSQL("drop Table if exists coordinates");

    }
}
