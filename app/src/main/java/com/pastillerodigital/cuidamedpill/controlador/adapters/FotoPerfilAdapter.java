package com.pastillerodigital.cuidamedpill.controlador.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class FotoPerfilAdapter extends BaseAdapter {
    private final Context context;
    private final int[] avatars;

    public FotoPerfilAdapter(Context context, int[] avatars) {
        this.context = context;
        this.avatars = avatars;
    }

    @Override
    public int getCount() { return avatars.length; }

    @Override
    public Object getItem(int position) { return avatars[position]; }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(250, 250));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(avatars[position]);
        return imageView;
    }
}
