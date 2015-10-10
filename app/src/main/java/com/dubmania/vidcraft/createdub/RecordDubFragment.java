package com.dubmania.vidcraft.createdub;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.dubmania.vidcraft.R;
import com.dubmania.vidcraft.communicator.eventbus.BusProvider;
import com.dubmania.vidcraft.communicator.eventbus.createdubevent.RecordingDoneEvent;
import com.dubmania.vidcraft.communicator.eventbus.createdubevent.SetRecordFilesEvent;
import com.dubmania.vidcraft.utils.media.AudioManager;
import com.dubmania.vidcraft.utils.media.CreateDubMediaControl;
import com.dubmania.vidcraft.utils.media.VideoManager;
import com.squareup.otto.Subscribe;

import java.io.File;


public class RecordDubFragment extends Fragment {

    private CreateDubMediaControl mMediaControl;
    private ProgressBar mProgressBar;

    private AudioManager mAudioManager;
    private VideoManager mVideoManager = null;

    private File mVideoFile = null;

    public RecordDubFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record_dub, container, false);
        mMediaControl = (CreateDubMediaControl) view.findViewById(R.id.mediaControl);
        mMediaControl.setAnchorView(view);
        mAudioManager = new AudioManager(getActivity().getApplicationContext());
        mVideoManager = new VideoManager((VideoView) view.findViewById(R.id.videoView));

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        if(mVideoFile != null) { // ok the event was recived before so set it
            mVideoManager.setVideoFilePath(mVideoFile);
            mMediaControl.setMediaControllers(mAudioManager, mVideoManager);
            mProgressBar.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create_dub, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create_dub) {
            BusProvider.getInstance().post(new RecordingDoneEvent(mAudioManager.prepareAudio()));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onSetRecordFilesEvent(SetRecordFilesEvent event) {
        if(mVideoManager == null) {
            mVideoFile = event.getVideoFile();
            return;
        }

        mVideoManager.setVideoFilePath(event.getVideoFile());
        mMediaControl.setMediaControllers(mAudioManager, mVideoManager);
        mProgressBar.setVisibility(View.GONE);
    }
}
