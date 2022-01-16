package com.tst.hytonefinance.Background_Service;


import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class FileUploader {


    private Context context;
    private String user;
    private fileUploadListener listener;
    private String file_type = "file_upload";

    private String TAG = "FileUploader";
    private String Base_Url="http://backend.getbridge.in";



    public FileUploader(Context context,String user,String file_type,fileUploadListener listener) {
        this.context = context;
        this.user = user;
        this.file_type = file_type;
        this.listener = listener;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public void UploadFile(final File file, final int pos) {

        final Uri file_data = Uri.fromFile(file);

        InputStream iStream = null;
        try {

            iStream = context.getContentResolver().openInputStream(file_data);
            final byte[] inputData = getBytes(iStream);


            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, Base_Url + "/api/v1/user/userMedia",
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            try {
                                JSONObject obj = new JSONObject(new String(response.data));
                                //Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();

                                //upload next file
                                listener.fileUploadComplete(pos, true);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                            listener.fileUploadComplete(pos, false);
                        }
                    }) {

                /*
                 * If you want to add more parameters with the image
                 * you can do it here
                 * here we have only one parameter with the image
                 * which is tags
                 * */
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("user", user);
                    return params;
                }

                /*
                 * Here we are passing image by renaming it with a unique name
                 * */
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    String fileName = file.getName();//System.currentTimeMillis();
                    params.put("medias", new DataPart(fileName, inputData,getMimeType(file_data)));
                    return params;
                }
            };

            //adding the request to volley
            Volley.newRequestQueue(context).add(volleyMultipartRequest);


            //start file uploading here
            Log.w(TAG, "file upload success file - " + file_data.getLastPathSegment());

        }catch (Exception e){
            e.printStackTrace();
        }


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

        byte[] fileToBytes(File file){
            byte[] bytes = new byte[0];
            try(FileInputStream inputStream = new FileInputStream(file)) {
                bytes = new byte[inputStream.available()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bytes;
        }



}
