package edu.grinnell.appdev.database;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * @brief shows the personal info on a member of appdev
 *
 * @author Liam Niehus-Staab
 */
public class personalData extends AppCompatActivity {

    private String TAG = personalData.class.getSimpleName();
    ImageView photo;
    TextView name;
    TextView title;
    TextView type1;
    TextView url1;
    TextView type2;
    TextView url2;
    TextView role;
    Toolbar backMenu;

    /**
     * @brief code that is run when this activity is launched.
     *        Sets all view objects
     * @param savedInstanceState - saved preferences (unused)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);

        Intent intent = getIntent();

        //find views
        photo = findViewById(R.id.personal_pic);
        role = findViewById(R.id.role);
        name = findViewById(R.id.name);
        title = findViewById(R.id.title);
        type1 = findViewById(R.id.t1);
        url1 = findViewById(R.id.url1);
        type2 = findViewById(R.id.t2);
        url2 = findViewById(R.id.url2);
        backMenu = findViewById(R.id.back_toolbar);
        setSupportActionBar(backMenu);

        //strange picasso header things
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        okhttp3.Request newRequest = chain.request().newBuilder()
                                .addHeader("Accept", "image/*")
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();

        Picasso picasso = new Picasso.Builder(this.getApplicationContext())
                .downloader(new OkHttp3Downloader(client))
                .build();

        //get image url via intent and convert to imageview using picasso
        String picURL = intent.getExtras().getString("IMG");
        Log.e(TAG, "IMAGE URL IS " + picURL);
        //Picasso.get()
        picasso.load(picURL)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.mipmap.ic_launcher)
                .into(photo, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "picasso ERROR: " + e.getMessage());
                    }
                });

        //set textviews correctly via intent
        name.setText(intent.getExtras().getString("NAME"));
        role.setText(intent.getExtras().getString("TIMG"));
        title.setText(intent.getExtras().getString("TITLE"));
        type1.setText(intent.getExtras().getString("T1"));
        url1.setText(intent.getExtras().getString("URL1"));
        type2.setText(intent.getExtras().getString("T2"));
        url2.setText(intent.getExtras().getString("URL2"));

        //set urls to hyperlink
        url1.setMovementMethod(LinkMovementMethod.getInstance());
        url2.setMovementMethod(LinkMovementMethod.getInstance());

    }


    /**
     * @brief inflates the toolbar menu at top of activity
     *
     * @param menuParam - the menu
     * @return - true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menuParam) {
        //inflate menu
        getMenuInflater().inflate(R.menu.menu_back, menuParam);

        return true;
    }

    /**
     * @brief actions to perform when a menu item is clicked
     *
     * @param item - the item in the menu
     * @return - boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.back_button:
                //back button is clicked, launch mainactivity
                launchMainActivity();
                return true;
            default:
                //unrecognized action
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @brief launches the main activity to return to listview
     */
    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
