package com.example.yuhasj.sock_widget_single;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.*;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of App Widget functionality.
 */



public class StockWidget extends AppWidgetProvider {

    private static final String CLICKED = "CLICKED";

    void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,final  int appWidgetId) {
        System.out.println("Called!");
        // Create a Queue, this doesn't seem to work, so trying context
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&interval=1min&symbol=AMZN" +
                "&apikey=TQ5U123VCD8ZZJFX&outputsize=compact";

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.stock_widget);
        views.setTextViewText(R.id.appwidget_text, "Loading...");
        appWidgetManager.updateAppWidget(appWidgetId, views);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        System.out.println("Response is: "+ response.substring(0,500));

                        RemoteViews views = new RemoteViews(context.getPackageName(),
                                R.layout.stock_widget);
                        views.setTextViewText(R.id.appwidget_text, getPriceFromJSON(response));
                        appWidgetManager.updateAppWidget(appWidgetId, views);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String body = error.toString();
                        System.out.println(body);
                    }
                });

        stringRequest.setRetryPolicy(getRetryPolicy());
        queue.add(stringRequest);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
        ComponentName watchWidget = new ComponentName(context, StockWidget.class);
        remoteViews.setOnClickPendingIntent(R.id.appwidget_text, getPendingSelfIntent(context, CLICKED));
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        System.out.println("I HAVE BEEN CREATED");
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        System.out.println("I HAVE BEEN Disabled");
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {

        if (CLICKED.equals(intent.getAction())) {
            System.out.println("clicked!");

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
            ComponentName thisWidget = new ComponentName(context.getApplicationContext(), StockWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }

        // Chain up to the super class so the onEnabled, etc callbacks get dispatched
        super.onReceive(context, intent);
        // Handle a different Intent
        System.out.println("Received!");

    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }


    public static String getPriceFromJSON(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            String lastRefreshed = obj.getJSONObject("Meta Data").getString("3. Last Refreshed");
            String price = obj.getJSONObject("Time Series (1min)")
                    .getJSONObject(lastRefreshed).getString("4. close");

            return "AMZN " + lastRefreshed + " $" +  price;
        } catch (JSONException e) {
            e.printStackTrace();
            return "ERROR PARSING";
        }

    }

    private static RetryPolicy getRetryPolicy() {
        return new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        };
    }

}

