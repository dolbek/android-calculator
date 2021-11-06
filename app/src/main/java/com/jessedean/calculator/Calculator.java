package com.jessedean.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import dalvik.system.DexClassLoader;

public class Calculator {

    private char mult;
    private char div;
    private char sqrt;

    //Enums for order of operations and error types
    enum Order {regular, running}
    enum ErrorType {none, input, divZero, negSqrt, other, memory}

    private ErrorType errorType;
    private Order order;
    private int maxDecimals;
    private BigDecimal memory;

    //Various state flags
    private boolean isError = false;
    private boolean memoryModified = false;
    private boolean disregardCalc = false;

    //Constructor
    public Calculator(int maxDec, char m, char d, char sq) {
        maxDecimals = maxDec;
        errorType = ErrorType.none;
        mult = m;
        div = d;
        sqrt = sq;
        order = Order.regular;
        memory = BigDecimal.ZERO;
    }

    //Converts string input to a postfix list, and passes it
    //to the calculation method.
    public String calculate(String calc) {
        BigDecimal result = calculate(generatePostfix(calc));
        if(disregardCalc) {
            disregardCalc = false;
            return calc;
        }
        else
            return formatBigDecimal(result);
    }


    //Performs evaluation of a postfix expression
    private BigDecimal calculate(List<String> postfix) {

        BigDecimal finalResult;
        BigDecimal operationResult = BigDecimal.ZERO;
        BigDecimal op1, op2;
        Stack<BigDecimal> stack = new Stack<>();

        //Iterates through each item in the postfix list
        for (int i = 0; i < postfix.size(); i++) {

            String item = postfix.get(i);

            //Push numbers onto the stack
            if(isNumber(item)) {
                stack.push(BigDecimal.valueOf(Double.parseDouble(item)));
            }

            //Square roots have one operand and one operator, so they
            //must be handled separately
            else if(item.charAt(0) == sqrt) {

                //A running calculation style of order of operations doesn't work with
                //the same methods of square roots, as it is a unary operator, so the
                //running calculations try to calculate before there is sufficient input.
                //This will always cause an input error before a square root sign, which
                //is addressed here.
                if(getOrder() == Order.running && errorType == ErrorType.input) {
                    clearError();

                    //If the square root sign is the last item on the list and is not preceded by
                    //a number in the postfix list, set the flag to disregard the calculation for
                    //now, and return an arbitrary value.
                    if(postfix.get(postfix.size()-1).charAt(0) == sqrt && !isNumber(postfix.get(postfix.size()-2))) {
                        disregardCalc = true;
                        return BigDecimal.ZERO;
                    }

                    //Split the operation into three, with the square root operation marking the divide.
                    LinkedList<String> subListA;
                    LinkedList<String> subListB;
                    LinkedList<String> subListC;
                    String lastOperator = "";

                    subListA = new LinkedList<>(postfix.subList(0, i-1));
                    subListB = new LinkedList<>(postfix.subList(i-1, i+1));

                    //If the first half is greater than three items, there will be one operator
                    //that needs to be moved to the end of the expression.
                    if(!isNumber(postfix.get(i-2))) {
                        lastOperator = subListA.remove(subListA.size()-1);
                    }

                    //Calculate the result of the square root
                    BigDecimal subCalc = calculate(subListB);

                    //Add the result to the other half
                    subListA.add(subCalc.toPlainString());

                    //Add back the removed operator, if there is one
                    subListA.add(lastOperator);

                    //If the list goes beyond the square root sign, the remaining end is made into
                    //a third list, to be added back one the square root in the middle has been
                    //evaluated.
                    if(postfix.size() > i+1) {
                        subListC = new LinkedList<>(postfix.subList(i + 1, postfix.size()));
                        subListA.addAll(subListC);
                    }

                    //Push the result onto the stack. This set of operations bypasses the regular
                    //logic, as the evaluation took place in recursive calls to this function, so
                    //we break out of the loop.
                    stack.push(calculate(subListA));
                    break;
                }

                try {
                    op1 = stack.pop();

                    //Prevent square roots of negative numbers
                    if(op1.doubleValue() < BigDecimal.ZERO.doubleValue()) {
                        isError = true;
                        errorType = ErrorType.negSqrt;
                    }
                    else {
                        //Calculate square root
                        BigDecimal x0 = BigDecimal.ZERO;
                        BigDecimal x1 = BigDecimal.valueOf(Math.sqrt(op1.doubleValue()));
                        while (!x0.equals(x1)) {
                            x0 = x1;
                            x1 = op1.divide(x0, maxDecimals + 1, RoundingMode.HALF_UP);
                            x1 = x1.add(x0);
                            x1 = x1.divide(new BigDecimal(2), maxDecimals + 1, RoundingMode.HALF_UP);
                        }
                        stack.push(x1);
                    }
                }
                catch(EmptyStackException e) {
                    isError = true;
                    errorType = ErrorType.input;
                }
            }

            //If there is an operator, pop two operands off the stack and
            //perform the calculation.
            else {
                char c = item.charAt(0);
                try {
                    op2 = stack.pop();
                    op1 = stack.pop();
                    //BigDecimal cannot do fractional exponent. In the event of a fractional
                    //second operator, we forgo the precision and use double values.
                    if (c == '^') {
                        if (isMathInteger(op2))
                            operationResult = op1.pow(op2.intValue());
                        else
                            operationResult = BigDecimal.valueOf(Math.pow(op1.doubleValue(), op2.doubleValue()));
                    }
                    else if (c == mult) {
                        operationResult = op1.multiply(op2);
                    }
                    else if (c == div) {
                        if (op2.doubleValue() == 0) {
                            isError = true;
                            errorType = ErrorType.divZero;
                        }
                        else
                            operationResult = op1.divide(op2, maxDecimals, RoundingMode.HALF_UP);
                    }
                    else if (c == '+') {
                        operationResult = op1.add(op2);
                    }
                    else if (c == '-') {
                        operationResult = op1.subtract(op2);
                    }
                    stack.push(operationResult);
                }
                catch(EmptyStackException e) {
                    isError = true;
                    errorType = ErrorType.input;
                }
            }
        }
        if(isError)
            return BigDecimal.ZERO;
        else {
            try {
                finalResult = stack.pop();
            } catch (EmptyStackException e) {
                isError = true;
                errorType = ErrorType.other;
                finalResult = BigDecimal.ZERO;
            }
            return finalResult;
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
    //and returns the result in a linked list.
    private LinkedList<String> generatePostfix(String str) {

        //The list to store the infix expression
        LinkedList<String> postfix = new LinkedList<>();

        //Stack for temporarily holding operators
        Stack<Character> stack = new Stack<>();

        int length;

        if(str != null)
            length = str.length();
        else
            length = 0;

        //Loops through each character in the expression, sorting them to be added to the
        //postfix list.
        for(int i = 0; i < length; i++)
        {
            char c = str.charAt(i);

            //If a character is a digit that is followed by more digits, (ie a multi-digit
            //number) it is divided into a substring to be added to the list as a whole.
            //Else, single-digit numbers are added as-is.
            //The right side of the first OR condition is as follows: "-" is considered a digit if
            //it is the first character, or if it follows an operator. Otherwise,
            // it must be a subtraction operator.
            if(Character.isDigit(c) || c == '.' || (c == '-' && (i == 0 || !Character.isDigit(str.charAt(i-1)))))
            {
                int j = i+1;
                if(j < length && (Character.isDigit(str.charAt(j)) || str.charAt(j) == '.')) {
                    while (j < length && (Character.isDigit(str.charAt(j)) || str.charAt(j) == '.'))
                        j++;
                    String number = str.substring(i, j);
                    i = j-1;
                    postfix.add(number);
                }
                else
                    postfix.add(Character.toString(c));
            }
            //Handle scientific notation. Skips over the "E+" in "x E+ y". The "x" is
            //already in the list. Adds 10 as the next entry, followed by "y", followed by
            //"^" and "*". Final result is adding the infix expression "x E+ y" as the postfix
            //expression "x 10 y ^ *" which evaluates correctly.
            else if(c == 'e' || c == 'E') {
                postfix.add("10");
                int j = i+2;
                if(j < length && (Character.isDigit(str.charAt(j)) || str.charAt(j) == '.')) {
                    while (j < length && (Character.isDigit(str.charAt(j)) || str.charAt(j) == '.'))
                        j++;
                    String number = str.substring(i+2, j);
                    i = j-1;
                    postfix.add(number);
                }
                else
                    postfix.add(Character.toString(c));
                postfix.add("^");
                postfix.add(Character.toString(mult));
            }
            //Open parentheses are pushed onto the stack
            else if(c == '(')
                stack.push(c);

                //On a close parenthesis, pop the contents of the stack and add it to the list
                //until the open parenthesis is reached, which is discarded.
            else if(c == ')')
            {
                try {
                    while (stack.peek() != '(')
                        postfix.add(stack.pop().toString());
                    stack.pop();
                }
                catch(EmptyStackException e) {
                    isError = true;
                    errorType = ErrorType.input;
                }
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
    //Returns 1 for all operators if the calculator is set to running calculations.
    private int operatorPrecedence(char op) {
        if(order == Order.running)
            return 1;
        else {
            if (op == '^' || op == sqrt)
                return 3;
            else if (op == mult || op == div)
                return 2;
            else if (op == '+' || op == '-')
                return 1;
            else
                return 0;
        }
    }

    //Formats a number to conform to the maximum number of decimals
    private String formatBigDecimal(BigDecimal number) {
        number = number.stripTrailingZeros();
        NumberFormat format = new DecimalFormat("0.0E0");
        format.setRoundingMode(RoundingMode.HALF_UP);
        format.setMinimumFractionDigits((number.scale() < 0) ? number.precision() : number.scale());

        NumberFormat formatAlt = new DecimalFormat("0.0");
        formatAlt.setRoundingMode(RoundingMode.HALF_UP);

        if(number.precision() > maxDecimals)
            return format.format(number);
        else if(number.scale() > maxDecimals)
            return formatAlt.format(number);
        else
            return number.toString();
    }

    //Sets the given number in memory
    public void setMemory(String str) {
        if(isNumber(str)) {
            memory = new BigDecimal(str);
            memoryModified = true;
        }
        else {
            errorType = ErrorType.memory;
            isError = true;
        }
    }

    //Returns the number in memory
    public String getMemory() {
        return memory.toString();
    }

    //Adds the given number to the number stored in memory
    public void memoryAdd(String str) {
        if(isNumber(str)) {
            BigDecimal number = new BigDecimal(str);
            memory = memory.add(number);
            memoryModified = true;
        }
        else {
            errorType = ErrorType.memory;
            isError = true;
        }
    }

    //Subtracts the given number from the number in memory
    public void memorySub(String str) {
        if(isNumber(str)) {
            BigDecimal number = new BigDecimal(str);
            memory = memory.subtract(number);
            memoryModified = true;
        }
        else {
            errorType = ErrorType.memory;
            isError = true;
        }
    }

    //Returns true if something has been stored in memory
    public boolean hasMemory() {
        return memoryModified;
    }

    //Returns true if the given BigDecimal is an integer in the mathematical sense
    private boolean isMathInteger(BigDecimal bd) {
        return bd.scale() <= 0 || bd.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }

    //Returns the value of the error flag
    public boolean hasError() {
        return isError;
    }

    //Clears the error flag
    public void clearError() {
        isError = false;
        errorType = ErrorType.none;
    }

    //Returns the error type if there is one
    public ErrorType getErrorType() {
        return errorType;
    }

    //Sets the number of allowed decimals
    public void setMaxDecimals(int max) {
        maxDecimals = max;
    }

    //Toggles the order of operations between running and regular
    public void setOrder(Order ord) {
        order = ord;
    }

    //Returns the current order of operations
    public Order getOrder() {
        return order;
    }
}
