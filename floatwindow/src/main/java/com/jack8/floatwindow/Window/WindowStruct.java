package com.jack8.floatwindow.Window;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.jack8.floatwindow.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WindowStruct implements View.OnClickListener,View.OnTouchListener{
//    private static int Index = 0;//計算視窗開啟數量

    final int Number;//視窗編號

    private final int parentWindowNumber;//父視窗編號
    private final HashSet<Integer> subWindowNumbers = new HashSet<>();//所有子視窗編號
    private static final int TITLE_LIFT_TO_EDGE_DISTANCE = 10;
    //static final int START_POINT = 60;//視窗預設座標
    private static final int FOCUS_FLAGE =  android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS//讓視窗超出螢幕
            | android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL//使可以操作視窗後方的物件
            | android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH//如果你已經設置了FLAG_NOT_TOUCH_MODAL,那麼你可以設置FLAG_WATCH_OUTSIDE_TOUCH這個flag, 這樣一個點擊事件如果發生在你的window之外的範圍,你就會接收到一個特殊的MotionEvent,MotionEvent.ACTION_OUTSIDE 注意,你只會接收到點擊事件的第一下,而之後的DOWN/MOVE/UP等手勢全都不會接收到
            ;
    private static final int NO_FOCUS_FLAGE =  android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS//讓視窗超出螢幕
            | android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            ;
    private static final int NO_FOCUS_FLAGE_FOR_MINI_STATE =  android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

    private int top,left,height,width;//視窗的座標及大小

    private Context context;
    //private WindowColor wColor;//視窗顏色
    private android.view.WindowManager wm;
    private android.view.WindowManager.LayoutParams wmlp;
    private ScreenSize screenSize;
    private WindowAction windowAction;
    private View winform;//視窗外框
    private ViewGroup wincon;//視窗內容框
    private View[] winconPage;//視窗子頁面
    private int transitionsDuration;//動畫持續時間
    private int currentWindowPagePosition = 0;
    private Scroller topMini,heightMini;
    private Button menu,close_button,mini,max,hide,fullscreen;
    private LinearLayout sizeBar,titleBarAndButtons,microMaxButtonBackground;
    private TextView title;
    private String[] windowTitle;

    private int buttonsHeight;
    private int buttonsWidth;
    private int sizeBarHeight;
    private int buttonWidthForMiniState;
    private int buttonHeightForMiniState;

    private MenuList menuList;

    private constructionAndDeconstructionWindow CDAW;

    private FullscreenWindowActivity fullscreenWindowActivity;

    private final ArrayList<OnWindowTitleChangeListener> onWindowTitleChangeListenerList = new ArrayList<>();
    private final ArrayList<OnWindowStateChangeListener> onWindowStateChangeListenerList = new ArrayList<>();
    private final ArrayList<OnWindowSizeChangeListener> onWindowSizeChangeListenerList = new ArrayList<>();
    private final ArrayList<OnWindowPositionChangeListener> onWindowPositionChangeListenerList = new ArrayList<>();

    //------------可隱藏或顯示的控制項物件------------------
    private int display_object;
    public static final int ALL_NOT_DISPLAY = 0x00;
    public static final int MENU_BUTTON = 0x01;
    public static final int HIDE_BUTTON = 0x02;
    public static final int MINI_BUTTON = 0x04;
    public static final int MAX_BUTTON = 0x08;
    public static final int CLOSE_BUTTON = 0x10;
    public static final int SIZE_BAR = 0x20;
    public static final int TITLE_BAR_AND_BUTTONS = 0x40;
    public static final int FULLSCREEN_BUTTON = 0x80;
//-------------------------------------------------------

    public final int MINI_WINDOW_WIDTH = 30;

    public enum State{
        GENERAL(0),
        MAX(1),
        MINI(2),
        HIDE(3),
        FULLSCREEN(4),
        CLOSE(5);

        private int type;

        State(int type){
            this.type = type;
        }

        public int getType(){
            return type;
        }

        public static State getStateByTypeNumber(int type){
            switch (type){
                case 0:
                    return GENERAL;
                case 1:
                    return MAX;
                case 2:
                    return MINI;
                case 3:
                    return HIDE;
                case 4:
                    return FULLSCREEN;
                case 5:
                    return CLOSE;
                default:
                    return null;
            }
        }
    }
    private State nowState = State.HIDE;//當前狀態
    private State previousState = null;//前一次的狀態


    /**
     * 建構WindowStruct的工廠模式
     */
    public static class Builder{
        private Context context;
        private android.view.WindowManager windowManager;
        private View[] windowPages;
        private String[] windowPageTitles = new String[]{""};
        private Map<String, Object> windowInitArgs = new HashMap<>();
        private int top;
        private int left;
        private int height;
        private int width;
        private int displayObject = TITLE_BAR_AND_BUTTONS | MENU_BUTTON | HIDE_BUTTON | MINI_BUTTON | MAX_BUTTON | CLOSE_BUTTON | SIZE_BAR | FULLSCREEN_BUTTON;
        private int transitionsDuration = 500;
        private int parentWindowNumber = -1;
        private int buttonsHeight;
        private int buttonsWidth;
        private int sizeBarHeight;
        private int buttonWidthForMiniState;
        private int buttonHeightForMiniState;
        private State openState = State.GENERAL;
        private WindowAction windowAction = new WindowAction() {
            @Override
            public void goHide(WindowStruct windowStruct) {

            }

            @Override
            public void goClose(WindowStruct windowStruct) {

            }
        };
        private constructionAndDeconstructionWindow constructionAndDeconstructionWindow = new constructionAndDeconstructionWindow() {
            @Override
            public void Construction(Context context, View pageView, int position, Map<String, Object> args, WindowStruct windowStruct) {

            }

            @Override
            public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct) {

            }

            @Override
            public void onResume(Context context, View pageView, int position, WindowStruct windowStruct) {

            }

            @Override
            public void onPause(Context context, View pageView, int position, WindowStruct windowStruct) {

            }
        };
        private ScreenSize screenSize;

        public Builder(Context context, final android.view.WindowManager windowManager){
            this.context = context;
            this.windowManager = windowManager;
            this.buttonsHeight = this.buttonsWidth = this.buttonHeightForMiniState = this.buttonWidthForMiniState = (int)(context.getResources().getDisplayMetrics().density*30);
            this.sizeBarHeight = (int)(context.getResources().getDisplayMetrics().density*10);
            this.windowPages = new View[]{new LinearLayout(context)};
            this.screenSize = new ScreenSize(context){
                @Override
                public int getWidth() {
                    return super.context.getResources().getDisplayMetrics().widthPixels;
                }

                @Override
                public int getHeight() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
                        if(super.context.getResources().getDisplayMetrics().heightPixels > super.context.getResources().getDisplayMetrics().widthPixels)
                            return displayMetrics.heightPixels - getStatusBarHeight() - getNavigationBarHeight();
                    }
                    return super.context.getResources().getDisplayMetrics().heightPixels - getStatusBarHeight();
                }
            };
            this.height = (int)(context.getResources().getDisplayMetrics().density * 240);
            this.width = (int)(context.getResources().getDisplayMetrics().density * 200);
            this.top = screenSize.getHeight() / 2 - height / 2;
            this.left = screenSize.getWidth() / 2 - width / 2;
        }
        public Builder windowPages(View[] windowPages){
            this.windowPages = windowPages;
            return this;
        }
        public Builder windowPageTitles(String[] windowPageTitles){
            this.windowPageTitles = windowPageTitles;
            return this;
        }
        public Builder windowInitArgs(Map<String, Object> windowInitArgs){
            this.windowInitArgs = windowInitArgs;
            return this;
        }
        public Builder top(int top){
            this.top = top;
            return this;
        }
        public Builder left(int left){
            this.left = left;
            return this;
        }
        public  Builder height(int height){
            this.height = height;
            return this;
        }
        public Builder heightAndTopAutoCenter(int height){
            this.height = height;
            this.top = screenSize.getHeight() / 2 - height / 2;
            return this;
        }
        public Builder width(int width){
            this.width = width;
            return this;
        }
        public Builder widthAndLeftAutoCenter(int width){
            this.width = width;
            this.left = screenSize.getWidth() / 2 - width / 2;
            return this;
        }
        public Builder windowButtonsWidth(int buttonsWidth){
            this.buttonsWidth = buttonsWidth;
            return this;
        }
        public Builder windowButtonsHeight(int buttonsHeight){
            this.buttonsHeight = buttonsHeight;
            return this;
        }
        public Builder windowSizeBarHeight(int sizeBarHeight){
            this.sizeBarHeight = sizeBarHeight;
            return this;
        }
        public Builder windowButtonWidthForMiniState(int buttonWidthForMiniState){
            this.buttonWidthForMiniState = buttonWidthForMiniState;
            return this;
        }
        public Builder windowButtonHeightForMiniState(int buttonHeightForMiniState){
            this.buttonHeightForMiniState = buttonHeightForMiniState;
            return this;
        }
        public Builder displayObject(int displayObject){
            this.displayObject = displayObject;
            return this;
        }
        public Builder transitionsDuration(int transitionsDuration){
            this.transitionsDuration = transitionsDuration;
            return this;
        }
        public Builder windowAction(WindowAction windowAction){
            this.windowAction = windowAction;
            return this;
        }
        public Builder constructionAndDeconstructionWindow(constructionAndDeconstructionWindow constructionAndDeconstructionWindow){
            this.constructionAndDeconstructionWindow = constructionAndDeconstructionWindow;
            return this;
        }
        public Builder openState(State openState){
            this.openState = openState;
            return this;
        }
        public Builder windowPages(int[] windowPagesForLayoutResources){
            this.windowPages = new View[windowPagesForLayoutResources.length];
            View wincon = LayoutInflater.from(context).inflate(R.layout.window,null).findViewById(R.id.wincon);
            for(int i = 0;i<windowPages.length;i++) {
                this.windowPages[i] = LayoutInflater.from(context).inflate(windowPagesForLayoutResources[i], (ViewGroup) wincon, false);
                //windowPages[i].setTag(windowPageTitles[i]);
            }
            return this;
        }

        public Builder screenSize(ScreenSize screenSize){
            this.screenSize = screenSize;
            return this;
        }

        public ScreenSize getScreenSize(){
            return screenSize;
        }

        public Builder parentWindow(WindowStruct parentWindow){
            this.parentWindowNumber = parentWindow.Number;
            return this;
        }
        public WindowStruct show(){
            return new WindowStruct(context, windowManager, windowPages, windowPageTitles, windowInitArgs, top, left, height, width, buttonsHeight, buttonsWidth, sizeBarHeight, buttonHeightForMiniState, buttonWidthForMiniState, displayObject, transitionsDuration, screenSize, windowAction, constructionAndDeconstructionWindow, parentWindowNumber, openState);
        }
    }

    /**
     * 建立一個浮動視窗
     * @param context Activity 或 Servict的context
     * @param wm WindowManager用來管理window
     * @param windowPages 浮動視窗所有子頁面的View
     * @param windowPageTitles 所有子頁面的標題
     * @param windowInitArgs 初始化視窗用的參數
     * @param Top 浮動視窗一開始顯示的位置的Top
     * @param Left 浮動視窗一開始顯示的位置的Left
     * @param Height 浮動視窗一開始高度
     * @param Width 浮動視窗一開始寬度
     * @param buttonsHeight 視窗按鈕高度
     * @param buttonsWidth 視窗按鈕寬度
     * @param sizeBarHeight 視窗大小調整列高度
     * @param buttonHeightForMiniState 最小化狀態視窗按鈕高度
     * @param buttonWidthForMiniState 最小化狀態視窗按鈕寬度
     * @param display_object 表示要顯示那些windows控制物件
     * @param transitionsDuration 過場動畫持續時間
     * @param windowAction 按下隱藏或關閉視窗按鈕時要處理的事件
     * @param CDAW 浮動視窗初始化與結束時的事件
     */
    public WindowStruct(Context context, android.view.WindowManager wm, View[] windowPages, String[] windowPageTitles , Map<String, Object> windowInitArgs , int Top, int Left, int Height, int Width, int buttonsHeight, int buttonsWidth, int sizeBarHeight, int buttonHeightForMiniState, int buttonWidthForMiniState, final int display_object, int transitionsDuration, ScreenSize screenSize, WindowAction windowAction, constructionAndDeconstructionWindow CDAW, int parentWindowNumber, State openState){
        if(WindowManager.windowList.containsKey(WindowManager.focusedWindowNumber))
            WindowManager.getWindowStruct(WindowManager.focusedWindowNumber).unFocusWindow();
        WindowManager.focusedWindowNumber = this.Number = WindowManager.getMiniFreeNumber();
        Log.d("new window id", String.valueOf(this.Number));
        WindowManager.addWindowStruct(this);
        this.context = context;
        this.wm = wm;
        this.winconPage = windowPages;
        this.windowAction = windowAction;
        this.windowTitle = windowPageTitles;
        this.CDAW = CDAW;
        this.display_object = display_object;
        this.transitionsDuration = transitionsDuration;
        this.screenSize = screenSize;
        this.buttonsWidth = buttonsWidth;
        this.buttonsHeight = buttonsHeight;
        this.sizeBarHeight = sizeBarHeight;
        this.buttonHeightForMiniState = buttonHeightForMiniState;
        this.buttonWidthForMiniState = buttonWidthForMiniState;
        topMini = new Scroller(context);
        heightMini = new Scroller(context);
        Top = Math.max(Top, 0);

        //wColor = new WindowColor(context);

        wmlp = new android.view.WindowManager.LayoutParams();
        wmlp.type = (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                ?android.view.WindowManager.LayoutParams.TYPE_PHONE
                :android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;//類型
        wmlp.format = PixelFormat.TRANSPARENT;//背景(透明)
        wmlp.flags = FOCUS_FLAGE;
        wmlp.gravity = Gravity.LEFT | Gravity.TOP;//設定重力(初始位置)
        wmlp.x = screenSize.getWidth() / 2;
        wmlp.y = screenSize.getHeight() / 2;//設定原點座標
        wmlp.width = 0;//設定視窗大小
        wmlp.height = 0;

        winform = LayoutInflater.from(context).inflate(R.layout.window,null);
        ((WindowFrom)winform).setWindowStruct(this);
        winform.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {//當點擊視窗以外的地方時，必須配和WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH以及WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL一同使用
                    WindowStruct.this.unFocusWindow();
                    return true;
                }
                return false;
            }
        });
        /*winform.setOnTouchListener(new View.OnTouchListener() {
            View titleBar = winform.findViewById(R.id.title_bar),
                    microMaxButtonBackground = winform.findViewById(R.id.micro_max_button_background),
                    closeButtonBackground = winform.findViewById(R.id.close_button_background);
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect rect = new Rect();
                v.getGlobalVisibleRect(rect);//取得視窗所在的範圍
                if (!rect.contains((int) event.getX(), (int) event.getY())) {//當Touch的點在視窗範圍外
                    wmlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//讓視窗不可聚焦
                    WindowStruct.this.wm.updateViewLayout(winform, wmlp);
                    titleBar.setBackgroundColor(wColor.getWindowNotFoucs());
                    sizeBar.setBackgroundColor(wColor.getWindowNotFoucs());
                    microMaxButtonBackground.setBackgroundColor(wColor.getWindowNotFoucs());
                    closeButtonBackground.setBackgroundColor(wColor.getWindowNotFoucs());
                }
                return false;
            }
        });*/
        wm.addView(winform,wmlp);
        wincon = (ViewGroup) winform.findViewById(R.id.wincon);

        top = Top;
        left = Left;
        width = Width;
        height = Height;

        menu = winform.findViewById(R.id.menu);
        close_button = winform.findViewById(R.id.close_button);
        mini = winform.findViewById(R.id.mini);
        max = winform.findViewById(R.id.max);
        hide = winform.findViewById(R.id.hide);
        fullscreen = winform.findViewById(R.id.fullscreen);
        title = winform.findViewById(R.id.title);
        sizeBar = winform.findViewById(R.id.size);
        titleBarAndButtons = winform.findViewById(R.id.title_bar_and_buttons);
        microMaxButtonBackground = winform.findViewById(R.id.micro_max_button_background);

        //---------------------------設定視窗按鈕大小-------------------------------
        setWindowDisplayObjectSize();
        //--------------------------------------------------------------------------

        //-------------------------建立視窗內容畫面----------------------------------------------------
        wincon.addView(winconPage[0]);
        setWindowTitle(windowPageTitles[0]);
        title.setOnTouchListener(this);
        title.setOnClickListener(this);//還原視窗大小
        close_button.setOnClickListener(this);
        winform.findViewById(R.id.size).setOnTouchListener(new View.OnTouchListener() {
            float W =- 1,H =- 1;
            @Override
            public boolean onTouch(View v, MotionEvent event) {//調整視窗大小
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (H == -1 || W == -1) {
                        W = event.getRawX();
                        H = event.getRawY();
                        return true;
                    }
                    setSize(getWidth() - (int) (W - event.getRawX()), getHeight() - (int) (H - event.getRawY()), true);
                    W = event.getRawX();
                    H = event.getRawY();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    setSize(getWidth(), getHeight());
                    W = -1;
                    H = -1;
                }
                return true;
            }
        });
        //-----------------------------------------------------------------------------
        //-------------------------建立Menu-------------------------------
        /*final PopupMenu pm = new PopupMenu(context,menu);
        Menu m = pm.getMenu();
        for(int i = 0;i<winconPage.length;i++)
            //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
            m.add(0,i,i,(String)winconPage[i].getTag());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            int ViweIndex = 0;
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                currentWindowPagePosition = item.getItemId();
                wincon.removeView(winconPage[ViweIndex]);
                wincon.addView(winconPage[item.getItemId()]);
                title.setText(windowTitle[item.getItemId()]);
                ViweIndex = item.getItemId();
                return true;
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pm.show();
            }
        });*/
        menuList = new MenuList(windowPageTitles);
        menu.setOnClickListener(menuList);
        //------------------------------------------------------------------
        //---------------------------縮到最小與最大按鈕---------------------------
        hide.setOnClickListener(this);
        mini.setOnClickListener(this);
        max.setOnClickListener(this);
        fullscreen.setOnClickListener(this);
        //------------------------------------------------------------------
        CDAW.onCreate(context, windowInitArgs, this);
        //---------------------------初始化視窗內容-------------------------------
        for(int i = 0;i<winconPage.length;i++)
            CDAW.Construction(context,winconPage[i], i, windowInitArgs, this);
        CDAW.onResume(context,winconPage[0],0,this);
        //---------------------------------------------------------------------------------------------
        CDAW.onPageInitialized(context, this);
        //---------------------------隱藏不顯示的控制項物件------------------------------------------
        setDisplayObject();
        //-------------------------------------------------------------------------------------
        //---------------------------視窗開啟動畫------------------------------------------------------
//        topMini.startScroll(screenSize.getWidth() / 2, screenSize.getHeight() / 2 ,left - screenSize.getWidth() / 2, top - screenSize.getHeight() / 2, transitionsDuration);
//        heightMini.startScroll(0, 0, width, height, transitionsDuration);
//        Utilities.uiThread.post(new runTransitions(nowState));
        switch (openState){
            case HIDE:
                hide();
                break;
            case MINI:
                mini();
                break;
            case GENERAL:
                general();
                break;
            case MAX:
                max();
                break;
            case FULLSCREEN:
                fullscreen();
                break;
        }
        //---------------------------------------------------------------------------------------------
        this.parentWindowNumber = parentWindowNumber;
        if(parentWindowNumber != -1)
            WindowManager.getWindowStruct(parentWindowNumber).subWindowNumbers.add(Number);
        CDAW.onFocus(context, this);//防止視窗閃爍，所以不呼叫focusWindow()
    }

    public interface WindowAction{
        void goHide(WindowStruct windowStruct);//當按下隱藏視窗按鈕
        void goClose(WindowStruct windowStruct);//當按下關閉視窗按鈕
    }

    public static class constructionAndDeconstructionWindow{
        public void onCreate(Context context, Map<String, Object> args, WindowStruct windowStruct){}
        public void onDestroy(Context context, WindowStruct windowStruct){}

        public void Construction(Context context, View pageView, int position, Map<String, Object> args, WindowStruct windowStruct){}
        public void onPageInitialized(Context context, WindowStruct windowStruct){}
        public void Deconstruction(Context context, View pageView, int position, WindowStruct windowStruct){}

        public void onResume(Context context, View pageView, int position, WindowStruct windowStruct){}
        public void onPause(Context context, View pageView, int position, WindowStruct windowStruct){}

        public void onFocus(Context context, WindowStruct windowStruct){}
        public void onUnFocus(Context context, WindowStruct windowStruct){}
    }

    public interface OnWindowTitleChangeListener{
        void onTitleChanged(Context context, WindowStruct windowStruct, String title);
    }
    public interface OnWindowStateChangeListener{
        void onStateChanged(Context context, WindowStruct windowStruct);
    }
    public interface OnWindowSizeChangeListener{
        void onSizeChanged(Context context, WindowStruct windowStruct);
    }
    public interface OnWindowPositionChangeListener{
        void onPositionChanged(Context context, WindowStruct windowStruct);
    }

    public static abstract class ScreenSize{
        public abstract int getWidth();
        public abstract int getHeight();

        protected Context context;

        public ScreenSize(Context context){
            this.context = context;
        }

        /**
             * 取得狀態列的高度
             * @return 狀態列的高度
             */
        protected int getStatusBarHeight() {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
            return 0;
        }

        /**
             * 取得導覽的高度
             * @return 狀態列的高度
             */
        protected int getNavigationBarHeight() {
            if(!hasNavigationBar())
                return 0;
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
            return 0;
        }

        /**
             * 判斷是否有導覽列(在虛擬機上會回傳false，來源：https://stackoverflow.com/questions/28983621/detect-soft-navigation-bar-availability-in-android-device-progmatically)
             * @return 是否有導覽列
             */
        protected boolean hasNavigationBar(){
            int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
            return id > 0 && context.getResources().getBoolean(id);
        }
    }
    private class MenuList implements View.OnClickListener,AdapterView.OnItemClickListener,Runnable{
        private ListView menu;
        private LinearLayout menuListAndContext;
        private Scroller scroller = new Scroller(context);
        private boolean isOpen = false;
        public MenuList(final String[] menuItems){
            menuListAndContext = (LinearLayout) winform.findViewById(R.id.menu_list_and_context);
            menu = (ListView) winform.findViewById(R.id.menu_list);
            menu.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return menuItems.length;
                }

                @Override
                public Object getItem(int position) {
                    return menuItems[position];
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if(convertView == null){
                        convertView = LayoutInflater.from(context).inflate(R.layout.hide_menu_item,null,false);
                        TextView itenText = (TextView) convertView.findViewById(R.id.item_text);
                        itenText.setText(menuItems[position]);
                    }
                    return convertView;
                }
            });
            menu.setOnItemClickListener(this);
        }
        public void showMenu(){
            isOpen = true;
            scroller.startScroll(0,0,-menu.getLayoutParams().width,0);
            Utilities.uiThread.post(this);
        }
        public void closeMenu(){
            isOpen = false;
            scroller.startScroll(-menu.getLayoutParams().width,0,menu.getLayoutParams().width,0);
            Utilities.uiThread.post(this);
        }
        public void showPage(int position){
            CDAW.onPause(context,winconPage[currentWindowPagePosition],currentWindowPagePosition,WindowStruct.this);
            wincon.removeView(winconPage[currentWindowPagePosition]);
            wincon.addView(winconPage[position]);
            CDAW.onResume(context,winconPage[position],position,WindowStruct.this);
            currentWindowPagePosition = position;
            setWindowTitle(windowTitle[position]);
        }
        @Override
        public void onClick(View v) {
            Log.i("startMenu",isOpen+","+menu.getLayoutParams().width);
            if(!isOpen)
                showMenu();
            else
                closeMenu();
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            showPage(position);
            closeMenu();
        }

        @Override
        public void run() {
            if(scroller.computeScrollOffset()) {
                menuListAndContext.scrollTo(scroller.getCurrX(), scroller.getCurrY());
                Utilities.uiThread.post(this);
            }else
                menuListAndContext.invalidate();
        }
    }

    /*
    移動視窗
    關於getX,getY等等的含數用法:
    http://blog.csdn.net/u013872857/article/details/53750682
     */
    private float H =- 1,W =- 1;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(nowState == State.GENERAL || nowState == State.MINI) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (H == -1 || W == -1) {
                    W = event.getRawX();
                    H = event.getRawY();
                    return true;
                }
                setPosition(getPositionX()-(int) (W-event.getRawX()),getPositionY()-(int) (H-event.getRawY()), true);
                W = event.getRawX();
                H = event.getRawY();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                setPosition(getRealPositionX(), getRealPositionY());
                W = -1;
                H= -1;
            }
    }
        return false;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.mini) {//最小化
            mini();
        } else if (i == R.id.max) {//最大化或一般大小
            switch (nowState) {
                case MAX:
                case FULLSCREEN:
                    general();
                    break;
                default:
                    max();
            }
        } else if (i == R.id.title) {//還原視窗大小
            if (nowState == State.MINI) {
                if (previousState == State.MAX)
                    max();
                else if (previousState == State.FULLSCREEN)
                    fullscreen();
                else
                    general();
            }
        } else if (i == R.id.close_button) {//關閉視窗
            close();
        } else if (i == R.id.hide) {//隱藏視窗
            hide();
        } else if (i == R.id.fullscreen){//全螢幕
            if(nowState == State.FULLSCREEN)
                max();
            else
                fullscreen();
        }
    }

    private class RunTransitions implements Runnable{//執行轉場動畫
        private State state, previousState;

        public RunTransitions(State state, State previousState){
            this.state = state;
            this.previousState = previousState;
        }

        @Override
        public void run() {
            if(state != nowState){//當transitionsDuration很大時，使用者很有機會在轉場動畫還沒播完時就按下其他狀態按鈕
                if(nowState != State.MINI && nowState != State.HIDE && (previousState == State.MINI || previousState == State.HIDE)){
                    wincon.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                    wincon.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                return;//當轉場動畫還沒播完時狀態就改變時，取消狀態就改變前動畫
            }
            if (transitionsDuration == 0) {
                //abortAnimation可以使Scroller終止動畫跳過滾動過程直接給出最後數值
                topMini.abortAnimation();
                heightMini.abortAnimation();
                wmlp.x = topMini.getCurrX();
                wmlp.y = topMini.getCurrY();
                winform.getLayoutParams().width = heightMini.getCurrX();
                winform.getLayoutParams().height = heightMini.getCurrY();
                wm.updateViewLayout(winform, wmlp);
            }
            if (!topMini.isFinished() || !heightMini.isFinished()) {
                if (topMini.computeScrollOffset()) {
                    wmlp.x = topMini.getCurrX();
                    wmlp.y = topMini.getCurrY();
                }
                if (heightMini.computeScrollOffset()) {
                    winform.getLayoutParams().width = heightMini.getCurrX();
                    winform.getLayoutParams().height = heightMini.getCurrY();
                }
                wm.updateViewLayout(winform, wmlp);
                Utilities.uiThread.post(this);
            } else {
                invokeWindowStateChangeListener();
                if(previousState == State.MINI || previousState == State.HIDE){
                    wincon.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                    wincon.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    wincon.requestLayout();
                }
                if (state == State.HIDE) {
                    wmlp.alpha = 0.0f;
                    wm.updateViewLayout(winform, wmlp);
                } else if (state == State.FULLSCREEN) {
                    wm.removeView(winform);
                    toFullscreenActivity();
                } else if (state == State.CLOSE) {
                    CDAW.onPause(context, winconPage[currentWindowPagePosition], currentWindowPagePosition, WindowStruct.this);
                    for (int i = 0; i < winconPage.length; i++)
                        CDAW.Deconstruction(context, winconPage[i], i, WindowStruct.this);
                    CDAW.onDestroy(context, WindowStruct.this);
                    windowAction.goClose(WindowStruct.this);
                    wm.removeView(winform);
                    ((WindowFrom)winform).setWindowStruct(null);
                    ((WindowFrom)winform).setWindowKeyEvent(null);
                    removeAllListener();
                    if(WindowManager.focusedWindowNumber == Number)
                        WindowManager.focusedWindowNumber = WindowManager.NON_FOCUSED_WINDOW;
                    WindowManager.removeWindowStruct(WindowStruct.this);

                    wincon = null;
                    winconPage = null;
                    winform = null;
                    windowAction = null;
                    CDAW = null;
                    screenSize = null;
                    Log.d("closed window id", String.valueOf(Number));
                }
            }
        }
    }

    private void toFullscreenActivity(){
        Intent intent = new Intent(context, FullscreenWindowActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT /*| Intent.FLAG_ACTIVITY_MULTIPLE_TASK*/);//因為當FullscreenWindowActivity沒有成功啟動時，被檢查到再次起動時，如果有FLAG_ACTIVITY_MULTIPLE_TASK的話，Android系統會無條件在新的task上建立Activity，但是原本沒有啟動的Activity還在系統中被記著，因此如果使用者沒有觸發找回沒啟動成功的Activity(例如：進到多工畫面)的話，關閉因為檢查到沒開啟成功而再次開啟的Activity時，原本沒啟動成功被系統記著的Activity就會嘗試啟動，但那時候該Number的WindowStruct早已關閉並從WindowManager中移除了。
        }else{
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
//                intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FullscreenWindowActivity.WINDOW_NUMBER_EXTRA_NAME, getNumber());
        intent.setData(Uri.parse(String.valueOf(getNumber())));//一定要setData，這樣才能在要叫回這個Activity時成功找到
        context.startActivity(intent);
        Utilities.uiThread.postDelayed(new Runnable() {//在短時間內大量以FLAG_ACTIVITY_MULTIPLE_TASK以及FLAG_ACTIVITY_NEW_DOCUMENT啟動多個Activity，有很大的機率會有幾個Activity沒有啟動，因此這邊另位開個執行續做檢查
            @Override
            public void run() {
                if(nowState == State.FULLSCREEN && fullscreenWindowActivity == null) {
                    toFullscreenActivity();
                }
            }
        }, 1000);//1秒其實沒有特別意義，只是因為覺得開啟Activity應該在1秒內就會做完
    }

    /**
     * 視窗最小化
     */
    public void mini() {
        if (nowState != State.CLOSE && nowState != State.MINI) {
            previousState = nowState;
            nowState = State.MINI;
            setWindowButtonsSize(this.buttonHeightForMiniState, this.buttonsWidth);
            wincon.getLayoutParams().height = wincon.getHeight();
            wincon.getLayoutParams().width = wincon.getWidth();
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            if (previousState == State.MAX) {
                int dy;
                topMini.startScroll(0, 0, (screenSize.getWidth() - buttonsWidth), 0, transitionsDuration);
                heightMini.startScroll(screenSize.getWidth(),
                        dy = screenSize.getHeight(),
                        buttonWidthForMiniState - screenSize.getWidth(), -(dy - buttonHeightForMiniState), transitionsDuration);
            } else if (previousState == State.GENERAL) {
                topMini.startScroll(left, top, (screenSize.getWidth() - buttonsWidth) - left, -top, transitionsDuration);
                heightMini.startScroll(width, height, buttonWidthForMiniState - width, -(height - buttonHeightForMiniState), transitionsDuration);
            } else if (previousState == State.HIDE) {
                topMini.startScroll(screenSize.getWidth() / 2, screenSize.getHeight() / 2,
                        screenSize.getWidth() / 2 - buttonsWidth, -(screenSize.getHeight() / 2), transitionsDuration);
                heightMini.startScroll(0, 0, buttonWidthForMiniState, buttonHeightForMiniState, transitionsDuration);
            }else if(previousState == State.FULLSCREEN){
                if(fullscreenWindowActivity != null) {//如果最大化動畫還在播放中還沒進到Activity時，使用者就調用其他狀態時，fullscreenWindowActivity就會是null
                    fullscreenWindowActivity.exitFullscreen();
                    wm.addView(winform, wmlp);
                }
                int dy;
                topMini.startScroll(0, 0, (screenSize.getWidth() - buttonsWidth), 0, transitionsDuration);
                heightMini.startScroll(screenSize.getWidth(),
                        dy = screenSize.getHeight(),
                        buttonWidthForMiniState - screenSize.getWidth(), -(dy - buttonHeightForMiniState), transitionsDuration);
            }
            wmlp.flags = NO_FOCUS_FLAGE_FOR_MINI_STATE;
            wmlp.alpha = 1.0f;
            wm.updateViewLayout(winform, wmlp);
            hideButtons();
            Utilities.uiThread.post(new RunTransitions(nowState, previousState));
        }
    }

    /**
     * 視窗最大化
     */
    public void max(){
        if(nowState != State.CLOSE && nowState != State.MAX) {
            previousState = nowState;
            nowState = State.MAX;
            setWindowButtonsSize(this.buttonsHeight, this.buttonsWidth);
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                max.setBackground(context.getResources().getDrawable(R.drawable.mini_window));
                fullscreen.setBackground(context.getResources().getDrawable(R.drawable.fullscreen_window));
            } else {
                max.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mini_window));
                fullscreen.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.fullscreen_window));
            }
            if (previousState == State.GENERAL) {
                topMini.startScroll(left, top, -left, -top, transitionsDuration);
                heightMini.startScroll(width, height, screenSize.getWidth() - width,
                        screenSize.getHeight() - height, transitionsDuration);
            } else if(previousState == State.MINI){
                topMini.startScroll(wmlp.x, wmlp.y, -wmlp.x, -wmlp.y, transitionsDuration);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height
                        , screenSize.getWidth() - winform.getLayoutParams().width,
                        screenSize.getHeight() - winform.getLayoutParams().height, transitionsDuration);
            } else if(previousState == State.HIDE){
                topMini.startScroll(screenSize.getWidth() / 2, screenSize.getHeight() / 2 , -(screenSize.getWidth() / 2), -(screenSize.getHeight() / 2) , transitionsDuration);
                heightMini.startScroll(0,0, screenSize.getWidth(),screenSize.getHeight(), transitionsDuration);
            }else if(previousState == State.FULLSCREEN){
                //topMini不需要，因為在進入全螢幕時就已經跑過視窗左上角座標變成(0, 0)的動畫了
                heightMini.startScroll(screenSize.getWidth(), screenSize.getHeight(), 0, 0, transitionsDuration);
                if(fullscreenWindowActivity != null) {//如果最大化動畫還在播放中還沒進到Activity時，使用者就調用其他狀態時，fullscreenWindowActivity就會是null
                    fullscreenWindowActivity.exitFullscreen();
                    wm.addView(winform, wmlp);
                }
            }
            wmlp.flags = FOCUS_FLAGE;
            wmlp.alpha =1.0f;
            wm.updateViewLayout(winform, wmlp);
            setDisplayObject();
            Utilities.uiThread.post(new RunTransitions(nowState, previousState));
        }
    }

    /**
     * 還原視窗大小
     */
    public void general(){
        if(nowState != State.CLOSE && nowState != State.GENERAL){
            previousState = nowState;
            nowState = State.GENERAL;
            setWindowButtonsSize(this.buttonsHeight, this.buttonsWidth);
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                max.setBackground(context.getResources().getDrawable(R.drawable.max_window));
                fullscreen.setBackground(context.getResources().getDrawable(R.drawable.fullscreen_window));
            } else {
                max.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.max_window));
                fullscreen.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.fullscreen_window));
            }
            if(previousState == State.MAX) {
                int dy;
                topMini.startScroll(0, 0, left, top, transitionsDuration);
                heightMini.startScroll( screenSize.getWidth(),
                        dy = screenSize.getHeight(),
                        width-screenSize.getWidth(), height-dy, transitionsDuration);
            }else if(previousState == State.MINI){
                topMini.startScroll(wmlp.x, wmlp.y, left - wmlp.x, top - wmlp.y, transitionsDuration);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height
                        , width - winform.getLayoutParams().width,
                        height - winform.getLayoutParams().height, transitionsDuration);
            }else if(previousState == State.HIDE){
                topMini.startScroll(screenSize.getWidth() / 2, screenSize.getHeight() / 2 ,left - screenSize.getWidth() / 2, top - screenSize.getHeight() / 2, transitionsDuration);
                heightMini.startScroll(0, 0, width, height, transitionsDuration);
            }else if(previousState == State.FULLSCREEN){
                if(fullscreenWindowActivity != null) {//如果最大化動畫還在播放中還沒進到Activity時，使用者就調用其他狀態時，fullscreenWindowActivity就會是null
                    fullscreenWindowActivity.exitFullscreen();
                    wm.addView(winform, wmlp);
                }
                int dy;
                topMini.startScroll(0, 0, left, top, transitionsDuration);
                heightMini.startScroll( screenSize.getWidth(),
                    dy = screenSize.getHeight(),
                    width-screenSize.getWidth(), height-dy, transitionsDuration);
            }
            wmlp.flags = FOCUS_FLAGE;
            wmlp.alpha =1.0f;
            wm.updateViewLayout(winform, wmlp);
            setDisplayObject();
            Utilities.uiThread.post(new RunTransitions(nowState, previousState));
        }
    }

    /**
     * 視窗隱藏
     */
    public void hide(){
        if(nowState != State.CLOSE && nowState != State.HIDE) {
            previousState = nowState;
            nowState = State.HIDE;
            setWindowButtonsSize(this.buttonsHeight, this.buttonsWidth);
            wincon.getLayoutParams().height = wincon.getHeight();
            wincon.getLayoutParams().width = wincon.getWidth();
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            if (previousState == State.MAX) {
                topMini.startScroll(0, 0, screenSize.getWidth() / 2, screenSize.getHeight() / 2, transitionsDuration);
                heightMini.startScroll(screenSize.getWidth(), screenSize.getHeight()
                        , -screenSize.getWidth(), -(screenSize.getHeight()), transitionsDuration);
            } else if(previousState == State.GENERAL){
                topMini.startScroll(left, top, screenSize.getWidth() / 2 - left, screenSize.getHeight() / 2 - top, transitionsDuration);
                heightMini.startScroll(width, height,
                        -width, -height, transitionsDuration);
            }else if(previousState == State.MINI){
                topMini.startScroll(wmlp.x, wmlp.y, screenSize.getWidth() / 2 - wmlp.x, screenSize.getHeight() / 2 - wmlp.y, transitionsDuration);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height,
                        -winform.getLayoutParams().width, -winform.getLayoutParams().height, transitionsDuration);
            }else if(previousState == State.FULLSCREEN){
                if(fullscreenWindowActivity != null) {//如果最大化動畫還在播放中還沒進到Activity時，使用者就調用其他狀態時，fullscreenWindowActivity就會是null
                    fullscreenWindowActivity.exitFullscreen();
                    wm.addView(winform, wmlp);
                }
                topMini.startScroll(0, 0, screenSize.getWidth() / 2, screenSize.getHeight() / 2, transitionsDuration);
                heightMini.startScroll(screenSize.getWidth(), screenSize.getHeight()
                        , -screenSize.getWidth(), -(screenSize.getHeight()), transitionsDuration);
            }
            wmlp.flags = NO_FOCUS_FLAGE;
            wm.updateViewLayout(winform, wmlp);
            windowAction.goHide(this);
            Utilities.uiThread.post(new RunTransitions(nowState, previousState));
        }
    }

    /**
     * 全螢幕
     */
    public void fullscreen(){
        if(nowState != State.CLOSE && nowState != State.FULLSCREEN) {
            previousState = nowState;
            nowState = State.FULLSCREEN;
            setWindowButtonsSize(this.buttonsHeight, this.buttonsWidth);
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                max.setBackground(context.getResources().getDrawable(R.drawable.mini_window));
                fullscreen.setBackground(context.getResources().getDrawable(R.drawable.max_window));
            } else {
                max.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mini_window));
                max.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.max_window));
            }
            if (previousState == State.GENERAL) {
                topMini.startScroll(left, top, -left, -top, transitionsDuration);
                heightMini.startScroll(width, height, screenSize.getWidth() - width,
                        screenSize.getHeight() - height, transitionsDuration);
            } else if(previousState == State.MINI){
                topMini.startScroll(wmlp.x, wmlp.y, -wmlp.x, -wmlp.y, transitionsDuration);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height
                        , screenSize.getWidth() - winform.getLayoutParams().width,
                        screenSize.getHeight() - winform.getLayoutParams().height, transitionsDuration);
            } else if(previousState == State.HIDE){
                topMini.startScroll(screenSize.getWidth() / 2, screenSize.getHeight() / 2 , -(screenSize.getWidth() / 2), -(screenSize.getHeight() / 2) , transitionsDuration);
                heightMini.startScroll(0,0, screenSize.getWidth(),screenSize.getHeight(), transitionsDuration);
            }
//            else if(previousState == State.MAX){
//
//            }
            wmlp.flags = FOCUS_FLAGE;
            wmlp.alpha =1.0f;
            wm.updateViewLayout(winform, wmlp);
            setDisplayObject();
            Utilities.uiThread.post(new RunTransitions(nowState, previousState));
        }
    }

    /**
     * 視窗關閉
     */
    public void close(){
        if(nowState != State.CLOSE) {
            previousState = nowState;
            nowState = State.CLOSE;
            titleBarAndButtons.getLayoutParams().height = buttonsHeight;
            if(parentWindowNumber != -1)
                WindowManager.getWindowStruct(this.parentWindowNumber).subWindowNumbers.remove(Number);
            for(int key : this.subWindowNumbers.toArray(new Integer[this.subWindowNumbers.size()]))
                if(WindowManager.windowIn(key))
                    WindowManager.getWindowStruct(key).close();
            if(!topMini.isFinished())
                topMini.abortAnimation();
            if(!heightMini.isFinished())
                heightMini.abortAnimation();
            if (previousState == State.MAX) {
                topMini.startScroll(0, 0, screenSize.getWidth() / 2, screenSize.getHeight() / 2, transitionsDuration);
                heightMini.startScroll(screenSize.getWidth(), screenSize.getHeight()
                        , -screenSize.getWidth(), -(screenSize.getHeight()), transitionsDuration);
            } else if(previousState == State.GENERAL){
                topMini.startScroll(left, top, screenSize.getWidth() / 2 - left, screenSize.getHeight() / 2 - top, transitionsDuration);
                heightMini.startScroll(width, height,
                        -width, -height, transitionsDuration);
            }else if(previousState == State.MINI){
                topMini.startScroll(wmlp.x, wmlp.y, screenSize.getWidth() / 2 - wmlp.x, screenSize.getHeight() / 2 - wmlp.y, transitionsDuration);
                heightMini.startScroll(winform.getLayoutParams().width, winform.getLayoutParams().height,
                        -winform.getLayoutParams().width, -winform.getLayoutParams().height, transitionsDuration);
            }else if(previousState == State.FULLSCREEN){
                if(fullscreenWindowActivity != null) {//如果最大化動畫還在播放中還沒進到Activity時，使用者就調用其他狀態時，fullscreenWindowActivity就會是null
                    fullscreenWindowActivity.exitFullscreen();
                }
                if(winform.getParent() == null){
                    //當執行視窗恢復時，會在短時間內連續快速開啟多個Activity，有機會讓啟動一個Activity的Intent被另一個Intent給取代掉，導致在多工畫面中不會顯示全部的全螢幕視窗，
                    //而那些沒有啟動到Activity的視窗，他們的fullscreenWindowActivity就會是null，但是他們卻已經執行了wm.removeView(winform);，
                    //所以如果使用者沒有從視窗清單中把視窗找回，然後直接按下通知中心的關閉FloatWindow按鈕，就會引發
                    //Fatal Exception: java.lang.IllegalArgumentException: View=com.jack8.floatwindow.Window.WindowFrom{1c51e66 V.E...... ......ID 0,0-1080,1936 #7f09023c app:id/window} not attached to window manager
                    //因此這邊才需要判斷winform的父層是否存在
                    //-------------------------------------
                    //目前已經增加toFullscreenActivity函數會在啟動Activity後的一段時間檢查Activity是否成功啟動，但以防萬一，wm.addView(winform, wmlp)就不移回if(winform.getParent() == null)內了
                    wm.addView(winform, wmlp);
                }
                topMini.startScroll(0, 0, screenSize.getWidth() / 2, screenSize.getHeight() / 2, transitionsDuration);
                heightMini.startScroll(screenSize.getWidth(), screenSize.getHeight()
                        , -screenSize.getWidth(), -(screenSize.getHeight()), transitionsDuration);
            }
            Utilities.uiThread.post(new RunTransitions(nowState, previousState));
        }
    }

    /**
     * 隱藏所有按鈕控制項
     */
    private void hideButtons(){
        if((display_object & MENU_BUTTON) == MENU_BUTTON)
            menu.setVisibility(View.GONE);
        else
            title.setPadding(0,0,0,0);
        microMaxButtonBackground.setVisibility(View.GONE);
        if((display_object & TITLE_BAR_AND_BUTTONS) != TITLE_BAR_AND_BUTTONS)
            titleBarAndButtons.setVisibility(View.VISIBLE);
        if((display_object & MINI_BUTTON) == MINI_BUTTON)
            mini.setVisibility(View.GONE);
        if((display_object & MAX_BUTTON) == MAX_BUTTON)
            max.setVisibility(View.GONE);
        if((display_object & HIDE_BUTTON) == HIDE_BUTTON)
            hide.setVisibility(View.GONE);
        if((display_object & CLOSE_BUTTON) == CLOSE_BUTTON)
            close_button.setVisibility(View.GONE);
        if((display_object & SIZE_BAR) == SIZE_BAR)
            sizeBar.setVisibility(View.GONE);
        if((display_object & FULLSCREEN_BUTTON) == FULLSCREEN_BUTTON)
            fullscreen.setVisibility(View.GONE);
    }

//    /**
//     * 顯示所有按鈕控制項
//     */
//    private void recoveryButtons(){
//        if((display_object & MENU_BUTTON) == MENU_BUTTON)
//            menu.setVisibility(View.VISIBLE);
//        else
//            title.setPadding((int)(context.getResources().getDisplayMetrics().density*TITLE_LIFT_TO_EDGE_DISTANCE),0,0,0);
//        microMaxButtonBackground.setVisibility(View.VISIBLE);
//        if((display_object & TITLE_BAR_AND_BUTTONS) == TITLE_BAR_AND_BUTTONS)
//            titleBarAndButtons.setVisibility(View.VISIBLE);
//        else
//            titleBarAndButtons.setVisibility(View.GONE);
//        if((display_object & MINI_BUTTON) == MINI_BUTTON)
//            mini.setVisibility(View.VISIBLE);
//        if((display_object & MAX_BUTTON) == MAX_BUTTON)
//            max.setVisibility(View.VISIBLE);
//        if((display_object & HIDE_BUTTON) == HIDE_BUTTON)
//            hide.setVisibility(View.VISIBLE);
//        if((display_object & CLOSE_BUTTON) == CLOSE_BUTTON)
//            close_button.setVisibility(View.VISIBLE);
//        if((display_object & SIZE_BAR) == SIZE_BAR)
//            sizeBar.setVisibility(View.VISIBLE);
//        if((display_object & FULLSCREEN_BUTTON) == FULLSCREEN_BUTTON)
//            fullscreen.setVisibility(View.VISIBLE);
//    }

    /**
     * 隱藏或顯示的控制項物件
     */
    private void setDisplayObject(){
        if(nowState == State.MINI)
            return;
        if((display_object & MENU_BUTTON) != MENU_BUTTON) {
            menu.setVisibility(View.GONE);
            title.setPadding((int)(context.getResources().getDisplayMetrics().density*TITLE_LIFT_TO_EDGE_DISTANCE),0,0,0);
        }else {
            menu.setVisibility(View.VISIBLE);
            title.setPadding(0,0,0,0);
        }
        microMaxButtonBackground.setVisibility(View.VISIBLE);
        if((display_object & TITLE_BAR_AND_BUTTONS) != TITLE_BAR_AND_BUTTONS)
            titleBarAndButtons.setVisibility(View.GONE);
        else
            titleBarAndButtons.setVisibility(View.VISIBLE);
        if((display_object & HIDE_BUTTON) != HIDE_BUTTON)
            hide.setVisibility(View.GONE);
        else
            hide.setVisibility(View.VISIBLE);
        if((display_object & MINI_BUTTON) != MINI_BUTTON)
            mini.setVisibility(View.GONE);
        else
            mini.setVisibility(View.VISIBLE);
        if((display_object & MAX_BUTTON) != MAX_BUTTON)
            max.setVisibility(View.GONE);
        else
            max.setVisibility(View.VISIBLE);
        if((display_object & CLOSE_BUTTON) != CLOSE_BUTTON)
            close_button.setVisibility(View.GONE);
        else
            close_button.setVisibility(View.VISIBLE);
        if((display_object & SIZE_BAR) != SIZE_BAR || nowState == State.MAX || nowState == State.FULLSCREEN)
            sizeBar.setVisibility(View.GONE);
        else
            sizeBar.setVisibility(View.VISIBLE);
        if((display_object & FULLSCREEN_BUTTON) != FULLSCREEN_BUTTON)
            fullscreen.setVisibility(View.GONE);
        else
            fullscreen.setVisibility(View.VISIBLE);
    }

    /**
     * 設定視窗按鈕大小
     */
    private void setWindowDisplayObjectSize(){
        setWindowButtonsSize(buttonsHeight, buttonsWidth);
        setWindowSizeBarHeightSize(sizeBarHeight);

    }
    private void setWindowButtonsSize(int buttonsHeight, int buttonsWidth){
        titleBarAndButtons.getLayoutParams().height = buttonsHeight;
        menu.getLayoutParams().height = buttonsHeight;
        menu.getLayoutParams().width = buttonsWidth;
        title.getLayoutParams().height = buttonsHeight;
        hide.getLayoutParams().height = buttonsHeight;
        hide.getLayoutParams().width = buttonsWidth;
        mini.getLayoutParams().height = buttonsHeight;
        mini.getLayoutParams().width = buttonsWidth;
        max.getLayoutParams().height = buttonsHeight;
        max.getLayoutParams().width = buttonsWidth;
        fullscreen.getLayoutParams().height = buttonsHeight;
        fullscreen.getLayoutParams().width = buttonsWidth;
        close_button.getLayoutParams().height = buttonsHeight;
        close_button.getLayoutParams().width = buttonsWidth;
    }
    private void setWindowSizeBarHeightSize(int sizeBarHeight){
        sizeBar.getLayoutParams().height = sizeBarHeight;
    }

    /**
     * 讓該視窗獲得焦點
     */
    public void focusWindow(){
        focusWindow(true);
    }

    /**
     * 讓該視窗獲得焦點
     * @param eachSubWindow 判斷是否要focus子視窗
     */
    private void focusWindow(boolean eachSubWindow){
        if(nowState != State.CLOSE && WindowManager.focusedWindowNumber != this.Number) {//如果視窗編號不是現在焦點視窗編號
            if (WindowManager.windowIn(WindowManager.focusedWindowNumber)) {//如果現在焦點視窗編號在有視窗清單裡
                WindowStruct WS = WindowManager.getWindowStruct(WindowManager.focusedWindowNumber);
                WS.unFocusWindow();
            }
            WindowManager.focusedWindowNumber = this.Number;
            ((WindowFrom) winform).setWindowStyleOfFocus();
            wmlp.flags = (nowState == State.MINI) ? NO_FOCUS_FLAGE_FOR_MINI_STATE : FOCUS_FLAGE;
            wmlp.alpha =1.0f;
            if(nowState != State.FULLSCREEN){
                wm.removeView(winform);
                wm.addView(winform,wmlp);
            }
            CDAW.onFocus(context, this);
        }
        if(eachSubWindow) {
            for (int key : this.subWindowNumbers)
                WindowManager.getWindowStruct(key).focusWindow(true);
        }
    }

    /**
     * 讓該視窗獲得焦點並顯示視窗
     */
    public void focusAndShowWindow(){
        focusWindow(false);
        if(nowState == State.MINI || nowState == State.HIDE) {
            if (previousState == State.MAX)
                max();
            else if(previousState == State.FULLSCREEN)
                fullscreen();
            else
                general();
        }else if(nowState == State.FULLSCREEN){
            Intent intent = new Intent(context, FullscreenWindowActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }else{
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            intent.putExtra(FullscreenWindowActivity.WINDOW_NUMBER_EXTRA_NAME, getNumber());
            intent.setData(Uri.parse(String.valueOf(getNumber())));//一定要setData才能找出經用過這組Intent開啟過的Activity
            context.startActivity(intent);
        }
        for(int key : this.subWindowNumbers)
            WindowManager.getWindowStruct(key).focusAndShowWindow();
    }

    /**
     * 讓該視窗失去焦點
     */
    public void unFocusWindow(){
        if(nowState != State.CLOSE && WindowManager.focusedWindowNumber == this.Number) {//如果視窗編號是現在焦點視窗編號
            WindowManager.focusedWindowNumber = WindowManager.NON_FOCUSED_WINDOW;
            if(nowState == State.MINI)
                wmlp.flags = NO_FOCUS_FLAGE_FOR_MINI_STATE;
            else {
                ((WindowFrom) winform).setWindowStyleOfUnFocus();
                wmlp.flags = NO_FOCUS_FLAGE;
            }
            if(nowState != State.FULLSCREEN)
                wm.updateViewLayout(winform,wmlp);
            CDAW.onUnFocus(context, this);
        }
    }

    /**
     * 隱藏或顯示的控制項物件
     * @param display_object 要顯示的控制物件
     */
    public void setDisplayObject(int display_object){
        this.display_object = display_object;
        setDisplayObject();
    }

    /**
     * 取得的控制項物件
     * @return  顯示的控制物件
     */
    public int getDisplayObject(){
        return display_object;
    }

    /**
     * 設定視窗位置
     * @param x X座標
     * @param y Y座標
     */
    public void setPosition(int x,int y){
        setPosition(x, y, false);
    }

    /**
     * 設定視窗位置
     * @param x X座標
     * @param y Y座標
     * @param isUncertain 表示這個視窗位置不是最終的，所以當值為true時，不會觸發onWindowPositionChange事件
     */
    public void setPosition(int x,int y, boolean isUncertain){
        if(nowState != State.CLOSE) {
            y = Math.max(y,0);//防止與狀態列重疊
            if (nowState != State.MINI) {
                left = x;
                top = y;
            }
            if (nowState == State.GENERAL || nowState == State.MINI) {
                wmlp.x = x;
                wmlp.y = y;
                wm.updateViewLayout(winform, wmlp);
            }
            if(!isUncertain){
                invokeWindowPositionChangeListener();
            }
        }
    }

    /**
     * 設定general狀態的視窗位置
     * @param x X座標
     * @param y Y座標
     */
    public void setGeneralPosition(int x,int y){
        setGeneralPosition(x, y, false);
    }

    /**
     * 設定general狀態的視窗位置
     * @param x X座標
     * @param y Y座標
     * @param isUncertain 表示這個視窗位置不是最終的，所以當值為true時，不會觸發onWindowPositionChange事件
     */
    public void setGeneralPosition(int x,int y, boolean isUncertain){
        if(nowState != State.CLOSE) {
            y = Math.max(y,0);//防止與狀態列重疊
            left = x;
            top = y;
            if (nowState == State.GENERAL) {
                wmlp.x = x;
                wmlp.y = y;
                wm.updateViewLayout(winform, wmlp);
            }
            if(!isUncertain){
                invokeWindowPositionChangeListener();
            }
        }
    }

    /**
     * 視窗X座標
     * @return X座標
     */
    public int getPositionX(){
        return (nowState == State.MINI) ? wmlp.x : left;
    }

    /**
     * 視窗Y座標
     * @return Y座標
     */
    public int getPositionY(){
        return (nowState == State.MINI) ? wmlp.y: top;
    }
    /**
     * 視窗當下真正的X座標
     * @return X座標
     */
    public int getRealPositionX(){
        return wmlp.x;
    }

    /**
     * 視窗當下真正的Y座標
     * @return Y座標
     */
    public int getRealPositionY(){
        return wmlp.y;
    }
    /**
     * 視窗general狀態的X座標
     * @return X座標
     */
    public int getGeneralPositionX(){
        return left;
    }

    /**
     * 視窗general狀態的Y座標
     * @return Y座標
     */
    public int getGeneralPositionY(){
        return top;
    }

    /**
     * 設定視窗大小
     * @param width 寬度
     * @param height 高度
     */
    public void setSize(int width, int height){
        setSize(width, height, false);
    }

    /**
     * 設定視窗大小
     * @param width 寬度
     * @param height 高度
     * @param isUncertain 表示這個視窗大小不是最終的，所以當值為true時，不會觸發onWindowSizeChange事件
     */
    public void setSize(int width, int height, boolean isUncertain){
        setWidth(width, true);
        setHeight(height, true);
        if(!isUncertain && (width == this.width || height == this.height)){
            invokeWindowSizeChangeListener();
        }
    }

    /**
     * 設定視窗寬度
     * @param width 寬度
     */
    public void setWidth(int width){
        setWidth(width, false);
    }

    /**
     * 設定視窗寬度
     * @param width 寬度
     * @param isUncertain 表示這個視窗寬度不是最終的，所以當值為true時，不會觸發onWindowSizeChange事件
     */
    public void setWidth(int width, boolean isUncertain){
        this.width = Math.max(width, MINI_WINDOW_WIDTH);
        if(nowState == State.GENERAL) {
            winform.getLayoutParams().width = this.width;
            wm.updateViewLayout(winform, wmlp);
        }
        if(!isUncertain && width == this.width){
            invokeWindowSizeChangeListener();
        }
    }

    /**
     * 取得general狀態視窗寬度
     * @return  寬度
     */
    public int getWidth(){
        return this.width;
    }
    /**
     * 取得視窗當下真正的寬度
     * @return  寬度
     */
    public int getRealWidth(){
        return winform.getWidth();
    }

    /**
     * 設定視窗高度
     * @param height 高度
     */
    public void setHeight(int height){
        setHeight(height, false);
    }

    /**
     * 設定視窗高度
     * @param height 高度
     * @param isUncertain 表示這個視窗高度不是最終的，所以當值為true時，不會觸發onWindowSizeChange事件
     */
    public void setHeight(int height, boolean isUncertain){
        this.height = Math.max(height, winform.getHeight() - wincon.getHeight());
        if(nowState == State.GENERAL) {
            winform.getLayoutParams().height = this.height;
            wm.updateViewLayout(winform, wmlp);
        }
        if(!isUncertain && height == this.height){
            invokeWindowSizeChangeListener();
        }
    }

    /**
     * 取得general狀態視窗高度
     * @return  高度
     */
    public int getHeight(){
        return this.height;
    }
    /**
     * 取得視窗當下真正的高度
     * @return  高度
     */
    public int getRealHeight(){
        return winform.getHeight();
    }

    /**
     * 設定視窗按鈕高度
     * @param buttonsHeight 視窗按鈕高度
     */
    public void setWindowButtonsHeight(int buttonsHeight){
        this.buttonsHeight = buttonsHeight;
        if(nowState != State.MINI){
            setWindowButtonsSize(this.buttonsHeight, this.buttonsWidth);
        }
    }

    /**
     *  取得視窗按鈕高度
     * @return 視窗按鈕高度
     */
    public int getWindowButtonsHeight(){
        return this.buttonsHeight;
    }

    /**
     * 設定視窗按鈕寬度
     * @param buttonsWidth 視窗按鈕寬度
     */
    public void setWindowButtonsWidth(int buttonsWidth){
        this.buttonsWidth = buttonsWidth;
        if(nowState != State.MINI){
            setWindowButtonsSize(this.buttonsHeight, this.buttonsWidth);
        }
    }

    /**
     *  取得視窗按鈕寬度
     * @return 視窗按鈕寬度
     */
    public int getWindowButtonsWidth(){
        return this.buttonsWidth;
    }

    /**
     * 設定視窗大小調整列高度
     * @param sizeBarHeight 視窗大小調整列高度
     */
    public void setWindowSizeBarHeight(int sizeBarHeight){
        this.sizeBarHeight = buttonsHeight;
        setWindowSizeBarHeightSize(this.sizeBarHeight);
    }

    /**
     *  取得視窗大小調整列高度
     * @return 視窗大小調整列高度
     */
    public int getWindowSizeBarHeight(){
        return this.sizeBarHeight;
    }

    /**
     * 設定最小化狀態視窗按鈕高度
     */
    public void setButtonHeightForMiniState(int buttonHeightForMiniState){
        this.buttonHeightForMiniState = buttonHeightForMiniState;
        if(nowState == State.HIDE){
            winform.getLayoutParams().height = this.buttonHeightForMiniState;
            wm.updateViewLayout(winform, wmlp);
        }
    }

    /**
     *  取得最小化狀態視窗按鈕高度
     * @return 最小化狀態視窗按鈕高度
     */
    public int getButtonHeightForMiniState(){
        return this.buttonHeightForMiniState;
    }

    /**
     * 設定最小化狀態視窗按鈕高度
     */
    public void setButtonWidthForMiniState(int buttonWidthForMiniState){
        this.buttonWidthForMiniState = buttonWidthForMiniState;
        if(nowState == State.HIDE){
            winform.getLayoutParams().width = this.buttonWidthForMiniState;
            wm.updateViewLayout(winform, wmlp);
        }
    }

    /**
     *  取得最小化狀態視窗按鈕高度
     * @return 最小化狀態視窗按鈕高度
     */
    public int getButtonWidthForMiniState(){
        return this.buttonWidthForMiniState;
    }

    /**
     * 取得該視窗的所有子視窗
     * @return 所有子視窗
     */
    public ArrayList<WindowStruct> getSubWindow(){
        ArrayList<WindowStruct> subWindows = new ArrayList<>();
        for(int key : this.subWindowNumbers)
            subWindows.add(WindowManager.getWindowStruct(key));
        return subWindows;
    }

    /**
     * 取得當前顯示的分頁編號
     * @return 分頁編號
     */
    public int getCurrentPagePosition(){
        return currentWindowPagePosition;
    }

    /**
     * 設定視窗標題
     * @param position 表示要設定標題的子頁面編號
     * @param titleText 標題文字
     */
    public void setWindowTitle(int position, String titleText){
        if(currentWindowPagePosition == position)
            setWindowTitle(titleText);
        else
            windowTitle[position] = titleText;
    }

    /**
     * 設定視窗標題
     * @param titleText 標題文字
     */
    public void setWindowTitle(String titleText){
        windowTitle[currentWindowPagePosition] = titleText;
        title.setText(titleText);
        invokeWindowTitleChangeListener(titleText);
    }

    /**
     * 新增視窗狀態改變監聽器
     * @param onWindowStateChangeListener 視窗模式改變監聽器
     */
    public void addWindowStateChangeListener(@NonNull OnWindowStateChangeListener onWindowStateChangeListener){
        this.onWindowStateChangeListenerList.add(onWindowStateChangeListener);
    }

    /**
     * 新增視窗大小改變監聽器
     * @param onWindowSizeChangeListener 視窗大小改變監聽器
     */
    public void addWindowSizeChangeListener(@NonNull OnWindowSizeChangeListener onWindowSizeChangeListener){
        this.onWindowSizeChangeListenerList.add(onWindowSizeChangeListener);
    }

    /**
     * 新增視窗位置改變監聽器
     * @param onWindowPositionChangeListener 視窗位置改變監聽器
     */
    public void addWindowPositionChangeListener(@NonNull OnWindowPositionChangeListener onWindowPositionChangeListener){
        this.onWindowPositionChangeListenerList.add(onWindowPositionChangeListener);
    }

    /**
     * 新增視窗標題改變監聽器
     * @param onWindowTitleChangeListener 視窗標題改變監聽器
     */
    public void addWindowTitleChangeListener(@NonNull OnWindowTitleChangeListener onWindowTitleChangeListener){
        this.onWindowTitleChangeListenerList.add(onWindowTitleChangeListener);
    }

    /**
     * 移除視窗狀態改變監聽器
     * @param onWindowStateChangeListener 視窗模式改變監聽器
     */
    public void removeWindowStateChangeListener(@NonNull OnWindowStateChangeListener onWindowStateChangeListener){
        this.onWindowStateChangeListenerList.remove(onWindowStateChangeListener);
    }

    /**
     * 移除視窗大小改變監聽器
     * @param onWindowSizeChangeListener 視窗大小改變監聽器
     */
    public void removeWindowSizeChangeListener(@NonNull OnWindowSizeChangeListener onWindowSizeChangeListener){
        this.onWindowSizeChangeListenerList.remove(onWindowSizeChangeListener);
    }

    /**
     * 移除視窗位置改變監聽器
     * @param onWindowPositionChangeListener 視窗位置改變監聽器
     */
    public void removeWindowPositionChangeListener(@NonNull OnWindowPositionChangeListener onWindowPositionChangeListener){
        this.onWindowPositionChangeListenerList.remove(onWindowPositionChangeListener);
    }

    /**
     * 移除視窗標題改變監聽器
     * @param onWindowTitleChangeListener 視窗標題改變監聽器
     */
    public void removeWindowTitleChangeListener(@NonNull OnWindowTitleChangeListener onWindowTitleChangeListener){
        this.onWindowTitleChangeListenerList.remove(onWindowTitleChangeListener);
    }

    /**
     * 移除所有監聽器
     */
    private void removeAllListener(){
        this.onWindowStateChangeListenerList.clear();
        this.onWindowSizeChangeListenerList.clear();
        this.onWindowPositionChangeListenerList.clear();
        this.onWindowTitleChangeListenerList.clear();
    }

    /**
     * 調用所有的視狀態式改變監聽器
     */
    private void invokeWindowStateChangeListener(){
        for(OnWindowStateChangeListener onWindowStateChangeListener : this.onWindowStateChangeListenerList){
            onWindowStateChangeListener.onStateChanged(context, this);
        }
    }

    /**
     * 調用所有的視窗大小改變監聽器
     */
    private void invokeWindowSizeChangeListener(){
        for(OnWindowSizeChangeListener onWindowSizeChangeListener : this.onWindowSizeChangeListenerList){
            onWindowSizeChangeListener.onSizeChanged(context, this);
        }
    }

    /**
     * 調用所有的視窗位置改變監聽器
     */
    private void invokeWindowPositionChangeListener(){
        for(OnWindowPositionChangeListener onWindowPositionChangeListener : this.onWindowPositionChangeListenerList){
            onWindowPositionChangeListener.onPositionChanged(context, this);
        }
    }

    /**
     * 調用所有的視窗標題改變監聽器
     * @param titleText 標題文字
     */
    private void invokeWindowTitleChangeListener(String titleText){
        for(OnWindowTitleChangeListener onWindowTitleChangeListener : this.onWindowTitleChangeListenerList){
            onWindowTitleChangeListener.onTitleChanged(context, this, titleText);
        }
    }

    /**
     * 取得視窗標題
     * @param position 表示要取得標題的子頁面編號
     * @return 標題文字
     */
    public String getWindowTitle(int position){
        return windowTitle[position];
    }

    /**
     * 取得當前視窗標題
     * @return 標題文字
     */
    public String getWindowTitle(){
        return title.getText().toString();
    }

    /**
     * 設定過場動畫持續時間
     * @param transitionsDuration 過場動畫持續時間
     */
    public void setTransitionsDuration(int transitionsDuration){
        this.transitionsDuration = transitionsDuration;
    }

    /**
     * 取得過場動畫持續時間
     * @return  transitionsDuration 過場動畫持續時間
     */
    public int getTransitionsDuration(){
        return transitionsDuration;
    }

    /**
     * 取得視窗的View
     * @return 視窗的View
     */
    public WindowFrom getWindowFrom(){
        return (WindowFrom)winform;
    }

    /**
     * 取得視窗編號
     * @return 視窗編號
     */
    public int getNumber(){
        return Number;
    }

    /**
     * 取得視窗實體
     * @param number 視窗編號'
     * @return 視窗實體
     */
    @Deprecated
    public static WindowStruct getWindowStruct(int number){
        return WindowManager.getWindowStruct(number);
    }

    /**
     * 取得視窗內容實作
     * @return 視窗內容實作
     */
    public constructionAndDeconstructionWindow getConstructionAndDeconstructionWindow(){
        return CDAW;
    }

    /**
     * 顯示側邊選單
     */
    public void showMenu(){
        menuList.showMenu();
    }

    /**
     * 關閉側邊選單
     */
    public void closeMenu(){
        menuList.closeMenu();
    }

    /**
     * 顯示指定頁面
     * @param position 頁面編號
     */
    public void showPage(int position){
        menuList.showPage(position);
    }

    /**
     * 取得視窗當下的狀態
     * @return 視窗當下的狀態
     */
    public State getCurrentState(){
        return nowState;
    }

    /**
     * 取得視窗上一個狀態
     * @return 視窗上一個狀態
     */
    public State getPreviousState(){
        return previousState;
    }


    /**
     * 讓全螢幕畫面的Activity取得winform
     * @return winform
     */
    View getWinformForFullScreenActivity(){
        return winform;
    }

    /**
     * 傳入全螢幕畫面的Activity
     * @param fullscreenWindowActivity 全螢幕畫面的Activity
     */
    void setFullscreenActivity(FullscreenWindowActivity fullscreenWindowActivity){
        this.fullscreenWindowActivity = fullscreenWindowActivity;
    }
}