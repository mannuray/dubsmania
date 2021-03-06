package com.dubmania.vidcraft.addvideo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dubmania.vidcraft.Adapters.TagListAdapter;
import com.dubmania.vidcraft.R;
import com.dubmania.vidcraft.communicator.eventbus.BusProvider;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.AddTagsEvent;
import com.dubmania.vidcraft.communicator.eventbus.addvideoevent.AddVideoChangeFragmentEvent;
import com.dubmania.vidcraft.communicator.networkcommunicator.TagsHandler;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class AddTagFragment extends Fragment implements AbsListView.OnItemClickListener {
    private TagListAdapter mAdapter;
    EditText search;
    private TextView mTagsView;
    private ArrayList<Tag> mTags;
    private ArrayList<Tag> mAddedTags;
    private TagsHandler mTagsDownloader;
    private MenuItem next;
    private ImageView mCross;
    private ProgressBar mProgressBar;

    public AddTagFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTagsDownloader = new TagsHandler();
        mTags = new ArrayList<>();
        mAddedTags = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_add_tag, container, false);
        mTagsView = (TextView) view.findViewById(R.id.textView);
        //mTagsView.setClickable(true);
        mTagsView.setMovementMethod(LinkMovementMethod.getInstance());
        mAdapter = new TagListAdapter(getActivity().getApplicationContext(), mTags);

        AbsListView mListView = (AbsListView) view.findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mCross = (ImageView) view.findViewById(R.id.crossImage);
        mCross.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                search.setText("");
                mTags.clear();
                mAdapter.notifyDataSetChanged();
                return false;
            }
        });
        search = (EditText) view.findViewById(R.id.editText);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (search.getText().toString().equals("")) {
                    mCross.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                }
                else {
                    mTagsDownloader.downloadTags(s.toString(), new OnTextWatcherListener());
                    mCross.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String sTag = mTags.get(position).getTag();
        for(Tag mTag: mAddedTags) {
            if(mTag.getTag().equals(sTag)) {
                // TO DO show toast as tag is already added
                return;
            }
        }

        mAddedTags.add(mTags.get(position));
        if( mAddedTags.size() >= 3) {
            next.setEnabled(true);
            next.setVisible(true);
        }
        addTag(mTags.get(position));
        search.setText("");
        mTags.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_tag, menu);
        super.onCreateOptionsMenu(menu, inflater);
        next = menu.getItem(0);
        if((mAddedTags == null) || (mAddedTags != null && mAddedTags.size() < 3)) {
            next.setEnabled(false);
            next.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == R.id.action_add_tag_video) {
            BusProvider.getInstance().post(new AddTagsEvent(mAddedTags));
        }
        return true;
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

    private class OnTextWatcherListener extends TagsHandler.TagsDownloaderCallback {

        @Override
        public void onTagsDownloadSuccess(ArrayList<Tag> tags) {
            mProgressBar.setVisibility(View.GONE);
            mTags.clear();
            mTags.addAll(tags);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onTagsDownloadFailure() {
            mProgressBar.setVisibility(View.GONE);
            // show toast
        }
    }

    private void addTag(Tag mTag) {
        TextView textview = new TextView(getActivity());


       // textview.setGravity(Gravity.CENTER_VERTICAL);
        textview.setText(mTag.getTag());
        textview.setBackgroundResource(R.drawable.rectangular_background);
        textview.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.cross_15,0);
        BitmapDrawable dd = (BitmapDrawable) getDrawableFromTextView(textview);
        mTagsView.append(addSpanText(mTag.getTag(), dd));
        mTagsView.append(" ");
    }

    private void deleteTag(String sTag) {
        Tag tTag = null;
        boolean found = false;
        for(Tag mTag: mAddedTags) {
            if(mTag.getTag().equals(sTag)) {
                tTag = mTag;
                found = true;
                break;
            }
        }

        if(found) {
            mAddedTags.remove(tTag);
            if( mAddedTags.size() < 3) {
                next.setEnabled(false);
                next.setVisible(false);
            }
            mTagsView.setText("");
            for(Tag mTag: mAddedTags) {
                addTag(mTag);
            }
        }
    }

    private SpannableStringBuilder addSpanText(final String ss, BitmapDrawable bd) {
        bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(ss);
        builder.setSpan(new ImageSpan(bd), builder.length() - ss.length(),
                builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTagsView.setMovementMethod(LinkMovementMethod.getInstance());
        ClickableSpan clickSpan = new ClickableSpan() {
            private String mSs = ss;
            @Override
            public void onClick(View view) {
                deleteTag(mSs);
                Log.i("Span", "clicked ");
            }
        };
        builder.setSpan(clickSpan, builder.length() - ss.length(),
                builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private Object getDrawableFromTextView(View view) {

        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return new BitmapDrawable(getResources(), viewBmp);
    }
}
