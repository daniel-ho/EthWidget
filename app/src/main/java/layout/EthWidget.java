package layout;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONObject;
import org.json.JSONException;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.github.daniel_ho.ethwidget.R;

/**
 * Implementation of App Widget functionality.
 */
public class EthWidget extends AppWidgetProvider {

    public String url = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=ETH&tsyms=USD";
    public RequestQueue requestQueue = null;

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        for (int appWidgetId : appWidgetIds) {
            final int id = appWidgetId;

            JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                updateData(context, appWidgetManager, id, response);
                            } catch(JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Volley", "Error");
                        }
                });
            requestQueue.add(request);
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

    private void updateData(Context context, AppWidgetManager appWidgetManager, int appWidgetId, JSONObject response) throws JSONException {

        String price = context.getString(R.string.price_text);
        String percent_change = context.getString(R.string.percentage_text);

        try{
            JSONObject eth_usd_data = response.getJSONObject("RAW").getJSONObject("ETH").getJSONObject("USD");
            double price_d = eth_usd_data.getDouble("PRICE");
            double percent_change_d = eth_usd_data.getDouble("CHANGEPCT24HOUR");
            price = "1 ETH = $" + String.format("%.2f", price_d);
            percent_change = "24 Hour Change: " + String.format("%.2f", percent_change_d) + "%";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("M/d hh:mm a");
        String localTime = "Updated " + date.format(currentLocalTime);

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

}

