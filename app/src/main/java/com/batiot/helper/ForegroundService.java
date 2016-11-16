package com.batiot.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class ForegroundService extends Service {


    private static final String LOG_TAG = "ForegroundService";

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Start Foreground Intent ");
                Intent notificationIntent = new Intent(this, MainActivity.class);
                notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,notificationIntent, 0);

                Intent previousIntent = new Intent(this, ForegroundService.class);
                previousIntent.setAction(Constants.ACTION.PREV_ACTION);
                PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,previousIntent, 0);

                Intent playIntent = new Intent(this, ForegroundService.class);
                playIntent.setAction(Constants.ACTION.PLAY_ACTION);
                PendingIntent pplayIntent = PendingIntent.getService(this, 0,playIntent, 0);

                Intent stopIntent = new Intent(this, ForegroundService.class);
                stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                PendingIntent pstopIntent = PendingIntent.getService(this, 0,stopIntent, 0);

                //Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.truiton_short);

                Notification notification = new NotificationCompat.Builder(this)
                        .setContentTitle("Truiton Music Player")
                        .setTicker("Truiton Music Player")
                        .setContentText("My Music")
                        .setSmallIcon(R.drawable.help)
                        //.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .addAction(android.R.drawable.ic_media_previous,"Previous", ppreviousIntent)
                        .addAction(android.R.drawable.ic_media_play, "Play",pplayIntent)
                        .addAction(android.R.drawable.ic_media_pause, "Stop",pstopIntent).build();
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,notification);
            } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
                Log.i(LOG_TAG, "Clicked Previous");
            } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
                Log.i(LOG_TAG, "Clicked Play");
                //On lance l'overlay + le shell
                overlay();
                Intent commandIntent = new Intent(getApplicationContext(), CommandService.class);
                commandIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                startService(commandIntent);
            } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
                Log.i(LOG_TAG, "Received Stop Foreground Intent");
                //On ferme le shell
                Intent commandIntent = new Intent(getApplicationContext(), CommandService.class);
                commandIntent.setAction(Constants.ACTION.STOPCOMMAND_ACTION);
                startService(commandIntent);

                stopForeground(true);
                stopSelf();//fermera l'overlay si ouvert
            }

            return START_STICKY;
    }

    private WindowManager windowManager;
    private ImageView close;
    private ImageView help;
    private RelativeLayout headView;


    private void overlay(){
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 20;
        params.y = 1300;
        headView = (RelativeLayout) inflater.inflate(R.layout.activity_alert_dialog, null);

        close=(ImageView)headView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"overlay close Clicked");
                windowManager.removeView(headView);
                Intent commandIntent = new Intent(getApplicationContext(), CommandService.class);
                stopService(commandIntent);

            }
        });
        help=(ImageView)headView.findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //windowManager.removeView(chatheadView);
                Log.d(LOG_TAG, "overlay help Clicked");
                boolean swipe = true;

                ScreenGrabber screenGrabber = ScreenGrabber.getInstance();
                if (screenGrabber != null) {
                    Intent commandIntent = new Intent(getApplicationContext(), CommandService.class);
                    commandIntent.setAction(Constants.ACTION.SHELL_ACTION);
                    commandIntent.putExtra("command", "input touchscreen swipe 540 1570 545 1575 5000");
                    startService(commandIntent);

                    int blancHaut = 0;
                    int blancBas = 0;
                    int targetHaut = 0;
                    int targetBas = 0;
                    int targetTeinte = 0;


                    Bitmap bitmap = screenGrabber.grabScreen();
                    if (bitmap != null) {
                        int[] pixels = new int[1 * 1351];
                        bitmap.getPixels(pixels, 0, 1, 540, 0, 1, 1350);
                        //int offset, //On rempli dès le début de pixel
                        //int stride, //on stocke 1 seul pixel par ligne
                        //int x,  //On démare au milieu
                        //int y, //On commence a 90%
                        //int width, //On ne regarde que une seul colonne de pixel
                        //int height) //On termine à 30%

                        double[] whiteLAB = new double[3];
                        ColorUtils.colorToLAB(Color.rgb(205, 210, 214), whiteLAB);


                        //On démarre du haut j'usqu'a trouver le rond blanc et le rond coloré
                        for (int i = 500; i < pixels.length; i++) {
                            double[] pixelLAB = new double[3];
                            ColorUtils.colorToLAB(pixels[i], pixelLAB);
                            double distance = ColorUtils.distanceEuclidean(whiteLAB, pixelLAB);
                            float[] hsv = new float[3];
                            Color.colorToHSV(pixels[i], hsv);
                            if (distance < 25.0) {
                                Log.d("pixel", "founds[" + i + "] " + Color.red(pixels[i]) + " " + Color.green(pixels[i]) + " " + Color.blue(pixels[i]) + " HSV " + hsv[0] + " " + hsv[1] * 100 + " " + hsv[2] * 100 + "  -> " + distance);
                                if (blancHaut == 0) {
                                    blancHaut = i;
                                }
                            } else if (hsv[1] > 0.98 && hsv[2] > 0.98) {
                                Log.d("pixel", "target[" + i + "] " + Color.red(pixels[i]) + " " + Color.green(pixels[i]) + " " + Color.blue(pixels[i]) + " HSV " + hsv[0] + " " + hsv[1] * 100 + " " + hsv[2] * 100 + "  -> " + distance);
                                if (targetHaut == 0) {
                                    targetHaut = i;
                                    targetTeinte = Math.round(hsv[0]);
                                    break;
                                }
                            } else {
                                Log.v("pixel", "not...[" + i + "] " + Color.red(pixels[i]) + " " + Color.green(pixels[i]) + " " + Color.blue(pixels[i]) + " HSV " + hsv[0] + " " + hsv[1] * 100 + " " + hsv[2] * 100 + "  -> " + distance);
                            }
                        }
                        //Puis pour eviter le nblanc de spokemon (dents de rattata...) On démarre depuis le bas j'usqu'a trouver le rond blanc et le rond coloré ou de rejoindre la precédente itération
                        for (int i = pixels.length - 1; (i > 500 || i > targetHaut); i--) {
                            double[] pixelLAB = new double[3];
                            ColorUtils.colorToLAB(pixels[i], pixelLAB);
                            double distance = ColorUtils.distanceEuclidean(whiteLAB, pixelLAB);
                            float[] hsv = new float[3];
                            Color.colorToHSV(pixels[i], hsv);
                            if (distance < 25.0) {
                                Log.d("pixel", "founds[" + i + "] " + Color.red(pixels[i]) + " " + Color.green(pixels[i]) + " " + Color.blue(pixels[i]) + " HSV " + hsv[0] + " " + hsv[1] * 100 + " " + hsv[2] * 100 + "  -> " + distance);
                                if (blancBas == 0) {
                                    blancBas = i;
                                }
                            } else if (hsv[1] > 0.98 && hsv[2] > 0.98) {
                                Log.d("pixel", "target[" + i + "] " + Color.red(pixels[i]) + " " + Color.green(pixels[i]) + " " + Color.blue(pixels[i]) + " HSV " + hsv[0] + " " + hsv[1] * 100 + " " + hsv[2] * 100 + "  -> " + distance);
                                if (targetBas == 0) {
                                    targetBas = i;
                                    targetTeinte = Math.round(hsv[0]);
                                    break;
                                }
                            } else {
                                Log.v("pixel", "not...[" + i + "] " + Color.red(pixels[i]) + " " + Color.green(pixels[i]) + " " + Color.blue(pixels[i]) + " HSV " + hsv[0] + " " + hsv[1] * 100 + " " + hsv[2] * 100 + "  -> " + distance);
                            }
                        }


                        int targetSize = 0;
                        int targetMillieu = 0;
                        if (targetHaut > 0 && targetBas > 0) {
                            targetSize = (targetBas - targetHaut);
                            targetMillieu = (targetHaut + targetSize / 2);
                            Log.i("pixel", "pok: target milieu " + targetMillieu + "             targetSize " + targetSize + "     targetTeinte " + targetTeinte);
                            //Rouge ~20
                            //Orange foncé ~ 37
                            //Orange clair ~ 48
                            //Jaune ~ 60
                            //vert ~ 83
                            //J'ai la taille de la target, je peux patienter pour l'atteindre a son minumum
                            //Target size 63 (tir imédiat), targetsize 312 (attente suivant la teinte)

                        }
                        int millieu = 0;
                        int whiteSize = 0;
                        if (blancHaut > 0 && blancBas > 0) {
                            whiteSize = blancBas - blancHaut;
                            millieu = (blancHaut + whiteSize / 2);
                            Log.i("pixel", "pok: white  milieu " + millieu + "              whiteSize " + whiteSize);
                            //si white size <200 on a capturé autre chose qu'un cercle blanc
                            //On peut meêm vérifier qu'il n'y a pas bcp de différence entre le millieu du blanc et de la target
                            if (whiteSize > 210 && whiteSize < 390) {//243 et 376
                                //J'ai le millieu je peux tirer de la bonne force
                                //926 souris sautant
                                //903 loin
                                //874 intermédiaire
                                //864 près
                            } else {
                                swipe = false;
                            }
                        }
                        if (millieu == 0 || targetSize == 0) {
                            Log.i("pixel", "pok: " + blancHaut + " " + blancBas + "  target " + targetHaut + " " + targetBas);
                        } else {
                            Log.d("pixel", "pok: " + blancHaut + " " + blancBas + "  target " + targetHaut + " " + targetBas);
                        }
                    } else {
                        Log.w("overlay", "Image not aquired");
                        swipe = false;
                    }
                    //1794 1152  1794 comprends la barre du haut, mais pas celle du bas ->  540 correspond bien au milieu de 1080
                    //1794 1080
                    //1920 1080
                    //706 ahut
                    //
                    if (swipe) {
                        commandIntent = new Intent(getApplicationContext(), CommandService.class);
                        commandIntent.setAction(Constants.ACTION.SHELL_ACTION);
                        commandIntent.putExtra("command", "input touchscreen swipe 540 1570 540 1270 80");
                        startService(commandIntent);
                    }
                } else {
                    Log.w("overlay", "ScreenGraber not initialized");
                }
            }
        });
        windowManager.addView(headView, params);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG,"onDestroy");
        if (headView != null) windowManager.removeView(headView);
        super.onDestroy();
    }

}
