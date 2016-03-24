package com.example.sergei.mywidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;
import android.widget.RemoteViews;

import java.util.concurrent.ExecutionException;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link NewAppWidgetConfigureActivity NewAppWidgetConfigureActivity}
 */
public class NewAppWidget extends AppWidgetProvider{

    private RemoteViews remoteViews;
    private static final String UPDATE_CLICKED = "WidgetButtonClick";

//    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
//                                   int appWidgetId) {
//
//        CharSequence widgetText = NewAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
//        // Construct the RemoteViews object
//        //RemoteViews views = context.getResources().getResource(R.layout.new_app_widget););
//        //views.setTextViewText(R.id.appwidget_text, widgetText);
//
//
//        ////////////////
//        //////////////
//
//        // Instruct the widget manager to update the widget
//        //appWidgetManager.updateAppWidget(appWidgetId, views);
//
//    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views;
        ComponentName watchWidget;

        views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        watchWidget = new ComponentName(context, NewAppWidget.class);
        views.setOnClickPendingIntent(R.id.updateButton, getPendingSelfIntent(context, UPDATE_CLICKED));

        // There may be multiple widgets active, so update all of them
        appWidgetManager.updateAppWidget(watchWidget,views);
//        for (int appWidgetId : appWidgetIds) {
//            updateAppWidget(context, appWidgetManager, appWidgetId);
//        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            NewAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (UPDATE_CLICKED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            //RemoteViews remoteViews;
            ComponentName watchWidget;
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
            watchWidget = new ComponentName(context, NewAppWidget.class);

            ConnectivityManager connMgr = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if(networkInfo != null && networkInfo.isConnected()) {
                new DownloadWebpageTask(remoteViews,appWidgetManager,watchWidget).execute("Moscow");
            }


            ////////update
            remoteViews.setTextViewText(R.id.cityNameTextView, "Moscow");

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);


        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

}

