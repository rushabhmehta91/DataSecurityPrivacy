package com.example.sohil.filesharing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by sohil on 4/8/2015.
 */
public class ChooseActivity extends Activity {

    //variables
    private Upload upload = null;
    private Button mUploadBtn;      //button
    private Button mDownloadBtn;    //button
    private View mChoose;   //layout
    private Context context;
    private View dialog = null;
    private int serverResponseCode = 0;
    private String upLoadServerUri = null;
    private static final int PICKFILE_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooseuploaddownload);
        mUploadBtn = (Button) findViewById(R.id.Upload);
        mDownloadBtn  = (Button) findViewById(R.id.Download);
        mChoose = findViewById(R.id.choose);
        dialog = findViewById(R.id.progress);
        context = getApplicationContext();
        //Scale image size for button
        Drawable upldrawable = getResources().getDrawable(R.drawable.upload);
        upldrawable.setBounds(0, 0, (int)(upldrawable.getIntrinsicWidth()*0.5),
                (int)(upldrawable.getIntrinsicHeight()*0.5));
        ScaleDrawable sdupl = new ScaleDrawable(upldrawable, 0, 10, 10);

        mUploadBtn.setCompoundDrawables(sdupl.getDrawable(), null, null, null);

        Drawable dwndrawable = getResources().getDrawable(R.drawable.download);
        dwndrawable.setBounds(0, 0, (int)(dwndrawable.getIntrinsicWidth()*0.5),
                (int)(dwndrawable.getIntrinsicHeight()*0.5));
        ScaleDrawable sddwn = new ScaleDrawable(dwndrawable, 0, 10, 10);

        mDownloadBtn.setCompoundDrawables(sddwn.getDrawable(), null, null, null);

        //On upload button
        mUploadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showProgress(true);
                Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
                fileintent.setType("file/*");
                try {
                    //open file explorer and select file.
                    startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
                } catch (ActivityNotFoundException e) {
                    Log.e("tag", "No activity can handle picking a file. Showing alternatives.");
                }

            }


        });
        mChoose = findViewById(R.id.choose);
        //open download activity
        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DownloadActivity.class);
                startActivity(intent);
            }
        });




    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mChoose.setVisibility(show ? View.GONE : View.VISIBLE);

            dialog.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            dialog.setVisibility(show ? View.VISIBLE : View.GONE);
            mChoose.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    //called when the user has selected file in file explorer view.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            showProgress(false);
            return;
        }
        showProgress(true);
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    Log.e("Data :::: ",data.getData().toString());
                    String filePath = data.getData().getPath();
                    int pos = 0;
                    for(int i=0;i<filePath.length();i++) {
                        Character tmp_filePath = filePath.charAt(i);
                        if(tmp_filePath.equals('/')){
                            pos = i;
                        }
                    }
                    String tmp_filepath = filePath.substring(pos+1);
                    //store the name of the file to use in other activity.
                    SharedPreferences loginDetails = getSharedPreferences("Upload_filename", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = loginDetails.edit();
                    editor.putString("filename",tmp_filepath);
                    editor.apply();
                    upload = new Upload(filePath,context);
                    upload.execute((Void) null);
                }
        }
        showProgress(false);
    }

    protected class Upload extends AsyncTask<Void, Void, Integer> {

        private String filePath;
        private String upLoadServerUri = IpAddress.ipAddress+"upload.php"; //url for the php file
        private Context mcontext;
        Upload(String filePath, Context context){
            this.filePath = filePath;
            mcontext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            new Thread(new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            showProgress(true);
                        }
                    });
                }
            }).start();

            return uploadFile(filePath);
        }
        public int uploadFile(String sourceFileUri) {
            String fileName = sourceFileUri;
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            Log.e("uploadFile", "Source File :"+sourceFileUri);
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(sourceFileUri);
            if (!sourceFile.isFile()) {
                Log.e("uploadFile", "Source File not exist :");
                return 2;
            }
            else
            {   String data = "";
                try {

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(upLoadServerUri);

                    SharedPreferences settings = getSharedPreferences("login_details",
                            Context.MODE_PRIVATE);
                    String username = settings.getString("username", "");

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);
                    conn.setRequestProperty("owner",username);
                    conn.setConnectTimeout(5000);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                    + fileName + "\"" + lineEnd);

                            dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();
                    InputStream is = conn.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    StringBuffer response = new StringBuffer();
                    while((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    rd.close();
                    Log.i("uploadFile", "HTTP Response is : "
                            + response + ": " + serverResponseCode);

                    JSONObject serverMsgObj = new JSONObject(String.valueOf(response));
                    final String serverMsg = serverMsgObj.get("serverReply").toString();
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
//                    Log.e("Over here","I am here");
                    if(serverMsg.equals("success")){
                        Log.e("inside","I got in");
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(mcontext, "Successfully uploaded",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        return 1;

                    }
                    else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mcontext,"Error : "+serverMsg,Toast.LENGTH_LONG).show();
                            }
                        });
                        return 10;
                    }
                } catch (MalformedURLException ex) {
                    showProgress(false);
                    ex.printStackTrace();
                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                    return 3;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return 4;
                } catch(SocketTimeoutException e){
                    e.printStackTrace();
                    return 5;
                } catch (ProtocolException e) {
                    e.printStackTrace();
                    return 6;
                } catch (IOException e) {
                    e.printStackTrace();
                    return 7;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return 8;
                }

            } // End else block

        }
        @Override
        protected void onPostExecute(final Integer success) {
            showProgress(false);
            if(success == 1) {
                Log.e("UserAccess","Here i can reach");
                Intent intent = new Intent(mcontext, UserAccessActivity.class);
                startActivity(intent);
            } else if(success == 2){
                Toast.makeText(mcontext,"file does not exist",Toast.LENGTH_LONG).show();
//                mChoose.invalidate();
            } else if(success == 3){
                Toast.makeText(mcontext,"Malformed URL",Toast.LENGTH_LONG).show();
            } else if(success == 4){
                Toast.makeText(mcontext,"File not found exception",Toast.LENGTH_LONG).show();
            } else if(success == 5){
                Toast.makeText(mcontext,"Server Timeout",Toast.LENGTH_LONG).show();
            } else if(success == 6){
                Toast.makeText(mcontext,"Protocol exception",Toast.LENGTH_LONG).show();
            } else if(success == 7){
                Toast.makeText(mcontext,"IOException",Toast.LENGTH_LONG).show();
            } else if(success == 8){
                Toast.makeText(mcontext,"JSON Error",Toast.LENGTH_LONG).show();
            }

        }
        protected void onCancelled(){
            upload = null;
            showProgress(false);
        }
    }
}
