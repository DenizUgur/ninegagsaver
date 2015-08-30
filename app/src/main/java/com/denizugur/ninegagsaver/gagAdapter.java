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
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

        ViewHolder.vRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Context context = v.getContext();
                Integer adapterPosition = ViewHolder.getAdapterPosition();
                String GAGS = "com.denizugur.deniz.ninegagsaver.gags";
                SharedPreferences.Editor editor = context.getSharedPreferences(GAGS, Context.MODE_PRIVATE).edit();
                gagInfo gi = gagList.get(adapterPosition);

                Log.d("Dismiss LOG", gi.photoId + File.separator + adapterPosition);
                editor.remove(gi.photoId).apply();
                File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "gags");
                if (dir.isDirectory()) {
                    File file = new File(dir + File.separator + gi.photoId);
                    file.delete();
                }
                gagList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                Intent i = new Intent(context, HomeCardActivity.class);
                context.startActivity(i);
            }
        });
    }

    @Override
    public gagViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new gagViewHolder(itemView);
    }

    public static class gagViewHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected TextView vSavedDate;
        protected TextView vLikes;
        protected TextView vComments;
        protected ImageView vImageView;
        protected Button vShareButton;
        protected Button vRemoveButton;

        public gagViewHolder(View v) {
            super(v);
            vTitle = (TextView)  v.findViewById(R.id.title);
            vSavedDate = (TextView)  v.findViewById(R.id.saved_date);
            vLikes = (TextView) v.findViewById(R.id.likes);
            vComments = (TextView) v.findViewById(R.id.comments);
            vImageView = (ImageView) v.findViewById(R.id.photo);
            vShareButton = (Button) v.findViewById(R.id.shareButton);
            vRemoveButton = (Button) v.findViewById(R.id.remove);
        }
    }
}