package com.dubmania.vidcraft.addvideo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dubmania.vidcraft.R;
import com.dubmania.vidcraft.communicator.eventbus.BusProvider;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.AddTagsEvent;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.AddVideoChangeFragmentEvent;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.AddVideoEditEvent;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.AddVideoFinishEvent;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.AddVideoInfoEvent;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.AddVideoRecordDoneEvent;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.AddVideoUploadFailed;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.CancelVideoUpload;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.CancelVideoWaterMarking;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.SearchVideoItemListEvent;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.SetProgressBarValue;
import com.dubmania.vidcraft.communicator.networkcommunicator.VideoHandler;
import com.dubmania.vidcraft.utils.ConstantsStore;
import com.dubmania.vidcraft.utils.MiscFunction;
import com.dubmania.vidcraft.utils.SessionManager;
import com.dubmania.vidcraft.utils.SnackFactory;
import com.dubmania.vidcraft.utils.VidCraftApplication;
import com.dubmania.vidcraft.utils.media.ImageOverlayer;
import com.dubmania.vidcraft.utils.media.VideoPreparer;
import com.dubmania.vidcraft.utils.media.VideoTrimmer;
import com.dubmania.vidcraft.utils.media.WaterMarkPositionCalculator;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AddVideoActivity extends AppCompatActivity {

    private VideoInfo mVideoInfo;
    private Bitmap mWaterMark;
    private CancelableThread overlayerThread;
    private VideoHandler mVideoHandler;
    private double mTotalFrame = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        new SessionManager(this).checkLogin(new SessionManager.LoginListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure() {
                finish();
            }
        });

        mVideoHandler = new VideoHandler();

        Intent intent = getIntent();
        changeFragment(intent.getIntExtra(ConstantsStore.INTENT_ADD_VIDEO_ACTION, ConstantsStore.INTENT_ADD_VIDEO_RECORD));
        mVideoInfo = new VideoInfo();
        mWaterMark = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), " an resurt called fail", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_empty, menu);
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

        return super.onOptionsItemSelected(item);
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
    public void onBackPressed() {
        BusProvider.getInstance().post(new AddVideoChangeFragmentEvent(-1));
    }

    private void changeFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, getFragment(position))
                .commit();
    }

    private Fragment getFragment(int position) {

        switch (position) {
            case 0:
                return new AddVideoPagerFragment().init(false);
            case 1:
                return new AddVideoPagerFragment().init(true);
        }
        return new AddVideoPagerFragment().init(false);
    }

    private class VideoInfo {
        private String mSrcFilePath;
        private String mDstFilePath;
        private ArrayList<Tag> mTags;
        private String mTitle;
        private String mLanguage;

        public String getSrcFilePath() {
            return mSrcFilePath;
        }

        public void setFilePath(String mFilePath) {
            this.mSrcFilePath = mFilePath;
            this.mDstFilePath = "Temp_" + mFilePath;
        }

        public ArrayList<Tag> getTags() {
            return mTags;
        }

        public void setTags(ArrayList<Tag> mTags) {
            this.mTags = mTags;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String mTitle) {
            this.mTitle = mTitle;
        }

        public String getLanguage() {
            return mLanguage;
        }

        public void setLanguage(String mLanguage) {
            this.mLanguage = mLanguage;
        }

        public String getDstFilePath() {
            return mDstFilePath;
        }
    }

    @Subscribe
    public void onSearchVideoItemListEvent(SearchVideoItemListEvent event) {
        //Log.i("Change", "Staring trimmer");
        mVideoInfo.setFilePath(event.getFilePath());
        BusProvider.getInstance().post(new AddVideoInfoEvent(mVideoInfo.getSrcFilePath(), mVideoInfo.getTags(), mVideoInfo.getTitle(), mVideoInfo.getLanguage()));
        BusProvider.getInstance().post(new AddVideoChangeFragmentEvent(1));
    }

    @Subscribe
    public void onSearchVideoItemListEvent(AddVideoRecordDoneEvent event) {
        mVideoInfo.setFilePath(event.getFilePath());
        BusProvider.getInstance().post(new AddVideoInfoEvent(mVideoInfo.getSrcFilePath(), mVideoInfo.getTags(), mVideoInfo.getTitle(), mVideoInfo.getLanguage()));
        BusProvider.getInstance().post(new AddVideoChangeFragmentEvent(1));
    }

    @Subscribe
    public void onAddVideoEditEvent(AddVideoEditEvent event) {
        try {
            //Log.i("Video Trimmer", "Staring trimmer");

            final File dst = File.createTempFile(MiscFunction.getRandomFileName("Video"), ".mp4", getApplicationContext().getCacheDir());
            mTotalFrame = VideoTrimmer.startTrim(new File(mVideoInfo.getSrcFilePath()), dst, event.getStartPos(), event.getEndPos());
            //Log.i("Progress", "total frame is " + mTotalFrame);
            // ok we got the trim video on enode water mark.
            overlayerThread = new CancelableThread() {

                @Override
                public void run() {

                    th = new ImageOverlayer(VidCraftApplication.getContext().getExternalCacheDir(),dst.getAbsolutePath());
                    th.overLay(new WaterMarkPositionCalculator(mWaterMark),
                            new ImageOverlayer.Callback() {
                                @Override
                                public void onProgressUpdated(double done) {
                                    //Log.i("Progress", "progress updated " + done);
                                    BusProvider.getInstance().post(new SetProgressBarValue((int)((done*100)/mTotalFrame)));
                                }

                                @Override
                                public void onConversionCompleted(String h264Track, int fps) {
                                    try {
                                        File out = File.createTempFile(MiscFunction.getRandomFileName("Video"), ".mp4", getApplicationContext().getCacheDir());
                                        mVideoInfo.mDstFilePath = out.getAbsolutePath();
                                        VideoPreparer.prepareMovieFromH264Track(dst, new File(h264Track), out, fps);

                                        // hope this shit work
                                        AddVideoActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                BusProvider.getInstance().post(new AddVideoChangeFragmentEvent(2));
                                            }
                                        });
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onConversionFailed(String error) {
                                    SnackFactory.getSnack(findViewById(android.R.id.content), "Unable to process video due to unknown error").show();
                                    finish();
                                }
                            });
                }

            };
            overlayerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onCancelVideoWaterMarking(CancelVideoWaterMarking event) {
        if(overlayerThread != null)
            overlayerThread.cancel();
    }

    @Subscribe
    public void onAddTagsEvent(AddTagsEvent event) {
        mVideoInfo.setTags(event.getTags());
        //BusProvider.getInstance().post(new AddVideoInfoEvent(mVideoInfo.getSrcFilePath(), mVideoInfo.getTags(), mVideoInfo.getTitle(), mVideoInfo.getLanguage()));
        BusProvider.getInstance().post(new AddVideoChangeFragmentEvent(3));
    }

    @Subscribe
    public void onAddVideoFinishEvent(AddVideoFinishEvent event) {
        // change it to dst file path once file is modified
        mVideoHandler.addVideo(mVideoInfo.getDstFilePath(), event.getTitle(), mVideoInfo.getTags(), event.getLanguage(), new VideoHandler.VideoUploaderCallback() {
            @Override
            public void onVideosUploadSuccess(long mId) {
                HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder()
                        .setCategory("Video")
                        .setAction("AddVideo")
                        .setLabel(String.valueOf(mId));

                VidCraftApplication application = new VidCraftApplication();
                Tracker mTracker = VidCraftApplication.tracker();
                mTracker.send(builder.build());
                finish();
            }

            @Override
            public void onVideosUploadFailure() {
                Toast.makeText(getApplicationContext(), " unable to add video", Toast.LENGTH_LONG).show();
                BusProvider.getInstance().post(new AddVideoUploadFailed());
            }

            @Override
            public void onProgress(int mPercentage) {
                BusProvider.getInstance().post(new SetProgressBarValue(mPercentage));
            }
        });
    }

    @Subscribe
    public void onCancelVideoUpload(CancelVideoUpload event) {
        if(mVideoHandler != null)
            mVideoHandler.cancelUpload();
    }

    class CancelableThread extends Thread {
        ImageOverlayer th;

        public void cancel() {
            if(th != null)
                th.cancel();
        }
    }
}
