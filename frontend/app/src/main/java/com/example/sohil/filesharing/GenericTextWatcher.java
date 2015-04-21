package com.example.sohil.filesharing;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * Created by sohil on 4/15/2015.
 */
public class GenericTextWatcher implements TextWatcher {

    private EditText view;

    protected GenericTextWatcher(View view){
        this.view = (EditText) view;
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = view.getText().toString();
        switch(view.getId()){
            case R.id.register_name:
//                Log.e("STRING NAME", text);
                if(text.length() < 1){
                    view.setBackgroundColor(Color.parseColor("#33FF6666"));
                    view.setError("Too Short");
                } else{
                    view.setError(null);
                    view.setBackgroundColor(Color.parseColor("#3311FF33"));
                }
                break;
            case R.id.register_email:
//                Log.e("STRING EMAIL", text);
                if(text.matches(".*?@.*?\\..+")){
                    view.setError(null);
                    view.setBackgroundColor(Color.parseColor("#3311FF33"));

                } else{
                    view.setBackgroundColor(Color.parseColor("#33FF6666"));
                    view.setError("Incorrect id");
                }
                break;
            case R.id.register_password:
                if(text.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{4,16}$")){
                    view.setError(null);
                    view.setBackgroundColor(Color.parseColor("#3311FF33"));
                }else{
                    view.setBackgroundColor(Color.parseColor("#33FF6666"));
                    view.setError("Incorrect password");
                }
                break;
        }
    }
}
