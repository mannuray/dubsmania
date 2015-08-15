package com.dubmania.dubsmania.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dubmania.dubsmania.R;
import com.dubmania.dubsmania.communicator.eventbus.BusProvider;
import com.dubmania.dubsmania.misc.MyAccountActivity;
import com.dubmania.dubsmania.signupandlogin.SignupAndLoginActivity;
import com.dubmania.dubsmania.utils.SessionManager;

public class SettingFragment extends Fragment {

    private TextView mLogin;
    private View mUsername;
    private View mEmail;
    private TextView mUsernameText;
    private TextView mEmailText;
    private TextView mLogout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View rootView =  inflater.inflate(R.layout.fragment_setting, container, false);
        mLogin = (TextView) rootView.findViewById(R.id.login_signup);
        mUsername = rootView.findViewById(R.id.fragment_setting_username_view);
        mUsernameText = (TextView) rootView.findViewById(R.id.frament_setting_username_edit);
        mEmail = rootView.findViewById(R.id.fragment_setting_email_view);
        mEmailText = (TextView) rootView.findViewById(R.id.frament_setting_email_edit);
        mLogout = (TextView) rootView.findViewById(R.id.logout_text);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SignupAndLoginActivity.class);
                startActivity(intent);
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyAccountActivity.class);
                startActivity(intent);
            }
        });

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            ((TextView) rootView.findViewById(R.id.version_text)).setText("version: " + pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // set up text listners
        rootView.findViewById(R.id.support_center).setOnClickListener(new OnSettingClickListner("http://www.google.com"));
        rootView.findViewById(R.id.libray_we_use).setOnClickListener(new OnSettingClickListner("http://www.google.com"));
        rootView.findViewById(R.id.term_of_use).setOnClickListener(new OnSettingClickListner("http://www.google.com"));
        rootView.findViewById(R.id.privacy_policy).setOnClickListener(new OnSettingClickListner("http://www.google.com"));
        //setLoginView();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_empty, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        setLoginView();
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

    private void setLoginView() {
        SessionManager manager = new SessionManager(getActivity().getApplicationContext());
        if(manager.isLoggedIn()) {
            mUsernameText.setText(manager.getUser());
            mEmailText.setText(manager.getUserEmail());

            mLogin.setVisibility(View.GONE);
            mUsername.setVisibility(View.VISIBLE);
            mEmail.setVisibility(View.VISIBLE);
            mLogout.setVisibility(View.VISIBLE);
        }
        else {
            mLogin.setVisibility(View.VISIBLE);
            mUsername.setVisibility(View.GONE);
            mEmail.setVisibility(View.GONE);
            mLogout.setVisibility(View.GONE);
        }
    }

    private class OnSettingClickListner implements View.OnClickListener {
        private String uri;

        private OnSettingClickListner(String uri) {
            this.uri = uri;
        }

        @Override
        public void onClick(View v) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(browserIntent);
        }
    }
}
