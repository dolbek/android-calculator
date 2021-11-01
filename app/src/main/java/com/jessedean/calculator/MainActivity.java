package com.jessedean.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.lang.Math;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView runningTotal;
    TextView total;
    String runningString = "";
    String calculationString = "";
    String resultString = "";
    int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runningTotal = (TextView) findViewById(R.id.runningDisplay);
        total = (TextView) findViewById(R.id.display);

        setButtons();
    }


    @Override
    public void onClick(View view) {
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
                calculationString += '\u2013';
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
                calculationString += '\u2014';
                break;
            case R.id.buttonMult:
                calculationString += '\u2715';
                break;
            case R.id.buttonDiv:
                calculationString += '\u00F7';
                break;
            case R.id.buttonEqual:
                calculationString += '=';
                break;
            case R.id.buttonExp:
                calculationString += '^';
                break;
            case R.id.buttonSqrt:
                calculationString += '\u221A';
                break;
            case R.id.buttonClear:
                calculationString = "";
                break;
            case R.id.buttonBack:
                if (calculationString != null && calculationString.length() > 0)
                    calculationString = calculationString.substring(0, calculationString.length() - 1);
                break;
            case R.id.buttonBase:
                break;
            case R.id.buttonMem:
                break;
            case R.id.buttonMemAdd:
                break;
            case R.id.buttonMemSub:
                break;
            case R.id.buttonMemRecall:
                break;


        }
        setDisplay();
    }

    private void setDisplay() {
        total.setText(calculationString);
    }

    private int calculate() {
        int result = 0;
        return result;
    }

    private void setButtons() {

        Button button0 = (Button) findViewById(R.id.button0);
        button0.setOnClickListener(this);
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);
        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(this);
        Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(this);
        Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(this);
        Button button6 = (Button) findViewById(R.id.button6);
        button6.setOnClickListener(this);
        Button button7 = (Button) findViewById(R.id.button7);
        button7.setOnClickListener(this);
        Button button8 = (Button) findViewById(R.id.button8);
        button8.setOnClickListener(this);
        Button button9 = (Button) findViewById(R.id.button9);
        button9.setOnClickListener(this);
        Button buttonDecimal = (Button) findViewById(R.id.buttonDecimal);
        buttonDecimal.setOnClickListener(this);
        Button buttonNeg = (Button) findViewById(R.id.buttonNeg);
        buttonNeg.setOnClickListener(this);
        Button buttonParLeft = (Button) findViewById(R.id.buttonParLeft);
        buttonParLeft.setOnClickListener(this);
        Button buttonParRight = (Button) findViewById(R.id.buttonParRight);
        buttonParRight.setOnClickListener(this);

        Button buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(this);
        Button buttonSub = (Button) findViewById(R.id.buttonSub);
        buttonSub.setOnClickListener(this);
        Button buttonMult = (Button) findViewById(R.id.buttonMult);
        buttonMult.setOnClickListener(this);
        Button buttonDiv = (Button) findViewById(R.id.buttonDiv);
        buttonDiv.setOnClickListener(this);
        Button buttonEqual = (Button) findViewById(R.id.buttonEqual);
        buttonEqual.setOnClickListener(this);
        Button buttonExp = (Button) findViewById(R.id.buttonExp);
        buttonExp.setOnClickListener(this);
        Button buttonSqrt = (Button) findViewById(R.id.buttonSqrt);
        buttonSqrt.setOnClickListener(this);

        Button buttonClear = (Button) findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(this);
        Button buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);
        Button buttonBase = (Button) findViewById(R.id.buttonBase);
        buttonBase.setOnClickListener(this);

        Button buttonMem = (Button) findViewById(R.id.buttonMem);
        buttonMem.setOnClickListener(this);
        Button buttonMemAdd = (Button) findViewById(R.id.buttonMemAdd);
        buttonMemAdd.setOnClickListener(this);
        Button buttonMemSub = (Button) findViewById(R.id.buttonMemSub);
        buttonMemSub.setOnClickListener(this);
        Button buttonMemRecall = (Button) findViewById(R.id.buttonMemRecall);
        buttonMemRecall.setOnClickListener(this);

    }
}