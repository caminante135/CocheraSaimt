package ucsd.cse110fa16.group14.ipark;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    private  final  int DURACION_SPLASH = 1500;
    ImageView img1;
    ImageView img2;
    Animation fromlogo2,fromlogo1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this,LoginPage.class);
                startActivity(intent);
                finish();
            };
        },DURACION_SPLASH);

        img1 = (ImageView)findViewById(R.id.log2);
        img2 = (ImageView)findViewById(R.id.log1);
        fromlogo2 = AnimationUtils.loadAnimation(this,R.anim.fromlogo2);
        fromlogo1 = AnimationUtils.loadAnimation(this,R.anim.fromlogo1);


        img1.setAnimation(fromlogo2);
        img2.setAnimation(fromlogo1);


    }
}
