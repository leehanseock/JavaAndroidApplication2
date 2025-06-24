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
import android.view.View; // 이 줄을 추가해야 합니다.


import androidx.annotation.Nullable;
// ScratchView.java
public class ScratchView extends View {

    private Bitmap scratchBitmap; // 스크래치 가능한 레이어를 위한 비트맵 (예: 회색 사각형)
    private Canvas scratchCanvas; // scratchBitmap에 그릴 캔버스
    private Paint scratchPaint;   // 지우기 위한 페인트 (PorterDuffXfermode)
    private Path scratchPath;     // 스크래치된 영역을 저장할 경로

    private float lastTouchX, lastTouchY;

    private boolean revealed = false; // 이미지가 완전히 공개되었는지 여부
    private static final float REVEAL_THRESHOLD = 70.0f; // 스크래치 완료 임계값 (70%)

    public ScratchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 지우기 위한 scratchPaint 초기화
        scratchPaint = new Paint();
        scratchPaint.setAlpha(0); // Xfermode를 사용하기 위해 투명하게 만듭니다.
        scratchPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // 픽셀을 지웁니다.
        scratchPaint.setAntiAlias(true); // 안티 앨리어싱
        scratchPaint.setDither(true);    // 디더링
        scratchPaint.setStyle(Paint.Style.STROKE); // 스트로크 스타일
        scratchPaint.setStrokeJoin(Paint.Join.ROUND); // 선 끝 조인 라운드
        scratchPaint.setStrokeCap(Paint.Cap.ROUND);   // 선 끝 캡 라운드
        scratchPaint.setStrokeWidth(100); // 스크래치 브러시 크기 조정

        scratchPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 뷰 크기만큼 scratchBitmap 생성
        scratchBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        scratchCanvas = new Canvas(scratchBitmap);
        // scratchBitmap을 초기 스크래치 가능한 색상/이미지로 채웁니다.
        scratchCanvas.drawColor(Color.LTGRAY); // 또는 드로어블/이미지 그리기
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 여기에서 표시될 콘텐츠를 그립니다 (예: 이미지)
        // 간단히 말해, 부모 레이아웃 또는 아래의 다른 뷰에 의해 그려진다고 가정합니다.

        // revealed 상태에 따라 스크래치 레이어를 그릴지 결정
        if (!revealed) {
            canvas.drawBitmap(scratchBitmap, 0, 0, null);
            scratchCanvas.drawPath(scratchPath, scratchPaint);
        }
        // else: revealed가 true면 스크래치 레이어를 그리지 않으므로 숨겨진 이미지가 완전히 보임
    }

    private float touchTolerance = 4; // 터치 허용 오차 (선택 사항)

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (revealed) { // 이미 공개되었다면 더 이상 터치 이벤트를 처리하지 않음
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
     * 스크래치된 비율을 확인하고, 임계값을 넘으면 이미지를 완전히 공개합니다.
     */
    private void checkRevealStatus() {
        float transparentPercentage = getTransparentPixelPercentage();
        if (transparentPercentage >= REVEAL_THRESHOLD) {
            revealed = true;
            // 선택 사항: 스크래치 완료 콜백 리스너 호출
            if (onScratchCompleteListener != null) {
                onScratchCompleteListener.onScratchComplete();
            }
            // confetti 애니메이션 등의 추가 효과를 여기에서 트리거할 수 있습니다.
            invalidate(); // 즉시 다시 그려 스크래치 레이어를 사라지게 함
        }
    }

    // 스크래치 완료 리스너 인터페이스 (선택 사항)
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
        // scratchBitmap을 다시 채워 초기 상태로 되돌립니다.
        if (scratchBitmap != null) {
            scratchBitmap.eraseColor(Color.LTGRAY); // 또는 초기 이미지 다시 그리기
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
            // ARGB_8888 포맷에서 알파 값은 최상위 8비트
            // Color.TRANSPARENT는 알파 값이 0x00xxxxxx
            if (Color.alpha(pixel) == 0x00) { // 완전히 투명한 픽셀인지 확인
                transparentPixels++;
            }
        }

        return (float) transparentPixels / (width * height) * 100.0f;
    }

}
