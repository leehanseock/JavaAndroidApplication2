package com.leehanseock;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// MainActivity.java 또는 Fragment 내에서

public class MainActivity extends AppCompatActivity {

    private ScratchView scratchView;
    private ImageView hiddenImageView; // 숨겨진 이미지를 표시하는 ImageView (layout.xml에 있어야 함)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 당신의 레이아웃 파일

        scratchView = findViewById(R.id.scratchView);
        hiddenImageView = findViewById(R.id.hiddenImageView); // 예시: 레이아웃에 hiddenImageView 추가했다고 가정

        // 숨겨진 이미지를 설정
        hiddenImageView.setImageResource(R.drawable.hi); // 실제 이미지로 변경

        scratchView.setOnScratchCompleteListener(new ScratchView.OnScratchCompleteListener() {
            @Override
            public void onScratchComplete() {
                // 스크래치가 70% 이상 완료되었을 때 실행될 코드
                Toast.makeText(MainActivity.this, "스크래치 완료! 이미지가 드러났습니다!", Toast.LENGTH_SHORT).show();
                // 여기에서 동영상에 나오는 색종이 조각 애니메이션 등을 시작할 수 있습니다.
            }
        });

         Button resetButton = findViewById(R.id.btnReset);
         resetButton.setOnClickListener(v -> scratchView.resetScratchView());
    }
}