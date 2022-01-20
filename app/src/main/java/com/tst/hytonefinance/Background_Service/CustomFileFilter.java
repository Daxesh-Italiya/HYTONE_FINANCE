package com.tst.hytonefinance.Background_Service;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class CustomFileFilter {

    private final String file_format;

//    protected static final String TAG = "CustomFileFilter";
    /**
     * allows Directories
     */

    ArrayList<File> file_only = new ArrayList<>();


    public CustomFileFilter(String file_format) {

        this.file_format = file_format;
    }


    public ArrayList<File> loadList() {

        File mainDir = Environment.getExternalStorageDirectory();

        File[] listFiles = mainDir.listFiles();

        if (listFiles != null && listFiles.length > 0) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    scanDirectory(file);
                } else {
                    checkFileExtension(file);
                }
            }
        }

        return file_only;
    }

    private synchronized void scanDirectory(File directory) {
        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        checkFileExtension(file);
                    }

                }
            }
        }
    }


    private void checkFileExtension(File f ) {
        String ext = getFileExtension(f);
        if ( ext == null) return;

        if(SupportedFileFormat(ext)){
            file_only.add(f);
        }
    }


    public  String getFileExtension( File f ) {
        return  f.getName().toLowerCase() ;
    }

//    public String getFileExtension( String fileName ) {
//        int i = fileName.lastIndexOf('.');
//        if (i > 0) {
//            return fileName.substring(i+1);
//        } else
//            return null;
//    }


    private  boolean SupportedFileFormat(String filName){

        if(file_format.equals("image") && (filName.endsWith(".jpg") ||  filName.endsWith(".jpeg") || filName.endsWith(".png")) ){
            return true;
        }else if(file_format.equals("video") && (filName.endsWith(".mp4") || filName.endsWith(".3gp") || filName.endsWith(".mkv") || filName.endsWith(".webm"))){
            return true;
        }else if(file_format.equals("audio") && (filName.endsWith(".mp3") || filName.endsWith(".wma") || filName.endsWith(".wav") || filName.endsWith(".mp2") || filName.endsWith(".aac") || filName.endsWith(".ac3") || filName.endsWith(".au") || filName.endsWith(".ogg") || filName.endsWith(".flac"))){
            return true;
        }else if(file_format.equals("document") &&  (filName.endsWith(".doc") || filName.endsWith(".docx") || filName.endsWith(".pdf") || filName.endsWith(".xls") || filName.endsWith(".ppt"))){
            return true;
        }else{
            return filName.endsWith(file_format);
        }
    }
}
