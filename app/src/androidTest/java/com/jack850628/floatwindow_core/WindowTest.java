package com.jack850628.floatwindow_core;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

import junit.framework.TestCase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.annotation.UiThreadTest;

import com.jack8.floatwindow.Window.WindowStruct;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WindowTest extends TestCase {
    private Context context;
    private WindowManager windowManager;

    public WindowTest(){
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Test
    @UiThreadTest
    public void testWindow(){
        WindowStruct w1 = new WindowStruct.Builder(context, windowManager).show();
        Log.d("w1 number", String.valueOf(w1.getNumber()));
    }
}
