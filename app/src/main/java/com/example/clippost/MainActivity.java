package com.example.clippost;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Bitmap bitmap;
    ImageView imageView;
    Button selectImg,uploadImg;
    EditText imgTitle;
    private  static final int IMAGE = 100;
    String selectedPath;
    private File file;
    ArrayList<String> arrayList = new ArrayList<> ();

    String sta, msg, data;
    private int serverResponseCode;
    String Upload_res="";

    String URL = "http://162.213.190.124:10544/upload.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        imageView = findViewById (R.id.imageView);
        selectImg = findViewById (R.id.selectImg);
        uploadImg = findViewById (R.id.uploadImg);
        imgTitle = findViewById (R.id.imgTitle);

        selectImg.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult (intent, IMAGE);
            }
        });

        uploadImg.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                uploadVideo();
            }
        });
    }

    private void uploadVideo() {
        @SuppressLint("StaticFieldLeak")
        class UploadVideo extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute (s);
                //uploading.dismiss();
                if (s.contains ("Successfully")) {
                    //Upload_res= upload_Media_On_Server();
                    if (!Upload_res.contains ("Success"))
                        Toast.makeText (MainActivity.this, "Failed To Upload File", Toast.LENGTH_LONG).show ();

                } else {
                    Toast.makeText (MainActivity.this, "Failed To Upload File", Toast.LENGTH_LONG).show ();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                //   Upload u = new Upload();
                String str_msg = uploadVideo (selectedPath);

                return str_msg;
            }
        }
        UploadVideo uv = new UploadVideo ();
        uv.execute ();
    }

    public String uploadVideo(String file) {
        int progress = 0;
        this.file = new File(file);

        String fileName = file;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        File sourceFile = new File(file);
        if (!sourceFile.isFile()) {
            Log.e("File", "Source File Does not exist");
            return null;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            java.net.URL url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setChunkedStreamingMode(1024);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("myFile", fileName);
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"myFile\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            Log.e("File Not Found", "Initial: " + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                try {
                    dos.write(buffer, 0, bufferSize);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    return "OutOfMemoryError";
                }
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                progress += bytesRead;
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            serverResponseCode = conn.getResponseCode();

            fileInputStream.close();
            dos.flush();
            dos.close();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (serverResponseCode == 200) {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader (conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
            } catch (IOException ioex) {
                Log.e ("Msg", "-- " +ioex.getMessage());
            }

            try{
                byte [] encodeByte=Base64.decode(sb.toString(),Base64.DEFAULT);
                InputStream inputStream  = new ByteArrayInputStream (encodeByte);
                Bitmap  bitmap_convert  = BitmapFactory.decodeStream(inputStream);

                imageView.setImageBitmap(bitmap_convert);
            }catch(Exception e){
                Log.e ("Bit", "-- " +e.getMessage());
            }
            Log.e("Result", "-- "+sb.toString());

            return sb.toString();
        }else {
            return "Could not upload";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== IMAGE && resultCode==RESULT_OK && data!=null)
        {
            Uri selectedImageUri = data.getData();
            selectedPath = getPathImage(selectedImageUri);

            if (file.exists()){
                uploadVideo();
            }
        }
    }
    public String getPathImage(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();
        this.file = new File(path);
        return path;
    }
}