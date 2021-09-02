package com.dust.exmusic.adapters.recyclerviews;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataproviders.MetaDataLoader;
import com.dust.exmusic.interfaces.OnLoadPicture;
import com.dust.exmusic.interfaces.OnSongChoose;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChooseSongRecyclerViewAdapter extends RecyclerView.Adapter<ChooseSongRecyclerViewAdapter.ChooseSongViewHolder> {

    private Context context;
    private List<MainDataClass> list;
    private int checkedPosition;
    private OnSongChoose onSongChoose;
    private AnimationSet animationSet;

    public ChooseSongRecyclerViewAdapter(Context context, List<MainDataClass> list, int checkedPosition, OnSongChoose onSongChoose, AnimationSet animationSet) {
        this.context = context;

        this.list = list;
        this.checkedPosition = checkedPosition;
        this.onSongChoose = onSongChoose;
        this.animationSet = animationSet;
    }

    @NonNull
    @Override
    public ChooseSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChooseSongViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choose_song, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseSongViewHolder holder, int position) {
        holder.itemView.startAnimation(animationSet);

        holder.checkBox.setChecked(checkedPosition != -1 && position == checkedPosition);

        new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPath(), new OnLoadPicture() {
            @Override
            public void onGetPicture(Bitmap bitmap) {
                try {
                    if (bitmap == null)
                        holder.itemImage.setImageResource(R.drawable.empty_music_pic);
                    else
                        holder.itemImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    holder.itemImage.setImageResource(R.drawable.empty_music_pic);
                }
            }
        });
        holder.artistName.setText(list.get(position).getArtistName());
        holder.musicName.setText(list.get(position).getMusicName().replace(".mp3", ""));

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    onSongChoose.onSongSelected(list.get(position).getPath());
                else
                    onSongChoose.onSongDeSelected(list.get(position).getPath());
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });

        class OnUnSelectOrder extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    holder.checkBox.setChecked(false);
                } catch (Exception e) {

                }
            }
        }

        context.registerReceiver(new OnUnSelectOrder(), new IntentFilter("com.dust.exmusic.OnUnSelectOrder"));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ChooseSongViewHolder extends RecyclerView.ViewHolder {
        CircleImageView itemImage;
        TextView musicName;
        TextView artistName;
        CheckBox checkBox;

        public ChooseSongViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = (CircleImageView) itemView.findViewById(R.id.musicImage);
            musicName = (TextView) itemView.findViewById(R.id.musicName);
            artistName = (TextView) itemView.findViewById(R.id.artistName);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }
}
