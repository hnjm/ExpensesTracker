package com.amilabs.android.expensestracker.tasks;

import com.amilabs.android.expensestracker.R;
import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.amilabs.android.expensestracker.utils.SharedPref;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExchangeRateTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "ExchangeRateTask";
    private static final String CURRENCY_RATE_REQUEST1 = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22";
    private static final String CURRENCY_RATE_REQUEST2 = "%22)&env=store://datatables.org/alltableswithkeys&format=json";
    private Context context;
    private Spinner spinner;
    private static DatabaseHandler db;
    private final ProgressDialog dialog;
    private int previousPosition;

    public ExchangeRateTask(Context context, Spinner spinner, int previousPosition) {
        this.context = context;
        this.spinner = spinner;
        this.previousPosition = previousPosition;
        db = DatabaseHandler.getInstance(context);
        dialog = new ProgressDialog(context);
    }

    public boolean isNetworkReachable() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wimax = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        NetworkInfo dataNetwork = connMgr.getActiveNetworkInfo();
        boolean wifiConnected = false, mobileConnected = false, wimaxConnected = false, dataNetworkConnected = false;
        if (null != wifi)
            wifiConnected = wifi.isConnected();
        if (null != mobile)
            mobileConnected = mobile.isConnected();
        if (null != wimax)
            wimaxConnected = wimax.isConnected();
        if (null != dataNetwork)
            dataNetworkConnected = dataNetwork.isConnected();
        if (wifiConnected || mobileConnected || wimaxConnected || dataNetworkConnected)
            return true;
        return false;
    }

    private String getJson(String url) throws ClientProtocolException, IOException {
        StringBuilder build = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpPost httpGet = new HttpPost(url);
        HttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        InputStream content = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
        String con;
        while ((con = reader.readLine()) != null)
            build.append(con);
        return build.toString();
    }

    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("Calculating new currency rate...");
        this.dialog.show();
    }

    @Override
    protected Boolean doInBackground(final Void... args) {
        boolean result = true;
        try {
            if (isNetworkReachable()) {
                String from = SharedPref.getCurrency(context);
                String to = spinner.getSelectedItem().toString();
                String fullUrlStr = CURRENCY_RATE_REQUEST1 + from + to + CURRENCY_RATE_REQUEST2;
                String s = getJson(fullUrlStr);
                double rate = new JSONObject(s).getJSONObject("query").getJSONObject("results")
                        .getJSONObject("rate").getDouble("Rate");
                db.updateAllWithNewRate(rate);
            } else
                result = false;
        } catch (JSONException e) {
            Log.e(TAG, "Exception occurred in doInBackground(). ", e);
            result = false;
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Exception occurred in doInBackground(). ", e);
            result = false;
        } catch (IOException e) {
            Log.e(TAG, "Exception occurred in doInBackground(). ", e);
            result = false;
        }
        return result;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (dialog.isShowing())
            dialog.dismiss();
        if (success || (db.getCategoriesCount() == 0 && db.getExpensesDataCount() == 0))
            SharedPref.saveCurrency(context, spinner.getSelectedItem().toString());
        else {
            spinner.setSelection(previousPosition);
            Toast.makeText(context, R.string.exchange_rate_failed, Toast.LENGTH_SHORT).show();
        }
    }

}