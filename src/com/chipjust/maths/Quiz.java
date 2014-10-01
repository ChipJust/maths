package com.chipjust.maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Quiz extends Activity {
	
	private static final String TAG = "QuizActivity";
	
	// args for the fragment
	private static final String QUESTION = "question";
	private static final String BUTTON_1 = "button_1";
	private static final String BUTTON_2 = "button_2";
	private static final String BUTTON_3 = "button_3";
	private static final String BUTTON_4 = "button_4";
	
	private static final List<String> operators = Arrays.asList("+", "-", "x", "/");
	private static final List<Integer> numbers = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
	private int operand1 = 0;
	private int operand2 = 0;
	private String operator = "?";
	private int answer = 0;
	
	private int button_1 = 0;
	private int button_2 = 0;
	private int button_3 = 0;
	private int button_4 = 0;
	
	// We have one button handler, and we pause between clicks. We only allow one click per question.
	// So this boolean gates the buttonClick function to ignore new input until we draw a new question (fragment)
	// and reset this flag.
	private boolean clicked = false;
	
	Handler handler;
	Runnable runNewQuestion;
	
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

	private Bundle setNewQuestion() {
		int i;
		Random rand = new Random();

		// NEWREL:we could filter the list of operands by the user preferences
		// NEWREL:we could filter the list of numbers by the user preferences

		// Operator
		List<String> myOperators = new ArrayList<String>();
		myOperators.addAll(operators);
		i = rand.nextInt(myOperators.size());
		operator = myOperators.get(i);
		myOperators.remove(i);
		
		// Operands
		i = rand.nextInt(numbers.size());
		operand1 = numbers.get(i);
		i = rand.nextInt(numbers.size());
		operand2 = numbers.get(i);
		
		// Right Answer
		answer = evaluate (operator, operand1, operand2);
		
		// Wrong Answers
		Set<Integer> wrongAnswers = new HashSet<Integer>();
		Integer wrongAnswer;
		for (String op : myOperators) {
			wrongAnswer = evaluate (op, operand1, operand2);
			if (wrongAnswer != answer) {
				wrongAnswers.add(wrongAnswer);
			}
		}
		for (Integer delta : new Integer[] {1, 2, 10}) {
			wrongAnswer = evaluate (operator, operand1+delta, operand2);
			if (wrongAnswer != answer) {
				wrongAnswers.add(wrongAnswer);
			}
			wrongAnswer = evaluate (operator, operand1, operand2+delta);
			if (wrongAnswer != answer) {
				wrongAnswers.add(wrongAnswer);
			}
			wrongAnswer = evaluate (operator, operand1-delta, operand2);
			if (wrongAnswer != answer) {
				wrongAnswers.add(wrongAnswer);
			}
			wrongAnswer = evaluate (operator, operand1, operand2-delta);
			if (wrongAnswer != answer) {
				wrongAnswers.add(wrongAnswer);
			}
		}
		
		// Shuffle answers and assign to buttons
		List<Integer> myAnswers = new ArrayList<Integer>();
		myAnswers.addAll(wrongAnswers);
		Collections.shuffle(myAnswers);
		myAnswers.subList(3, myAnswers.size()).clear();
		myAnswers.add(answer); // add the correct answer last
		Collections.shuffle(myAnswers);
		button_1 = myAnswers.get(0);
		button_2 = myAnswers.get(1);
		button_3 = myAnswers.get(2);
		button_4 = myAnswers.get(3);
		
		// Fill in the bundle with question and answers
		Bundle args = new Bundle();
		args.putString(QUESTION, String.format("%d %s %d", operand1, operator, operand2));
		args.putString(BUTTON_1, String.format("%d", button_1));
		args.putString(BUTTON_2, String.format("%d", button_2));
		args.putString(BUTTON_3, String.format("%d", button_3));
		args.putString(BUTTON_4, String.format("%d", button_4));
		
		return args;
	}

	private void newQuestion() {
		FragmentTransaction t = getFragmentManager().beginTransaction();
		Fragment fragment = new QuestionFragment();
		Bundle args = setNewQuestion();
		fragment.setArguments(args);
		t.replace(R.id.container, fragment);
		t.commit();
		clicked = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handler = new Handler();
		runNewQuestion = new Runnable() {
			public void run () {
				newQuestion();
			}};
		if (savedInstanceState == null) {
			newQuestion();
		}
	}
	
	// There is only one handler for all the question buttons.
	public void buttonClick (View view) {
		if (clicked) {
			return;
		}
		clicked = true; // no more clicks
		// NEWREL:This button binding is not good...refactor to something less coupled.
		Integer userAnswer;
		switch(view.getId()) {
		case R.id.button_1:
			userAnswer = button_1;
			break;
		case R.id.button_2:
			userAnswer = button_2;
			break;
		case R.id.button_3:
			userAnswer = button_3;
			break;
		case R.id.button_4:
			userAnswer = button_4;
			break;
		default:
			Log.e(TAG, "buttonClick: Unknown Button");
			userAnswer = 0;
			break;
		}
		if (answer == userAnswer) {
			view.setBackgroundColor(Color.parseColor("#30E873"));
		} else {
			view.setBackgroundColor(Color.parseColor("#FF1028"));
		}
		handler.postDelayed(runNewQuestion, 750); // NEWREL: make this delay configurable in user preferences
	}

	public static class QuestionFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_question, container, false);
			Bundle args = getArguments();
			((TextView) rootView.findViewById(R.id.question)).setText(args.getString(QUESTION, "No Question"));
			((Button) rootView.findViewById(R.id.button_1)).setText(args.getString(BUTTON_1, "No Button 1"));
			((Button) rootView.findViewById(R.id.button_2)).setText(args.getString(BUTTON_2, "No Button 2"));
			((Button) rootView.findViewById(R.id.button_3)).setText(args.getString(BUTTON_3, "No Button 3"));
			((Button) rootView.findViewById(R.id.button_4)).setText(args.getString(BUTTON_4, "No Button 4"));
			return rootView;
		}
	}
}