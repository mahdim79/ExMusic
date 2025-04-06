package com.dust.exmusic.adapters.recyclerviews;

import static android.content.Context.RECEIVER_EXPORTED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.activities.PlayerActivity;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataproviders.MetaDataLoader;
import com.dust.exmusic.fragments.others.MusicDetailsBottomDialog;
import com.dust.exmusic.interfaces.OnLoadPicture;
import com.dust.exmusic.sharedpreferences.SharedPreferencesCenter;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllMusicsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MainDataClass> list;
    private Context context;
    private FragmentManager fragmentManager;
    private AnimationSet set;
    private SharedPreferencesCenter sharedPreferencesCenter;
    private String playListName;

    public AllMusicsRecyclerViewAdapter(List<MainDataClass> list, Context context, FragmentManager fragmentManager, AnimationSet set, SharedPreferencesCenter sharedPreferencesCenter, String playListName) {
        this.list = list;
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.set = set;
        this.sharedPreferencesCenter = sharedPreferencesCenter;
        this.playListName = playListName;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MainViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MainViewHolder mholder = (MainViewHolder) holder;
        new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPath(), new OnLoadPicture() {
            @Override
            public void onGetPicture(Bitmap bitmap) {
                try {
                    if (bitmap == null)
                        mholder.itemImage.setImageResource(R.drawable.empty_music_pic);
                    else
                        mholder.itemImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    mholder.itemImage.setImageResource(R.drawable.empty_music_pic);
                }
            }
        });

        mholder.artistName.setText(list.get(position).getArtistName());
        mholder.musicName.setText(list.get(position).getMusicName().replace(".mp3", ""));
        if (list.get(position).getPlaying())
            mholder.isPlayingLinear.setVisibility(View.VISIBLE);

        mholder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("PATH", list.get(position).getPath());
                intent.putExtra("PLAY_LIST", playListName);
                context.startActivity(intent);
            }
        });

        if (!playListName.equals("null")) {
            mholder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent("com.dust.exmusic.OnItemLongClick");
                    intent.putExtra("POSITION", position);
                    context.sendBroadcast(intent);
                    return true;
                }
            });
        }

        mholder.moreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicDetailsBottomDialog dialog = new MusicDetailsBottomDialog(list.get(position).getPath());
                dialog.show(fragmentManager, "null");
            }
        });

        boolean favAvailability = sharedPreferencesCenter.checkFavoriteListPathAvailability(list.get(position).getPath());
        if (favAvailability) {
            mholder.likedIcon.setImageResource(R.drawable.gradient_added_favlist);
        }
        mholder.likedIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedPreferencesCenter.checkFavoriteListPathAvailability(list.get(position).getPath())) {
                    sharedPreferencesCenter.removeFavoriteList(list.get(position).getPath());
                    mholder.likedIcon.setImageResource(R.drawable.gradient_add_to_favlist);
                } else {
                    sharedPreferencesCenter.addToFavoriteListPaths(list.get(position).getPath());
                    mholder.likedIcon.setImageResource(R.drawable.gradient_added_favlist);
                }
                context.sendBroadcast(new Intent("com.dust.exmusic.OnFavoriteListChanged"));
            }
        });

        mholder.itemView.startAnimation(set);

        class OnReceivePath extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (intent.getExtras().getString("PATH").equals(list.get(position).getPath()) && intent.getExtras().getBoolean("IS_PLAYING"))
                        mholder.isPlayingLinear.setVisibility(View.VISIBLE);
                    else
                        mholder.isPlayingLinear.setVisibility(View.GONE);
                } catch (Exception e) {
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.registerReceiver(new OnReceivePath(), new IntentFilter("com.dust.exmusic.OnReceivePath"),RECEIVER_EXPORTED);
        else
            context.registerReceiver(new OnReceivePath(), new IntentFilter("com.dust.exmusic.OnReceivePath"));


        class OnFavoriteListChanged extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (sharedPreferencesCenter.checkFavoriteListPathAvailability(list.get(position).getPath())) {
                        mholder.likedIcon.setImageResource(R.drawable.gradient_added_favlist);
                    } else {
                        mholder.likedIcon.setImageResource(R.drawable.gradient_add_to_favlist);
                    }
                } catch (Exception e) {
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.registerReceiver(new OnFavoriteListChanged(), new IntentFilter("com.dust.exmusic.OnFavoriteListChanged"),RECEIVER_EXPORTED);
        else
            context.registerReceiver(new OnFavoriteListChanged(), new IntentFilter("com.dust.exmusic.OnFavoriteListChanged"));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MainViewHolder extends RecyclerView.ViewHolder {

        CircleImageView itemImage;
        TextView musicName;
        TextView artistName;
        LinearLayout isPlayingLinear;
        CircleImageView moreIcon;
        CircleImageView likedIcon;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = (CircleImageView) itemView.findViewById(R.id.musicImage);
            musicName = (TextView) itemView.findViewById(R.id.musicName);
            artistName = (TextView) itemView.findViewById(R.id.artistName);
            isPlayingLinear = (LinearLayout) itemView.findViewById(R.id.isPlayingLinear);
            moreIcon = (CircleImageView) itemView.findViewById(R.id.moreIcon);
            likedIcon = (CircleImageView) itemView.findViewById(R.id.likedIcon);
        }
    }
}
