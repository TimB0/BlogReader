package com.racecondition.blogreader.app;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainListActivity extends ListActivity {

    public static final int NUMBER_OF_POSTS = 20;
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected JSONObject mBlogData;
    protected ProgressBar mProgressBar;

    private final String KEY_TITLE = "title";
    private final String KEY_AUTHOR = "author";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
            getBlogPostsTask.execute();
        } else {
            Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(this, getString(R.string.no_items), Toast.LENGTH_LONG).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }


        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main_list, menu);
            return true;
        }

        public void handleBlogResponse() {
            mProgressBar.setVisibility(View.INVISIBLE);

            if (mBlogData == null) {
                updateDisplayForError();
            }
            else {
                try {
                    JSONArray jsonPosts = mBlogData.getJSONArray("posts");
                    ArrayList<HashMap<String, String>> blogPosts =
                            new ArrayList<HashMap<String, String>>();
                    for (int i = 0; i < jsonPosts.length(); i++) {
                        JSONObject post = jsonPosts.getJSONObject(i);
                        String title = post.getString(KEY_TITLE);
                        title = Html.fromHtml(title).toString();
                        String author = post.getString(KEY_AUTHOR);
                        author = Html.fromHtml(author).toString();

                        HashMap<String, String> blogPost = new HashMap<String, String>();
                        blogPost.put(KEY_TITLE, title);
                        blogPost.put(KEY_AUTHOR, author);

                        blogPosts.add(blogPost);

                    }

                    String[] keys = { KEY_TITLE, KEY_AUTHOR };
                    int[] ids = { android.R.id.text1, android.R.id.text2 };
                    SimpleAdapter adapter = new SimpleAdapter(this, blogPosts,
                            android.R.layout.simple_list_item_2,
                            keys, ids);

                    setListAdapter(adapter);
                }
                catch (JSONException e) {
                    Log.e(TAG, "JSONException caught!", e);
                }
            }

        }

    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView emptyTextView = (TextView) getListView().getEmptyView();
        emptyTextView.setText(getString(R.string.no_items));
    }

    private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {

            @Override
            protected JSONObject doInBackground(Object... args) {
                int responseCode = -1;
                JSONObject jsonResponse = null;

                try {
                    URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);
                    HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                    connection.connect();

                    responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        Reader reader = new InputStreamReader(inputStream);
                        int contentLength = connection.getContentLength();
                        char[] charArray = new char[contentLength];
                        reader.read(charArray);
                        String responseData = new String(charArray);

                        jsonResponse = new JSONObject(responseData);

                    }
                    else {
                        Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                    }
                }
                catch (MalformedURLException e) {
                    Log.e(TAG, "MalformedURLException caught by Developer: ", e);
                }
                catch (IOException e) {
                    Log.e(TAG, "IOException caught by Developer: ", e);
                }
                catch (Exception e) {
                    Log.e(TAG, "Exception caught by Developer: ", e);
                }

                return jsonResponse;
                // jsonResponse is automatically passed to onPostExecute by this doInBackground method
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                mBlogData = result;
                handleBlogResponse();

            }

        }


}




//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }*
//
//}
