package com.dust.exmusic.adapters.recyclerviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataclasses.PlayListDataClass;
import com.dust.exmusic.dataproviders.MetaDataLoader;
import com.dust.exmusic.interfaces.OnLoadPicture;
import com.dust.exmusic.interfaces.OnSongChoose;

import java.util.List;

public class ChoosePlayListRecyclerViewAdapter extends RecyclerView.Adapter<ChoosePlayListRecyclerViewAdapter.PlayListViewHolder> {

    private Context context;
    private List<PlayListDataClass> list;
    private OnSongChoose onSongChoose;
    private AnimationSet animationSet;

    public ChoosePlayListRecyclerViewAdapter(Context context, List<PlayListDataClass> list, OnSongChoose onSongChoose, AnimationSet animationSet) {
        this.context = context;

        this.list = list;
        this.onSongChoose = onSongChoose;
        this.animationSet = animationSet;
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_playlist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {
        holder.itemView.startAnimation(animationSet);
        holder.playListName.setText(list.get(position).getPlayListName());
        try {
            new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPaths().get(0), new OnLoadPicture() {
                @Override
                public void onGetPicture(Bitmap bitmap) {
                    holder.img1.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
        }
        try {
            new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPaths().get(1), new OnLoadPicture() {
                @Override
                public void onGetPicture(Bitmap bitmap) {
                    holder.img2.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
        }
        try {
            new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPaths().get(2), new OnLoadPicture() {
                @Override
                public void onGetPicture(Bitmap bitmap) {
                    holder.img3.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
        }
        try {
            new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPaths().get(3), new OnLoadPicture() {
                @Override
                public void onGetPicture(Bitmap bitmap) {
                    holder.img4.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.checkbox.setChecked(!holder.checkbox.isChecked());
            }
        });

        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    onSongChoose.onSongSelected(list.get(position).getPlayListName());
                else
                    onSongChoose.onSongDeSelected(list.get(position).getPlayListName());
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class PlayListViewHolder extends RecyclerView.ViewHolder {
        ImageView img1;
        ImageView img2;
        ImageView img3;
        ImageView img4;
        CTextView playListName;
        CheckBox checkbox;

        public PlayListViewHolder(@NonNull View itemView) {
            super(itemView);
            img1 = (ImageView) itemView.findViewById(R.id.img1);
            img2 = (ImageView) itemView.findViewById(R.id.img2);
            img3 = (ImageView) itemView.findViewById(R.id.img3);
            img4 = (ImageView) itemView.findViewById(R.id.img4);
            playListName = (CTextView) itemView.findViewById(R.id.playListName);
            checkbox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }
}
