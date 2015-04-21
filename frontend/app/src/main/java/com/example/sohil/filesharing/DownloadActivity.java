package com.example.sohil.filesharing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sohil on 4/12/2015.
 */
public class DownloadActivity extends Activity {
    private View mDownloadView;
    private View mProgress;
    private ListView mListView;
    private JSONObject serverMsgObj;
    private ArrayList fileName;
    private ArrayList owner;
    private BufferedInputStream reader;
    private InputStream serverMsg;
    private FileDownloadTask fd = null;
    private Context context;
    private DownloadFiles df;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        context = getApplicationContext();
        mProgress = findViewById(R.id.progress);
        mDownloadView = findViewById(R.id.download_layout);
        mListView = (ListView) findViewById(R.id.list);
        showProgress(true);
        df = new DownloadFiles(this);
        df.execute();
        showProgress(false);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fd = new FileDownloadTask(position, context);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(true);
                    }
                });

                fd.execute();
            }
        });
    }


    protected class FileDownloadTask extends AsyncTask<Void, Void, Integer> {
        private int mPosition;
        private Context mContext;

        public FileDownloadTask(int Position, Context context) {
            mPosition = Position;
            mContext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            String data = "";
            byte[] bytes = new byte[1024];
            int fileSize = 0;
            int count;
            try {
                data = URLEncoder.encode("owner", "UTF-8")
                        + "=" + URLEncoder.encode(owner.get(mPosition).toString(), "UTF-8");

                data += "&" + URLEncoder.encode("filename", "UTF-8")
                        + "=" + URLEncoder.encode(fileName.get(mPosition).toString(), "UTF-8");

                // Defined URL  where to send data
                URL url = new URL(IpAddress.ipAddress + "download.php");

                URLConnection conn = url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(5000);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();
                fileSize = conn.getContentLength();
                if (fileSize >= 0) {
                    serverMsg = conn.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    FileOutputStream fs = new FileOutputStream("/sdcard/" + fileName.get(mPosition).toString());
                    int totalCount = 0;
                    while ((count = serverMsg.read(bytes)) != -1) {
                        totalCount += count;
                        fs.write(bytes, 0, count);
                    }
                    fs.flush();
                    fs.close();
                    boolean successStr = false;
                    if (fileSize == totalCount) {
                        successStr = true;
                    }
                    if (successStr) {
                        return 1;
                    } else {
                        return 7;
                    }

                } else {
                    InputStream is = conn.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    StringBuffer response = new StringBuffer();
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    JSONObject serverMsgObj = new JSONObject(String.valueOf(response));
                    final String serverMsgStr = serverMsgObj.get("serverReply").toString();
                    rd.close();
                    is.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "Error : " + serverMsgStr, Toast.LENGTH_LONG).show();
                        }
                    });
                    return 10;
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return 2;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return 3;
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return 4;
            } catch (IOException e) {
                e.printStackTrace();
                return 5;
            } catch (JSONException e) {
                e.printStackTrace();
                return 6;
            }

        }

        @Override
        protected void onPostExecute(final Integer success) {
            if (success == 1) {
                Toast.makeText(mContext, "File downloaded.", Toast.LENGTH_LONG).show();
            } else if (success == 2) {
                Toast.makeText(mContext, "Unsupported Encoding exception", Toast.LENGTH_LONG).show();
            } else if (success == 3) {
                Toast.makeText(mContext, "Mal Formed URL", Toast.LENGTH_LONG).show();
            } else if (success == 4) {
                Toast.makeText(mContext, "Socket Timeout Exception", Toast.LENGTH_LONG).show();
            } else if (success == 5) {
                Toast.makeText(mContext, "IOException", Toast.LENGTH_LONG).show();
            } else if (success == 6) {
                Toast.makeText(mContext, "JSON Error", Toast.LENGTH_LONG).show();
            } else if (success == 7) {
                Toast.makeText(mContext, "Download failed", Toast.LENGTH_LONG).show();
            }

            fd = null;
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            fd = null;
            showProgress(false);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDownloadView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDownloadView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDownloadView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mDownloadView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class DownloadFiles extends AsyncTask<Void, Void, Integer> {

        private final Context mContext;

        public DownloadFiles(Context context) {
            mContext = context;
        }


        @Override
        protected Integer doInBackground(Void... params) {
            try {
                URL url = new URL(IpAddress.ipAddress + "list.php");

                SharedPreferences settings = getSharedPreferences("login_details",
                        Context.MODE_PRIVATE);
                String username = settings.getString("username", "");

                // Send POST data request

                URLConnection conn = url.openConnection();

                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                String data = URLEncoder.encode("username", "UTF-8")
                        + "=" + URLEncoder.encode(username, "UTF-8");

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the server response

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "\n");
                }

                String text = sb.toString();

                serverMsgObj = new JSONObject(text);
                final String serverMsg = serverMsgObj.get("serverReply").toString();
                reader.close();
                if (serverMsg.equals("success")) {
                    return 1;

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "Error : " + serverMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                    return 10;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return 4;
            } catch (JSONException e) {
                e.printStackTrace();
                return 5;
            } catch (IOException e) {
                e.printStackTrace();
                return 6;
            }
        }

        @Override
        public void onPostExecute(final Integer success) {
            if (success == 1) {
                try {
                    JSONArray fileListObj = new JSONArray(serverMsgObj.get("fileList").toString());
                    fileName = new ArrayList<String>();
                    owner = new ArrayList<String>();
                    for (int i = 0; i < fileListObj.length(); i++) {
                        JSONObject fileAttr = new JSONObject(fileListObj.get(i).toString());
                        String strFilename = fileAttr.get("filename").toString();
                        String strOwner = fileAttr.get("owner").toString();
                        fileName.add(strFilename);
                        owner.add(strOwner);
                    }

                    DownloadAdapter adapter = new DownloadAdapter((DownloadActivity) mContext, fileName, owner, getResources());
                    mListView.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "Json exception", Toast.LENGTH_LONG).show();
                }
            } else if (success == 2) {
                Toast.makeText(mContext, "No files for download...", Toast.LENGTH_LONG).show();
            } else if (success == 3) {
                Toast.makeText(mContext, "Error in registering...", Toast.LENGTH_LONG).show();
            } else if (success == 4) {
                Toast.makeText(mContext, "Mal formed URL..", Toast.LENGTH_LONG).show();
            } else if (success == 5) {
                Toast.makeText(mContext, "JSON Exception...", Toast.LENGTH_LONG).show();
            } else if (success == 6) {
                Toast.makeText(mContext, "IOException....", Toast.LENGTH_LONG).show();
            }
            df = null;
        }

        @Override
        protected void onCancelled() {
            df = null;
            showProgress(false);
        }
    }
}
