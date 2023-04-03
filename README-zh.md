<img src="icon.png" width="100"></img>
# FloatWindowCore 浮動視窗核心
Core for floating Window app for Android\
![Alt text](Demonstration.png)


這套核心應用在[FloatWindow 浮動視窗](https://github.com/jack850628/FloatWindow)中。

  *說明*\
  ![Alt text](Demonstration2.png)


## 使用FloatWindoew在自己的APP裡

1. 下載[FloatWindow模組](https://github.com/jack850628/FloatWindow-Core/releases/)

2. 引入aar檔到專案中 
    - 先到專案的app資料夾中建立libs \
    <img src="說明1.png" width="300"></img>
    - 然後將floatwindow-release.aar複製到libs內

3. 在專案的build.gradle中加入對FloatWindow的引用
    ```
    dependencies {
        ...
        implementation files('libs/floatwindow-release.aar')
        ...
    }
    ```

4. 最後，FloatWindow有使用到ACTION_MANAGE_OVERLAY_PERMISSION權限，所以必須在自己的APP中加入跟使用者請求權限的對應處理。

## FloatWindow的使用方法

使用WindowStruct.Builder可以創建一個視窗 \
創建一個Window最簡當的方式為
```
WindowStruct windowStruct = new WindowStruct.Builder(this,(WindowManager) getSystemService(Context.WINDOW_SERVICE)).show();
```
這樣會創建出一個內容空白的Window \
則
```
WindowStruct windowStruct = new WindowStruct.Builder(this,(WindowManager) getSystemService(Context.WINDOW_SERVICE)).windowPages(new int[]{R.layout.my_layout}).windowPageTitles(new String[]{"My Title"}).show();
```
這將會為您創建出一個具有內容與標題的Window。

如果想在建立視窗時，帶點參數使用的話，可以這樣做
```
Map<String, Object> args = new HashMap<String, Object>();
args.put("字串參數", "abc");
args.put("數字參數", 123);

windowStruct.Builder builder = new WindowStruct.Builder(this,(WindowManager) getSystemService(Context.WINDOW_SERVICE));
builder.windowInitArgs(args);//放入參數
builder.constructionAndDeconstructionWindow(new constructionAndDeconstructionWindow(){
    @Override
    public void onCreate(Context context, Map<String, Object> args, WindowStruct windowStruct){
        //可以在這裡
    }
    @Override
    public void Construction(Context context, View pageView, int position, Map<String, Object> args, WindowStruct windowStruct){
        //和這裡接收到參數
    }
});
builder.show();
```

使用WindowColor可以設定或取得視窗顏色
```
WindowColor windowColor = new WindowColor(this);
int windowBackgroundColor = windowColor.getWindowBackground();//取得視窗背景顏色

windowColor.setTitleBar(0x79afe47a);//ARGB
windowColor.save();//儲存顏色設定
```
## com.jack8.floatwindow.Window.WindowManager
以下簡稱為WindowManager，WindowManager中擁有所有尚未被關閉的視窗，如果您想要取得所有未關閉的視窗，可以使用WindowManager.entrySet()
```
Set<Map.Entry<Integer, WindowStruct>> allWindows = WindowManager.entrySet();//<Number, Window>
```
每個視窗都有自己的編號，可以透過WindowStruct.getNumber取得
```
int windowNumber = new WindowStruct.Builder(this,(WindowManager) getSystemService(Context.WINDOW_SERVICE)).show().getNumber();
```
並且可以透過WindowManager.getWindowStruct()取得視窗
```
WindowStruct window = WindowManager.getWindowStruct(windowNumber);
```
因此建議您不需要自己維護WindowStruct的參照，您只需要維護每個視窗的Number並且在需要WindowStruct時從WindowManager中取得就可以了。

## Sub Window
如果有需要開新window並蓋住舊window來阻擋使用者操作舊window(例如: Alert Window)的話，您可以在建立WindowStruct時使用WindowStruct.Builder.parentWindow()
```
WindowStruct mainWindow = new WindowStruct.Builder(this,(WindowManager) getSystemService(Context.WINDOW_SERVICE)).show();

WindowStruct alertWindow = new WindowStruct.Builder(this,(WindowManager) getSystemService(Context.WINDOW_SERVICE))
    .parentWindow(mainWindow)
    .show();
```
另外，一個window可以擁有多個sub window，您可以透過WindowStruct.getSubWindow()取得所有sub window
```
List<WindowStruct> subWindows = mainWindow.getSubWindow();
```

## Window Control Object
您可以在Window建立時調整要顯示那些視窗控制物件(例如: 最大化按鈕、標題列、大小調整列)
```
WindowStruct alertWindow = new WindowStruct.Builder(this,(WindowManager) getSystemService(Context.WINDOW_SERVICE))
    .displayObject(WindowStruct.TITLE_BAR_AND_BUTTONS | WindowStruct.CLOSE_BUTTON)
    .show();
```
或建立後調整
```
alertWindow.setDisplayObject(alertWindow.getDisplayObject() | WindowStruct.MINI_BUTTON);
```
以下是所有可用的Control Object
- ALL_NOT_DISPLAY
- MENU_BUTTON
- HIDE_BUTTON
- MINI_BUTTON
- MAX_BUTTON
- CLOSE_BUTTON
- SIZE_BAR
- TITLE_BAR_AND_BUTTONS
- FULLSCREEN_BUTTON

## WindowStruct Lifecycle (constructionAndDeconstructionWindow)
<img src="WindowStruct_Lifecycle.jpg" width="500"></img>

## WindowStruet中可監聽的事件
> - OnWindowTitleChange    當視窗標題改變
> - OnWindowStateChange    當視窗狀態改變，例如：最大化、最小化、關閉
> - OnWindowSizeChange     當視窗大小改變
> - OnWindowPositionChange 當視窗位置改變
## Hello World
#### MainActivity.java
```
package com.example.testwindow;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.jack8.floatwindow.Window.WindowStruct;

import java.util.Map;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M&&!Settings.canDrawOverlays(MainActivity.this))
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + MainActivity.this.getPackageName())), 1);
        else {
            startFloatWindow();
        }

    }
    private void startFloatWindow(){
        findViewById(R.id.hello).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //建立一個hello world的FloatWindow視窗
                new WindowStruct.Builder(MainActivity.this, (WindowManager) getSystemService(Context.WINDOW_SERVICE)).
                        windowPages(new int[]{R.layout.hello_page_1, R.layout.hello_page_2}).
                        windowPageTitles(new String[]{"Hello FloatWindow","Submit Hello"}).
                        constructionAndDeconstructionWindow(new WindowStruct.constructionAndDeconstructionWindow() {
                            String helloString = "";
                            @Override
                            public void Construction(Context context, final View view, int i, Map<String, Object> args, final WindowStruct windowStruct) {
                                switch (i){
                                    case 0:
                                        view.findViewById(R.id.get_hello).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                windowStruct.showPage(1);
                                            }
                                        });
                                        break;
                                    case 1:
                                        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                                            EditText helloEdit = view.findViewById(R.id.hello_string);
                                            @Override
                                            public void onClick(View v) {
                                                helloString = helloEdit.getText().toString();
                                                windowStruct.showPage(0);
                                            }
                                        });
                                        break;
                                }
                            }

                            @Override
                            public void Deconstruction(Context context, View view, int i, WindowStruct windowStruct) {

                            }

                            @Override
                            public void onResume(Context context, View view, int i, WindowStruct windowStruct) {
                                if(i == 0)
                                    ((TextView)view.findViewById(R.id.hello_string_view)).setText("Hello " + helloString);
                            }

                            @Override
                            public void onPause(Context context, View view, int i, WindowStruct windowStruct) {

                            }
                        }).show();

            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (Settings.canDrawOverlays(this))
                startFloatWindow();
            else
                finish();
        }
    }
}
```
#### activity_main.xml
```
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <Button
        android:id="@+id/hello"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="hello"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

#### hello_page_1.xml
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        android:id="@+id/hello_string_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_horizontal"
        android:text="TextView" />

    <Button
        android:id="@+id/get_hello"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="get hello" />
</LinearLayout>
```
#### hello_page_2.xml
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <EditText
        android:id="@+id/hello_string"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:ems="10"
        android:hint="your name"
        android:inputType="textPersonName" />

    <Button
        android:id="@+id/submit"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="submit" />
</LinearLayout>
```

## License
使用[MIT license](https://github.com/jack850628/FloatWindow-Core/blob/main/LICENSE)
