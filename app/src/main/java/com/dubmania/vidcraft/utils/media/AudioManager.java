package com.dubmania.vidcraft.utils.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.dubmania.vidcraft.utils.MiscFunction;
//import com.google.common.collect.Iterables;
import com.google.common.collect.Iterables;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by rat on 8/25/2015.
 */

public class AudioManager {

    private ArrayList<Audio> mAudioFlileList;
    private MediaPlayer mAudioPlayer;
    private int mPlayingPosition = 0;
    private boolean isRecordingAvailable = false;
    private boolean isPlayRecordingIntilized = false;
    //private long mCurrentTime = 0;
    private AudioRecorder mAudioRecorder;
    private Context mContext;
    private OnCompletionCallback mCallback = null;
    private OnTrackChangeCallback mTrackCallback = null;

    enum State {recording, playing, pause}
    State mState = State.pause;

    public AudioManager(Context mContext) {
        this.mContext = mContext;
        mAudioFlileList = new ArrayList<>();
        mAudioRecorder = new AudioRecorder();
        mAudioPlayer = new MediaPlayer();
        mAudioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                changeTrack();
            }
        });
    }

    public void setOnCompletionListener(OnCompletionCallback mCallback, OnTrackChangeCallback mTrackCallback) {
        this.mCallback = mCallback;
        this.mTrackCallback = mTrackCallback;
    }

    public void recordFrom(int position) throws IOException {
        for (int i = mAudioFlileList.size() - 1; i > position - 1; i--) {
            //Log.i("Maker", "size of file list is " + mAudioFlileList.size());
            mAudioFlileList.remove(i);
        }

        File audioFile = getRandomFileName();
        mAudioRecorder.startRecording(audioFile);
        mState = State.recording;
    }

    public void pause(int pos) throws IOException {
        if(mState == State.recording) {
            Audio audio;
            if(mAudioFlileList.size() <= 0 )
                audio = new Audio(mAudioRecorder.stopRecording(), 0, pos);
            else
                audio = new Audio(mAudioRecorder.stopRecording(), /*mAudioFlileList.get(mAudioFlileList.size()-1).getEndTime()*/Iterables.getLast(mAudioFlileList).getEndTime(), pos);
            mAudioFlileList.add(audio);
            isRecordingAvailable = true; // improve this
        }
        else if (mState == State.playing) {
            mAudioPlayer.pause();
        }
        mState = State.pause;
    }

    public void setPlayingPos(int pos) {
        if(!(pos < 0 || pos >= mAudioFlileList.size())) {
            mPlayingPosition = pos;
            isPlayRecordingIntilized = false;
        }
    }

    public void setProgress(int position) {
        for(Audio audio: mAudioFlileList) {
            if(position >= audio.getStartTime() && position <= audio.getEndTime()) { // ok this is the record we are interested in
                mPlayingPosition = mAudioFlileList.indexOf(audio);
                playFrom(mPlayingPosition);
                //mAudioPlayer.pause();
                mAudioPlayer.seekTo((int) (position - audio.getStartTime()));
                break;
            }
        }
    }

    public void playFrom(int position) {
        mPlayingPosition = position;
        isPlayRecordingIntilized = false;
        if(mState != State.pause)
            return;
        try {
            if(initializePlayRecording()) {
                mAudioPlayer.start();
                mState = State.playing;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if(mState != State.pause)
            return;
        try {
            if(initializePlayRecording()) {
                mAudioPlayer.start();
                mState = State.playing;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean initializePlayRecording() throws IOException {
        if(!isRecordingAvailable)
            return false;
        if(isPlayRecordingIntilized)
            return true;
        mAudioPlayer.reset();
        mAudioPlayer.setDataSource(mAudioFlileList.get(mPlayingPosition).getAudioFile().getAbsolutePath());
        mAudioPlayer.prepare();
        isPlayRecordingIntilized = true;
        return true;
    }

    private void changeTrack() {
        if(mTrackCallback != null)
            mTrackCallback.onTrackChangeStart();
        try {
            mPlayingPosition++;
            isPlayRecordingIntilized = false;
            if(mPlayingPosition >= mAudioFlileList.size()) {
                Log.i("Test Record", " ok playing positino greater");
                mPlayingPosition = 0;
                mState = State.pause;
                if(mCallback != null)
                    mCallback.onComplete();
                return;
            }
            initializePlayRecording();
            mAudioPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(mTrackCallback != null)
            mTrackCallback.onTrackChangeCompleted();
    }

    public File prepareAudio() {
        if(mAudioFlileList.isEmpty())
            return null;
        List<Track> audioTracks = new LinkedList<>();
        for(Audio audio: mAudioFlileList) {
            Track t = audio.getTrack();
            audioTracks.add(t);
        }

        Movie audio = new Movie();
        try {
            audio.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
            File output = getRandomFileName();
            Container mContainer =  new DefaultMp4Builder().build(audio);
            WritableByteChannel wbc = new FileOutputStream(output).getChannel();
            mContainer.writeContainer(wbc);
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getCurrentStartTimeOf(int position) {
        if(mAudioFlileList.size() <= 0)
            return 0;
        Log.i("Record Test", "mRecord position satret" + position + " du " + mAudioFlileList.get(position).getStartTime() + mAudioFlileList.get(position).getDuration());
        return mAudioFlileList.get(position).getStartTime();
    }

    public long getCurrentEndTimeOf(int position) {
        if(mAudioFlileList.size() <= 0)
            return 0;
        Log.i("Record Test", "mRecord position end " + position + " du " + mAudioFlileList.get(position).getEndTime() + mAudioFlileList.get(position).getDuration());
        return mAudioFlileList.get(position).getEndTime();
    }

    private File getRandomFileName() throws IOException {
        return File.createTempFile(MiscFunction.getRandomFileName("Audio"), ".mp4", mContext.getCacheDir());
    }


    public static class Audio {
        private long mStartTime;
        private long mEndTime;
        private File mAudioFile;
        private Track mTrack;

        public Audio(File mAudioFile, long mStartTime, long mEndTime) throws IOException {
            this.mAudioFile = mAudioFile;
            this.mStartTime = mStartTime;
            this.mEndTime = mEndTime;
            mTrack = MovieCreator.build(mAudioFile.getAbsolutePath()).getTracks().get(0);
        }

        public Track getTrack() {
            return mTrack;
        }

        public File getAudioFile() {
            return mAudioFile;
        }

        public long getStartTime() {
            return mStartTime;
        }

        public long getEndTime() { return  mEndTime; }

        public long getDuration() {
            return mEndTime - mStartTime;
            //return mTrack.getDuration()/10;
        }
    }

    public static abstract class OnCompletionCallback {
        public abstract void onComplete();
    }

    public static abstract class OnTrackChangeCallback {
        public abstract void onTrackChangeStart();
        public abstract void onTrackChangeCompleted();
    }
}
