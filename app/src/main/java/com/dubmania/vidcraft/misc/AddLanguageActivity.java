package com.dubmania.vidcraft.misc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dubmania.vidcraft.Adapters.LanguageAndCountryDataHandler;
import com.dubmania.vidcraft.R;
import com.dubmania.vidcraft.communicator.networkcommunicator.AccountLanguageHandler;
import com.dubmania.vidcraft.communicator.networkcommunicator.LanguageHandler;
import com.dubmania.vidcraft.utils.ConstantsStore;
import com.dubmania.vidcraft.utils.SessionManager;
import com.dubmania.vidcraft.utils.database.InstalledLanguage;

import io.realm.Realm;

public class AddLanguageActivity extends AppCompatActivity {
    Toolbar mToolbar;
    private NumberPicker mCountryPicker;
    NumberPicker mLanguagePicker;
    private LanguageAndCountryDataHandler mLanguageData;
    private int mLanguagePosition;
    private int mCountryPosition;
    ProgressBar progressBar;
    LinearLayout languagePickerLayout;
    RelativeLayout start;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_language);

        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        mLanguagePicker = (NumberPicker) findViewById(R.id.language_picker);
        mCountryPicker = (NumberPicker) findViewById(R.id.country_picker);
        progressBar=(ProgressBar)findViewById(R.id.progressBar3);
        languagePickerLayout=(LinearLayout)findViewById(R.id.language_picker_layout);

        start = (RelativeLayout) findViewById(R.id.add_language);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setEnabled(false);
                final LanguageAndCountryDataHandler.Language lan = mLanguageData.getLanguage(mLanguagePosition);;
                if(new SessionManager(AddLanguageActivity.this).isLoggedIn()) {
                    new AccountLanguageHandler().putUserLanguage(lan.getId(), new AccountLanguageHandler.PutLanguageCallback() {

                        @Override
                        public void onPutLanguageCallbackSuccess() {
                            Intent intent = new Intent(AddLanguageActivity.this, LanguageActivity.class);
                            intent.putExtra(ConstantsStore.INTENT_INSTALL_LANGUAGE, lan.getLanguage());
                            intent.putExtra(ConstantsStore.INTENT_INSTALL_LANGUAGE_ID, lan.getId());
                            setResult(Activity.RESULT_OK, intent);
                            AddLanguageActivity.this.finish();
                        }

                        @Override
                        public void onPutLanguageCallbackFailure() {
                            start.setEnabled(true);
                        }
                    });
                }
                else {
                    Realm realm = Realm.getInstance(getApplicationContext());
                    realm.beginTransaction();
                    InstalledLanguage installedLanguage = realm.createObject(InstalledLanguage.class);
                    installedLanguage.setLanguageId(lan.getId());
                    installedLanguage.setLanguage(lan.getLanguage());
                    LanguageAndCountryDataHandler.Country con = lan.getCountry(mCountryPosition);
                    installedLanguage.setCountryId(con.getId());
                    installedLanguage.setCountry(con.getCountry());
                    realm.commitTransaction();

                    Intent intent = new Intent(AddLanguageActivity.this, LanguageActivity.class);
                    intent.putExtra(ConstantsStore.INTENT_INSTALL_LANGUAGE, lan.getLanguage());
                    intent.putExtra(ConstantsStore.INTENT_INSTALL_LANGUAGE_ID, lan.getId());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });
        start.setEnabled(false);

        populateData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_empty, menu);
        return super.onCreateOptionsMenu(menu);
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

    private void setData() {
        mLanguagePicker.setMinValue(0);
        mLanguagePicker.setMaxValue(mLanguageData.getLanguageSize() - 1);
        mLanguagePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mLanguagePicker.setDisplayedValues(mLanguageData.getLanguages());
        int pos = mLanguagePicker.getValue();
        mCountryPicker.setMinValue(0);
        mCountryPicker.setMaxValue(mLanguageData.getCountriesSize(mLanguagePosition) - 1);
        mCountryPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mCountryPicker.setDisplayedValues(mLanguageData.getCountries(pos));
        mLanguagePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mLanguagePosition = newVal;
                mCountryPicker.setDisplayedValues(null);
                mCountryPicker.setMinValue(0);
                mCountryPicker.setMaxValue(mLanguageData.getCountriesSize(mLanguagePosition) - 1);

                mCountryPicker.setDisplayedValues(mLanguageData.getCountries(mLanguagePosition));
                mCountryPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        mCountryPosition = newVal;
                    }
                });
            }
        });
    }

    private void populateData() {
        new LanguageHandler().downloadLanguageAndCountry(new LanguageHandler.LanguageListDownloadCallback() {
            @Override
            public void onLanguageListDownloadSuccess(LanguageAndCountryDataHandler mData) {
                mLanguageData = mData;
                progressBar.setVisibility(View.GONE);
                languagePickerLayout.setVisibility(View.VISIBLE);
                setData();
                start.setEnabled(true);
            }

            @Override
            public void onLanguageListDownloadFailure() {
                //Toast.makeText(this, "Unable to download list ", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
