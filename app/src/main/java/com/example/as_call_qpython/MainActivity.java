package com.example.as_call_qpython;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


/**
通过 调用 Qpython APP 来执行 python 程序

 */
public class MainActivity extends AppCompatActivity {

    private final int SCRIPT_EXEC_PY = 40001;  //用于调用其他Activity的识别ID
    private final String extPlgPlusName = "org.qpython.qpy3"; //qpython的包名 org.qpython.qpy3; com.hipipal.qpyplus

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText code = (EditText)findViewById(R.id.edit_text);
        code.setText("#qpy:console\n" +
                "try:\n" +
                "    import androidhelper\n" +
                "\n" +
                "    droid = androidhelper.Android()\n" +
                "    line = droid.dialogGetInput()\n" +
                "    s = 'Hello %s' % line.result\n" +
                "    droid.makeToast(s)\n" +
                "except:\n" +
                "    print(\"Hello, Please update to newest QPython version from (http://play.qpython.com/qrcode-python.html) to use this feature\")\n");



    }





    @Override  //菜单
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }



    //检查是否安装了某个包
    //PackageManager里的getPackageInfo()方法，该方法不能获得已安装应用的列表，
    // 但是可以获得指定包名的PackageInfo，当指定包名的PackageInfo不存在的时候，系统会抛出PackageManager.NameNotFoundException异常，可以以此为依据来进行判断系统是否安装某应用。
    public static boolean checkAppInstalledByName(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);

            Log.d("QPYMAIN",  "checkAppInstalledByName:"+packageName+" found");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("QPYMAIN",  "checkAppInstalledByName:"+packageName+" not found");

            return false;
        }
    }

    //点击按键RUN; 执行了将
    public void onQPyExec(View v) {

        if (checkAppInstalledByName(getApplicationContext(), extPlgPlusName)) {
            Toast.makeText(this, "调用QPython API例子，Sample of calling QPython API", Toast.LENGTH_SHORT).show();

            //添加意图
            Intent intent = new Intent();
            intent.setClassName(extPlgPlusName, "org.qpython.qpylib.MPyApi");
            intent.setAction(extPlgPlusName + ".action.MPyApi");

            Bundle mBundle = new Bundle();  //Bundle经常使用在Activity之间或者线程间传递数据，
            mBundle.putString("app", "myappid");
            mBundle.putString("act", "onPyApi");
            mBundle.putString("flag", "onQPyExec");  // 可以在上下文中使用的任何字符串标志
            mBundle.putString("param", "");          // param String param可以在上下文中使用

            /*在字符串Python代码中，您可以将py文件放入res、raw或intenet中，
                这样您就可以以同样的方式获得它，从而使它具有可伸缩性
             * The String Python code, you can put your py file in res or raw or intenet,
             so that you can get it the same way, which can make it scalable
             */
            EditText codeTxt = (EditText)findViewById(R.id.edit_text);
            String code = codeTxt.getText().toString();
            mBundle.putString("pycode", code);  //获取python代码 （会将代码保存在qpython/tmp/main.py）
            intent.putExtras(mBundle);
            startActivityForResult(intent, SCRIPT_EXEC_PY);
        } else {
            Toast.makeText(getApplicationContext(), "请先安装QPython", Toast.LENGTH_LONG).show();

            //打开下载连接
            try { //应用市场搜索包名
                Uri uLink = Uri.parse("market://details?id=com.hipipal.qpyplus");
                Intent intent = new Intent( Intent.ACTION_VIEW, uLink );
                startActivity(intent);
            } catch (Exception e) {
                Uri uLink = Uri.parse("http://qpython.com");
                Intent intent = new Intent( Intent.ACTION_VIEW, uLink );
                startActivity(intent);
            }

        }
    }

    /*
    Activity 回调
    第一个参数：这个整数requestCode提供给onActivityResult，是以便确认返回的数据是从哪个Activity返回的，就是在startActivityForResult设置的requestCode。
    第二个参数：这整数resultCode是由子Activity通过其setResult()方法返回，就是setResult(int resultCode, Intent data)的第一个参数resultCode。
    一般来说，resultCode主要指定为RESULT_CANCELED和RESULT_OK ，然后在onActivityResult获取到resultCode进行判断，
    如果是RESULT_CANCELED就不执行回调方法，如果是RESULT_OK 就执行回调方法
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCRIPT_EXEC_PY) { //40001
            if (data != null) {
                Bundle bundle = data.getExtras();
                String flag = bundle.getString("flag");     // 你设置的flag you set
                String param = bundle.getString("param");   // 你设置的param you set
                String result = bundle.getString("result"); // 返回的结果 Result your Pycode generate
                Toast.makeText(this, "onQPyExec: return (" + result + ")", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "onQPyExec: data is null", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //判断是否为模拟器
    public boolean isEmulator() {
        /*
        <!-- 添加拨号权限,但安卓6.0以后该句无效果了。采用了动态权限控制-->
        <uses-permission android:name="android.permission.CALL_PHONE"/>
         */
        //动态的请求权限
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CALL_PHONE
        }, 0x11); //0x11是请求码，可以在回调中获取
        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(url));
        //startActivity(intent);
        // 是否可以处理跳转到拨号的 Intent
        boolean isPhone = intent.resolveActivity(this.getPackageManager()) != null;
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("MuMu")
                || Build.MODEL.contains("virtual")
                || Build.SERIAL.equalsIgnoreCase("android")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)
                || ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE))
                .getNetworkOperatorName().toLowerCase().equals("android")
                || !isPhone;
    }

    public void checkphone(View view) {
        if (isEmulator())
            Toast.makeText(this,"是模拟器",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this,"是手机",Toast.LENGTH_LONG).show();
    }
}
