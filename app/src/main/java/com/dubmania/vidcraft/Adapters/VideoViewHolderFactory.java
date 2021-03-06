package com.dubmania.vidcraft.Adapters;

import android.support.v7.widget.RecyclerView;
import android.widget.CompoundButton;

import com.dubmania.vidcraft.communicator.eventbus.BusProvider;
import com.dubmania.vidcraft.communicator.eventbus.miscevent.CreateDubEvent;
import com.dubmania.vidcraft.communicator.eventbus.miscevent.OnClickListnerEvent;
import com.dubmania.vidcraft.communicator.eventbus.miscevent.VideoFavoriteChangedEvent;
import com.dubmania.vidcraft.communicator.eventbus.miscevent.VideoItemMenuEvent;

/**
 * Created by rat on 8/2/2015.
 */
public class VideoViewHolderFactory {
    public static void bindViewHolder(VideoListItem mItem, RecyclerView.ViewHolder holder, int position) {
        final VideoListItem item = mItem;
        VideoViewHolder videoHolder = (VideoViewHolder) holder;
        videoHolder.mVideoName.setText(item.getName());
        videoHolder.mVideoUserName.setText("Uploaded by " + item.getUser());
        videoHolder.mFavourite.setOnCheckedChangeListener(null);
        videoHolder.mFavourite.setChecked(item.isFavourite());
        videoHolder.mVideoThumbnail.setImageBitmap(item.getThumbnail());

        videoHolder.mMenuIcon.setOnClickListener(new OnClickListnerEvent<>(new VideoItemMenuEvent(item.getId(), item.getName(), videoHolder.mMenuIcon)));
        videoHolder.mInfoBoxLayout.setOnClickListener(new OnClickListnerEvent<>(new CreateDubEvent(item.getId(), item.getName()))); //
        videoHolder.mFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BusProvider.getInstance().post(new VideoFavoriteChangedEvent(isChecked, item.getId()));
                item.setFavourite(isChecked);
            }
        });
    }
}
