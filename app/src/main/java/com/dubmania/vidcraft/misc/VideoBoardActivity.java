package com.dubmania.vidcraft.misc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dubmania.vidcraft.Adapters.VideoAdapter;
import com.dubmania.vidcraft.Adapters.VideoListItem;
import com.dubmania.vidcraft.R;
import com.dubmania.vidcraft.communicator.eventbus.BusProvider;
import com.dubmania.vidcraft.communicator.eventbus.miscevent.CreateDubEvent;
import com.dubmania.vidcraft.communicator.eventbus.miscevent.VideoDeletedEvent;
import com.dubmania.vidcraft.communicator.eventbus.miscevent.VideoFavoriteChangedEvent;
import com.dubmania.vidcraft.communicator.eventbus.miscevent.VideoItemMenuEvent;
import com.dubmania.vidcraft.communicator.networkcommunicator.FavoriteHandler;
import com.dubmania.vidcraft.communicator.networkcommunicator.VideoBoardHandler;
import com.dubmania.vidcraft.communicator.networkcommunicator.VideoBoardVideoHandler;
import com.dubmania.vidcraft.createdub.CreateDubActivity;
import com.dubmania.vidcraft.dialogs.VideoItemPopupMenu;
import com.dubmania.vidcraft.utils.ConstantsStore;
import com.dubmania.vidcraft.utils.SnackFactory;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class VideoBoardActivity extends AppCompatActivity {
    private ArrayList<VideoListItem> mVideoItemList;
    private VideoAdapter mAdapter;
    private Menu mMenu;

    private Long mBoardId;
    private boolean mUserBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_board);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mBoardId = intent.getLongExtra(ConstantsStore.INTENT_BOARD_ID, (long) 0);
        String mBoardName = intent.getStringExtra(ConstantsStore.INTENT_BOARD_NAME); // set it in action bar
        String mUserName = intent.getStringExtra(ConstantsStore.INTENT_BOARD_USER_NAME); // set it in action bar
        int icon = intent.getIntExtra(ConstantsStore.INTENT_BOARD_ICON, 0); // set it in action bar

        getSupportActionBar().setTitle(mBoardName);
        mToolbar.setSubtitle("Uploaded by " + mUserName);
        mToolbar.setLogo(icon);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.boardRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mVideoItemList = new ArrayList<>();
        mVideoItemList.add(null);
        populateData();
        mAdapter = new VideoAdapter(mVideoItemList, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);

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
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_video_board, menu);
        mMenu.setGroupVisible(R.id.deleteBoardMenu, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if(id == R.id.action_delete_board) {
            new VideoBoardHandler(this).deleteVideoBoard(mBoardId, new VideoBoardHandler.DeleteVideoBoardCallback() {
                @Override
                public void onDeleteVideoBoardSuccess() {
                    Intent intent = new Intent();
                    intent.putExtra(ConstantsStore.INTENT_BOARD_ID, mBoardId);
                    intent.putExtra(ConstantsStore.INTENT_BOARD_DELETED, true);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onDeleteVideoBoardFailure() {

                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateData() {

        new VideoBoardVideoHandler().downloadBoardVideo(mBoardId, new VideoBoardVideoHandler.VideoListDownloaderCallback() {

            @Override
            public void onVideosDownloadSuccess(ArrayList<VideoListItem> videos, boolean userBoard, String cursor) {
                mMenu.setGroupVisible(R.id.deleteBoardMenu, userBoard);
                mUserBoard = userBoard;
                mAdapter.addData(videos);
            }

            @Override
            public void onVideosDownloadFailure() {
                SnackFactory.getInternetConnectionRetrySnack(findViewById(android.R.id.content), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        populateData();
                    }
                }).show();
                finish();
            }
        });
    }

    @Subscribe
    public void onCreateDubEvent(CreateDubEvent event) {
        Intent intent = new Intent(this, CreateDubActivity.class);
        intent.putExtra(ConstantsStore.VIDEO_ID, event.getId());
        intent.putExtra(ConstantsStore.INTENT_VIDEO_TITLE, event.getTitle());
        startActivity(intent);
    }

    @Subscribe
    public void onVideoFavoriteChangedEvent(VideoFavoriteChangedEvent event) {
        FavoriteHandler f = new FavoriteHandler();
        if(event.ismFavrioute())
            f.markFavorite(event.getId());
        else
            f.deleteFavorite(event.getId());
    }

    @Subscribe
    public void onVideoItemMenuEvent(VideoItemMenuEvent event) {
        new VideoItemPopupMenu(mBoardId, this, event.getId(), event.getTitle(), event.getView(), mUserBoard).show();
    }

    @Subscribe
    public void onVideoDeletedEvent(VideoDeletedEvent event) {
        for(int i = 0; i < mVideoItemList.size(); i++) {
            if(mVideoItemList.get(i).getId().equals(event.getmVideoId())) {
                mVideoItemList.remove(i);
                mAdapter.notifyDataSetChanged();;
                break;
            }
        }
    }
}
