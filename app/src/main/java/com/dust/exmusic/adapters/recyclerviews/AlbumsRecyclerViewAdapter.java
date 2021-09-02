package com.dust.exmusic.adapters.recyclerviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.dust.exmusic.R;
import com.dust.exmusic.dataclasses.MainDataClass;
import com.dust.exmusic.dataproviders.MetaDataLoader;
import com.dust.exmusic.fragments.navigationviewfragments.others.AlbumsDetailsFragment;
import com.dust.exmusic.interfaces.OnLoadPicture;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AlbumsRecyclerViewAdapter extends RecyclerView.Adapter<AlbumsRecyclerViewAdapter.AlbumsViewHolder> {

    private List<MainDataClass> list;
    private FragmentManager fragmentManager;
    private AnimationSet animationSet;
    private Context context;

    public AlbumsRecyclerViewAdapter(List<MainDataClass> list, FragmentManager fragmentManager, AnimationSet animationSet) {
        this.list = list;
        this.fragmentManager = fragmentManager;
        this.animationSet = animationSet;
    }

    @NonNull
    @Override
    public AlbumsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new AlbumsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumsViewHolder holder, int position) {
        holder.itemView.startAnimation(animationSet);
        new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPath(), new OnLoadPicture() {
            @Override
            public void onGetPicture(Bitmap bitmap) {
                try {
                    if (bitmap == null)
                        holder.albumImage.setImageResource(R.drawable.empty_music_pic);
                    else
                        holder.albumImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    holder.albumImage.setImageResource(R.drawable.empty_music_pic);
                }
            }
        });
        holder.albumName.setText(list.get(position).getAlbum());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(holder.albumName.getWindowToken(), 0);
                fragmentManager.beginTransaction()
                        .add(R.id.drawerLayout, AlbumsDetailsFragment.newInstance(list.get(position).getAlbum()))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("AlbumsDetailsFragment")
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class AlbumsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView albumImage;
        TextView albumName;

        public AlbumsViewHolder(@NonNull View itemView) {
            super(itemView);
            albumImage = (CircleImageView) itemView.findViewById(R.id.artistImage);
            albumName = (TextView) itemView.findViewById(R.id.artistName);
        }
    }
}
