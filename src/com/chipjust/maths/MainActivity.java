package com.chipjust.maths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.app.Activity;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends MathsActivity {
	
	private static final String USER_LIST = "User List";
	private static final String NEW_USER = "New User";
	
	private static final String QUIZ_LIST = "Quiz List";
	private static final String NEW_QUIZ = "New Quiz";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().replace(R.id.container, new MainFragment()).addToBackStack(null).commit();
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
		case R.id.action_quiz:
			startActivity(new Intent(this, QuizActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void mainButtonClick (View view) {
		Log.v(TAG, "mainButtonClick");
		switch (view.getId()) {
		case R.id.select_user:
			getFragmentManager().beginTransaction().replace(R.id.container, new UserSelectionFragment()).addToBackStack(null).commit();
			return;
		case R.id.select_quiz:
			getFragmentManager().beginTransaction().replace(R.id.container, new QuizSelectionFragment()).addToBackStack(null).commit();
			break;
		case R.id.user_stats:
			//NEWREL:getFragmentManager().beginTransaction().replace(R.id.container, new UserStatsFragment()).addToBackStack(null).commit();
			break;
		default:
			break;
		}
	}
	
	public static class MainFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			String currentUser = getActivity().getSharedPreferences(USERS_FILE, Context.MODE_PRIVATE).getString(CURRENT_USER, "No User Selected");
			((TextView) rootView.findViewById(R.id.current_user)).setText(currentUser);
			String currentQuiz = getActivity().getSharedPreferences(QUIZES_FILE, Context.MODE_PRIVATE).getString(CURRENT_QUIZ, "");
			((TextView) rootView.findViewById(R.id.current_quiz)).setText(currentQuiz);
			return rootView;
		}
	}
	
	public static class UserSelectionFragment extends Fragment {
		
		public class UserSelectionFragmentListener implements View.OnClickListener {

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
				SharedPreferences.Editor editor = getActivity().getSharedPreferences(USERS_FILE, Context.MODE_PRIVATE).edit();
				editor.putString(CURRENT_USER, buttonText);
				editor.commit();
				
				getFragmentManager().beginTransaction().replace(R.id.container, new UserFragment()).addToBackStack(null).commit();
				return;
			}
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);
			LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.user_list_ll);

			SharedPreferences pref = getActivity().getSharedPreferences(USERS_FILE, Context.MODE_PRIVATE);
			Set<String> user_list = pref.getStringSet(USER_LIST, new HashSet<String>());
			
			// Add the New User psuedo-user to the set. We can always add more users.
			user_list.add(NEW_USER);
			//NEWREL: Convert to list and sort.
			
			String currentUser = pref.getString(CURRENT_USER, "");
			
			// Create a button for each user.
			for (Object user: user_list) {
				Button button = new Button(getActivity());
				button.setText((CharSequence) user);
				button.setOnClickListener(new UserSelectionFragmentListener());
				// Check to see if this button is for the currently selected user. If so, highlight it.
				if (user.equals(currentUser)) {
					button.setBackgroundColor(Color.parseColor(COLOR_SELECTED));
				}
				ll.addView(button);
			}

			return rootView;
		}
	}

	public static class QuizSelectionFragment extends Fragment {
		
		public class QuizSelectionFragmentListener implements View.OnClickListener {

			@Override
			public void onClick(View v) {
				Button b = (Button) v;
			    String buttonText = b.getText().toString();
				Log.v(TAG, String.format("onClick:%s.", buttonText));
				if (buttonText.equals(NEW_QUIZ)) {
					getFragmentManager().beginTransaction().replace(R.id.container, new NewQuizFragment()).addToBackStack(null).commit();
					return;
				}

				// Change the current quiz
				SharedPreferences.Editor editor = getActivity().getSharedPreferences(QUIZES_FILE, Context.MODE_PRIVATE).edit();
				editor.putString(CURRENT_QUIZ, buttonText);
				editor.commit();
				
				getFragmentManager().beginTransaction().replace(R.id.container, new QuizFragment()).addToBackStack(null).commit();
				return;
			}
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_quiz_list, container, false);
			LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.quiz_list_ll);

			SharedPreferences pref = getActivity().getSharedPreferences(QUIZES_FILE, Context.MODE_PRIVATE);
			TreeSet<String> quizSet = new TreeSet<String>(pref.getStringSet(QUIZ_LIST, new HashSet<String>()));
			ArrayList<String> quizList = new ArrayList<String>(quizSet);
			
			// Add the New User psuedo-user to the set. We can always add more users.
			quizList.add(NEW_QUIZ);
			
			String currentQuiz = pref.getString(CURRENT_QUIZ, "");
			
			// Create a button for each user.
			for (Object quiz: quizList) {
				Button button = new Button(getActivity());
				button.setText((CharSequence) quiz);
				button.setOnClickListener(new QuizSelectionFragmentListener());
				// Check to see if this button is for the currently selected user. If so, highlight it.
				if (quiz.equals(currentQuiz)) {
					button.setBackgroundColor(Color.parseColor(COLOR_SELECTED));
				}
				ll.addView(button);
			}

			return rootView;
		}
	}

	public void deleteQuizButtonClick (View view) {
		SharedPreferences pref = getSharedPreferences(QUIZES_FILE, Context.MODE_PRIVATE);
		Set<String> quiz_list = pref.getStringSet(QUIZ_LIST, new HashSet<String>());
		String currentQuiz = pref.getString(CURRENT_QUIZ, "");
		
		Log.v(TAG, String.format("deleteQuizButtonClick:%s.", currentQuiz));
		
		quiz_list.remove(currentQuiz);
		
		SharedPreferences.Editor editor = pref.edit();
		editor.putStringSet(QUIZ_LIST, quiz_list);
		editor.remove(CURRENT_QUIZ);
		editor.commit();
		
		// Transition back to the User Selection Screen.
		getFragmentManager().beginTransaction().replace(R.id.container, new QuizSelectionFragment()).commit();
		return;
	}

	public void createQuizButtonClick (View view) {
		Log.v(TAG, "createQuizButtonClick");
		EditText input = (EditText) findViewById(R.id.new_quiz_input);
		String newQuiz = input.getText().toString();
		String newQuizFile = QUIZES_FILE + "." + newQuiz;
		
		TextView status = (TextView) findViewById(R.id.quiz_status_message);
		if (newQuiz.equals("")) {
			status.setText(R.string.blank_quiz_error);
			return;
		}
		if (newQuiz.equals(NEW_QUIZ)) {
			status.setText(R.string.new_quiz_error);
			return;
		}
		//NEWREL: Limit the length of the user name
		
		SharedPreferences pref = getSharedPreferences(QUIZES_FILE, Context.MODE_PRIVATE);
		Set<String> quizSet = pref.getStringSet(QUIZ_LIST, new HashSet<String>());
		// NEWREL: Make case insensitive, but preserve case in the set.
		
		if (quizSet.contains(newQuiz)) {
			status.setText(R.string.quiz_exists_error);
			return;
		}
		
		// Create the preferences file for this user.
		SharedPreferences quizPref = getSharedPreferences(newQuizFile, Context.MODE_PRIVATE);
		SharedPreferences.Editor quizEditor = quizPref.edit();
		for (String op : operators) {
			quizEditor.putBoolean(op, true);
		}
		for (Integer num : numbers) {
			quizEditor.putBoolean(num.toString(), true);
		}
		quizEditor.commit();

		// Add the new user to the set of users.
		quizSet.add(newQuiz);
		SharedPreferences.Editor editor = pref.edit();
		editor.putStringSet(QUIZ_LIST, quizSet);
		editor.commit();
		
		// Transition back to the User Selection Screen.
		// BUGBUG:?I think this might need to do a pop on the BackStack instead...
		getFragmentManager().beginTransaction().replace(R.id.container, new QuizSelectionFragment()).commit();
		return;
	}
	
	public static class NewQuizFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_new_quiz, container, false);
			return rootView;
		}
	}
	
	public static class QuizFragment extends Fragment {
		
		public class QuizFragmentListener implements View.OnClickListener {

			@Override
			public void onClick(View view) {
				Activity activity = getActivity();
				String currentQuiz = activity.getSharedPreferences(QUIZES_FILE, Context.MODE_PRIVATE).getString(CURRENT_QUIZ, "");
				String currentQuizFile = QUIZES_FILE + "." + currentQuiz;
				CheckBox b = (CheckBox) view;
			    String bText = b.getText().toString();
				boolean bChecked = b.isChecked();
				Log.v(TAG, String.format("QuizFragmentListener:%s.%s.%b.", currentQuiz, bText, bChecked));
				SharedPreferences.Editor userEditor = activity.getSharedPreferences(currentQuizFile, Context.MODE_PRIVATE).edit();
				userEditor.putBoolean(bText, bChecked);
				userEditor.commit();
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);
			Activity activity = getActivity();
			String currentQuiz = activity.getSharedPreferences(QUIZES_FILE, Context.MODE_PRIVATE).getString(CURRENT_QUIZ, "");
			String currentQuizFile = QUIZES_FILE + "." + currentQuiz;
			LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.quiz_ll);
			
			SharedPreferences quizPref = activity.getSharedPreferences(currentQuizFile, Context.MODE_PRIVATE);
			for(String op : operators) {
				CheckBox b = new CheckBox(activity);
				b.setText((CharSequence) op);
				b.setOnClickListener(new QuizFragmentListener());
				b.setChecked(quizPref.getBoolean(op, true));
				ll.addView(b);
			}
			for(Integer i : numbers) {
				CheckBox b = new CheckBox(activity);
				String num = i.toString();
				b.setText((CharSequence) num);
				b.setOnClickListener(new QuizFragmentListener());
				b.setChecked(quizPref.getBoolean(num, true));
				ll.addView(b);
			}
			return rootView;
		}
	}
	
	public void createUserButtonClick (View view) {
		Log.v(TAG, "createUserButtonClick");
		EditText input = (EditText) findViewById(R.id.new_user_input);
		String newUser = input.getText().toString();
		String newUserFile = QUIZES_FILE + "." + newUser;
		
		TextView status = (TextView) findViewById(R.id.status_message);
		if (newUser.equals("")) {
			status.setText(R.string.blank_user_error);
			return;
		}
		if (newUser.equals(NEW_USER)) {
			status.setText(R.string.new_user_error);
			return;
		}
		//NEWREL: Limit the length of the user name
		
		SharedPreferences pref = getSharedPreferences(USERS_FILE, Context.MODE_PRIVATE);
		Set<String> userSet = pref.getStringSet(USER_LIST, new HashSet<String>());
		// NEWREL: Make case insensitive, but preserve case in the set.
		
		if (userSet.contains(newUser)) {
			status.setText(R.string.user_exists_error);
			return;
		}
		
		// Create the preferences file for this user.
		SharedPreferences userPref = getSharedPreferences(newUserFile, Context.MODE_PRIVATE);
		SharedPreferences.Editor userEditor = userPref.edit();
		userEditor.commit();

		// Add the new user to the set of users.
		userSet.add(newUser);
		SharedPreferences.Editor editor = pref.edit();
		editor.putStringSet(USER_LIST, userSet);
		editor.commit();
		
		// Transition back to the User Selection Screen.
		// BUGBUG:?I think this might need to do a pop on the BackStack instead...
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
		SharedPreferences pref = getSharedPreferences(USERS_FILE, Context.MODE_PRIVATE);
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
	
	public static class UserFragment extends Fragment {
		
		public class UserFragmentListener implements View.OnClickListener {

			@Override
			public void onClick(View view) {//NEWREL: might not need this listener anymore.
				Activity activity = getActivity();
				String currentUser = activity.getSharedPreferences(USERS_FILE, Context.MODE_PRIVATE).getString(CURRENT_USER, "");
				CheckBox b = (CheckBox) view;
			    String bText = b.getText().toString();
				boolean bChecked = b.isChecked();
				Log.v(TAG, String.format("UserFragmentListener:%s.%s.%b.", currentUser, bText, bChecked));
				SharedPreferences.Editor userEditor = activity.getSharedPreferences(currentUser, Context.MODE_PRIVATE).edit();
				userEditor.putBoolean(bText, bChecked);
				userEditor.commit();
			}
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_user, container, false);
			String currentUser = getActivity().getSharedPreferences(USERS_FILE, Context.MODE_PRIVATE).getString(CURRENT_USER, "");
			LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.user_ll);
			Activity activity = getActivity();
			
			SharedPreferences userPref = activity.getSharedPreferences(currentUser, Context.MODE_PRIVATE);

			
			// NEWREL: display the score.
			
			return rootView;
		}
	}
}
