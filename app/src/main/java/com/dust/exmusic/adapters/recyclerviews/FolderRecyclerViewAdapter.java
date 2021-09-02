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
import com.dust.exmusic.fragments.navigationviewfragments.others.FolderDetailsFragment;
import com.dust.exmusic.interfaces.OnLoadPicture;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FolderRecyclerViewAdapter extends RecyclerView.Adapter<FolderRecyclerViewAdapter.FolderViewHolder> {

    private List<MainDataClass> list;
    private FragmentManager fragmentManager;
    private AnimationSet animationSet;
    private Context context;

    public FolderRecyclerViewAdapter(List<MainDataClass> list, FragmentManager fragmentManager, AnimationSet animationSet) {
        this.list = list;
        this.fragmentManager = fragmentManager;
        this.animationSet = animationSet;
    }

    @NonNull
    @Override
    public FolderRecyclerViewAdapter.FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new FolderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FolderRecyclerViewAdapter.FolderViewHolder holder, int position) {
        holder.itemView.startAnimation(animationSet);
        new MetaDataLoader(context).getLowSizePictureAsync(list.get(position).getPath(), new OnLoadPicture() {
            @Override
            public void onGetPicture(Bitmap bitmap) {
                try {
                    if (bitmap == null)
                        holder.artistImage.setImageResource(R.drawable.empty_music_pic);
                    else
                        holder.artistImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    holder.artistImage.setImageResource(R.drawable.empty_music_pic);
                }
            }
        });
        String RawFolderName = list.get(position).getPath().substring(0, list.get(position).getPath().lastIndexOf("/"));
        if (RawFolderName.contains("/storage/emulated/0/"))
            RawFolderName = RawFolderName.replace("/storage/emulated/0/", "");
        holder.artistName.setText(RawFolderName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(holder.artistName.getWindowToken(), 0);
                fragmentManager.beginTransaction()
                        .add(R.id.drawerLayout, FolderDetailsFragment.newInstance(list.get(position).getPath()))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack("FolderDetailsFragment")
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        CircleImageView artistImage;
        TextView artistName;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            artistImage = (CircleImageView) itemView.findViewById(R.id.artistImage);
            artistName = (TextView) itemView.findViewById(R.id.artistName);
        }
    }
}
