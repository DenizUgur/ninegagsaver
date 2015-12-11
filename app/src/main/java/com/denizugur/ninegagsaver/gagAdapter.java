package com.denizugur.ninegagsaver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.List;

public class gagAdapter extends RecyclerView.Adapter<gagAdapter.gagViewHolder> {

    private List<gagInfo> gagList;
    private Context context;
    private int lastPosition = -1;

    public gagAdapter(List<gagInfo> gagList, Context context) {
        this.gagList = gagList;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return gagList.size();
    }

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onBindViewHolder(final gagViewHolder ViewHolder, int i) {
        gagInfo gi = gagList.get(i);
        ViewHolder.vTitle.setText(gi.title);
        ViewHolder.vSavedDate.setText(gi.saved_date);
        ViewHolder.vLikes.setText(gi.likes);
        ViewHolder.vComments.setText(gi.comments);
        ViewHolder.vDraweeView.setImageURI(Uri.fromFile(new File(gi.file_path)));

        setAnimation(ViewHolder.vCardView, i);

        ViewHolder.vDraweeView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                Integer adapterPosition = ViewHolder.getAdapterPosition();
                Context context = v.getContext();
                gagInfo gi = gagList.get(adapterPosition);

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://9gag.com/gag/" + gi.photoId));
                context.startActivity(intent);

                return false;
            }
        });

        ViewHolder.vDraweeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Integer adapterPosition = ViewHolder.getAdapterPosition();
                Context context = v.getContext();
                gagInfo gi = gagList.get(adapterPosition);

                Intent intent = new Intent(context, FullScreenImageActivity.class);
                intent.putExtra("BitmapImage", gi.file_path);
                context.startActivity(intent);
            }
        });

        ViewHolder.vShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Integer adapterPosition = ViewHolder.getAdapterPosition();
                Context context = v.getContext();

                gagInfo gi = gagList.get(adapterPosition);
                File file_path = new File(gi.file_path);
                String gagTitle = gi.title;
                Uri uri = Uri.fromFile(file_path);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                Boolean boo = prefs.getBoolean("useTitleAsMessage", false);

                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                if (boo) {
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, gagTitle);
                } else {
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                }
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                context.startActivity(Intent.createChooser(intent, "Share Gag to..."));
            }
        });
    }

    @Override
    public gagViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(viewGroup.getContext());
        String card = prefs.getString("card_view", "0");
        Integer layoutID;

        if (card.equals("0")) {
            layoutID = R.layout.card_layout;
        } else {
            layoutID = R.layout.card_layout_nowhitebar;
        }

        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(layoutID, viewGroup, false);

        return new gagViewHolder(itemView);
    }

    public static class gagViewHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected TextView vSavedDate;
        protected TextView vLikes;
        protected TextView vComments;
        protected Button vShareButton;
        protected SimpleDraweeView vDraweeView;
        protected CardView vCardView;

        public gagViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.title);
            vSavedDate = (TextView) v.findViewById(R.id.saved_date);
            vLikes = (TextView) v.findViewById(R.id.likes);
            vComments = (TextView) v.findViewById(R.id.comments);
            vDraweeView = (SimpleDraweeView) v.findViewById(R.id.photo);
            vShareButton = (Button) v.findViewById(R.id.shareButton);
            vCardView = (CardView) v.findViewById(R.id.card_view);
        }
    }
}