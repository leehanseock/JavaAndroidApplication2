package com.leehanseock;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout; // LinearLayout 임포트
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// FileProvider를 사용하기 위한 임포트
import androidx.core.content.FileProvider;

public class ScratchActivity extends AppCompatActivity {

    private ScratchView scratchView;
    private TextView messageTextView;
    private ShimmerFrameLayout shimmerFrameLayout;
    private Button resetButton;
    private Button shareButton; // 공유 버튼 추가
    private LinearLayout scratchCardLayout; // 스크린샷을 찍을 레이아웃

    private List<String> fortuneMessages;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scratch);

        scratchView = findViewById(R.id.scratchView);
        messageTextView = findViewById(R.id.messageTextView);
        shimmerFrameLayout = findViewById(R.id.shimmer);
        resetButton = findViewById(R.id.resetButton);
        shareButton = findViewById(R.id.shareButton); // 공유 버튼 연결
        scratchCardLayout = findViewById(R.id.scratchCardLayout); // 레이아웃 연결

        // 행운 메시지 초기화
        fortuneMessages = new ArrayList<>(Arrays.asList(
                "오늘 당신에게 큰 행운이 찾아옵니다!",
                "모든 일이 술술 풀리는 하루가 될 거예요.",
                "뜻밖의 좋은 소식이 들려올 것입니다.",
                "당신의 노력은 결코 헛되지 않습니다.",
                "긍정적인 생각은 좋은 결과를 가져다줍니다.",
                "사랑과 행복이 가득한 하루를 보내세요.",
                "새로운 기회가 당신을 기다리고 있습니다.",
                "오늘 당신의 미소가 세상을 밝힙니다.",
                "어려움 속에서도 희망을 찾으세요.",
                "용기를 내면 못 이룰 것이 없습니다."
        ));

        // 앱 시작 시 쉬머 애니메이션 시작
        shimmerFrameLayout.startShimmer();
        // 초기에는 리셋 버튼과 메시지 TextView, 공유 버튼 숨기기
        resetButton.setVisibility(View.GONE);
        messageTextView.setVisibility(View.INVISIBLE);
        shareButton.setVisibility(View.GONE); // 공유 버튼 숨김

        // 초기 메시지 설정 (숨겨진 상태)
        setRandomFortuneMessage();

        scratchView.setOnScratchCompleteListener(new ScratchView.OnScratchCompleteListener() {
            @Override
            public void onScratchComplete() {
                // 스크래치 완료 시 쉬머 애니메이션 정지
                shimmerFrameLayout.stopShimmer();
                Toast.makeText(ScratchActivity.this, "메시지 공개!", Toast.LENGTH_SHORT).show();

                // 메시지 TextView 보이게 설정
                messageTextView.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.VISIBLE); // 리셋 버튼 표시
                shareButton.setVisibility(View.VISIBLE); // 공유 버튼 표시
            }
        });

        resetButton.setOnClickListener(v -> {
            scratchView.resetScratchView(); // 스크래치 뷰 초기화
            shimmerFrameLayout.startShimmer(); // 쉬머 애니메이션 다시 시작
            messageTextView.setVisibility(View.INVISIBLE); // 메시지 TextView 다시 숨기기
            resetButton.setVisibility(View.GONE); // 리셋 버튼 다시 숨기기
            shareButton.setVisibility(View.GONE); // 공유 버튼 다시 숨김
            setRandomFortuneMessage(); // 새로운 메시지 설정
        });

        shareButton.setOnClickListener(v -> {
            shareScreenshot(); // 공유 버튼 클릭 시 스크린샷 공유 함수 호출
        });
    }

    private void setRandomFortuneMessage() {
        String randomMessage = fortuneMessages.get(random.nextInt(fortuneMessages.size()));
        messageTextView.setText(randomMessage);
    }

    // 화면 스크린샷을 찍는 메서드
    private Bitmap takeScreenshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // 스크린샷을 저장하고 공유하는 메서드
    private void shareScreenshot() {
        Bitmap screenshot = takeScreenshot(scratchCardLayout); // 전체 레이아웃 스크린샷

        // 스크린샷을 임시 파일로 저장
        File imagePath = new File(getCacheDir(), "images");
        boolean mkdirsResult = imagePath.mkdirs(); // 디렉토리가 없으면 생성
        File newFile = new File(imagePath, "lucky_message.png");

        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "스크린샷 저장 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        // FileProvider를 사용하여 URI 얻기
        Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", newFile);

        if (contentUri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 읽기 권한 부여

            try {
                startActivity(Intent.createChooser(shareIntent, "행운 메시지 공유"));
            } catch (Exception e) {
                Toast.makeText(this, "공유 가능한 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        shimmerFrameLayout.startShimmer();
    }

    @Override
    protected void onPause() {
        shimmerFrameLayout.stopShimmer();
        super.onPause();
    }
}