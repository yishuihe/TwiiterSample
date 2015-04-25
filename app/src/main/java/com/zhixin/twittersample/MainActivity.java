package com.zhixin.twittersample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.zhixin.twittersample.model.adapter.TwitterAdapter;
import com.zhixin.twittersample.model.twitter.Tweet;
import com.zhixin.twittersample.model.twitter.TwitterManager;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements TwitterManager.LoadTweetListener{
    private TwitterManager mTwitterManager = null;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        download();
    }

    private void initViews() {
        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


    }

    private void download() {
        if(mTwitterManager == null) {
//            mTwitterManager = TwitterManager.createInstance(this, "therockncoder");
            mTwitterManager = TwitterManager.createInstance(this, "yishuihe");
        }


        mTwitterManager.downloadTweets(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTweetLoaded(ArrayList<Tweet> tweet) {
        // specify an adapter (see also next example)
        mAdapter = new TwitterAdapter(tweet);
        mRecyclerView.setAdapter(mAdapter);
    }
}
