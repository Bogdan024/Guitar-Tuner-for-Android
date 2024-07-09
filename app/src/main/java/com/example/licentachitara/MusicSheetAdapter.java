package com.example.licentachitara;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MusicSheetAdapter extends RecyclerView.Adapter<MusicSheetAdapter.MusicSheetViewHolder> {
    private List<MusicSheet> musicSheetsList;
    private Context forumContext;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public MusicSheetAdapter(Context context, List<MusicSheet> musicSheetsList) {
        this.musicSheetsList = musicSheetsList;
        this.forumContext = context;
    }

    @NonNull
    @Override
    public MusicSheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_sheet_item, parent, false);
        return new MusicSheetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicSheetViewHolder holder, int position) {
        MusicSheet musicSheet = musicSheetsList.get(position);
        holder.titleTv.setText(musicSheet.getTitle());
        holder.sheetDescriptionTv.setText(musicSheet.getSheetDescription());
        Date postDate = musicSheet.getTimestamp();
        if (postDate != null)
        {
            holder.timeTv.setText(sdf.format(postDate));
        }
        else {
            holder.timeTv.setText("No date available");
        }
        holder.usernameTv.setText(musicSheet.getUsername());

        // Load the image using Glided
        //fsafsa //
        ///fasfsafsaf//
        //fsafsagasf

        if (musicSheet.getSheetMusicUrl() != null && !musicSheet.getSheetMusicUrl().isEmpty()) {
            Glide.with(forumContext)
                    .load(musicSheet.getSheetMusicUrl())
                    .placeholder(R.drawable.img_placeholder_svg) // Replace with a placeholder image resource
                    .into(holder.sheetMusicIv);
        } else {
            holder.sheetMusicIv.setImageResource(R.drawable.feather_icon_2); // Replace with a placeholder image resource
        }
    }

    @Override
    public int getItemCount() {
        return musicSheetsList.size();
    }

    public static class MusicSheetViewHolder extends RecyclerView.ViewHolder {
        TextView titleTv;
        TextView timeTv;
        TextView usernameTv;
        TextView sheetDescriptionTv;
        ImageView sheetMusicIv;

        public MusicSheetViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.titleTv);
            sheetDescriptionTv = itemView.findViewById(R.id.sheetDescriptionTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            sheetMusicIv = itemView.findViewById(R.id.sheetMusicIv);// Initialize username TextView
        }
    }
}
