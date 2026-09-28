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

/** TH07 Android v2.2 mobile controls and Practice+ tools. */
public class PerfectCherryBlossom extends SDLActivity {
    private static final String PREFS = "th07_mobile_controls";
    private static final String PREF_FREE_MOVE = "free_move";
    private static final String PREF_PRACTICE_INVINCIBLE = "practice_invincible";

    private MomentaryPad keyEsc;
    private TogglePad keyZ;
    private TogglePad keyS;
    private MomentaryPad keyX;
    private TextView moveMode;
    private TogglePad practiceInv;
    private boolean freeMove = true;
    private boolean practiceInvincible = false;

    private static native void nativeSetFreeMove(boolean enabled);
    private static native void nativeSetShotLatched(boolean enabled);
    private static native void nativeSetFocusLatched(boolean enabled);
    private static native void nativeSetPracticeInvincible(boolean enabled);
    private static native void nativeRequestPracticeNextPhase();
    private static native void nativeRequestPracticeRetryPhase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        freeMove = prefs.getBoolean(PREF_FREE_MOVE, true);
        practiceInvincible = prefs.getBoolean(PREF_PRACTICE_INVINCIBLE, false);
        installMobileOverlay();
        pushMoveModeToNative();
        pushToggleStateToNative();
        pushPracticeStateToNative();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pushMoveModeToNative();
        pushToggleStateToNative();
        pushPracticeStateToNative();
    }

    @Override
    protected void onPause() {
        // Avoid a stuck action when Android backgrounds the SDL activity.
        if (keyEsc != null) keyEsc.release();
        if (keyX != null) keyX.release();
        if (keyZ != null) keyZ.setLatched(false);
        if (keyS != null) keyS.setLatched(false);
        pushToggleStateToNative();
        super.onPause();
    }

    private void installMobileOverlay() {
        PassThroughLayout overlay = new PassThroughLayout(this);
        overlay.setClipChildren(false);
        overlay.setClipToPadding(false);
        addContentView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        keyEsc = new MomentaryPad(this, "ESC", KeyEvent.KEYCODE_ESCAPE, 14f);
        FrameLayout.LayoutParams escLp = new FrameLayout.LayoutParams(dp(66), dp(46));
        escLp.gravity = Gravity.TOP | Gravity.LEFT;
        escLp.leftMargin = dp(16);
        escLp.topMargin = dp(16);
        overlay.addView(keyEsc, escLp);

        // Single tap toggles continuous fire / focus. This mirrors common mobile
        // Touhou ports: tap Z once to keep firing, tap again to stop.
        keyZ = new TogglePad(this, "Z", 21f, enabled -> nativeSetShotSafe(enabled));
        keyS = new TogglePad(this, "S", 21f, enabled -> nativeSetFocusSafe(enabled));
        keyX = new MomentaryPad(this, "X", KeyEvent.KEYCODE_X, 21f);

        addBottomLeftKey(overlay, keyX, 18);
        addBottomLeftKey(overlay, keyS, 82);
        addBottomLeftKey(overlay, keyZ, 146);

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

        practiceInv = new TogglePad(this, "INV", 14f, enabled -> {
            practiceInvincible = enabled;
            getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putBoolean(PREF_PRACTICE_INVINCIBLE, enabled).apply();
            pushPracticeStateToNative();
        });
        practiceInv.setLatched(practiceInvincible);
        FrameLayout.LayoutParams invLp = new FrameLayout.LayoutParams(dp(86), dp(50));
        invLp.gravity = Gravity.TOP | Gravity.RIGHT;
        invLp.rightMargin = dp(18);
        invLp.topMargin = dp(74);
        overlay.addView(practiceInv, invLp);

        ActionPad nextPhase = new ActionPad(this, "NEXT\nPHASE", () -> {
            try { nativeRequestPracticeNextPhase(); } catch (UnsatisfiedLinkError ignored) { }
        });
        FrameLayout.LayoutParams nextLp = new FrameLayout.LayoutParams(dp(86), dp(50));
        nextLp.gravity = Gravity.TOP | Gravity.RIGHT;
        nextLp.rightMargin = dp(18);
        nextLp.topMargin = dp(132);
        overlay.addView(nextPhase, nextLp);

        ActionPad retryPhase = new ActionPad(this, "RETRY\nPHASE", () -> {
            try { nativeRequestPracticeRetryPhase(); } catch (UnsatisfiedLinkError ignored) { }
        });
        FrameLayout.LayoutParams retryLp = new FrameLayout.LayoutParams(dp(86), dp(50));
        retryLp.gravity = Gravity.TOP | Gravity.RIGHT;
        retryLp.rightMargin = dp(18);
        retryLp.topMargin = dp(190);
        overlay.addView(retryPhase, retryLp);
    }

    private void addBottomLeftKey(FrameLayout overlay, View key, int bottomDp) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(60), dp(60));
        lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        lp.leftMargin = dp(18);
        lp.bottomMargin = dp(bottomDp);
        overlay.addView(key, lp);
    }

    private void updateMoveModeText() {
        if (moveMode != null) moveMode.setText(freeMove ? "MOVE\nFREE" : "MOVE\nLIMIT");
    }

    private void pushMoveModeToNative() {
        try { nativeSetFreeMove(freeMove); } catch (UnsatisfiedLinkError ignored) { }
    }

    private void pushToggleStateToNative() {
        nativeSetShotSafe(keyZ != null && keyZ.isLatched());
        nativeSetFocusSafe(keyS != null && keyS.isLatched());
    }

    private void pushPracticeStateToNative() {
        try { nativeSetPracticeInvincible(practiceInvincible); }
        catch (UnsatisfiedLinkError ignored) { }
    }

    private void nativeSetShotSafe(boolean enabled) {
        try { nativeSetShotLatched(enabled); } catch (UnsatisfiedLinkError ignored) { }
    }

    private void nativeSetFocusSafe(boolean enabled) {
        try { nativeSetFocusLatched(enabled); } catch (UnsatisfiedLinkError ignored) { }
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

    private interface LatchListener { void onChanged(boolean enabled); }

    private final class TogglePad extends TextView implements View.OnTouchListener {
        private final String label;
        private final LatchListener listener;
        private boolean latched;

        TogglePad(Context context, String label, float textSp, LatchListener listener) {
            super(context);
            this.label = label;
            this.listener = listener;
            setTextSize(textSp);
            setTextColor(Color.WHITE);
            setTypeface(Typeface.DEFAULT_BOLD);
            setGravity(Gravity.CENTER);
            setOnTouchListener(this);
            setClickable(true);
            setFocusable(false);
            refreshVisual();
        }

        @Override public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                setLatched(!latched);
                return true;
            }
            return true;
        }

        boolean isLatched() { return latched; }

        void setLatched(boolean enabled) {
            if (latched == enabled) return;
            latched = enabled;
            refreshVisual();
            if (listener != null) listener.onChanged(latched);
        }

        private void refreshVisual() {
            setText(latched ? label + "\nON" : label);
            setAlpha(latched ? 1.0f : 0.78f);
            setBackground(makeRoundedBackground(
                    latched ? 0xAA245B2A : 0x88111111,
                    latched ? 0xFFFFFFFF : 0xD9FFFFFF,
                    dp(30), dp(1)));
        }
    }

    private final class ActionPad extends TextView {
        ActionPad(Context context, String label, Runnable action) {
            super(context);
            setText(label);
            setTextSize(13f);
            setTextColor(Color.WHITE);
            setTypeface(Typeface.DEFAULT_BOLD);
            setGravity(Gravity.CENTER);
            setBackground(makeRoundedBackground(0x88111111, 0xD9FFFFFF, dp(18), dp(1)));
            setClickable(true);
            setFocusable(false);
            setAlpha(0.78f);
            setOnClickListener(v -> {
                setAlpha(1.0f);
                if (action != null) action.run();
                postDelayed(() -> setAlpha(0.78f), 120);
            });
        }
    }

    private final class MomentaryPad extends TextView implements View.OnTouchListener {
        private final int androidKeyCode;
        private boolean down;

        MomentaryPad(Context context, String label, int keyCode, float textSp) {
            super(context);
            androidKeyCode = keyCode;
            setText(label);
            setTextSize(textSp);
            setTextColor(Color.WHITE);
            setTypeface(Typeface.DEFAULT_BOLD);
            setGravity(Gravity.CENTER);
            setBackground(makeRoundedBackground(0x88111111, 0xD9FFFFFF, dp(30), dp(1)));
            setOnTouchListener(this);
            setClickable(true);
            setFocusable(false);
            setAlpha(0.78f);
        }

        @Override public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    press(); return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL:
                    release(); return true;
                default: return true;
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

    private static final class PassThroughLayout extends FrameLayout {
        PassThroughLayout(Context context) {
            super(context);
            setClickable(false);
            setFocusable(false);
            setMotionEventSplittingEnabled(true);
        }
        @Override public boolean onInterceptTouchEvent(MotionEvent ev) { return false; }
        @Override public boolean onTouchEvent(MotionEvent event) { return false; }
    }
}
