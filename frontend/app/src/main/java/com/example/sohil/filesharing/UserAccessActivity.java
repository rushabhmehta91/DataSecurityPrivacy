package com.example.sohil.filesharing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
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
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sohil on 4/15/2015.
 */
public class UserAccessActivity extends Activity {

    private ArrayList<String> userList;
    private GetUserList FetchUserList = null;
    private JSONObject serverMsgObj;
    private View mUserAccess;
    private MyCustomAdapter dataAdapter = null;
    private Context context;
    private View mProgressView;
    private HashMap<String, Boolean> selectedUsers;
    private String mEmail = "";
    private View mButton;
    private String fileName = "";
    private SendDataUsers datasend = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_access);
        SharedPreferences uploadDetails = getSharedPreferences("Upload_filename", Context.MODE_PRIVATE);
        fileName = uploadDetails.getString("filename", "");
        mProgressView = findViewById(R.id.user_access_progress);
        mUserAccess = findViewById(R.id.parentView);
        mButton = findViewById(R.id.add_user_btn);
        selectedUsers = new HashMap<String, Boolean>();
        context = getApplicationContext();
        populateUserList();
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (datasend != null) {
                    return;
                }
                datasend = (SendDataUsers) new SendDataUsers(context).execute((Void) null);
            }

        });
    }

    class SendDataUsers extends AsyncTask<Void, Void, Integer> {
        private Context mContext;

        public SendDataUsers(Context context) {
            mContext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgress(true);
                }
            });
            String data = "";
            try {
                data = URLEncoder.encode("owner", "UTF-8")
                        + "=" + URLEncoder.encode(mEmail, "UTF-8");
                data += "&" + URLEncoder.encode("filename", "UTF-8")
                        + "=" + URLEncoder.encode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (selectedUsers.size() > 0) {
                int count = 0;
                Iterator it = selectedUsers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if ((Boolean) pair.getValue() == true) {
                        try {
                            count++;
                            data += "&" + URLEncoder.encode("user" + count, "UTF-8")
                                    + "=" + URLEncoder.encode(pair.getKey().toString(), "UTF-8");

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    data += "&" + URLEncoder.encode("count", "UTF-8")
                            + "=" + URLEncoder.encode(String.valueOf(count), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                try {
                    URL url = new URL(IpAddress.ipAddress+"addUsers.php");
                    URLConnection conn = url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setConnectTimeout(5000);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(data);
                    wr.flush();
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
                    String serverMsg = serverMsgObj.get("serverReply").toString();
                    reader.close();
                    return 1;
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
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "No users have been selected...", Toast.LENGTH_LONG).show();
                    }
                });
                return 2;
            }
        }

        @Override
        protected void onPostExecute(final Integer success) {
            if (success == 1) {
                Toast.makeText(mContext, "Users added", Toast.LENGTH_LONG).show();
                finish();
            } else if (success == 2) {
                Toast.makeText(mContext, "No users selected", Toast.LENGTH_LONG).show();
                finish();
            } else if (success == 3) {
                Toast.makeText(mContext, "Malformed URL", Toast.LENGTH_LONG).show();
            } else if (success == 4) {
                Toast.makeText(mContext, "Socket timeout", Toast.LENGTH_LONG).show();
            } else if (success == 5) {
                Toast.makeText(mContext, "IOException", Toast.LENGTH_LONG).show();
            } else if (success == 6) {
                Toast.makeText(mContext, "JSON exception", Toast.LENGTH_LONG).show();
            }
            datasend = null;
            showProgress(false);

        }

        @Override
        protected void onCancelled() {
            datasend = null;
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

            mUserAccess.setVisibility(show ? View.GONE : View.VISIBLE);
            mUserAccess.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mUserAccess.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mUserAccess.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void populateUserList() {

        if (FetchUserList != null) {
            return;
        }
        showProgress(true);
        FetchUserList = (GetUserList) new GetUserList(getApplicationContext()).execute();
    }


    class GetUserList extends AsyncTask<Void, Void, Integer> {
        private String text = "";
        private Context mContext;

        public GetUserList(Context applicationContext) {
            mContext = applicationContext;
        }

        @Override
        protected Integer doInBackground(Void... params) {

            BufferedReader reader = null;

            try {
                // Defined URL  where to send data
                URL url = new URL(IpAddress.ipAddress+"getUsers.php");
                SharedPreferences settings = getSharedPreferences("login_details",
                        Context.MODE_PRIVATE);
                mEmail = settings.getString("username", "");

                URLConnection conn = url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(5000);
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "\n");
                }

                text = sb.toString();
                serverMsgObj = new JSONObject(text);
                String serverMsg = serverMsgObj.get("serverReply").toString();
                reader.close();
                if (serverMsg.equals("success")) {
                    return 1;
                }
                if (serverMsg.equals("No users available")) {
                    return 2;
                } else {
                    return 3;
                }
            } catch (MalformedURLException e) {
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
                try {
                    showProgress(false);
                    JSONArray fileListObj = new JSONArray(serverMsgObj.get("userList").toString());
                    userList = new ArrayList<String>();
                    for (int i = 0; i < fileListObj.length(); i++) {
                        JSONObject fileAttr = new JSONObject(fileListObj.get(i).toString());
                        String strUsrname = fileAttr.get("username").toString();
                        if (!(strUsrname.equals(mEmail))) {
                            userList.add(strUsrname);
                        }
                    }
                    dataAdapter = new MyCustomAdapter(context, R.layout.user_access, userList);
                    ListView lv = (ListView) findViewById(R.id.username_list);
                    lv.setAdapter(dataAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "Error accessing userlist in Json", Toast.LENGTH_LONG).show();
                }

            } else if (success == 2) {
                userList = new ArrayList<String>();
                showProgress(false);
            } else if (success == 3) {
                Toast.makeText(mContext, "Error in registering...", Toast.LENGTH_LONG).show();
                showProgress(false);
            } else if (success == 4) {
                Toast.makeText(mContext, "Malformed URL...", Toast.LENGTH_LONG).show();
                showProgress(false);
            } else if (success == 5) {
                Toast.makeText(mContext, "IOException... ", Toast.LENGTH_LONG).show();
                showProgress(false);
            } else if (success == 6) {
                Toast.makeText(mContext, "JSON Format Exception...", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
            FetchUserList = null;
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            FetchUserList = null;
            showProgress(false);
        }

    }

    private class MyCustomAdapter extends ArrayAdapter<String> {

        private ArrayList<String> userList;

        public MyCustomAdapter(Context context, int user_access, ArrayList<String> userList) {
            super(context, user_access, userList);
            this.userList = new ArrayList<String>();
            this.userList = userList;
        }

        private class ViewHolder {
            TextView usrNameView;
            CheckBox check;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.user_access, null);

                holder = new ViewHolder();
                holder.usrNameView = (TextView) convertView.findViewById(R.id.code);
                holder.check = (CheckBox) convertView.findViewById(R.id.select_user);
                convertView.setTag(holder);

                holder.check.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        Toast.makeText(getApplicationContext(),
                                "Clicked on Checkbox: " + cb.getText() +
                                        " is " + cb.isChecked(),
                                Toast.LENGTH_LONG).show();
                        selectedUsers.put(userList.get(position), cb.isChecked());
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.usrNameView.setText(userList.get(position));

            return convertView;

        }
    }
}


