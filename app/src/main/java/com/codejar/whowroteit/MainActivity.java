package com.codejar.whowroteit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    private EditText mBookInput;
    private TextView mTitleText;
    private TextView mAuthorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookInput = (EditText)findViewById(R.id.bookInput);
        mTitleText = (TextView)findViewById(R.id.titleText);
        mAuthorText = (TextView)findViewById(R.id.authorText);
    }

    public void searchBooks(View view) {
        // Get the search string from the input field.
        InputMethodManager inputManager=
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null){
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
        String queryString = mBookInput.getText().toString();
        ConnectivityManager connManager = (
                ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connManager != null) {
            networkInfo = connManager.getActiveNetworkInfo();
        } else {
            mTitleText.setText((R.string.no_connection));
        }
        if (networkInfo != null) {
            if (queryString.trim().length() >= 1) {
                if (networkInfo.isConnected()) {
                    new FetchBook(mTitleText, mAuthorText).execute(queryString);
                    mAuthorText.setText("");
                    mTitleText.setText((R.string.loading));
                }else {
                    mTitleText.setText((R.string.no_connection));
                    mAuthorText.setText("");
                }
            }else {
                mTitleText.setText((R.string.empty_field));
                mAuthorText.setText("");
            }
        }else {
            mTitleText.setText((R.string.no_connection));
            mAuthorText.setText("");
        }
    }

       public static class FetchBook extends AsyncTask<String, Void, String> {

        private WeakReference<TextView> mTitleText;
        private WeakReference<TextView> mAuthorText;

        FetchBook(TextView titleText, TextView authorText) {
            this.mTitleText = new WeakReference<>(titleText);
            this.mAuthorText = new WeakReference<>(authorText);
        }

        @Override
        protected String doInBackground(String... strings) {

            return NetworkUtils.getBookInfo(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject= new JSONObject(s);
                JSONArray itemsArray=jsonObject.getJSONArray("items");
                int i=0;
                String title="";
                String Authors="";

                while (i<itemsArray.length()){

                    JSONObject book= itemsArray.getJSONObject(i);
                    JSONObject volumeInfo = book.getJSONObject("volumeInfo");
                    try {
                        title += volumeInfo.getString("title");
                        Authors += volumeInfo.getString("authors");
                        title +="\n\n";
                        Authors += "\n\n";
                    }catch (Exception err){
                        mTitleText.get().setText(R.string.no_result);
                        mAuthorText.get().setText("");
                        Log.d("\n"+MainActivity.class.getSimpleName(), "\n"+err.getMessage()+"********************************\n");
                        err.printStackTrace();
                    }
                    i++;
                }
                mTitleText.get().setText(title);
                mAuthorText.get().setText(Authors);
            }catch (JSONException j_err){
                j_err.printStackTrace();
            }
        }
    }
}
