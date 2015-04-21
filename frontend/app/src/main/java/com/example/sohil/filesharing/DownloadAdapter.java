package com.example.sohil.filesharing;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import static android.view.View.OnClickListener;

/**
 * Created by sohil on 4/13/2015.
 */
public class DownloadAdapter extends BaseAdapter {

    private ArrayList owner;
    private ArrayList fileName;
    private Activity context;
    private Resources res;
    private static LayoutInflater inflater=null;
    public DownloadAdapter(Activity context, ArrayList fileName, ArrayList owner, Resources resources) {
        this.fileName = fileName;
        this.context = context;
        res = resources;
        this.owner = owner;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }



    @Override
    public int getCount() {
        if(fileName.size()<=0)
            return 1;
        return fileName.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public static class ViewHolder{

        public TextView fileNameView;
        public TextView ownerView;


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.row_layout, null);
            holder = new ViewHolder();
            holder.fileNameView = (TextView) convertView.findViewById(R.id.filename);
            holder.ownerView = (TextView) convertView.findViewById(R.id.owner);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }
        if(fileName.size()<=0){
            holder.fileNameView.setText("No data");
        }
        else{
            holder.fileNameView.setText(fileName.get(position).toString());
            holder.ownerView.setText(owner.get(position).toString());

        }
        return convertView;
    }
}
