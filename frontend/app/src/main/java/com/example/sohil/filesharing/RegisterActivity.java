package com.example.sohil.filesharing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

/**
 * Created by sohil on 4/11/2015.
 */


public class RegisterActivity extends Activity {
    private View mRegisterForm;
    private EditText mRegisterEmail;
    private EditText mRegisterPassword;
    private EditText mRegisterName;
    private View mProgressView;
    private View mRegisterBtn;
    private RegisterTask mRegister = null;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = getApplicationContext();
        mRegisterEmail = (EditText) findViewById(R.id.register_email);
        mRegisterForm = findViewById(R.id.register_form);
        mRegisterPassword = (EditText) findViewById(R.id.register_password);
        mRegisterName = (EditText) findViewById(R.id.register_name);
        mProgressView = findViewById(R.id.register_progress);
        mRegisterBtn = findViewById(R.id.register_btn);
        mRegisterName.addTextChangedListener(new GenericTextWatcher(mRegisterName));
        mRegisterEmail.addTextChangedListener(new GenericTextWatcher(mRegisterEmail));
        mRegisterPassword.addTextChangedListener(new GenericTextWatcher(mRegisterPassword));
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });


    }

    private void attemptLogin() {
        if (mRegister != null) {
            return;
        }

        String email = mRegisterEmail.getText().toString();
        String password = mRegisterPassword.getText().toString();
        String name = mRegisterName.getText().toString();

        boolean cancel = false;
        View focusView = null;



        if (name.length() < 1 || !(mRegisterName.getError() == null)) {
            mRegisterName.setError("Enter Name");
            focusView = mRegisterName;
            cancel = true;
        }else if(email.length() < 1 || !(mRegisterEmail.getError() == null)) {
            mRegisterEmail.setError("Enter Email");
            focusView = mRegisterEmail;
            cancel = true;
        }  else if (password.length() < 1 || !(mRegisterPassword.getError() == null)) {
            mRegisterPassword.setError("Enter Password");
            focusView = mRegisterPassword;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form fie
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            showProgress(true);
            mRegister = new RegisterTask(email, password, name, context);
            mRegister.execute((Void) null);
            mRegister = null;
        }


    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class RegisterTask extends AsyncTask<Void, Void, Integer> {
        private String mEmail;
        private String mPassword;
        private String mName;
        private Context mContext;
        private String text = "";

        public RegisterTask(String email, String password, String name, Context context) {
            mEmail = email;
            mPassword = password;
            mName = name;
            mContext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            String data = "";
            try {
                data = URLEncoder.encode("name", "UTF-8")
                        + "=" + URLEncoder.encode(mName, "UTF-8");

                data += "&" + URLEncoder.encode("username", "UTF-8") + "="
                        + URLEncoder.encode(mEmail, "UTF-8");

                data += "&" + URLEncoder.encode("password", "UTF-8")
                        + "=" + URLEncoder.encode(mPassword, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return 3;
            }

            BufferedReader reader = null;

            try {

                // Defined URL  where to send data
                URL url = new URL(IpAddress.ipAddress+"register.php");

                // Send POST data request

                URLConnection conn = url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(5000);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "\n");
                }


                text = sb.toString();

                JSONObject serverMsgObj = new JSONObject(text);
                String serverMsg = serverMsgObj.get("serverReply").toString();
                reader.close();
                wr.close();
                if (serverMsg.equals("success")) {
                    return 1;

                } else {
                    return 2;
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
                return 4;
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return 5;
            } catch (IOException ex) {
                ex.printStackTrace();
                return 6;
            } catch (JSONException e) {
                e.printStackTrace();
                return 7;
            }
        }

        @Override
        protected void onPostExecute(final Integer success) {
            mRegister = null;
            showProgress(false);

            if (success == 1) {
                finish();
            } else if (success == 2) {
                Toast.makeText(mContext, "ERROR in registering : " + text, Toast.LENGTH_LONG).show();
            } else if (success == 3) {
                Toast.makeText(mContext, "ERROR in Encoding ", Toast.LENGTH_LONG).show();
            } else if (success == 4) {
                Toast.makeText(mContext, "Malformed URL exception", Toast.LENGTH_LONG).show();
            } else if (success == 5) {
                Toast.makeText(mContext, "TimeOut", Toast.LENGTH_LONG).show();
            } else if (success == 6) {
                Toast.makeText(mContext, "IO Error", Toast.LENGTH_LONG).show();
            } else if (success == 7) {
                Toast.makeText(mContext, "JSON error", Toast.LENGTH_LONG).show();
            }

        }

        protected void onCancelled() {
            mRegister = null;
            showProgress(false);
        }
    }
}
