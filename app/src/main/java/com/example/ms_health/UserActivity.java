package com.example.ms_health;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;

public class UserActivity extends AppCompatActivity {

    EditText setWeight;
    Button set;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        setWeight = (EditText)findViewById(R.id.set_weight);
        set = (Button)findViewById(R.id.set_button);

        setWeight.setText("");

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("Weight", setWeight.getText().toString());

                setResult(Activity.RESULT_OK, intent);

                finish();
            }
        });
    }
}