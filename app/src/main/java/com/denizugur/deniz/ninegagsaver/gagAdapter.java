package com.denizugur.deniz.ninegagsaver;

/**
 * Created by deniz on 28/08/15.
 */
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class gagAdapter extends RecyclerView.Adapter<gagAdapter.gagViewHolder> {

    private List<gagInfo> gagList;

    public gagAdapter(List<gagInfo> gagList) {
        this.gagList = gagList;
    }


    @Override
    public int getItemCount() {
        return gagList.size();
    }


    @Override
    public void onBindViewHolder(gagViewHolder ViewHolder, int i) {
        gagInfo ci = gagList.get(i);
        ViewHolder.vPhotoId.setText(ci.photoId);
        ViewHolder.vTitle.setText(ci.title);
        ViewHolder.vSavedDate.setText(ci.saved_date);
        ViewHolder.vLikes.setText(ci.likes);
        ViewHolder.vComments.setText(ci.comments);
    }

    @Override
    public gagViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new gagViewHolder(itemView);
    }

    public static class gagViewHolder extends RecyclerView.ViewHolder {

        protected TextView vPhotoId;
        protected TextView vTitle;
        protected TextView vSavedDate;
        protected TextView vLikes;
        protected TextView vComments;

        public gagViewHolder(View v) {
            super(v);
            vPhotoId =  (TextView) v.findViewById(R.id.photo_id);
            vTitle = (TextView)  v.findViewById(R.id.title);
            vSavedDate = (TextView)  v.findViewById(R.id.saved_date);
            vLikes = (TextView) v.findViewById(R.id.likes);
            vComments = (TextView) v.findViewById(R.id.comments);
        }
    }
}