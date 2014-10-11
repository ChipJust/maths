package com.chipjust.maths;

import java.util.HashSet;
import java.util.Set;

import com.chipjust.maths.Quiz.QuestionFragment;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	
	private static final String USER_LIST = "user_list";
	private static final String NEW_USER = "New User";
	private static final String CURRENT_USER = "Current User";
	private static final String COLOR_SELECTED_USER = "#D9E3B1";
	
	private static final String USER_PREFERNCE_MULTIPLICATION = "mult";
	private static final String USER_PREFERNCE_DIVISION = "div";
	private static final String USER_PREFERNCE_ADDITION = "add";
	private static final String USER_PREFERNCE_SUBTRACTION = "sub";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			FragmentTransaction t = getFragmentManager().beginTransaction();
			Fragment fragment = new UserSelectionFragment();
			t.add(R.id.container, fragment);
			t.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		case R.id.action_quiz:
			startActivity(new Intent(this, Quiz.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public static class UserSelectionFragment extends Fragment {
		public class UserButtonListener implements View.OnClickListener {

			@Override
			public void onClick(View v) {
				Button b = (Button) v;
			    String buttonText = b.getText().toString();
				Log.v(TAG, String.format("onClick:%s.", buttonText));
				if (buttonText.equals(NEW_USER)) {
					getFragmentManager().beginTransaction().replace(R.id.container, new NewUserFragment()).commit();
					return;
				}

				// Change the current user to this user.
				SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
				editor.putString(CURRENT_USER, buttonText);
				editor.commit();
				
				getFragmentManager().beginTransaction().replace(R.id.container, new UserFragment()).commit();
				return;
			}
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);
			LinearLayout l = (LinearLayout) rootView.findViewById(R.id.linearlayout);

			SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
			Set<String> user_list = pref.getStringSet(USER_LIST, new HashSet<String>());
			
			// Add the New User psuedo-user to the set. We can always add more users.
			user_list.add(NEW_USER);
			//NEWREL: Convert to list and sort.
			
			String currentUser = pref.getString(CURRENT_USER, "");
			
			// Create a button for each user.
			for (Object user: user_list) {
				Button button = new Button(getActivity());
				button.setText((CharSequence) user);
				button.setOnClickListener(new UserButtonListener());
				// Check to see if this button is for the currently selected user. If so, highlight it.
				if (user.equals(currentUser)) {
					//button.setBackground(getResources().getDrawable(R.drawable.button_border));
					button.setBackgroundColor(Color.parseColor(COLOR_SELECTED_USER));
				}
				l.addView(button);
			}

			return rootView;
		}
	}
	
	public void createUserButtonClick (View view) {
		Log.v(TAG, "createUserButtonClick");
		EditText input = (EditText) findViewById(R.id.new_user_input);
		String newUser = input.getText().toString();
		
		TextView status = (TextView) findViewById(R.id.status_message);
		if (newUser.equals("")) {
			status.setText(R.string.blank_user_error);
			return;
		}
		if (newUser.equals(NEW_USER)) {
			status.setText(R.string.new_user_error);
			return;
		}
		
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		Set<String> user_list = pref.getStringSet(USER_LIST, new HashSet<String>());
		// NEWREL: Make case insensitive, but preserve case in the set.
		
		if (user_list.contains(newUser)) {
			status.setText(R.string.user_exists_error);
			return;
		}
		
		// Add the new user to the set of users.
		user_list.add(newUser);
		SharedPreferences.Editor editor = pref.edit();
		editor.putStringSet(USER_LIST, user_list);
		editor.commit();
		
		// Create the preferences file for this user.
		SharedPreferences userPref = getSharedPreferences(newUser, Context.MODE_PRIVATE);
		SharedPreferences.Editor userEditor = userPref.edit();
		userEditor.putBoolean(USER_PREFERNCE_ADDITION, true);
		userEditor.putBoolean(USER_PREFERNCE_DIVISION, true);
		userEditor.putBoolean(USER_PREFERNCE_MULTIPLICATION, true);
		userEditor.putBoolean(USER_PREFERNCE_SUBTRACTION, true);

		// Transition back to the User Selection Screen.
		getFragmentManager().beginTransaction().replace(R.id.container, new UserSelectionFragment()).commit();
		return;
	}
	
	public static class NewUserFragment extends Fragment {
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_new_user, container, false);
			return rootView;
		}
	}
	
	public void deleteUserButtonClick (View view) {
		SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
		Set<String> user_list = pref.getStringSet(USER_LIST, new HashSet<String>());
		String currentUser = pref.getString(CURRENT_USER, "");
		
		Log.v(TAG, String.format("deleteUserButtonClick:%s.", currentUser));
		
		user_list.remove(currentUser);
		
		SharedPreferences.Editor editor = pref.edit();
		editor.putStringSet(USER_LIST, user_list);
		editor.remove(CURRENT_USER);
		editor.commit();
		
		// Transition back to the User Selection Screen.
		getFragmentManager().beginTransaction().replace(R.id.container, new UserSelectionFragment()).commit();
		return;
	}
	
	public void userPreferncesRadioButtonsClick (View view){
		String currentUser = getPreferences(Context.MODE_PRIVATE).getString(CURRENT_USER, "");
		RadioButton b = (RadioButton) view;
	    String bText = b.getText().toString();
		boolean bChecked = b.isChecked();
		Log.v(TAG, String.format("userPreferncesRadioButtonsClick:%s.%s.%b.", currentUser, bText, bChecked));
		//NEWREL: Handle the button click.
	}

	public static class UserFragment extends Fragment {
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_user, container, false);
			String currentUser = getActivity().getPreferences(Context.MODE_PRIVATE).getString(CURRENT_USER, "");
			TextView userName = (TextView) rootView.findViewById(R.id.user);
			userName.setText(currentUser);
			
			//NEWREL: Set the values of the current user's preference to this form.
			return rootView;
		}
	}
}
