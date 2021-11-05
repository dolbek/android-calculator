package com.jessedean.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Math;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //Enums for order of operations and error types
    enum Order {regular, running};
    enum ErrorType {none, input, divZero, negSqrt, other};

    //Textviews to update
    TextView runningTotalDisplay;
    TextView totalDisplay;
    TextView memoryIndicator;

    //Storage variables
    String runningString = "";
    String calculationString = "";
    BigDecimal result;
    BigDecimal memory;
    Order order = Order.regular;
    final int maxDecimals = 14;

    //Error handling variables
    final String ERR = "ERR";
    boolean isError = false;
    ErrorType errorType = ErrorType.none;
    String [] errorMessages;

    String [] buttonText;

    //Symbols that use codes
    final char MULT = '\u00D7';
    final char DIV = '\u00F7';
    final char SUB = '\u2212';
    final char NEG = '-';
    final char SQRT = '\u221A';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runningTotalDisplay = (TextView) findViewById(R.id.runningDisplay);
        totalDisplay = (TextView) findViewById(R.id.display);
        memoryIndicator = (TextView) findViewById(R.id.memoryIndicator);
        errorMessages = getResources().getStringArray(R.array.errorMessages);
        buttonText = getResources().getStringArray(R.array.buttonText);

        setButtons();
    }


    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.buttonEqual) {
            //Perform the calculation
            result = calculate(calculationString);
            setDisplay(formatBigDecimal(result));
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
                    calculationString += NEG;
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
                    calculationString += SUB;
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
                    break;

                //Delete only the most recently added character
                case R.id.buttonBack:
                    if (calculationString != null && calculationString.length() > 0)
                        calculationString = calculationString.substring(0, calculationString.length() - 1);
                    break;

                //Change the order of operations
                case R.id.buttonOrder:
                    if (order == Order.regular)
                        order = Order.running;
                    else
                        order = Order.regular;
                    break;

                //Memory functions
                case R.id.buttonMem:
                    memory = calculate(calculationString);
                    break;
                case R.id.buttonMemAdd:
                    memory = memory.add(calculate(calculationString));
                    break;
                case R.id.buttonMemSub:
                    memory = memory.subtract(calculate(calculationString));
                    break;
                case R.id.buttonMemRecall:
                    calculationString = memory.toString();
                    break;
            }
            //Update the display after each button press
            setDisplay(calculationString);
        }
    }

    //Updates the display
    private void setDisplay(String string) {
        if(!isError) {
            totalDisplay.setText(string);
            runningTotalDisplay.setText(calculationString);
        }
        else {
            String errorString = "";
            switch(errorType) {
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
            isError = false;
            errorType = ErrorType.none;
        }
        if (memory == null)
            memoryIndicator.setVisibility(View.INVISIBLE);
        else
            memoryIndicator.setVisibility(View.VISIBLE);
    }

    //Updates the running display
    private void setRunningDisplay(String str) {
        runningTotalDisplay.setText(str);
    }

    //Performs the calculation on the postfix expression
    private BigDecimal calculate(String calc) {

        //Get the calculation, and convert it to postfix in list form
        LinkedList<String> postfix = generatePostfix(calc);

        BigDecimal finalResult;
        BigDecimal operationResult = BigDecimal.ZERO;
        BigDecimal op1, op2;
        Stack<BigDecimal> stack = new Stack<>();

        //Iterates through each item in the postfix list
        for (String item : postfix) {

            //Push numbers onto the stack
            if(isNumber(item)) {
                stack.push(BigDecimal.valueOf(Double.parseDouble(item)));
            }

            //Square roots have one operand and one operator, so they
            //must be handled separately
            else if(item.charAt(0) == SQRT) {

                try {
                    op1 = stack.pop();
                    if(op1.doubleValue() < BigDecimal.ZERO.doubleValue()) {
                        isError = true;
                        errorType = ErrorType.negSqrt;
                        return BigDecimal.ZERO;
                    }

                    //Calculate square root
                    BigDecimal x0 = BigDecimal.ZERO;
                    BigDecimal x1 = new BigDecimal(Math.sqrt(op1.doubleValue()));
                    while (!x0.equals(x1)) {
                        x0 = x1;
                        x1 = op1.divide(x0, maxDecimals + 1, RoundingMode.HALF_UP);
                        x1 = x1.add(x0);
                        x1 = x1.divide(new BigDecimal(2), maxDecimals + 1, RoundingMode.HALF_UP);
                    }
                    stack.push(x1);
                }
                catch(EmptyStackException e) {
                    isError = true;
                    errorType = ErrorType.input;
                    return BigDecimal.ZERO;
                }
            }

            //If there is an operator, pop two operands off the stack and
            //perform the calculation.
            else {
                Character c = item.charAt(0);
                try {
                    op2 = stack.pop();
                    op1 = stack.pop();
                    switch (c) {

                        //BigDecimal cannot do fractional exponent. In the event of a fractional
                        //second operator, we forgo the precision and use double values.
                        case '^':
                            if (isMathInteger(op2))
                                operationResult = op1.pow(op2.intValue());
                            else
                                operationResult = BigDecimal.valueOf(Math.pow(op1.doubleValue(), op2.doubleValue()));
                            break;
                        case MULT:
                            operationResult = op1.multiply(op2);
                            break;
                        case DIV:
                            if(op2.doubleValue() == 0) {
                                isError = true;
                                errorType = ErrorType.divZero;
                                return BigDecimal.ZERO;
                            }
                            else
                                operationResult = op1.divide(op2);
                            break;
                        case '+':
                            operationResult = op1.add(op2);
                            break;
                        case SUB:
                            operationResult = op1.subtract(op2);
                            break;
                    }
                    stack.push(operationResult);
                }
                catch(EmptyStackException e) {
                    isError = true;
                    errorType = ErrorType.input;
                    return BigDecimal.ZERO;
                }
            }
        }
        try {
            finalResult = stack.pop();
            return finalResult;
        }
        catch(EmptyStackException e) {
            isError = true;
            errorType = ErrorType.other;
            return BigDecimal.ZERO;
        }
    }

    //Returns true if the given string represents a correctly
    //formatted number.
    private boolean isNumber(String str)
    {
        try {
            Double.parseDouble(str);
            return true;
        }
        catch(NumberFormatException e) {
                return false;
        }
    }

    //Converts the given infix notation expression to postfix
    // and returns the result in a linked list.
    private LinkedList<String> generatePostfix(String str) {

        //The list to store the infix expression
        LinkedList<String> postfix = new LinkedList<>();

        //Stack for temporarily holding operators
        Stack<Character> stack = new Stack<>();
        int length = str.length();

        //Loops through each character in the expression, sorting them to be added to the
        //postfix list.
        for(int i = 0; i < length; i++)
        {
            Character c = str.charAt(i);

            //If a character is a digit that is followed by more digits, (ie a multi-digit
            //number) it is divided into a substring to be added to the list as a whole.
            //Else, single-digit numbers are added as-is.
            //The right side of the first OR condition is as follows: "-" is considered a digit if
            //it is the first character, or if it follows an operator. Otherwise,
            // it must be a subtraction operator.
            if(Character.isDigit(c) || c == '.' || (c == NEG && (i == 0 || !Character.isDigit(str.charAt(i-1)))))
            {
                int j = i+1;
                if(j < length && (Character.isDigit(str.charAt(j)) || Character.compare(str.charAt(j), '.') == 0)) {
                    while (j < length && (Character.isDigit(str.charAt(j)) || str.charAt(j) == '.'))
                        j++;
                    String number = str.substring(i, j);
                    i = j-1;
                    postfix.add(number);
                }
                else
                    postfix.add(c.toString());
            }
            //Open parentheses are pushed onto the stack
            else if(c == '(')
                stack.push(c);

            //On a close parenthesis, pop the contents of the stack and add it to the list
            //until the open parenthesis is reached, which is discarded.
            else if(c == ')')
            {
                while (stack.peek() != '(')
                    postfix.add(stack.pop().toString());
                stack.pop();
            }

            //Operators are pushed onto the stack if it is empty, or if the operator on top of the stack
            //has a lower precedence than the new one. If not, the higher precedence operator is popped
            // and added to the list, after which the new operator is pushed to the stack.
            else
            {
                while(!stack.empty() && operatorPrecedence(c) <= operatorPrecedence(stack.peek()))
                {
                    postfix.add(stack.pop().toString());
                }
                stack.push(c);
            }
        }

        //The remaining operators on the stack are popped and added to the list.
        while(!stack.empty())
            postfix.add(stack.pop().toString());

        //Returns the postfix list.
        return postfix;
    }

    //Returns a numeric representation of an operator's precedence
    private int operatorPrecedence(char op) {
        if(op == '^' || op == SQRT)
            return 3;
        else if(op == MULT || op == DIV)
            return 2;
        else if(op == '+' || op == SUB)
            return 1;
        else
            return 0;
    }

    //Returns true if the given BigDecimal is an integer in the mathematical sense
    private boolean isMathInteger(BigDecimal bd) {
        return bd.scale() <= 0 || bd.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }

    private String formatBigDecimal(BigDecimal number) {
        number = number.stripTrailingZeros();
        NumberFormat format = new DecimalFormat("0.0E0");
        format.setRoundingMode(RoundingMode.HALF_UP);
        format.setMinimumFractionDigits((number.scale() < 0) ? number.precision() : number.scale());

        if(number.precision() > maxDecimals)
            return format.format(number);
        else
            return number.toString();
    }

    //Set up buttons
    private void setButtons() {

        //Operands
        Button button0 = (Button) findViewById(R.id.button0);
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button3 = (Button) findViewById(R.id.button3);
        Button button4 = (Button) findViewById(R.id.button4);
        Button button5 = (Button) findViewById(R.id.button5);
        Button button6 = (Button) findViewById(R.id.button6);
        Button button7 = (Button) findViewById(R.id.button7);
        Button button8 = (Button) findViewById(R.id.button8);
        Button button9 = (Button) findViewById(R.id.button9);
        //Operand modifiers
        Button buttonDecimal = (Button) findViewById(R.id.buttonDecimal);
        Button buttonNeg = (Button) findViewById(R.id.buttonNeg);
        Button buttonParLeft = (Button) findViewById(R.id.buttonParLeft);
        Button buttonParRight = (Button) findViewById(R.id.buttonParRight);
        //Operators
        Button buttonAdd = (Button) findViewById(R.id.buttonAdd);
        Button buttonSub = (Button) findViewById(R.id.buttonSub);
        Button buttonMult = (Button) findViewById(R.id.buttonMult);
        Button buttonDiv = (Button) findViewById(R.id.buttonDiv);
        Button buttonEqual = (Button) findViewById(R.id.buttonEqual);
        Button buttonExp = (Button) findViewById(R.id.buttonExp);
        Button buttonSqrt = (Button) findViewById(R.id.buttonSqrt);
        //Calculator functions
        Button buttonClear = (Button) findViewById(R.id.buttonClear);
        Button buttonBack = (Button) findViewById(R.id.buttonBack);
        Button buttonOrder = (Button) findViewById(R.id.buttonOrder);
        //Memory functions
        Button buttonMem = (Button) findViewById(R.id.buttonMem);
        Button buttonMemAdd = (Button) findViewById(R.id.buttonMemAdd);
        Button buttonMemSub = (Button) findViewById(R.id.buttonMemSub);
        Button buttonMemRecall = (Button) findViewById(R.id.buttonMemRecall);

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