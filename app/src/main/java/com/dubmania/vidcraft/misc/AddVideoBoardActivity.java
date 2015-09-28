package com.dubmania.vidcraft.misc;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.dubmania.vidcraft.Adapters.ImageAdapter;
import com.dubmania.vidcraft.R;
import com.dubmania.vidcraft.communicator.networkcommunicator.VideoBoardCreaterCallback;
import com.dubmania.vidcraft.communicator.networkcommunicator.VideoBoardCreator;
import com.dubmania.vidcraft.utils.SessionManager;

import java.util.ArrayList;

public class AddVideoBoardActivity extends AppCompatActivity {
    Toolbar mToolbar;
    private EditText mBoardName;
    private View mAdd;
    private int mIconId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video_board);

        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
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

        mBoardName = (EditText) findViewById(R.id.add_video_board_name_edit);
        GridView mBoardIcon = (GridView) findViewById(R.id.add_video_board_gridView);

        TypedArray mBoardIcons = getResources()
                .obtainTypedArray(R.array.video_board_icons);
        ArrayList<Integer> mThumbIds = new ArrayList<>();
        for(int i = 0; i < mBoardIcons.length(); i++) {
            mThumbIds.add(mBoardIcons.getResourceId(i, -1));
        }
        mBoardIcons.recycle();

        mBoardIcon.setAdapter(new ImageAdapter(getApplicationContext(), mThumbIds));
        mBoardIcon.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mIconId = position;
            }
        });

        mAdd = findViewById(R.id.addVideoboard);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new VideoBoardCreator().addVideoBoard(mBoardName.getText().toString(), mIconId, new VideoBoardCreaterCallback() {
                    @Override
                    public void onVideoBoardCreateSuccess() {
                        finish();
                    }

                    @Override
                    public void onVideoBoardCreateFailure() {
                        //
                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
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
}