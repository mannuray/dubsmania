package com.dubmania.vidcraft.communicator.eventbus.mainevent;

/**
 * Created by rat on 8/15/2015.
 */
public class VideoBoardScrollEndedEvent {
    private int mId;
    private int current_page;

    public VideoBoardScrollEndedEvent(int mId, int current_page) {
        this.mId = mId;
        this.current_page = current_page;
    }

    public int getmId() {
        return mId;
    }

    public int getCurrent_page() {
        return current_page;
    }
}
