package au.com.wsit.ribbit.ui;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;

import au.com.wsit.ribbit.R;


public class ViewImageActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        getSupportActionBar().hide();

        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        // Get the image URI from the intent that started this one
        Uri imageUri = getIntent().getData();

        // We use the Picasso lib to load the URL from parse
        // Context - Image Uri - object in the XML
        Picasso.with(this).load(imageUri.toString()).into(imageView);

        // Go back to the inbox after 10 seconds
        Timer timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        };

        timer.schedule(timerTask, 10*1000);


    }



}
