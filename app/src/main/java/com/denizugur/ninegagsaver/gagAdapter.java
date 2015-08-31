package com.denizugur.ninegagsaver;

/**
 * Created by deniz on 28/08/15.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.List;

public class gagAdapter extends RecyclerView.Adapter<gagAdapter.gagViewHolder> {

    private List<gagInfo> gagList;

    public gagAdapter(List<gagInfo> gagList) {
        this.gagList = gagList;
    }

    private Bitmap bitmap(String file_path) {
        Bitmap bitmap = BitmapFactory.decodeFile(file_path);
        return bitmap;
    }

    @Override
    public int getItemCount() {
        return gagList.size();
    }


    @Override
    public void onBindViewHolder(final gagViewHolder ViewHolder, int i) {
        gagInfo gi = gagList.get(i);
        ViewHolder.vTitle.setText(gi.title);
        ViewHolder.vSavedDate.setText(gi.saved_date);
        ViewHolder.vLikes.setText(gi.likes);
        ViewHolder.vComments.setText(gi.comments);
        ViewHolder.vImageView.setImageBitmap(bitmap(gi.file_path));



        ViewHolder.vImageView.setOnClickListener(new View.OnClickListener() {
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
        protected ImageView vImageView;
        protected Button vShareButton;

        public gagViewHolder(View v) {
            super(v);
            vTitle = (TextView)  v.findViewById(R.id.title);
            vSavedDate = (TextView)  v.findViewById(R.id.saved_date);
            vLikes = (TextView) v.findViewById(R.id.likes);
            vComments = (TextView) v.findViewById(R.id.comments);
            vImageView = (ImageView) v.findViewById(R.id.photo);
            vShareButton = (Button) v.findViewById(R.id.shareButton);
        }
    }
}