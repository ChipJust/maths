package com.chipjust.maths;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.chipjust.maths.MainActivity.UserSelectionFragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class QuizActivity extends MathsActivity {
	
	// args for the fragment
	private static final String QUESTION = "question";
	private static final String BUTTONS = "buttons";
	private Map<Integer, Integer> buttons;
	
	private int operand1 = 0;
	private int operand2 = 0;
	private String operator = "?";
	private int answer = 0;
	private static final String COLOR_GREEN = "#30E873";
	private static final String COLOR_RED = "#FF1028";
	
	
	// We have one button handler, and we pause between clicks. We only allow one click per question.
	// So this boolean gates the buttonClick function to ignore new input until we draw a new question (fragment)
	// and reset this flag.
	private boolean clicked = false;
	
	Handler handler;
	Runnable runNewQuestion;
	Runnable runShowScore;
	
	long questionStart;
	long quizTimeRemaining;
	
	int score;
	int questionCount;
	int correctCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// BUGBUG: I am pretty sure that I am not handling savedInstanceState correctly. Try changing to landscape during a quiz.
		handler = new Handler();
		runNewQuestion = new Runnable() {
			public void run () {
				newQuestion();
			}};
		runShowScore = new Runnable() {
			public void run () {
				showScore();
			}};
		buttons = new HashMap<Integer, Integer>();
		quizTimeRemaining = quizTime;
		score = 0;
		if (savedInstanceState == null) {
			newQuestion();
		}
	}

	private Integer evaluate (String myOperator, Integer myOperand1, Integer myOperand2) {
		switch (myOperator) {
		case "+":
			return myOperand1 + myOperand2;
		case "-":
			return myOperand1 - myOperand2;
		case "x":
			return myOperand1 * myOperand2;
		case "/":
			if (myOperand2 == 0) {
				break;
			}
			return myOperand1 / myOperand2;
		default:
			break;
		}
		return 0;
	}

	private List<Integer> generateWrongAnswers (List<String> myOperators, int operand1, int operand2, int numberOfWrongAnswers) {
		// NEWREL: maybe add a transpose wrong answer, so if the answer was 81 we should have a wrong answer of 18 available - my children do this...
		Set<Integer> wrongAnswers = new HashSet<Integer>();
		
		Integer wrongAnswer;
		for (String op : myOperators) {
			for (Integer delta : new Integer[] {0, 1, -1, 2, -2}) {
				wrongAnswer = evaluate (op, operand1+delta, operand2);
				if (wrongAnswer != answer && wrongAnswer > 0) {
					wrongAnswers.add(wrongAnswer);
				}
				wrongAnswer = evaluate (op, operand1, operand2+delta);
				if (wrongAnswer != answer && wrongAnswer > 0) {
					wrongAnswers.add(wrongAnswer);
				}
			}
		}
		
		List<Integer> wrongAnswersList = new ArrayList<Integer>();
		wrongAnswersList.addAll(wrongAnswers);
		Collections.shuffle(wrongAnswersList);
		//BUGBUG:Check to see if the list is long enough
		wrongAnswersList.subList(numberOfWrongAnswers, wrongAnswersList.size()).clear();	
		return wrongAnswersList;
	}
	
	private Bundle setNewQuestion() {
		Random rand = new Random();
		//NEWREL: move these to the class
		String currentUser = getSharedPreferences(USERS_FILE, Context.MODE_PRIVATE).getString(CURRENT_USER, "");
		SharedPreferences userPref = getSharedPreferences(currentUser, Context.MODE_PRIVATE);

		// Operator
		List<String> myOperators = new ArrayList<String>();
		for (String op : operators) {
			if (userPref.getBoolean(op, true)) {
				myOperators.add(op);
			}
		}
		//BUGBUG: what if there are no operators selected?
		operator = myOperators.get(rand.nextInt(myOperators.size()));
		
		// Operands
		List<Integer> myNumbers = new ArrayList<Integer>();
		for (Integer i : numbers) {
			if (userPref.getBoolean(i.toString(), true)) {
				myNumbers.add(i);
			}
		}
		//BUGBUG: What if there are no numbers selected?
		int num1 =  myNumbers.get(rand.nextInt(myNumbers.size()));
		int num2 =  myNumbers.get(rand.nextInt(myNumbers.size()));
		switch (operator) {
		case "/":
			operand1 = num1 * num2;
			operand2 = num2;
			break;
		case "-":
			operand1 = num1 + num2;
			operand2 = num2;
			break;
		default:
			operand1 = num1;
			operand2 = num2;
			break;
		}
		
		// Right Answer
		answer = evaluate (operator, operand1, operand2);
		
		// Shuffle answers and assign to buttons
		List<Integer> myAnswers = new ArrayList<Integer>();
		myAnswers.addAll(generateWrongAnswers(myOperators, operand1, operand2, 3));
		myAnswers.add(answer);
		Collections.shuffle(myAnswers);
		buttons.put(R.id.button_1, myAnswers.get(0));
		buttons.put(R.id.button_2, myAnswers.get(1));
		buttons.put(R.id.button_3, myAnswers.get(2));
		buttons.put(R.id.button_4, myAnswers.get(3));
		
		// Fill in the bundle with question and answers
		Bundle args = new Bundle();
		args.putString(QUESTION, String.format("%d %s %d", operand1, operator, operand2));
		args.putSerializable(BUTTONS, (Serializable) buttons);
		return args;
	}
	
	private void updateScore(long elapsedTime, boolean correct) {
		questionCount += 1;
		if (correct) {
			if (elapsedTime < 400) {
				score += 5;
			} else if (elapsedTime < 800) {
				score += 4;
			} else if (elapsedTime < 1600) {
				score += 3;
			} else if (elapsedTime < 3200) {
				score += 2;
			} else {
				score += 1;
			}
			correctCount += 1;
		} else {
			score -= 1;
		}
		Log.v(TAG, String.format("updateScore(%d, %b): questionCount=%d, correctCount=%d, score=%d", elapsedTime, correct, questionCount, correctCount, score));
	}

	private void newQuestion() {
		FragmentTransaction t = getFragmentManager().beginTransaction();
		Fragment fragment = new QuestionFragment();
		Bundle args = setNewQuestion();
		fragment.setArguments(args);
		t.replace(R.id.container, fragment);
		t.commit();
		clicked = false;
		questionStart = SystemClock.uptimeMillis ();
	}
	
	private void showScore() {
		getFragmentManager().beginTransaction().replace(R.id.container, new ScoreFragment()).commit();
	}

	// There is only one handler for all the question buttons.
	public void buttonClick (View view) {
		if (clicked) {
			return;
		}
		clicked = true; // no more clicks
		long elapsedTime = SystemClock.uptimeMillis () - questionStart;
		Log.v(TAG, String.format("elapsed:%dms remaining:%dms", elapsedTime, quizTimeRemaining));

		Integer userAnswer = buttons.get(view.getId());
		updateScore (elapsedTime, (answer == userAnswer));
		if (answer == userAnswer) {
			view.setBackgroundColor(Color.parseColor(COLOR_GREEN));
		} else {
			view.setBackgroundColor(Color.parseColor(COLOR_RED));
			// find the right answer and turn than button green.
			for (Object button : buttons.keySet()) {
				if ((int) buttons.get(button) == answer) {
					findViewById((int) button).setBackgroundColor(Color.parseColor(COLOR_GREEN));
					break;
				}
			}
		}
		// NEWREL: log the question, answer, userAnswer and time-consumed-on-this-question in history
		if (quizTimeRemaining > elapsedTime) {
			quizTimeRemaining -= elapsedTime;
			handler.postDelayed(runNewQuestion, 750); // NEWREL: make this delay configurable in user preferences
			return;
		}
		
		// Out of time, report the results.
		handler.postDelayed(runShowScore, 750); // NEWREL: make this delay configurable in user preferences
	}

	public static class QuestionFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_question, container, false);
			Bundle args = getArguments();
			((TextView) rootView.findViewById(R.id.question)).setText(args.getString(QUESTION, "No Question"));
			Map<Integer, Integer> buttonMap = (Map<Integer, Integer>) args.getSerializable(BUTTONS);
			for (Object button : buttonMap.keySet()) {
				((Button) rootView.findViewById((int) button)).setText(String.format("%d", buttonMap.get(button)));
			}
			return rootView;
		}
	}
	
	public static class ScoreFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_score, container, false);
			return rootView;
		}
	}
}