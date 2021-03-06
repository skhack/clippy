package clippy.com.clippy;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class FloatingClippyService extends Service {

    private static final int MIN_ENGAGEMENT_DELAY_MS = 5 * 1000;
    private static final int ENAGEMENT_VARAINCE_MS = 15 * 1000;
//    private static final int MIN_ENGAGEMENT_DELAY_MS = 500;
//    private static final int ENAGEMENT_VARAINCE_MS = 1000;
    private WindowManager mWindowManager;
    private View mClippyView;

    private Handler mHandler = new Handler();

    private static String [] ENGAGEMENT_PHRASES = new String[] {
            "Would you like to try YouTube Red for free for 3 months?",
            "Get started talking with someone on Allo now!",
            "Would you like to try YouTube TV for free for one month?",
            "Try G+! Google's state of the art social network, featuring Circles",
            "Did you know? A background service is probably running",
            "You may be talking with someone. Try talking with them using Duo instead!",
            "Did you know? Android has strict internal guidelines on preventing notification abuse.",
            "Maps: Turn left in 800 feet. Turn left in 300 feet. Turn left.",
            "Wifi assistant is running. We don't know what that does, either.",
    };

    private Runnable mIncreaseEngagementRunnable = new Runnable() {
        @Override
        public void run() {
            Random r = new Random();
            Log.i("Clippy", ENGAGEMENT_PHRASES[r.nextInt(ENGAGEMENT_PHRASES.length)]);

            mHandler.postDelayed(this, MIN_ENGAGEMENT_DELAY_MS + r.nextInt(ENAGEMENT_VARAINCE_MS));
        }
    };

    public FloatingClippyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mClippyView = LayoutInflater.from(this).inflate(R.layout.layout_clippy, null);

        final WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }

        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 0;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mClippyView, params);

        mClippyView.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FloatingClippyService.this, ClippyActionsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                //close the service and remove the chat heads
                stopSelf();
            }
        });

        mClippyView.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideViews();
            }
        });

        hideViews();

        ////Set the close button.
        //ImageView closeButton = (ImageView) mClippyView.findViewById(R.id.close_btn);
        //closeButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        //close the service and remove the chat head from the window
        //        stopSelf();
        //    }
        //});
        final ImageView clippyBackground = mClippyView.findViewById(R.id.clippy_background);

        clippyBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //chatHead.setX(event.getRawX());
                        //chatHead.setY(event.getRawY());
                        //clippy.setX(mChatHeadView.getWidth());
                        //clippy.setVisibility(View.VISIBLE);
                        startAction("It looks like you want to open an app, would you like some help with that?");
                        //mWindowManager.updateViewLayout(mChatHeadView, params);
                        return true;
//                    case MotionEvent.ACTION_UP:
//                        Intent intent = new Intent(ChatHeadService.this, ChatActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//
//                        //close the service and remove the chat heads
//                        stopSelf();
//                        return true;
                }
                return false;
            }
        });

        mHandler.post(mIncreaseEngagementRunnable);

        /*
        //Drag and move chat head using user's touch action.
        chatHeadImage.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_UP:
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            //Open the chat conversation click.
                            Intent intent = new Intent(FloatingClippyService.this, ClippyActionsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            //close the service and remove the chat heads
                            stopSelf();
                        }
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mClippyView, params);
                        lastAction = event.getAction();
                        return true;
                }
                return false;
            }
        });
        */
    }

    private void startAction(String question) {
        final ImageView clippy = mClippyView.findViewById(R.id.clippy_icon);
        float screenWidth = mClippyView.getWidth();
        clippy.setVisibility(View.VISIBLE);
        clippy.setX(screenWidth - clippy.getWidth());

        final TextView message = mClippyView.findViewById(R.id.message);
        message.setVisibility(View.VISIBLE);
        message.setText(question);
        final TextView yes = mClippyView.findViewById(R.id.yes);
        yes.setVisibility(View.VISIBLE);
        final TextView no = mClippyView.findViewById(R.id.no);
        no.setVisibility(View.VISIBLE);
//        for (int i = 0; i < clippy.getWidth(); i++) {
//            clippy.setX(screenWidth - i);
//
//        }

        //mWindowManager.updateViewLayout(mClippyView, params);

    }
    private void hideViews() {
        mClippyView.findViewById(R.id.clippy_icon).setVisibility(View.INVISIBLE);
        mClippyView.findViewById(R.id.message).setVisibility(View.INVISIBLE);
        mClippyView.findViewById(R.id.yes).setVisibility(View.INVISIBLE);
        mClippyView.findViewById(R.id.no).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mClippyView != null) mWindowManager.removeView(mClippyView);
    }
}
