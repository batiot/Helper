package com.batiot.helper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import static android.content.Context.WINDOW_SERVICE;

public class MainActivity extends AppCompatActivity {


    static int WRITE_STORAGE_REQ_CODE = 113;
    static int OVERLAY_PERMISSION_REQ_CODE = 12121;
    static int SCREEN_CAPTURE_REQ_CODE = 444;

    private DisplayMetrics displayMetrics;
    private DisplayMetrics rawDisplayMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new RenderView(this));
        //setContentView(R.layout.activity_main);

        displayMetrics = this.getResources().getDisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        rawDisplayMetrics = new DisplayMetrics();
        Display disp = windowManager.getDefaultDisplay();
        disp.getRealMetrics(rawDisplayMetrics);

        if (!isSystemAlertPermissionGranted(MainActivity.this)){
            requestSystemAlertPermission(MainActivity.this,OVERLAY_PERMISSION_REQ_CODE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_REQ_CODE);
        }
        if(ScreenGrabber.getInstance()==null) {
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_CAPTURE_REQ_CODE);
        }

        Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
        startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(startIntent);
    }

    public static void requestSystemAlertPermission(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        final String packageName = context == null ? context.getPackageName() : context.getPackageName();
        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName));
        context.startActivityForResult(intent, requestCode);
    }
    @TargetApi(23)
    public static boolean isSystemAlertPermissionGranted(Context context) {
        final boolean result = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || Settings.canDrawOverlays(context);
        return result;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_LONG);
            } else {
                Toast.makeText(this, "Error: Overlay permission refused", Toast.LENGTH_LONG);
            }
        }else if (requestCode == WRITE_STORAGE_REQ_CODE) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Write permission granted",Toast.LENGTH_LONG);
                } else {
                    Toast.makeText(this,"Error: Write permission refused",Toast.LENGTH_LONG);
                }
        } else if (requestCode == SCREEN_CAPTURE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                MediaProjection mProjection = projectionManager.getMediaProjection(resultCode, data);
                ScreenGrabber.init(mProjection, rawDisplayMetrics, displayMetrics);
            } else {
                Toast.makeText(this,"ScreenCapture permission refused",Toast.LENGTH_LONG);
            }

        }

    }


    // Création d'une classe interne RenderView pour gérer un affichage simple permettant
    // de montrer que nous occupons bien tout l'écran
    class RenderView extends View {

        public RenderView(Context context) {
            super(context);
        }

        // Dessinons sur la totalité de l'écran
        protected void onDraw(Canvas canvas) {

            Paint paint = new Paint();
            paint.setAntiAlias(false);

            // Nous allons dessiner nos points par rapport à la résolution de l'écran
            int iWidth = canvas.getWidth(); // Largeur
            int iHeight = canvas.getHeight(); // Hauteur

            for (int x=540; x < 541; x++) {
                for (int y = 0; y < iHeight; y++) {

                    int r = Math.round(y%255);
                    int g = Math.round(y/255*40);
                    int b = Math.round(x%255);
                    int a = 255;//Math.round(y/255*40);

                    if(r==90 && g==0 && b==30){
                        Log.d("first",x+"-"+y+"     "+r+" "+b+" " +g);//screengrab à 301  y=90 + titlebar
                    }
                    if(r==69 && g==160 && b==30){
                        Log.d("last",x+"-"+y+"     "+r+" "+b+" " +g);//screengrab à 1300  y=1089 + titlebar
                    }

                    paint.setARGB(a,r,g,b);
                    // Définir l'épaisseur du segment
                    //paint.setStrokeWidth(1);
                    // Puis dessiner nos points dans le cavenas
                    canvas.drawPoint(x,y, paint);
                }
            }
        }
    }

}
