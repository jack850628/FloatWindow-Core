package com.jack850628.floatwindow_core;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.jack8.floatwindow.Window.WindowFrom;
import com.jack8.floatwindow.Window.WindowStruct;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WindowControl#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WindowControl extends Fragment {
    private WindowStruct windowStruct;
    private int showObj = WindowStruct.TITLE_BAR_AND_BUTTONS |
            WindowStruct.MENU_BUTTON |
            WindowStruct.HIDE_BUTTON |
            WindowStruct.MINI_BUTTON |
            WindowStruct.MAX_BUTTON |
            WindowStruct.CLOSE_BUTTON |
            WindowStruct.SIZE_BAR |
            WindowStruct.FULLSCREEN_BUTTON;
    private float H =- 1,W =- 1;

    private View.OnClickListener controlWindowState = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(windowStruct == null) return;
            if(view.getId() == R.id.hide_btn){
                windowStruct.hide();
            }else if(view.getId() == R.id.mini_btn){
                windowStruct.mini();
            }else if(view.getId() == R.id.general_btn){
                windowStruct.general();
            }else if(view.getId() == R.id.max_btn){
                windowStruct.max();
            }else if(view.getId() == R.id.fullscreen_btn){
                windowStruct.fullscreen();
            }else if(view.getId() == R.id.close_btn){
                windowStruct.close();
            }
        }
    };
    private View.OnClickListener changeWindowController = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(windowStruct == null) return;
            if(view.getId() == com.jack8.floatwindow.R.id.title){
                showObj ^= WindowStruct.TITLE_BAR_AND_BUTTONS;
            }else if(view.getId() == com.jack8.floatwindow.R.id.menu){
                showObj ^= WindowStruct.MENU_BUTTON;
            }else if(view.getId() == com.jack8.floatwindow.R.id.hide){
                showObj ^= WindowStruct.HIDE_BUTTON;
            }else if(view.getId() == com.jack8.floatwindow.R.id.mini){
                showObj ^= WindowStruct.MINI_BUTTON;
            }else if(view.getId() == com.jack8.floatwindow.R.id.max){
                showObj ^= WindowStruct.MAX_BUTTON;
            }else if(view.getId() == com.jack8.floatwindow.R.id.fullscreen){
                showObj ^= WindowStruct.FULLSCREEN_BUTTON;
            }else if(view.getId() == com.jack8.floatwindow.R.id.close_button){
                showObj ^= WindowStruct.CLOSE_BUTTON;
            }else if(view.getId() == com.jack8.floatwindow.R.id.size){
                showObj ^= WindowStruct.SIZE_BAR;
            }
            windowStruct.setDisplayObject(showObj);
        }
    };


    public WindowControl() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WindowControl.
     */
    // TODO: Rename and change types and number of parameters
    public static WindowControl newInstance() {
        WindowControl fragment = new WindowControl();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.window_control));
        return inflater.inflate(R.layout.fragment_window_control, container, false);
    }

    @Override
    public void onViewCreated(View pageView, Bundle savedInstanceState){
        super.onViewCreated(pageView, savedInstanceState);
        pageView.findViewById(R.id.hello).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(windowStruct != null){
                    windowStruct.focusAndShowWindow();
                    return;
                }
                windowStruct = new WindowStruct.Builder(getContext(), (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE))
                        .windowPageTitles(new String[]{getString(R.string.popup_window)})
                        .displayObject(showObj)
                        .top(0)
                        .width((int)(getResources().getDisplayMetrics().density*230))
                        .constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow(){
                            @Override
                            public void onDestroy(Context context, WindowStruct ws){
                                windowStruct = null;
                            }
                        })
                        .show();
            }
        });
        pageView.findViewById(R.id.hide_btn).setOnClickListener(controlWindowState);
        pageView.findViewById(R.id.mini_btn).setOnClickListener(controlWindowState);
        pageView.findViewById(R.id.general_btn).setOnClickListener(controlWindowState);
        pageView.findViewById(R.id.max_btn).setOnClickListener(controlWindowState);
        pageView.findViewById(R.id.fullscreen_btn).setOnClickListener(controlWindowState);
        pageView.findViewById(R.id.close_btn).setOnClickListener(controlWindowState);

        WindowFrom windowFrom = pageView.findViewById(R.id.window);
        windowFrom.findViewById(com.jack8.floatwindow.R.id.title).setOnClickListener(changeWindowController);
        ((TextView)windowFrom.findViewById(com.jack8.floatwindow.R.id.title)).setText(getString(R.string.window_control_win_title));
        windowFrom.findViewById(com.jack8.floatwindow.R.id.title).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(windowStruct == null) return false;
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (H == -1 || W == -1) {
                        H = event.getRawX();//取得點擊的X座標到視窗頂點的距離
                        W = event.getRawY();//取得點擊的Y座標到視窗頂點的距離
                        return true;
                    }
                    windowStruct.setPosition(windowStruct.getPositionX()-(int) (H-event.getRawX()),windowStruct.getPositionY()-(int) (W-event.getRawY()), true);//當isUncertain為true時，不會觸發onWindowPositionChange事件
                    H = event.getRawX();
                    W = event.getRawY();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    windowStruct.setPosition(windowStruct.getRealPositionX(), windowStruct.getRealPositionY());
                    H = -1;
                    W = -1;
                }
                return false;
            }
        });
        windowFrom.findViewById(com.jack8.floatwindow.R.id.size).setOnClickListener(changeWindowController);
        windowFrom.findViewById(com.jack8.floatwindow.R.id.size).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(windowStruct == null) return false;
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (H == -1 || W == -1) {
                        H = event.getRawX();//取得點擊的X座標到視窗頂點的距離
                        W = event.getRawY();//取得點擊的Y座標到視窗頂點的距離
                        return true;
                    }
                    windowStruct.setSize(windowStruct.getHeight()-(int) (W-event.getRawY()), windowStruct.getWidth()-(int) (H-event.getRawX()), true);//當isUncertain為true時，不會觸發onWindowSizeChange事件
                    H = event.getRawX();
                    W = event.getRawY();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    windowStruct.setSize(windowStruct.getWidth(), windowStruct.getHeight());
                    H = -1;
                    W = -1;
                }
                return false;
            }
        });
        windowFrom.findViewById(com.jack8.floatwindow.R.id.menu).setOnClickListener(changeWindowController);
        windowFrom.findViewById(com.jack8.floatwindow.R.id.hide).setOnClickListener(changeWindowController);
        windowFrom.findViewById(com.jack8.floatwindow.R.id.mini).setOnClickListener(changeWindowController);
        windowFrom.findViewById(com.jack8.floatwindow.R.id.max).setOnClickListener(changeWindowController);
        windowFrom.findViewById(com.jack8.floatwindow.R.id.fullscreen).setOnClickListener(changeWindowController);
        windowFrom.findViewById(com.jack8.floatwindow.R.id.close_button).setOnClickListener(changeWindowController);
        TextView textView = new TextView(getContext());
        textView.setText(getString(R.string.window_control_win_content));
        ((ViewGroup)windowFrom.findViewById(com.jack8.floatwindow.R.id.wincon)).addView(textView);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(windowStruct != null && windowStruct.getCurrentState() != WindowStruct.State.FULLSCREEN){
            windowStruct.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(windowStruct != null){
            windowStruct.close();
        }
    }
}