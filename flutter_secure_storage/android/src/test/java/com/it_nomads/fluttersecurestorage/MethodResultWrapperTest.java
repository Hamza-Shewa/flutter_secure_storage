package com.it_nomads.fluttersecurestorage;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodChannel.Result;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MethodResultWrapperTest {

    /** Like Flutter's reply: a second reply throws "Reply already submitted". */
    private static final class FlutterLikeResult implements Result {
        final List<String> replies = new ArrayList<>();

        private void reply(String what) {
            if (!replies.isEmpty()) throw new IllegalStateException("Reply already submitted");
            replies.add(what);
        }

        @Override
        public void success(@Nullable Object result) {
            reply("success:" + result);
        }

        @Override
        public void error(@NonNull String errorCode, @Nullable String errorMessage, @Nullable Object errorDetails) {
            reply("error:" + errorCode);
        }

        @Override
        public void notImplemented() {
            reply("notImplemented");
        }
    }

    @Test
    public void aSecondReplyIsIgnoredInsteadOfCrashing() {
        FlutterLikeResult flutter = new FlutterLikeResult();
        FlutterSecureStoragePlugin.MethodResultWrapper wrapper =
                new FlutterSecureStoragePlugin.MethodResultWrapper(flutter);

        wrapper.error("Exception encountered", "Biometric authentication error [13]: Cancel", null);
        wrapper.success("late");
        shadowOf(Looper.getMainLooper()).idle();

        assertEquals(List.of("error:Exception encountered"), flutter.replies);
    }

    @Test
    public void aSingleReplyIsDelivered() {
        FlutterLikeResult flutter = new FlutterLikeResult();
        FlutterSecureStoragePlugin.MethodResultWrapper wrapper =
                new FlutterSecureStoragePlugin.MethodResultWrapper(flutter);

        wrapper.success("value");
        shadowOf(Looper.getMainLooper()).idle();

        assertEquals(List.of("success:value"), flutter.replies);
    }
}
