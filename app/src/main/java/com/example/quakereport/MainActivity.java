package com.example.quakereport;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /** Tag for the log messages */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&limit=25";

    ArrayList<Earthquake> earthquakes_output = new ArrayList<>();

    private ArrayList<Earthquake> myArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a fake list of earthquake locations.
           // Uses query from QueryUtil
//        earthquakes_output = QueryUtils.extractEarthquakes();
//        earthquakes.add(new Earthquake("7.2","San Francisco","Feb 2,2020"));
//        earthquakes.add(new Earthquake("4.2","San Francisco","Feb 5,2020"));




        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new adapter that takes the list of earthquakes as input
//        EarthquakeAdapter adapter = new EarthquakeAdapter(this,earthquakes);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
//        earthquakeListView.setAdapter(adapter);

        for(Earthquake x:earthquakes_output){
            Log.d("print","sf"+x.getmLocation()+" "+x.getTimeInMilliseconds());
        }

        Context context = this;
        EarthquakeAsyncTask task = new EarthquakeAsyncTask(context);
        task.execute();
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class EarthquakeAsyncTask extends AsyncTask<URL, Void, ArrayList<Earthquake>> {

        private Context mContext;

        public EarthquakeAsyncTask (Context context){
            mContext = context;
        }

        @Override
        protected ArrayList<Earthquake> doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(USGS_REQUEST_URL);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            ArrayList<Earthquake> earthquake = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return earthquake;
        }




        /**
         * Update the screen with the given earthquake (which was the result of the
         * {@link }).
         */
        @Override
        protected void onPostExecute(ArrayList<Earthquake> er) {
            if (er.size() == 0) {
                return;
            }
            for(Earthquake x:er){
                Log.d("sss",x.getUrl());
                myArray.add(x);
            }

            for(Earthquake x:myArray){
                Log.d("ssprints",x.getUrl());
                earthquakes_output.add(x);
            }

            ListView earthquakeListView = (ListView) findViewById(R.id.list);

            // Create a new adapter that takes the list of earthquakes as input
            final EarthquakeAdapter adapter = new EarthquakeAdapter(mContext, earthquakes_output);

            // Set the adapter on the {@link ListView}
            // so the list can be populated in the user interface
            earthquakeListView.setAdapter(adapter);

            earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    // Find the current earthquake that was clicked on
                    Earthquake currentEarthquake = adapter.getItem(position);

                    // Convert the String URL into a URI object (to pass into the Intent constructor)
                    Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                    // Create a new intent to view the earthquake URI
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                    // Send the intent to launch a new activity
                    startActivity(websiteIntent);
                }
            });
            return;

        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            // If the URL is null, then return early.
            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                // If the request was successful (response code 200),
                // then read the input stream and parse the response.
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Return an {@link } object by parsing out information
         * about the first earthquake from the input earthquakeJSON string.
         */
        private ArrayList<Earthquake> extractFeatureFromJson(String earthquakeJSON) {

            // If the JSON string is empty or null, then return early.
            if (TextUtils.isEmpty(earthquakeJSON)) {
                return null;
            }

            try {
                JSONObject baseJsonResponse = new JSONObject(earthquakeJSON);
                JSONArray arr = baseJsonResponse.getJSONArray("features");
                ArrayList<Earthquake> earthquakes = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    JSONObject properties = obj.getJSONObject("properties");
                    String mag = properties.getString("mag");
                    String place = properties.getString("place");
                    String time = properties.getString("time");

                    double currMag = Double.parseDouble(mag);

                    long timeInMilliseconds = Long.parseLong(time);

                    // Extract the value for the key called "url"
                    String url = properties.getString("url");

                    earthquakes.add(new Earthquake(currMag, place, timeInMilliseconds, url));
                }
                return earthquakes;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
            }
            return null;
        }
    }

}
