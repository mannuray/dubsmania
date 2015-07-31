package com.dubmania.dubsmania.signup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.dubmania.dubsmania.R;

public class EmailFragment extends Fragment {

	OnButtonClickListner mCallback;
	
	
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnButtonClickListner) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_signup_email, container, false);
        final EditText username = (EditText) rootView.findViewById(R.id.email);
        try{
            Account[] accounts = AccountManager.get(getActivity()).getAccountsByType("com.google");
            for (Account account : accounts) {
                username.setText(account.name);
                break;
            }
        }
        catch(Exception exception)
        {
            Log.i("Exception", "Exception:" + exception) ;
        }


        Button next = (Button) rootView.findViewById(R.id.next);
		next.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mCallback.onClickNextButton(1);
				}
			}); 
        return rootView;
    }
}