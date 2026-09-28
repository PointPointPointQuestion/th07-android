package com.zun.th07;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.libsdl.app.SDLActivity;

/**
 * Android v2 shell for TH07.
 *
 * The SDL surface keeps receiving ordinary gameplay drags.  The transparent
 * overlay only consumes touches that land on the explicit virtual controls.
 */
public class PerfectCherryBlossom extends SDLActivity {
    private static final String PREFS = "th07_mobile_controls";
    private static final String PREF_FREE_MOVE = "free_move";

    private KeyPadView keyEsc;
    private KeyPadView keyZ;
    private KeyPadView keyS;
    private KeyPadView keyX;
    private TextView moveMode;
    private boolean freeMove = true;

    private static native void nativeSetFreeMove(boolean enabled);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        freeMove = prefs.getBoolean(PREF_FREE_MOVE, true);
        installMobileOverlay();
        pushMoveModeToNative();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pushMoveModeToNative();
    }

    @Override
    protected void onPause() {
        releaseVirtualKeys();
        super.onPause();
    }

    private void installMobileOverlay() {
        PassThroughLayout overlay = new PassThroughLayout(this);
        overlay.setClipChildren(false);
        overlay.setClipToPadding(false);

        addContentView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Left upper: ESC
        keyEsc = new KeyPadView(this, "ESC", KeyEvent.KEYCODE_ESCAPE, 14f);
        FrameLayout.LayoutParams escLp = new FrameLayout.LayoutParams(dp(66), dp(46));
        escLp.gravity = Gravity.TOP | Gravity.LEFT;
        escLp.leftMargin = dp(16);
        escLp.topMargin = dp(16);
        overlay.addView(keyEsc, escLp);

        // Left lower: Z / S / X.  S is a visual label; it sends Shift (Focus).
        keyZ = new KeyPadView(this, "Z", KeyEvent.KEYCODE_Z, 21f);
        keyS = new KeyPadView(this, "S", KeyEvent.KEYCODE_SHIFT_LEFT, 21f);
        keyX = new KeyPadView(this, "X", KeyEvent.KEYCODE_X, 21f);

        addBottomLeftKey(overlay, keyX, 18);
        addBottomLeftKey(overlay, keyS, 82);
        addBottomLeftKey(overlay, keyZ, 146);

        // Right upper: switch between direct finger following and original speed cap.
        moveMode = new TextView(this);
        moveMode.setGravity(Gravity.CENTER);
        moveMode.setTextColor(Color.WHITE);
        moveMode.setTextSize(12f);
        moveMode.setTypeface(Typeface.DEFAULT_BOLD);
        moveMode.setBackground(makeRoundedBackground(0x99111111, 0xCCFFFFFF, dp(10), dp(1)));
        moveMode.setPadding(dp(8), dp(3), dp(8), dp(3));
        updateMoveModeText();
        moveMode.setOnClickListener(v -> {
            freeMove = !freeMove;
            getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putBoolean(PREF_FREE_MOVE, freeMove).apply();
            updateMoveModeText();
            pushMoveModeToNative();
        });

        FrameLayout.LayoutParams moveLp = new FrameLayout.LayoutParams(dp(86), dp(50));
        moveLp.gravity = Gravity.TOP | Gravity.RIGHT;
        moveLp.rightMargin = dp(18);
        moveLp.topMargin = dp(16);
        overlay.addView(moveMode, moveLp);
    }

    private void addBottomLeftKey(FrameLayout overlay, View key, int bottomDp) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(56), dp(56));
        lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        lp.leftMargin = dp(18);
        lp.bottomMargin = dp(bottomDp);
        overlay.addView(key, lp);
    }

    private void updateMoveModeText() {
        if (moveMode != null) {
            moveMode.setText(freeMove ? "MOVE\nFREE" : "MOVE\nLIMIT");
        }
    }

    private void pushMoveModeToNative() {
        try {
            nativeSetFreeMove(freeMove);
        } catch (UnsatisfiedLinkError ignored) {
            // SDL may still be finishing native startup; onResume / toggle retries it.
        }
    }

    private void releaseVirtualKeys() {
        if (keyEsc != null) keyEsc.release();
        if (keyZ != null) keyZ.release();
        if (keyS != null) keyS.release();
        if (keyX != null) keyX.release();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable makeRoundedBackground(int fill, int stroke, int radius, int strokeWidth) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(fill);
        bg.setCornerRadius(radius);
        bg.setStroke(strokeWidth, stroke);
        return bg;
    }

    private final class KeyPadView extends TextView implements View.OnTouchListener {
        private final int androidKeyCode;
        private boolean down;

        KeyPadView(Context context, String label, int keyCode, float textSp) {
            super(context);
            androidKeyCode = keyCode;
            setText(label);
            setTextSize(textSp);
            setTextColor(Color.WHITE);
            setTypeface(Typeface.DEFAULT_BOLD);
            setGravity(Gravity.CENTER);
            setBackground(makeRoundedBackground(0x88111111, 0xD9FFFFFF, dp(28), dp(1)));
            setOnTouchListener(this);
            setClickable(true);
            setFocusable(false);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    press();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    release();
                    return true;
                default:
                    return true;
            }
        }

        private void press() {
            if (!down) {
                down = true;
                SDLActivity.onNativeKeyDown(androidKeyCode);
                setAlpha(1.0f);
            }
        }

        void release() {
            if (down) {
                down = false;
                SDLActivity.onNativeKeyUp(androidKeyCode);
                setAlpha(0.78f);
            }
        }
    }

    /** Full-screen overlay that refuses empty-area touches so SDL keeps them. */
    private static final class PassThroughLayout extends FrameLayout {
        PassThroughLayout(Context context) {
            super(context);
            setClickable(false);
            setFocusable(false);
            setMotionEventSplittingEnabled(true);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return false;
        }
    }
}
