package layout;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import org.json.JSONObject;
import org.json.JSONException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.github.daniel_ho.ethwidget.R;

//TODO: Create configuration activity file to choose background color

/**
 * Implementation of App Widget functionality.
 */
public class EthWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=ETH&tsyms=USD";
        String price = context.getString(R.string.price_text);
        String percent_change = context.getString(R.string.percentage_text);
        String localTime = context.getString(R.string.updateTime_text);

        /*try{
            JSONObject jsonObject = getJSONObjectFromURL(url);
            JSONObject eth_usd_data = jsonObject.getJSONObject("RAW").getJSONObject("ETH").getJSONObject("USD");
            double price_d = eth_usd_data.getDouble("PRICE");
            double percent_change_d = eth_usd_data.getDouble("CHANGEPCT24HOUR");
            price = String.format("%.2f", price_d);
            percent_change = "24 Hour Change: " + String.format("%.2f", percent_change_d) + "%";

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
            Date currentLocalTime = cal.getTime();
            DateFormat date = new SimpleDateFormat("M/d HH:mm a");
            date.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
            localTime = "Updated " + date.format(currentLocalTime);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }*/

        double price_d = 1001.245;
        double percent_change_d = 5.981341;
        price = "1 ETH = $" + String.format("%.2f", price_d);
        percent_change = "24 Hour Change: " + String.format("%.2f", percent_change_d) + "%";

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-7:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("M/d hh:mm a");
        date.setTimeZone(TimeZone.getTimeZone("GMT-7:00"));
        localTime = "Updated " + date.format(currentLocalTime);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.eth_widget);
        views.setTextViewText(R.id.price_text, price);
        views.setTextViewText(R.id.percentage_text, percent_change);
        views.setTextViewText(R.id.updateTime_text, localTime);

        Intent intent = new Intent(context, EthWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.refreshButton, pIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName component = new ComponentName(context, EthWidget.class);
            onUpdate(context, manager, manager.getAppWidgetIds(component));
        }
    }


    // Credits: https://stackoverflow.com/a/34691486
    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();
        return new JSONObject(jsonString);
    }
}

