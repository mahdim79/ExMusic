package com.dust.exmusic.adapters.recyclerviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.customviews.CTextView;
import com.dust.exmusic.dataclasses.PlayListDataClass;
import com.dust.exmusic.dataproviders.MetaDataLoader;
import com.dust.exmusic.fragments.navigationviewfragments.others.SinglePlayListFragment;
import com.dust.exmusic.interfaces.OnLoadPicture;

import java.util.List;

public class PlayListRecyclerViewAdapter extends RecyclerView.Adapter<PlayListRecyclerViewAdapter.PlayListViewHolder> {

    private List<PlayListDataClass> list;
    private FragmentManager fragmentManager;
    private AnimationSet animationSet;
    private Context context;

    public PlayListRecyclerViewAdapter(List<PlayListDataClass> list, FragmentManager fragmentManager, AnimationSet animationSet) {

        this.list = list;
        this.fragmentManager = fragmentManager;
        this.animationSet = animationSet;
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new PlayListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {
        holder.itemView.startAnimation(animationSet);
        holder.playListName.setText(list.get(position).getPlayListName());
        try {
            new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPaths().get(0), new OnLoadPicture() {
                @Override
                public void onGetPicture(Bitmap bitmap) {
                    if (bitmap == null)
                        holder.img1.setImageResource(R.drawable.empty_music_pic);
                    else
                        holder.img1.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
        }
        try {
            new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPaths().get(1), new OnLoadPicture() {
                @Override
                public void onGetPicture(Bitmap bitmap) {
                    if (bitmap == null)
                        holder.img2.setImageResource(R.drawable.empty_music_pic);
                    else
                        holder.img2.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
        }
        try {
            new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPaths().get(2), new OnLoadPicture() {
                @Override
                public void onGetPicture(Bitmap bitmap) {
                    if (bitmap == null)
                        holder.img3.setImageResource(R.drawable.empty_music_pic);
                    else
                        holder.img3.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
        }
        try {
            new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPaths().get(3), new OnLoadPicture() {
                @Override
                public void onGetPicture(Bitmap bitmap) {
                    if (bitmap == null)
                        holder.img4.setImageResource(R.drawable.empty_music_pic);
                    else
                        holder.img4.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.beginTransaction()
                        .replace(R.id.drawerLayout, SinglePlayListFragment.newInstance(list.get(position).getPlayListName()))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("SinglePlayListFragment")
                        .commit();
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

        public PlayListViewHolder(@NonNull View itemView) {
            super(itemView);
            img1 = (ImageView) itemView.findViewById(R.id.img1);
            img2 = (ImageView) itemView.findViewById(R.id.img2);
            img3 = (ImageView) itemView.findViewById(R.id.img3);
            img4 = (ImageView) itemView.findViewById(R.id.img4);
            playListName = (CTextView) itemView.findViewById(R.id.playListName);
        }
    }
}
