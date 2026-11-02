package com.it_nomads.fluttersecurestorage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

// Robolectric only for android.util.Log in the error path.
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class BiometricPromptOutcomeTest {

    /** Records every callback the waiting operation receives. */
    private static final class Recorder implements SecurePreferencesCallback<String> {
        final List<String> calls = new ArrayList<>();

        @Override
        public void onSuccess(String result) {
            calls.add("success:" + result);
        }

        @Override
        public void onError(Exception e) {
            calls.add("error:" + e.getMessage());
        }
    }

    @Test
    public void negativeButton_completesTheOperationAsCancelled() {
        // The framework prompt reports its negative button only to the button's
        // listener. Without a callback here the Dart call never completes.
        Recorder recorder = new Recorder();
        BiometricPromptOutcome<String> outcome = new BiometricPromptOutcome<>(recorder);

        outcome.negativeButton("الغاء");

        assertEquals(List.of("error:Biometric authentication error [13]: الغاء"), recorder.calls);
    }

    @Test
    public void negativeButtonThenSystemError_completesOnce() {
        // A device may also send onAuthenticationError (e.g. 5, canceled) after
        // the button: a second completion would reply twice to Dart.
        Recorder recorder = new Recorder();
        BiometricPromptOutcome<String> outcome = new BiometricPromptOutcome<>(recorder);

        outcome.negativeButton("Cancel");
        outcome.failed(5, "Fingerprint operation canceled");

        assertEquals(1, recorder.calls.size());
        assertEquals("error:Biometric authentication error [13]: Cancel", recorder.calls.get(0));
    }

    @Test
    public void successThenError_completesOnceWithTheSuccess() {
        Recorder recorder = new Recorder();
        BiometricPromptOutcome<String> outcome = new BiometricPromptOutcome<>(recorder);

        outcome.succeeded("authenticated");
        outcome.failed(5, "late");
        outcome.negativeButton("Cancel");

        assertEquals(List.of("success:authenticated"), recorder.calls);
    }

    @Test
    public void errorThenSuccess_completesOnceWithTheError() {
        Recorder recorder = new Recorder();
        BiometricPromptOutcome<String> outcome = new BiometricPromptOutcome<>(recorder);

        outcome.failed(10, "User canceled");
        outcome.succeeded("late");

        assertEquals(List.of("error:Biometric authentication error [10]: User canceled"), recorder.calls);
    }
}
