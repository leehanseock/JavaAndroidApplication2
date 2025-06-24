package com.leehanseock;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            // 버튼 클릭 시 ScratchCardActivity로 이동
            Intent intent = new Intent(MainActivity.this, ScratchActivity.class);
            startActivity(intent);
        });
    }
}