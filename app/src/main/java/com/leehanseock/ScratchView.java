package com.leehanseock;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.BitmapFactory;


import androidx.annotation.Nullable;
// ScratchView.java
public class ScratchView extends View {

    private Bitmap scratchBitmap; // 스크래치 가능한 레이어를 위한 비트맵 (예: 회색 사각형)
    private Canvas scratchCanvas; // scratchBitmap에 그릴 캔버스
    private Paint scratchPaint;   // 지우기 위한 페인트 (PorterDuffXfermode)
    private Path scratchPath;     // 스크래치된 영역을 저장할 경로

    private float lastTouchX, lastTouchY;

    private boolean revealed = false; // 이미지가 완전히 공개되었는지 여부
    private static final float REVEAL_THRESHOLD = 60.0f; // 스크래치 완료 임계값 (60%)

    private Bitmap scratchLayerImageBitmap;

    public ScratchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 지우기 위한 scratchPaint 초기화
        scratchPaint = new Paint();
        scratchPaint.setAlpha(0);
        scratchPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        scratchPaint.setAntiAlias(true); // 안티 앨리어싱
        scratchPaint.setDither(true);    // 디더링
        scratchPaint.setStyle(Paint.Style.STROKE); // 스트로크 스타일
        scratchPaint.setStrokeJoin(Paint.Join.ROUND); // 선 끝 조인 라운드
        scratchPaint.setStrokeCap(Paint.Cap.ROUND);   // 선 끝 캡 라운드
        scratchPaint.setStrokeWidth(130); // 스크래치 브러시 크기 조정
        scratchLayerImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gradation01);
        scratchPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 뷰 크기만큼 scratchBitmap 생성
        scratchBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        scratchCanvas = new Canvas(scratchBitmap);
        // scratchBitmap을 초기 스크래치 가능한 색상/이미지로 채우기
        if (scratchLayerImageBitmap != null) {
            scratchCanvas.drawBitmap(Bitmap.createScaledBitmap(scratchLayerImageBitmap, w, h, true), 0, 0, null);
        } else {
            scratchCanvas.drawColor(Color.LTGRAY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // revealed 상태에 따라 스크래치 레이어를 그릴지 결정
        if (!revealed) {
            canvas.drawBitmap(scratchBitmap, 0, 0, null);
            scratchCanvas.drawPath(scratchPath, scratchPaint);
        }
        // else: revealed가 true면 스크래치 레이어를 그리지 않으므로 숨겨진 이미지가 완전히 보임
    }

    private float touchTolerance = 4;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (revealed) {
            return false;
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                scratchPath.reset();
                scratchPath.moveTo(x, y);
                lastTouchX = x;
                lastTouchY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - lastTouchX);
                float dy = Math.abs(y - lastTouchY);
                if (dx >= touchTolerance || dy >= touchTolerance) {
                    scratchPath.quadTo(lastTouchX, lastTouchY, (x + lastTouchX) / 2, (y + lastTouchY) / 2);
                    lastTouchX = x;
                    lastTouchY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                scratchPath.lineTo(x, y);

                // 터치 업 시 투명도 확인
                checkRevealStatus();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    /**
     * 스크래치된 비율을 확인하고, 임계값을 넘으면 이미지를 공개
     */
    private void checkRevealStatus() {
        float transparentPercentage = getTransparentPixelPercentage();
        if (transparentPercentage >= REVEAL_THRESHOLD) {
            revealed = true;
            // 선택 사항: 스크래치 완료 콜백 리스너 호출
            if (onScratchCompleteListener != null) {
                onScratchCompleteListener.onScratchComplete();
            }
            // confetti 애니메이션 등의 추가 효과를 여기에서 트리거
            invalidate(); // 즉시 다시 그려 스크래치 레이어를 사라지게 함
        }
    }

    // 스크래치 완료 리스너 인터페이스
    public interface OnScratchCompleteListener {
        void onScratchComplete();
    }

    private OnScratchCompleteListener onScratchCompleteListener;

    public void setOnScratchCompleteListener(OnScratchCompleteListener listener) {
        this.onScratchCompleteListener = listener;
    }

    // 스크래치 뷰 초기화 (리셋) 메서드 추가
    public void resetScratchView() {
        revealed = false;
        if (scratchBitmap != null) {
            scratchBitmap.eraseColor(Color.LTGRAY);
        }
        scratchPath.reset();
        invalidate();
    }

    private float getTransparentPixelPercentage() {
        if (scratchBitmap == null || scratchBitmap.isRecycled() || scratchBitmap.getWidth() == 0 || scratchBitmap.getHeight() == 0) {
            return 0.0f;
        }

        int width = scratchBitmap.getWidth();
        int height = scratchBitmap.getHeight();
        int[] pixels = new int[width * height];
        scratchBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int transparentPixels = 0;
        for (int pixel : pixels) {
            if (Color.alpha(pixel) == 0x00) {
                transparentPixels++;
            }
        }

        return (float) transparentPixels / (width * height) * 100.0f;
    }
    public interface OnScratchListener {
        void onScratch(float percentage);
    }

    private OnScratchListener scratchListener;

    public void setOnScratchListener(OnScratchListener listener) {
        this.scratchListener = listener;
    }

    private void onScratchPercentageChanged(float percentage) {
        if (scratchListener != null) {
            scratchListener.onScratch(percentage);
        }
    }

}
