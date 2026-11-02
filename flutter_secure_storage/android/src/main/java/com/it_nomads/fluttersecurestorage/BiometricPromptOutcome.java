package com.it_nomads.fluttersecurestorage;

import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Delivers one BiometricPrompt outcome to the operation waiting on it, exactly once.
 *
 * <p>The framework {@code android.hardware.biometrics.BiometricPrompt} reports its negative
 * button only to the button's listener, not to {@code onAuthenticationError}, so a listener that
 * does nothing leaves the operation (and the Dart call) waiting forever. Some devices may still
 * send {@code onAuthenticationError} afterwards (for example 5, canceled), and the success and
 * error callbacks can race, so every path completes through here and only the first one counts.
 */
final class BiometricPromptOutcome<T> {
    private static final String TAG = "FlutterSecureStorage";

    /** {@code BiometricPrompt.BIOMETRIC_ERROR_NEGATIVE_BUTTON}. */
    static final int ERROR_NEGATIVE_BUTTON = 13;

    private final SecurePreferencesCallback<T> callback;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    BiometricPromptOutcome(SecurePreferencesCallback<T> callback) {
        this.callback = callback;
    }

    void succeeded(T result) {
        if (completed.compareAndSet(false, true)) {
            callback.onSuccess(result);
        }
    }

    /**
     * Reports a prompt error as "Biometric authentication error [code]: text", the format the
     * Dart side parses the code from.
     */
    void failed(int errorCode, CharSequence errorText) {
        if (completed.compareAndSet(false, true)) {
            Log.e(TAG, "Biometric authentication error [" + errorCode + "]: " + errorText);
            callback.onError(new Exception("Biometric authentication error [" + errorCode + "]: " + errorText));
        }
    }

    /** The prompt's negative button was tapped: the user cancelled. */
    void negativeButton(CharSequence buttonText) {
        failed(ERROR_NEGATIVE_BUTTON, buttonText);
    }
}
