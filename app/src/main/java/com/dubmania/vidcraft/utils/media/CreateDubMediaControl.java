package com.dubmania.vidcraft.utils.media;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.dubmania.vidcraft.R;
import com.dubmania.vidcraft.utils.RecordMarkerBar;

import java.io.IOException;

/**
 * Created by rat on 8/31/2015.
 */
public class CreateDubMediaControl extends LinearLayout {

    private Context mContext;
    private View mRoot;
    private boolean mRecordingAvailable = false;
    private RecordMarkerBar<Integer> mMarkerBar;
    private OnRecordingCompleteCallback mCallback;

    private ImageButton mPlayOriginal;

    private LinearLayout mRecordView;
    private ImageButton mPlayRecorded;
    private ImageButton mRecord;
    private ImageButton mNext;
    private ImageButton mPrevious;

    private AudioManager mAudioManager;
    private VideoManager mVideoManager;

    private int mNumberOfAudioSegment = 0;
    private int mSelectedMarker = 0; // will have a marker i.e start position

    enum State {initial, playingOriginal, playingRecorded, recording, pausePlayOriginal, pausePlayRecording, pauseRecording, posChanged}
    State mState = State.initial;
    State mActiveState = State.initial;

    public CreateDubMediaControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public void setMediaControllers(AudioManager mAudioManager, VideoManager mVideoManager) {
        this.mAudioManager = mAudioManager;
        this.mVideoManager = mVideoManager;
        this.mVideoManager.setOnCompletionListener(mCompletion);
        this.mVideoManager.setOnPrepareListener(mPrepare);
        this.mVideoManager.setOnTouchListener(mVideoViewClickListener);
        this.mAudioManager.setOnCompletionListener(mAudioCompletionListner, mTrackChangeListner);
    }

    public void setOnRecordingCompleteListner(OnRecordingCompleteCallback mListener) {
        mCallback = mListener;
    }

    public void setAnchorView(View view) {
        //mAnchor = view;

        LayoutParams frameParams = new LayoutParams(
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

        mMarkerBar = (RecordMarkerBar<Integer>) v.findViewById(R.id.recordMarkerBar);

        mPlayOriginal = (ImageButton) v.findViewById(R.id.playOriginal);
        mPlayOriginal.setOnClickListener(mPlayOriginalListener);

        mRecordView = (LinearLayout) v.findViewById(R.id.record);
        mPrevious = (ImageButton) v.findViewById(R.id.previous);
        mPrevious.setOnClickListener(mPreviousListener);

        mPlayRecorded = (ImageButton) v.findViewById(R.id.playRecorded);
        mPlayRecorded.setOnClickListener(mPlayRecordedListener);

        mRecord = (ImageButton) v.findViewById(R.id.recordDub);
        mRecord.setOnClickListener(mRecordListener);

        mNext = (ImageButton) v.findViewById(R.id.next);
        mNext.setOnClickListener(mNextListener);

        mMarkerBar.setOnRecordBarTouchListener(mMarkerBarListener);
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
                    mMarkerBar.addMarker(mVideoManager.getPos());
                    mSelectedMarker++;
                    mNumberOfAudioSegment++;
                    if(mCallback != null)
                        mCallback.onRecordingComplete(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(mState == State.playingRecorded) {
                mPlayRecorded.setImageResource(R.drawable.play);
                try {
                    mAudioManager.pause(mVideoManager.getPos());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            enableAllButton();
            mState = State.initial;
        }
    };

    private VideoManager.OnPrepareCallback mPrepare = new VideoManager.OnPrepareCallback() {

        @Override
        public void onPrepare() {
            mMarkerBar.setRangeValues(mVideoManager.getDuration());
        }
    };

    private AudioManager.OnCompletionCallback mAudioCompletionListner = new AudioManager.OnCompletionCallback() {

        @Override
        public void onComplete() {
            mPlayRecorded.setImageResource(R.drawable.play);

            mVideoManager.pause();
            mVideoManager.setPos(0);
            videoStart(true);
            mVideoManager.pause(); // due to synchronization issue

            enableAllButton();
            mState = State.initial;
        }
    };

    private OnTouchListener mVideoViewClickListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (mActiveState) {
                case initial:
                case playingOriginal:
                case pausePlayOriginal:
                    mPlayOriginalListener.onClick(view);
                    break;
                case playingRecorded:
                case pausePlayRecording:
                    mPlayRecordedListener.onClick(view);
                    break;
                case recording:
                case pauseRecording:
                    mRecordListener.onClick(view);
                    break;
            }
            return false;
        }
    };

    private RecordMarkerBar.OnRecordMarkerBarTouchListener mMarkerBarListener = new RecordMarkerBar.OnRecordMarkerBarTouchListener() {
        @Override
        public void onOnRecordMarkerBarTouch(double touchPosition, int mRecordPos, double lastRecordPos) {
            Log.i("Click", " touc pos " + touchPosition + " mRecord " + mRecordPos + " last record pos " + lastRecordPos);
            if(mState == State.playingOriginal) {
                //mVideoManager.pause();
                mVideoManager.setProgress(touchPosition);
            }
            else if(mState == State.pausePlayOriginal) {
                mVideoManager.setProgress(touchPosition);
                mMarkerBar.setCurrentProgressValue(mVideoManager.getPos());
            }
            else if(mState == State.pausePlayRecording || mState == State.pauseRecording || mState == State.posChanged) {
                if(mRecordPos >= 0) {
                    mSelectedMarker = mRecordPos;
                    mMarkerBar.setSelectedMarker(mSelectedMarker);
                    mState = State.posChanged;
                }
            }
            else if(mState == State.playingRecorded) {
                if(touchPosition < lastRecordPos) {
                    mVideoManager.setProgress(touchPosition);
                    mAudioManager.setProgress(mVideoManager.getPos());
                }
            }
        }
    };

    private AudioManager.OnTrackChangeCallback mTrackChangeListner = new AudioManager.OnTrackChangeCallback() {

        @Override
        public void onTrackChangeStart() {
            mVideoManager.pause();
        }

        @Override
        public void onTrackChangeCompleted() {
            videoPlay(true);
        }
    };

    private OnClickListener mPlayOriginalListener = new OnClickListener() {
        public void onClick(View v) {
            if(!(mState == State.pausePlayOriginal || mState == State.pausePlayRecording || mState == State.posChanged ||
                    mState == State.pauseRecording || mState == State.initial || mState == State.playingOriginal)) {
                return;
            }

            if(mState == State.playingOriginal) {
                mPlayOriginal.setImageResource(R.drawable.play);
                mRecordView.setEnabled(true);
                mVideoManager.pause();
                mState = State.pausePlayOriginal;
                mActiveState = State.pausePlayOriginal;
                return;
            }

            if(mState == State.pausePlayRecording || mState == State.pauseRecording) {
                mVideoManager.setPos(0);
            }

            if(mState == State.posChanged) {
                int selectedMarker = mSelectedMarker == mNumberOfAudioSegment ? 0 : mSelectedMarker;
                long position = mAudioManager.getCurrentStartTimeOf(selectedMarker);
                mVideoManager.setPos((int) position);
            }

            mPlayOriginal.setImageResource(R.drawable.pause);
            mRecordView.setEnabled(false);
            videoPlay(false);
            mState = State.playingOriginal;
            mActiveState = State.playingOriginal;
        }
    };

    private OnClickListener mPreviousListener = new OnClickListener() {
        public void onClick(View v) {
            if(mState == State.pausePlayOriginal || mState == State.pausePlayRecording ||
                    mState == State.pauseRecording || mState == State.posChanged || mState == State.initial) {
                //Log.i("position", "back button before" + mSelectedMarker);
                mSelectedMarker = Math.max(0, --mSelectedMarker);
                //Log.i("position", "back button " + mSelectedMarker);
                mMarkerBar.setSelectedMarker(mSelectedMarker);
                mState = State.posChanged;
            }
        }
    };

    private OnClickListener mPlayRecordedListener = new OnClickListener() {
        public void onClick(View v) {
            if(!mRecordingAvailable)
                return;

            if(!(mState == State.pausePlayOriginal || mState == State.pausePlayRecording || mState == State.posChanged ||
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
                mActiveState = State.pausePlayRecording;
                return;
            }

            if(mState == State.pausePlayOriginal || mState == State.pauseRecording || mState == State.initial) {
                mVideoManager.setPos(0);
                mAudioManager.setPlayingPos(0);
            }

            if(mState == State.posChanged) {
                int selectedMarker = mSelectedMarker == mNumberOfAudioSegment ? 0 : mSelectedMarker;
                long position = mAudioManager.getCurrentStartTimeOf(selectedMarker);
                mVideoManager.setPos((int) position);
                mAudioManager.playFrom(selectedMarker);
            }
            else {
                mAudioManager.play();
            }
            videoPlay(true);

            mPlayRecorded.setImageResource(R.drawable.pause);
            disableAllBut(mPlayRecorded);
            mState = State.playingRecorded;
            mActiveState = State.playingRecorded;

        }
    };

    private OnClickListener mRecordListener = new OnClickListener() {
        public void onClick(View v) {
            if(!(mState == State.pausePlayOriginal || mState == State.pausePlayRecording || mState == State.posChanged ||
                    mState == State.pauseRecording || mState == State.initial || mState == State.recording)) {
                return;
            }

            if(mState == State.recording) {
                mRecord.setImageResource(R.drawable.record);
                try {
                    mVideoManager.pause();
                    mAudioManager.pause(mVideoManager.getPos());
                    mMarkerBar.addMarker(mVideoManager.getPos());
                    mSelectedMarker++;
                    mNumberOfAudioSegment++;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                enableAllButton();
                mState = State.pauseRecording;
                mActiveState = State.pauseRecording;
                mRecordingAvailable = true;
                return;
            }

            long position = 0;
            if(mSelectedMarker == mNumberOfAudioSegment) { // ok we at the end of recording
                position = mAudioManager.getCurrentEndTimeOf(mSelectedMarker-1);
            }
            else { // play from the beginig of current marker
                position = mAudioManager.getCurrentStartTimeOf(mSelectedMarker);
            }
            mVideoManager.setPos((int) position);

            mRecord.setImageResource(R.drawable.pause);

            disableAllBut(mRecord);
            mState = State.recording;
            mActiveState = State.recording;
            try {
                mMarkerBar.removeMarkersFrom(mSelectedMarker);
                videoPlay(true);
                mAudioManager.recordFrom(mSelectedMarker);
                mNumberOfAudioSegment = mSelectedMarker;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(mCallback != null)
                mCallback.onRecordingComplete(false);
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

    public void setEnable(Boolean enable) {
        if(!enable) {
            mPlayOriginal.setEnabled(false);
            mPrevious.setEnabled(false);
            mPlayRecorded.setEnabled(false);
            mRecord.setEnabled(false);
            mNext.setEnabled(false);
            mMarkerBar.setEnabled(false);
        }
        else {
            mPlayOriginal.setEnabled(true);
            mPrevious.setEnabled(false);
            mPlayRecorded.setEnabled(false);
            mRecord.setEnabled(true);
            mNext.setEnabled(false);
            mMarkerBar.setEnabled(true);
        }
    }

    private OnClickListener mNextListener = new OnClickListener() {
        public void onClick(View v) {
            if(mState == State.pausePlayOriginal || mState == State.pausePlayRecording ||
                    mState == State.pauseRecording || mState == State.posChanged || mState == State.initial) {
                Log.i("position", "nxt button befor" + mSelectedMarker);
                mSelectedMarker = Math.min(++mSelectedMarker, mNumberOfAudioSegment);
                Log.i("position", "back button after" + mSelectedMarker);
                mMarkerBar.setSelectedMarker(mSelectedMarker);
                mState = State.posChanged;
            }
        }
    };

    private void videoPlay(boolean state) {
        mVideoManager.play(state);
        new TimeUpdater().start();
    }

    private void videoStart(boolean state) {
        mVideoManager.start(state);
        new TimeUpdater().start();
    }

    private class TimeUpdater extends Thread {
        @Override
        public void run() {
            while (mVideoManager.isPlaying()) {
                try {
                    Thread.sleep(100);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMarkerBar.setCurrentProgressValue(mVideoManager.getPos());
                        }
                    });
                } catch (Exception e) {
                    return;
                }
            }
        }
    }

    public static abstract class OnRecordingCompleteCallback {
        public abstract void onRecordingComplete(boolean status);
    }
}
