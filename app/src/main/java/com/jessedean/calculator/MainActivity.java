package com.jessedean.calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Calculator calculator;

    //Text views to update
    TextView runningTotalDisplay;
    TextView totalDisplay;
    TextView memoryIndicator;
    TextView orderIndicator;
    Resources res;

    //Storage variables
    String calculationString = "";
    String result = "";

    //State variables
    boolean isRegularOrder;
    boolean hasMemory;

    final int maxDecimals = 14;

    String [] errorMessages;
    final String ERR = "ERR";
    String [] buttonText;

    //Buttons that need to be accessed for running order of operations
    Button buttonParLeft;
    Button buttonParRight;
    Button buttonOrder;

    //Symbols that use codes
    final char MULT = '\u00D7';
    final char DIV = '\u00F7';
    final char SQRT = '\u221A';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runningTotalDisplay = findViewById(R.id.runningDisplay);
        totalDisplay = findViewById(R.id.display);
        memoryIndicator = findViewById(R.id.memoryIndicator);
        orderIndicator = findViewById(R.id.orderIndicator);
        res = getResources();
        errorMessages = getResources().getStringArray(R.array.errorMessages);
        buttonText = getResources().getStringArray(R.array.buttonText);

        calculator = new Calculator(maxDecimals, MULT, DIV, SQRT);

        setButtons();
        buttonParLeft = findViewById(R.id.buttonParLeft);
        buttonParRight = findViewById(R.id.buttonParRight);
        buttonOrder = findViewById(R.id.buttonOrder);
        memoryIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        calculationString = savedInstanceState.getString("calculationString");
        isRegularOrder = savedInstanceState.getBoolean("order");
        hasMemory = savedInstanceState.getBoolean("hasMem");
        result = savedInstanceState.getString("result");

        if(hasMemory)
            calculator.setMemory(savedInstanceState.getString("mem"));

        if(isRegularOrder)
            setOrder(Calculator.Order.regular);
        else
            setOrder(Calculator.Order.running);

        if(!(calculationString == ""))
            setDisplay(calculationString);
        else
            setDisplay(result);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("calculationString", calculationString);
        outState.putString("result", result);

        if(calculator.getOrder() == Calculator.Order.regular)
            isRegularOrder = true;
        else
            isRegularOrder = false;
        outState.putBoolean("order", isRegularOrder);

        if(calculator.hasMemory()) {
            hasMemory = true;
            outState.putString("mem", calculator.getMemory());
        }
        else
            hasMemory = false;
        outState.putBoolean("hasMem", hasMemory);
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.buttonEqual) {
            //Perform the calculation
            result = calculator.calculate(calculationString);
            setDisplay(result);
            calculationString = "";
        }
        else {
            //On click, add the appropriate character to the string storing the calculation.
            switch (view.getId()) {
                case R.id.button0:
                    calculationString += '0';
                    break;
                case R.id.button1:
                    calculationString += '1';
                    break;
                case R.id.button2:
                    calculationString += '2';
                    break;
                case R.id.button3:
                    calculationString += '3';
                    break;
                case R.id.button4:
                    calculationString += '4';
                    break;
                case R.id.button5:
                    calculationString += '5';
                    break;
                case R.id.button6:
                    calculationString += '6';
                    break;
                case R.id.button7:
                    calculationString += '7';
                    break;
                case R.id.button8:
                    calculationString += '8';
                    break;
                case R.id.button9:
                    calculationString += '9';
                    break;
                case R.id.buttonDecimal:
                    calculationString += '.';
                    break;
                case R.id.buttonNeg:
                    calculationString += '-';
                    break;
                case R.id.buttonParLeft:
                    calculationString += '(';
                    break;
                case R.id.buttonParRight:
                    calculationString += ')';
                    break;
                case R.id.buttonAdd:
                    calculationString += '+';
                    break;
                case R.id.buttonSub:
                    calculationString += '-';
                    break;
                case R.id.buttonMult:
                    calculationString += MULT;
                    break;
                case R.id.buttonDiv:
                    calculationString += DIV;
                    break;
                case R.id.buttonExp:
                    calculationString += '^';
                    break;
                case R.id.buttonSqrt:
                    calculationString += SQRT;
                    break;

                //Clear the calculation string
                case R.id.buttonClear:
                    calculationString = "";
                    result = "";
                    break;

                //Delete only the most recently added character
                case R.id.buttonBack:
                    if (calculationString != null && calculationString.length() > 0)
                        calculationString = calculationString.substring(0, calculationString.length() - 1);
                    break;

                //Change the order of operations
                case R.id.buttonOrder:
                    toggleOrder();
                    break;

                //Memory functions
                case R.id.buttonMem:
                    if(calculationString != "")
                        calculator.setMemory(calculationString);
                    else if(result != "")
                        calculator.setMemory(result);
                    break;
                case R.id.buttonMemAdd:
                    if(calculationString != "")
                        calculator.memoryAdd(calculationString);
                    else if(result != "")
                        calculator.memoryAdd(result);
                    break;
                case R.id.buttonMemSub:
                    if(calculationString != "")
                        calculator.memorySub(calculationString);
                    else if(result != "")
                        calculator.memorySub(result);
                    break;
                case R.id.buttonMemRecall:
                    calculationString += calculator.getMemory();
                    break;
            }
            //Update the display after each button press
            setDisplay(calculationString);
        }
    }

    //Updates the display
    private void setDisplay(String string) {
        String running = "";
        if(!calculator.hasError()) {
            totalDisplay.setText(string);
            runningTotalDisplay.setText(calculationString);

            if(calculator.getOrder() == Calculator.Order.running) {
                orderIndicator.setText(res.getString(R.string.running));
                running = calculator.calculate(calculationString);
                if(calculator.hasError())
                    calculator.clearError();
                else
                    setRunningDisplay(running);
            }
            else
                orderIndicator.setText(res.getString(R.string.regular));
        }
        else {
            String errorString = "";
            switch(calculator.getErrorType()) {
                case input:
                    errorString = errorMessages[1];
                    break;
                case divZero:
                    errorString = errorMessages[2];
                    break;
                case negSqrt:
                    errorString = errorMessages[3];
                    break;
                case other:
                    errorString = errorMessages[4];
                    break;
                default:
                    errorString = errorMessages[5];
            }
            totalDisplay.setText(ERR);
            Context context = getApplicationContext();
            Toast errorMessage = Toast.makeText(context, "Error: " + errorString, Toast.LENGTH_SHORT);
            errorMessage.show();
            calculator.clearError();
        }
        if (!calculator.hasMemory())
            memoryIndicator.setVisibility(View.INVISIBLE);
        else
            memoryIndicator.setVisibility(View.VISIBLE);
    }

    //Updates the running display
    private void setRunningDisplay(String str) {
        runningTotalDisplay.setText(str);
    }

    //Toggles the order of operations
    private void toggleOrder() {
        if(calculator.getOrder() == Calculator.Order.regular)
            setOrder(Calculator.Order.running);
        else
            setOrder(Calculator.Order.regular);
    }

    //Sets the order of operations explicitly
    private void setOrder(Calculator.Order ord) {
        if(ord == Calculator.Order.running) {
            buttonParLeft.setEnabled(false);
            buttonParRight.setEnabled(false);
            buttonOrder.setText(res.getString(R.string.regular));
            calculator.setOrder(Calculator.Order.running);
        }
        else {
            buttonParLeft.setEnabled(true);
            buttonParRight.setEnabled(true);
            buttonOrder.setText(res.getString(R.string.running));
            calculator.setOrder(Calculator.Order.regular);
        }
    }

    //Set up buttons
    private void setButtons() {

        //Operands
        Button button0 = findViewById(R.id.button0);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        Button button5 = findViewById(R.id.button5);
        Button button6 = findViewById(R.id.button6);
        Button button7 = findViewById(R.id.button7);
        Button button8 = findViewById(R.id.button8);
        Button button9 = findViewById(R.id.button9);
        //Operand modifiers
        Button buttonDecimal = findViewById(R.id.buttonDecimal);
        Button buttonNeg = findViewById(R.id.buttonNeg);
        buttonParLeft = findViewById(R.id.buttonParLeft);
        buttonParRight = findViewById(R.id.buttonParRight);
        //Operators
        Button buttonAdd = findViewById(R.id.buttonAdd);
        Button buttonSub = findViewById(R.id.buttonSub);
        Button buttonMult = findViewById(R.id.buttonMult);
        Button buttonDiv = findViewById(R.id.buttonDiv);
        Button buttonEqual = findViewById(R.id.buttonEqual);
        Button buttonExp = findViewById(R.id.buttonExp);
        Button buttonSqrt = findViewById(R.id.buttonSqrt);
        //Calculator functions
        Button buttonClear = findViewById(R.id.buttonClear);
        Button buttonBack = findViewById(R.id.buttonBack);
        buttonOrder = findViewById(R.id.buttonOrder);
        //Memory functions
        Button buttonMem = findViewById(R.id.buttonMem);
        Button buttonMemAdd = findViewById(R.id.buttonMemAdd);
        Button buttonMemSub = findViewById(R.id.buttonMemSub);
        Button buttonMemRecall = findViewById(R.id.buttonMemRecall);

        Button[] buttons = {button0, button1, button2, button3, button4, button5, button6, button7,
        button8, button9, buttonDecimal, buttonNeg, buttonParLeft, buttonParRight, buttonAdd,
        buttonSub, buttonMult, buttonDiv, buttonEqual, buttonExp, buttonSqrt, buttonClear, buttonBack,
        buttonOrder, buttonMem, buttonMemAdd, buttonMemSub, buttonMemRecall};

        for(int i = 0; i < buttons.length; i++)
        {
            buttons[i].setOnClickListener(this);
            buttons[i].setText(buttonText[i]);
        }

    }
}