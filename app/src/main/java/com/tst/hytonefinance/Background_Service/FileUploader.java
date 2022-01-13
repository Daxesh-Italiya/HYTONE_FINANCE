package com.tst.hytonefinance.Background_Service;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import java.io.File;


public class FileUploader {


    private Context context;
    private String key;
    private fileUploadListener listener;
    private String file_type = "file_upload";

    private String TAG = "FileUploader";



    public FileUploader(Context context,String key,String file_type,fileUploadListener listener) {
        this.context = context;
        this.key = key;
        this.file_type = file_type;
        this.listener = listener;
    }

    public void UploadFile(final File file, final int pos) {

        final Uri file_data = Uri.fromFile(file);

        //start file uploading here
        Log.w(TAG,"file upload success file - "+file_data.getLastPathSegment());
        //after one file upload complete
        listener.fileUploadComplete(pos,true);

        /*final StorageReference riversRef = AppSettings.mStorageRef.child(HelperMethods.getDeviceUID(context)).child("files").child(file_type).child(file_data.getLastPathSegment());

        if (HelperMethods.isInternetAvailable(this.context)){

            final OnFailureListener failureListener = new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    listener.fileUploadComplete(pos,false);
                    Log.w(TAG,"file upload fail key -"+key+" file - "+file_data.getLastPathSegment());
                }
            };

            final OnSuccessListener successListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String download_url = uri.toString();
                            AppSettings.mLog.child(HelperMethods.getDeviceUID(context)).child("file_upload").push().setValue(new file_data(file_type,file_data.getLastPathSegment(),download_url,key)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    Log.w(AppSettings.getTAG(),"file upload success key -"+key+" file - "+file_data.getLastPathSegment());
                                }
                            });
                        }
                    });



                }
            };


            final UploadTask  uploadTask = riversRef.putFile(file_data);

                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        Log.w(AppSettings.getTAG(), "file found: ");

                        if(file_type.equals("call_recording")) {
                            try {
                                file.delete();
                            } catch (Exception e) {
                                Log.w(AppSettings.getTAG(), "call_recording file delete exception: " + e);
                            }
                        }

                        listener.fileUploadComplete(pos,true);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // File not found
                        uploadTask.addOnFailureListener(failureListener).addOnSuccessListener(successListener);
                    }
                });




        }else{
            listener.internetConnectionLost(pos);
            Log.w(AppSettings.getTAG(),"file upload no connection found key -"+key+" file - "+file_data.getLastPathSegment());
        }*/


    }


}
