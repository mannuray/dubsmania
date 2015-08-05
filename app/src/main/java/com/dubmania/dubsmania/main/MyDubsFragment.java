package com.dubmania.dubsmania.main;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dubmania.dubsmania.Adapters.MyVideoAdapter;
import com.dubmania.dubsmania.Adapters.MyVideoListItem;
import com.dubmania.dubsmania.R;
import com.dubmania.dubsmania.communicator.BusProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;


public class MyDubsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<MyVideoListItem> mMyVideoItemList;

    // TO Do remove it after experimenth
    private TypedArray navMenuIcons;

    public MyDubsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_my_dubs, container, false);
        final FragmentActivity c = getActivity();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_dubs_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(c);
        mRecyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mMyVideoItemList = new ArrayList<MyVideoListItem>(Arrays.asList(
                new MyVideoListItem(navMenuIcons.getResourceId(0, -1), "hotel calfornia is here", "rock",new GregorianCalendar(0,0,0,0,0,0))

        ));
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
