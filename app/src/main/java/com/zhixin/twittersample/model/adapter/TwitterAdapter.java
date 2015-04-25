package com.zhixin.twittersample.model.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhixin.twittersample.R;
import com.zhixin.twittersample.model.twitter.Tweet;

import java.util.ArrayList;

/**
 * Created by Zhixin on 4/21/2015.
 */
public class TwitterAdapter extends RecyclerView.Adapter<TwitterAdapter.ViewHolder> {
    private ArrayList<Tweet> mDataSet;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtHeader;
        public TextView txtFooter;

        public ViewHolder(View itemView) {
            super(itemView);
            txtHeader = (TextView)itemView.findViewById(R.id.tweet_id);
            txtFooter = (TextView)itemView.findViewById(R.id.tweet_content);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TwitterAdapter(ArrayList<Tweet> myDataset) {
        mDataSet = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TwitterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_item/*tweet_entry*/, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        String tweetContent = mDataSet.get(i).getText();
        String displayName = mDataSet.get(i).getUser().getName();
        viewHolder.txtFooter.setText(tweetContent);
        viewHolder.txtHeader.setText(displayName);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
