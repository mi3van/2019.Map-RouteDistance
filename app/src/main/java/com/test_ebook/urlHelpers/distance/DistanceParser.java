package com.test_ebook.urlHelpers.distance;

import android.os.AsyncTask;

import com.test_ebook.urlHelpers.TaskLoadedCallback;

import org.json.JSONObject;

public class DistanceParser extends AsyncTask<String, Integer, String> {
    private TaskLoadedCallback _taskCallback;

    public DistanceParser(TaskLoadedCallback taskCallback) {
        this._taskCallback = taskCallback;
    }

    // Parsing the data in non-ui thread
    private String _distance;
    @Override
    protected String doInBackground(String... jsonData) {

        String distance = "не рассчитана";

        try {
            JSONObject jObject = new JSONObject(jsonData[0]);
            DistanceDataParser parser = new DistanceDataParser();

            // Starts parsing data
            distance = parser.parse(jObject);

        } catch (Exception e) {
            e.printStackTrace();
        }
        _distance = distance;
        return distance;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            _distance = result;
        }
        _taskCallback.onTaskDone(_distance);
    }
}