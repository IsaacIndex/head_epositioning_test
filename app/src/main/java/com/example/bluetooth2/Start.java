package com.example.bluetooth2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Start extends AppCompatActivity {

    EditText positions, sets, rounds;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        positions = findViewById(R.id.positions);
        rounds = findViewById(R.id.rounds);
        sets = findViewById(R.id.sets);

    }

    public void next(View v){
        Intent intent = new Intent(Start.this, MainActivity.class);
        Bundle bundle = new Bundle();

        int a = Integer.valueOf(positions.getText().toString());
        int b = Integer.parseInt(rounds.getText().toString());
        int c = Integer.parseInt(sets.getText().toString());
        //Toast.makeText(getApplicationContext(), String.valueOf(a+b+c), Toast.LENGTH_SHORT).show();

        bundle.putInt("positions", a);
        bundle.putInt("rounds", b);
        bundle.putInt("sets", c);

        intent.putExtras(bundle);
        //intent.putExtra("name","abc");
        startActivity(intent);
        //finish();
    }
}

