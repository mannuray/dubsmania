package com.dubmania.dubsmania.utils.media;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.dubmania.dubsmania.R;

import java.io.IOException;

/**
 * Created by rat on 8/31/2015.
 */
public class CreateDubMediaControl extends LinearLayout {

    private Context mContext;
    private ViewGroup mAnchor;
    private View mRoot;
    private ProgressBar mProgress;
    private boolean mShowing;

    private ImageButton mPlayOriginal;

    private LinearLayout mRecordView;
    private ImageButton mPlayRecorded;
    private ImageButton mRecord;
    private ImageButton mNext;
    private ImageButton mPrevious;

    private AudioManager mAudioManager;
    private VideoManager mVideoManager;

    enum State {initial, playingOriginal, playingRecorded, recording, pausePlayOriginal, pausePlayRecording, pauseRecording, posChanged }
    State mState = State.initial;

    public CreateDubMediaControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public void setMediaControllers(AudioManager mAudioManager, VideoManager mVideoManager) {
        this.mAudioManager = mAudioManager;
        this.mVideoManager = mVideoManager;
        this.mVideoManager.setOnCompletionListener(mCompletion);
        this.mAudioManager.setOnCompletionListener(mAudioCompletionListner, mTrackChangeListner);
    }

    public void setAnchorView(View view) {
        //mAnchor = view;

        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.create_dub_media_layout, null);

        initControllerView(mRoot);

        return mRoot;
    }

    private void initControllerView(View v) {
        mPlayOriginal = (ImageButton) v.findViewById(R.id.playOriginal);
        mPlayOriginal.setImageResource(R.drawable.play);
        mPlayOriginal.setOnClickListener(mPlayOriginalListener);

        mRecordView = (LinearLayout) v.findViewById(R.id.record);
        mPrevious = (ImageButton) v.findViewById(R.id.previous);
        mPrevious.setImageResource(R.drawable.previous);
        mPrevious.setOnClickListener(mPreviousListener);

        mPlayRecorded = (ImageButton) v.findViewById(R.id.playRecorded);
        mPlayRecorded.setImageResource(R.drawable.play);
        mPlayRecorded.setOnClickListener(mPlayRecordedListener);

        mRecord = (ImageButton) v.findViewById(R.id.recordDub);
        mRecord.setImageResource(R.drawable.record);
        mRecord.setOnClickListener(mRecordListener);

        mNext = (ImageButton) v.findViewById(R.id.next);
        mNext.setImageResource(R.drawable.next);
        mNext.setOnClickListener(mNextListener);
    }

    private VideoManager.OnCompletionCallback mCompletion = new VideoManager.OnCompletionCallback() {

        @Override
        public void onComplete() {
            if(mState == State.playingOriginal) {
                mPlayOriginal.setImageResource(R.drawable.play);
                mRecordView.setEnabled(true);
            }
            else if(mState == State.recording) {
                mRecord.setImageResource(R.drawable.record);
                try {
                    mAudioManager.pause(mVideoManager.getPos());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(mState == State.playingRecorded) {
                mPlayRecorded.setImageResource(R.drawable.play);
                try {
                    mAudioManager.pause(mVideoManager.getPos());
                    mAudioManager.setPlayingPos(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            enableAllButton();
            mState = State.initial;
        }
    };

    private AudioManager.OnCompletionCallback mAudioCompletionListner = new AudioManager.OnCompletionCallback() {

        @Override
        public void onComplete() {
            mPlayRecorded.setImageResource(R.drawable.play);

            mVideoManager.pause();
            mVideoManager.setPos(0);
            mVideoManager.start(true);
            mVideoManager.pause(); // due to synchronization issue
            mAudioManager.setPlayingPos(0);

            enableAllButton();
            mState = State.initial;
        }
    };

    private AudioManager.OnTrackChangeCallback mTrackChangeListner = new AudioManager.OnTrackChangeCallback() {

        @Override
        public void onTrackChangeStart() {
            mVideoManager.pause();
        }

        @Override
        public void onTrackChangeCompleted() {
            mVideoManager.play(true);
        }
    };

    private View.OnClickListener mPlayOriginalListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(!(mState == State.pausePlayOriginal || mState == State.pausePlayRecording ||
                    mState == State.pauseRecording || mState == State.initial || mState == State.playingOriginal)) {
                return;
            }

            if(mState == State.playingOriginal) {
                mPlayOriginal.setImageResource(R.drawable.play);
                mRecordView.setEnabled(true);
                mVideoManager.pause();
                mState = State.pausePlayOriginal;
                return;
            }

            if(mState == State.pausePlayRecording || mState == State.pauseRecording) {
                mVideoManager.setPos(0);
            }

            mPlayOriginal.setImageResource(R.drawable.pause);
            mRecordView.setEnabled(false);
            mVideoManager.play(false);
            mState = State.playingOriginal;
        }
    };

    private View.OnClickListener mPreviousListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(mState == State.pausePlayOriginal || mState == State.pausePlayRecording ||
                    mState == State.pauseRecording || mState == State.initial) {
                mAudioManager.setPrevPos();
            }
        }
    };

    private View.OnClickListener mPlayRecordedListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(!(mState == State.pausePlayOriginal || mState == State.pausePlayRecording ||
                    mState == State.pauseRecording || mState == State.initial || mState == State.playingRecorded)) {
                return;
            }

            if(mState == State.playingRecorded) {
                mPlayRecorded.setImageResource(R.drawable.play);
                try {
                    mVideoManager.pause();
                    mAudioManager.pause(mVideoManager.getPos());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                enableAllButton();
                mState = State.pausePlayRecording;
                return;
            }

            if(mState == State.pausePlayOriginal || mState == State.pauseRecording || mState == State.initial) {
                mVideoManager.setPos(0);
                mAudioManager.setPlayingPos(0);
            }

            mPlayRecorded.setImageResource(R.drawable.pause);

            disableAllBut(mPlayRecorded);
            mState = State.playingRecorded;
            mAudioManager.play();
            mVideoManager.play(true);
        }
    };

    private View.OnClickListener mRecordListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(!(mState == State.pausePlayOriginal || mState == State.pausePlayRecording ||
                    mState == State.pauseRecording || mState == State.initial || mState == State.recording)) {
                return;
            }

            if(mState == State.recording) {
                mRecord.setImageResource(R.drawable.record);
                try {
                    mVideoManager.pause();
                    mAudioManager.pause(mVideoManager.getPos());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                enableAllButton();
                mState = State.pauseRecording;
                return;
            }

            long position = mAudioManager.getCurrentTime();
            mVideoManager.setPos((int) position);

            mRecord.setImageResource(R.drawable.pause);

            disableAllBut(mRecord);
            mState = State.recording;
            mVideoManager.play(true);
            try {
                mAudioManager.record();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void enableAllButton() {
        mPlayOriginal.setEnabled(true);
        mPrevious.setEnabled(true);
        mPlayRecorded.setEnabled(true);
        mRecord.setEnabled(true);
        mNext.setEnabled(true);
    }

    private void disableAllBut(ImageButton button) {
        mPlayOriginal.setEnabled(false);
        mPrevious.setEnabled(false);
        mPlayRecorded.setEnabled(false);
        mRecord.setEnabled(false);
        mNext.setEnabled(false);

        button.setEnabled(true);
    }

    private View.OnClickListener mNextListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(mState == State.pausePlayOriginal || mState == State.pausePlayRecording ||
                    mState == State.pauseRecording || mState == State.initial) {
                mAudioManager.setNextPos();
            }
        }
    };
}
