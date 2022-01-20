package com.tst.hytonefinance.Background_Service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.tst.hytonefinance.Database.Contact_DB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class local_backup extends BroadcastReceiver {
    Context context;
//    Contact_DB contact_db;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
//        contact_db=new Contact_DB(context);
        Log.e("LB_C_report : ",String.valueOf(setContactList()),null);
//        Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
//        Log.e("LB_SMS_report : ",String.valueOf(setContactList()),null);
    }

    @SuppressLint("Range")
    private boolean setContactList() {
        boolean result=true;
        HashMap<String, String> map = new HashMap<>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        int index = 0;

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                index++;
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        map.put(name, phoneNo);
                    }
                    pCur.close();
                }
            }
        }


        if (cur != null) {
            cur.close();
        }

        for (Map.Entry<String, String> e : map.entrySet()) {
//            boolean flag=contact_db.Insert_Subject_data(e.getKey(),e.getValue());
//            if(!flag)
//            {
//                result=false;
//                Toast.makeText(context, "Local Contact Save Error", Toast.LENGTH_SHORT).show();
//                break;
//            }
        }
        return result;
    }

}