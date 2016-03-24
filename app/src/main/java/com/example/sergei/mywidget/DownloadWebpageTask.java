package com.example.sergei.mywidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Sergei on 18.03.2016.
 */
public class DownloadWebpageTask extends AsyncTask<String, Void, Weather> {
        private RemoteViews remoteViews;
        private AppWidgetManager widgetManager;
        private ComponentName watchWidget;

    public DownloadWebpageTask(RemoteViews remoteViews,AppWidgetManager widgetManager,ComponentName watchWidget) {
        this.remoteViews = remoteViews;
        this.widgetManager = widgetManager;
        this.watchWidget = watchWidget;
    }

    @Override
    protected void onPostExecute(Weather weatherAttributes) {
        super.onPostExecute(weatherAttributes);
        remoteViews.setTextViewText(R.id.temperatureTextView, weatherAttributes.getTemperature());
        remoteViews.setTextViewText(R.id.conditionTextView, weatherAttributes.getCondition());
        remoteViews.setTextViewText(R.id.sunriseTextView, weatherAttributes.getSunrise());
        remoteViews.setTextViewText(R.id.sunsetTextView,weatherAttributes.getSunset());
        widgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    @Override
        protected Weather doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return new Weather();
            }
        }

    public Weather downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        Weather weatherAttributes = new Weather();
        myurl = "http://weather.yahooapis.com/forecastrss?w="+getWOEIDByCityName(myurl)+"&u=c";
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            //Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            //horrible
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            weatherAttributes.setTemperature(getElementAttributeByTagName("yweather:condition","temp",doc));
            weatherAttributes.setCondition(getElementAttributeByTagName("yweather:condition", "text", doc));
            weatherAttributes.setSunrise(getElementAttributeByTagName("yweather:astronomy", "sunrise", doc));
            weatherAttributes.setSunset(getElementAttributeByTagName("yweather:astronomy", "sunset", doc));

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return weatherAttributes;
    }
    public String getElementAttributeByTagName(String tagName,String attribute,Document doc){
        String result = null;
        NodeList nods = doc.getElementsByTagName(tagName);
        if (nods.getLength() > 0) {
            Element nodo = (Element) nods.item(0);
            result =  nodo.getAttribute(attribute);
        }
        return result;
    }

    public String getWOEIDByCityName(String cityName) throws IOException {
        String fullUrl = getFullUrl(cityName);
        URL url;
        Document docs = null;
        InputStream is = null;
        try {
            url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            is = conn.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            docs = factory.newDocumentBuilder().parse(is);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return getElementAttributeByTagName("locality1","woeid",docs);
    }

    private String getFullUrl(String cityName){
        String yahooPlaceApisBase = "http://query.yahooapis.com/v1/public/yql?q=select*from%20geo.places%20where%20text=";
        String yahooapisFormat = "&format=xml";
        return yahooPlaceApisBase
                + "%22" + cityName + "%22"
                + yahooapisFormat;
    }
}
