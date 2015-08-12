package com.dubmania.dubsmania.main;

import android.app.Activity;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dubmania.dubsmania.Adapters.MyVideoAdapter;
import com.dubmania.dubsmania.Adapters.MyVideoListItem;
import com.dubmania.dubsmania.R;
import com.dubmania.dubsmania.communicator.eventbus.BusProvider;
import com.dubmania.dubsmania.utils.SavedDubsData;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;


public class MyDubsFragment extends Fragment {
    private RecyclerView.Adapter mAdapter;
    private ArrayList<MyVideoListItem> mMyVideoItemList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_my_dubs, container, false);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.my_dubs_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mMyVideoItemList = new ArrayList<>();

        Realm realm = Realm.getInstance(getActivity().getApplicationContext());
        RealmResults<SavedDubsData> dubs = realm.allObjects(SavedDubsData.class).where().findAll();
        for(SavedDubsData dub: dubs) {
            mMyVideoItemList.add(new MyVideoListItem(ThumbnailUtils.createVideoThumbnail(dub.getFilePath(), MediaStore.Video.Thumbnails.MICRO_KIND), dub.getTitle(), "boardName", dub.getFilePath(), dub.getCreationDate()));
        }
        mAdapter = new MyVideoAdapter(mMyVideoItemList);
        mRecyclerView.setAdapter(mAdapter);

        return view;
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
}
