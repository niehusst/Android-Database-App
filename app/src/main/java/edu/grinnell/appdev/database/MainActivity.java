package edu.grinnell.appdev.database;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;


/**
 * @brief the main activity; shows the listview containing all the
 *        members of appdev. Also handles asynchronously loading all
 *        data from JSON file into the listview
 * @author Liam Niehus-Staab
 */
public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private ListView lv;
    private ArrayList<HashMap<String, String>> allAppDevers; //holds all data from JSON file
    private ProgressBar spinner;
    private ImageView img;
    private TextView dever_name;
    private TextView dever_title;
    private Toolbar menu;

    /**
     * @brief code to run on the creation of this class
     * @param savedInstanceState - saved prefereneces (unused)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init vars
        lv = (ListView) findViewById(R.id.list_view);
        final MainActivity.LoadData loader = new MainActivity.LoadData();
        allAppDevers = new ArrayList<HashMap<String, String>>();
        spinner = (ProgressBar) findViewById(R.id.load_wheel);
        menu = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(menu);

        //async load JSON file data and render to listview
        loader.execute();

        //make lv clickable to launch next activity with personal info
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
                launchPersonalDataActivity(position);
            }
        });


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
        getMenuInflater().inflate(R.menu.menu_refresh, menuParam);

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
        //action options for dropdown list on toolbar
        switch(item.getItemId()) {
            case R.id.refresh:
                //user chose the refresh item, reload JSON
                MainActivity.LoadData refresh_loader = new MainActivity.LoadData();
                refresh_loader.execute();
                return true;
            default:
                //unrecognized action
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * @brief an adapter class for inflating the listview
     */
    private class CustomAdapter extends BaseAdapter {

        Picasso picasso;
        OkHttpClient client;

        /**
         * constructor for CustomAdapter class to eliminate multiple constructions
         * of costly objects
         */
        public CustomAdapter() {
            //okhttp picasso header authentification
            client = new OkHttpClient.Builder()
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

            //build single picasso instance for whole class
            picasso = new Picasso.Builder(getApplicationContext())
                    .downloader(new OkHttp3Downloader(client))
                    .build();

        }

        /**
         * @brief gets the number of items in the listview
         *
         * @return - the number of items in listview
         */
        @Override
        public int getCount() {
            return allAppDevers.size();
        }

        /**
         * @brief should get item of listview at the input position.
         *
         * @param position - the position of desired item
         * @return - a HashMap containing data on an appdever
         */
        @Override
        public Object getItem(int position) {
            return allAppDevers.get(position);
        }

        /**
         * @brief should get id of item at input position.
         *        actually does nothing.
         *
         * @param position - position of desired id
         * @return - position
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * @brief gets the correct values for each item view
         * @param position - the position in the listview
         * @param convertView
         * @param parent
         * @return - the inflated view
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //inflate layout
            convertView = getLayoutInflater().inflate(R.layout.item_layout, null);

            //get item xml info by id
            img = (ImageView) convertView.findViewById(R.id.thumbnail_photo);
            dever_name = (TextView) convertView.findViewById(R.id.name_item);
            dever_title = (TextView) convertView.findViewById(R.id.title_item);


                //get image from picasso and load into imageview thumbnail
            String img_url = allAppDevers.get(position).get("image");
            picasso.load(img_url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .fit().centerInside()
                    .into(img, new Callback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "picasso ERROR: " + e.getMessage());
                        }
                    });


            //using data form allAppDevers, set what each item should be
            dever_name.setText(allAppDevers.get(position).get("name"));
            dever_title.setText(allAppDevers.get(position).get("title"));

            return convertView;
        }
    }


    /**
     * @brief Async task class to get data from JSON file and put in ListView
     */
    private class LoadData extends AsyncTask<Void, Void, Void> {

        /**
         * @brief sets up the loading wheel for when the JSON
         *        file data is being loaded
         */
        @Override
        protected void onPreExecute() {
            //activate loading wheel
            spinner.setVisibility(View.VISIBLE);
        }

        /**
         * @brief loads data from target_url into ArrayList
         *
         * @param nothing - literally nothing. a place holder
         * @return null
         */
        @Override
        protected Void doInBackground(Void... nothing) {
            //url for JSON file where the data for the ListView comes from
            String target_url = "http://www.cs.grinnell.edu/~pradhanp/android.json";

            HttpHandler handler = new HttpHandler();
            //make request to target_url
            final String jsonString = handler.makeServiceCall(target_url);

            if(jsonString != null) {
                //try to get things from page
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);

                    //get JSON array node by name
                    JSONArray developers = jsonObject.getJSONArray("members");

                    //get image url prefix
                    String prefix = jsonObject.optString("image-url-prefix");

                    //loop through all appdevelpers in JSON file
                    for(int i = 0; i < developers.length(); i++) {
                        //put all data into allAppDevers
                        JSONObject developer = developers.getJSONObject(i);

                        String name = developer.optString("name");
                        String title = developer.optString("title");

                        //get hyperlink data
                        JSONObject link1 = developer.optJSONObject("link1");
                        String type1 = null;
                        String url1 = null;
                        if(link1 != null) { //test if link1 exists in JSON file
                            type1 = link1.optString("type");
                            url1 = link1.optString("url");
                        }
                        JSONObject link2 = developer.optJSONObject("link2");
                        String type2 = null;
                        String url2 = null;
                        if(link2 != null) {
                            type2 = link2.optString("type");
                            url2 = link2.optString("url");
                        }

                        //get full image url (including prefix)
                        String image = prefix + developer.optString("image");
                        String type = developer.optString("type");

                        //temp hashmap to hold the data of a single developer
                        HashMap<String, String> developerData = new HashMap<String, String>();
                        //add JSON data to hashmap
                        developerData.put("name", name);
                        developerData.put("title", title);
                        developerData.put("image", image);
                        developerData.put("type", type);
                        if(link1 != null) { //only add data from link1 if it exists
                            developerData.put("type1", type1);
                            developerData.put("url1", url1);
                        }
                        if(link2 != null) { //only add data from link2 if it exists
                            developerData.put("type2", type2);
                            developerData.put("url2", url2);
                        }

                        //if getting data for the first time, add hashmap data to allAppDevers
                        if(i >= allAppDevers.size()) {
                            allAppDevers.add(developerData);
                        } else { //if refresh, update existing elements fo allAppDevers
                            allAppDevers.set(i, developerData);
                        }
                    }

                } catch (final JSONException je) {
                    //make toast if error while parsing
                    Log.e(TAG, "JSONException while parsing: " + je.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "JSON parsing error: " + je.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                //make toast if server error
                Log.e(TAG, "Failed to get JSON file from server");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Failed to get JSON form server",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }


        /**
         * @brief updates data parsed from JSON file into ListView object
         *
         * @param v - literally nothing. a place holder
         */
        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            //dismiss loading wheel
            spinner.setVisibility(View.GONE);

            //use CustomAdapter class to set listview by adapter
            CustomAdapter adapter = new CustomAdapter();
            lv.setAdapter(adapter);

        }
    }


    /**
     * @brief launches the personalData activity and passes values to it by intent
     */
    private void launchPersonalDataActivity(int position) {
        Intent intent = new Intent(this, personalData.class);
        //pass MANY values by intent
        intent.putExtra("NAME", allAppDevers.get(position).get("name"));
        intent.putExtra("TITLE", allAppDevers.get(position).get("title"));
        intent.putExtra("IMG", allAppDevers.get(position).get("image"));
        intent.putExtra("TIMG", allAppDevers.get(position).get("type"));
        intent.putExtra("T1", allAppDevers.get(position).get("type1"));
        intent.putExtra("URL1", allAppDevers.get(position).get("url1"));
        intent.putExtra("T2", allAppDevers.get(position).get("type2"));
        intent.putExtra("URL2", allAppDevers.get(position).get("url2"));

        startActivity(intent);
        finish();
    }
}
