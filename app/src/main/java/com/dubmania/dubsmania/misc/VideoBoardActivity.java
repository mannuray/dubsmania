package com.dubmania.dubsmania.misc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.dubmania.dubsmania.Adapters.VideoAdapter;
import com.dubmania.dubsmania.Adapters.VideoListItem;
import com.dubmania.dubsmania.R;
import com.dubmania.dubsmania.communicator.eventbus.BusProvider;
import com.dubmania.dubsmania.communicator.eventbus.CreateDubEvent;
import com.dubmania.dubsmania.communicator.eventbus.VideoFavriouteChangedEvent;
import com.dubmania.dubsmania.communicator.networkcommunicator.VideoFavoriteMarker;
import com.dubmania.dubsmania.communicator.networkcommunicator.VideoListDownloader;
import com.dubmania.dubsmania.communicator.networkcommunicator.VideoListDownloaderCallback;
import com.dubmania.dubsmania.createdub.CreateDubActivity;
import com.dubmania.dubsmania.utils.ConstantsStore;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class VideoBoardActivity extends AppCompatActivity {
    private ArrayList<VideoListItem> mVideoItemList;
    private RecyclerView.Adapter mAdapter;

    private Long mBoardId;
    private String mBoardName;
    private String mUserName;



    ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_board);

        Intent intent = getIntent();
        mBoardId = intent.getLongExtra(ConstantsStore.INTENT_BOARD_ID, Long.valueOf(0));
        mBoardName = intent.getStringExtra(ConstantsStore.INTENT_BOARD_NAME);
        mUserName = intent.getStringExtra(ConstantsStore.INTENT_BOARD_USER_NAME);

        spinner = (ProgressBar) findViewById(R.id.BoardProgressBar);
        spinner.setVisibility(View.VISIBLE);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.boardRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mVideoItemList = new ArrayList<>();
        mAdapter = new VideoAdapter(mVideoItemList);
        mRecyclerView.setAdapter(mAdapter);
        populateData();
    }

    @Override public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    private void populateData() {
        new VideoListDownloader().downloadBoardVideo(mBoardId, "mannuk", new VideoListDownloaderCallback() {
            @Override
            public void onVideosDownloadSuccess(ArrayList<VideoListItem> videos) {
                mVideoItemList.addAll(videos);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onVideosDownloadFailure() {

            }
        });
    }

    @Subscribe
    public void onCreateDubEvent(CreateDubEvent event) {
        Intent intent = new Intent(this, CreateDubActivity.class);
        intent.putExtra(ConstantsStore.VIDEO_ID, event.getId());
        startActivity(intent);
    }

    @Subscribe
    public void onnVideoFavriouteChangedEvent(VideoFavriouteChangedEvent event) {
        new VideoFavoriteMarker().markVavrioute(event.getId(), event.ismFavrioute());
    }
}