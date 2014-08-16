package jp.fkmsoft.demo.fitdemo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Bucket;
import com.google.android.gms.fitness.DataInsertRequest;
import com.google.android.gms.fitness.DataPoint;
import com.google.android.gms.fitness.DataReadRequest;
import com.google.android.gms.fitness.DataReadResult;
import com.google.android.gms.fitness.DataSet;
import com.google.android.gms.fitness.DataSource;
import com.google.android.gms.fitness.DataSourcesRequest;
import com.google.android.gms.fitness.DataSourcesResult;
import com.google.android.gms.fitness.DataType;
import com.google.android.gms.fitness.DataTypes;
import com.google.android.gms.fitness.Device;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessScopes;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.ListSubscriptionsResult;
import com.google.android.gms.fitness.Session;
import com.google.android.gms.fitness.Subscription;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fragment for top page
 */
public class TopFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final int REQUEST_OAUTH = 1000;
    private static final int REQUEST_START_SESSION = 1001;
    private static final int[] BUTTON_IDS = {
            R.id.button_sensor_api,
            R.id.button_subscribe, R.id.button_list_subscribe, R.id.button_unsubscribe, R.id.button_start_session,
            R.id.button_read_data, R.id.button_insert_data};

    public static TopFragment newInstance() {
        TopFragment fragment = new TopFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    private GoogleApiClient mApiClient = null;
    private TextView mMessageText = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_top, container, false);

        mMessageText = (TextView) root.findViewById(R.id.text_message);

        for (int id : BUTTON_IDS) {
            root.findViewById(id).setOnClickListener(mClickListener);
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mApiClient == null || !mApiClient.isConnected()) {
            connect();
        }
    }

    private void connect() {
        Activity activity = getActivity();
        if (activity == null) { return; }

        // Create the Google API Client
        mApiClient = new GoogleApiClient.Builder(activity)
                // select the Fitness API
                .addApi(Fitness.API)
                        // specify the scopes of access
                .addScope(FitnessScopes.SCOPE_ACTIVITY_READ)
                .addScope(FitnessScopes.SCOPE_BODY_READ_WRITE)
                        // provide callbacks
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Connect the Google API client
        mApiClient.connect();
    }

    private void addMessage(String message) {
        TextView messageText = mMessageText;
        if (messageText == null) { return; }
        messageText.setText(messageText.getText().toString() + message + "\n");
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.button_sensor_api:
                getDataSources();
                break;
            case R.id.button_subscribe:
                subscribe();
                break;
            case R.id.button_list_subscribe:
                listSubscription();
                break;
            case R.id.button_unsubscribe:
                unsubscribe();
                break;
            case R.id.button_start_session:
                showStartSessionDialog();
                break;
            case R.id.button_read_data:
                readData();
                break;
            case R.id.button_insert_data:
                insertData();
                break;
            }
        }
    };

    private void getDataSources() {
        DataSourcesRequest req = new DataSourcesRequest.Builder()
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .setDataTypes(DataTypes.STEP_COUNT_CUMULATIVE)
                .build();

        // 2. Invoke the Sensors API with:
        // - The Google API client object
        // - The data sources request object
        PendingResult<DataSourcesResult> pendingResult =
                Fitness.SensorsApi.findDataSources(mApiClient, req);

        addMessage("getting available data sources");
        // 3. Obtain the list of data sources asynchronously
        pendingResult.setResultCallback(new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                List<DataSource> dataSources = dataSourcesResult.getDataSources();
                addMessage("count of data sources : " + dataSources.size());
                for (DataSource ds : dataSources) {
                    String dsName = ds.getName();
                    Device device = ds.getDevice();
                    addMessage("Name:" + dsName + " device=" + device.getUid());
                }
            }
        });
    }

    private void subscribe() {
        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.google.android.gms.common.api.Status doInBackground(Void... voids) {
                PendingResult<com.google.android.gms.common.api.Status> pendingResult =
                        Fitness.RecordingApi.subscribe(mApiClient, DataTypes.ACTIVITY_SAMPLE);

                // 2. Retrieve the result synchronously
                // (For the subscribe method, this call returns immediately)
                return pendingResult.await();
            }

            @Override
            protected void onPostExecute(com.google.android.gms.common.api.Status status) {
                super.onPostExecute(status);
                if (status.isSuccess()) {
                    addMessage("Successfully subscribed!");
                } else {
                    addMessage("There was a problem subscribing.");
                }
            }
        }.execute();
    }

    private void listSubscription() {
        new AsyncTask<Void, Void, ListSubscriptionsResult>() {

            @Override
            protected ListSubscriptionsResult doInBackground(Void... voids) {
                PendingResult<ListSubscriptionsResult> pendingResult =
                        Fitness.RecordingApi.listSubscriptions(mApiClient, DataTypes.ACTIVITY_SAMPLE);

                // 2. Retrieve the list of subscriptions synchronously
                // (For the listSubscriptions method, this call returns immediately)
                return pendingResult.await();
            }

            @Override
            protected void onPostExecute(ListSubscriptionsResult listResult) {
                super.onPostExecute(listResult);
                List<Subscription> subscriptions = listResult.getSubscriptions();
                addMessage("count of subscriptions : " + subscriptions.size());
                for (Subscription sc : subscriptions) {
                    // Get information about each subscription
                    DataType dt = sc.getDataType();
                    addMessage("dataType : " + dt.getName());
                }
            }
        }.execute();
    }

    private void unsubscribe() {
        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.google.android.gms.common.api.Status doInBackground(Void... voids) {
                PendingResult<com.google.android.gms.common.api.Status> pendingResult =
                        Fitness.RecordingApi.unsubscribe(mApiClient, DataTypes.ACTIVITY_SAMPLE);

                // 2. Retrieve the result of the request synchronously
                // (For the unsubscribe method, this call returns immediately)
                return pendingResult.await();
            }

            @Override
            protected void onPostExecute(com.google.android.gms.common.api.Status status) {
                super.onPostExecute(status);
                if (status.isSuccess()) {
                    addMessage("Subscription removed successfully.");
                } else {
                    addMessage("Subscription not removed.");
                }
            }
        }.execute();
    }

    private void showStartSessionDialog() {
        StartSessionDialog dialog = StartSessionDialog.newInstance(this, REQUEST_START_SESSION);
        dialog.show(getFragmentManager(), "");
    }

    private void startSession(final String name, final String identifier, final String description) {
        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.google.android.gms.common.api.Status doInBackground(Void... voids) {
                // 1. Create a session object
                // (provide a name, identifier, description and start time)
                Session session = new Session.Builder()
                        .setName(name)
                        .setIdentifier(identifier)
                        .setDescription(description)
                        .setStartTimeMillis(1000)
                        // optional - if your app knows what activity:
                        .setActivity(FitnessActivities.RUNNING)
                        .build();

                // 2. Invoke the Recording API with:
                // - The Google API client object
                // - The request object
                PendingResult<com.google.android.gms.common.api.Status> pendingResult =
                        Fitness.RecordingApi.startSession(mApiClient, session);

                return pendingResult.await();
            }

            @Override
            protected void onPostExecute(com.google.android.gms.common.api.Status status) {
                super.onPostExecute(status);
                if (status.isSuccess()) {
                    addMessage("Session is started successfully.");
                } else {
                    addMessage("Session is not started.");
                }
            }
        }.execute();
    }

    private void readData() {
        // 1. Obtain start and end times
        // (In this example, the start time is one week before this moment)
        long WEEK_IN_MS = 1000 * 60 * 60 * 24 * 7;
        Date now = new Date();
        long endTime = now.getTime();
        long startTime = endTime - (WEEK_IN_MS);

        // 2. Create a data request specifying data types and a time range
        // (In this example, group the data to find how many steps were walked per day)
        DataReadRequest readreq = new DataReadRequest.Builder()
                .addAggregatedDefaultDataSource(DataTypes.STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime)
                .build();

        // 3. Invoke the History API with:
        // - The Google API client object
        // - The read data request
        PendingResult<DataReadResult> pendingResult =
                Fitness.HistoryApi.readData(mApiClient, readreq);

        // 4. Access the results of the query asynchronously
        // (The result is not immediately available)
        pendingResult.setResultCallback(new ResultCallback<DataReadResult>() {
                @Override
                public void onResult(DataReadResult readDataResult) {
                    // If the request specified aggregated data, the data is returned as buckets
                    // that contain lists of DataSet objects
                    if (readDataResult.getBuckets().size() > 0) {
                        for (Bucket bucket : readDataResult.getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet dataSet : dataSets) {
                                // Show the data points (see next example)
                                dumpDataSet(dataSet);
                            }
                        }
                        // Otherwise, the data is returned as a list of DataSet objects
                    } else if (readDataResult.getDataSets().size() > 0) {
                        for (DataSet dataSet : readDataResult.getDataSets()) {
                            // Show the data points (see next example)
                            dumpDataSet(dataSet);
                        }
                    }
                }
        });
    }

    private void dumpDataSet(DataSet dataSet) {
        addMessage("Data type: " + dataSet.getDataType().getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        for (DataPoint dp : dataSet.getDataPoints()) {

            // Obtain human-readable start and end times
            long dpStart = dp.getStartTimeNanos() / 1000000;
            long dpEnd = dp.getEndTimeNanos() / 1000000;
            addMessage("Data point:");
            addMessage("\tType: " + dp.getDataType().getName());
            addMessage("\tStart: " + dateFormat.format(dpStart));
            addMessage("\tEnd: " + dateFormat.format(dpEnd));
            for(DataType.Field field : dp.getDataType().getFields()) {
                String fieldName = field.getName();
                addMessage("\tField: " + fieldName + " Value: " + dp.getValue(field));
            }
       }
    }

    private void insertData() {
        Activity activity = getActivity();
        if (activity == null) { return; }

        // 1. Create a data source
        DataSource dsApp = new DataSource.Builder()
                .setAppPackageName(activity)
                .setDataType(DataTypes.STEP_COUNT_DELTA)
                .setName("myapp-stepcount")
                .setType(DataSource.TYPE_RAW)
                .build();

        // 2. Create a data set
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date now = new Date();
        long endTime = now.getTime();
        long startTime = endTime - (DAY_IN_MS);

        DataSet dataSet = DataSet.create(dsApp);
        // for each data point (startTime, endTime, stepDeltaValue):
        dataSet.add(
                dataSet.createDataPoint()
                        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                        .setIntValues(1024)
        );

        // 3. Build a data insert request
        DataInsertRequest insreq = new DataInsertRequest.Builder()
                .setDataSet(dataSet)
                .build();

        // 4. Invoke the History API with:
        // - The Google API client object
        // - The insert data request
        PendingResult<Status> pendingResult =
                Fitness.HistoryApi.insert(mApiClient, insreq);

        // 5. Check the result asynchronously
        // (The result is not immediately available)
        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    // Insertion succeeded
                    addMessage("insert is succeeded.");
                } else {
                    // Insertion failed
                    addMessage("insert is failed.");
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_OAUTH:
            if (resultCode != Activity.RESULT_OK) { return; }

            mApiClient.connect();
            return;
        case REQUEST_START_SESSION:
            if (resultCode != Activity.RESULT_OK) { return; }

            String name = data.getStringExtra(StartSessionDialog.EXTRA_NAME);
            String identifier = data.getStringExtra(StartSessionDialog.EXTRA_IDENTIFIER);
            String description = data.getStringExtra(StartSessionDialog.EXTRA_DESCRIPTION);

            startSession(name, identifier, description);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mMessageText = null;

    }

    // callbacks

    @Override
    public void onConnected(Bundle bundle) {
        addMessage("Connected");
        setButtonEnable(true);
    }

    private void setButtonEnable(boolean value) {
        View root = getView();
        if (root == null) { return; }

        for (int id : BUTTON_IDS) {
            root.findViewById(id).setEnabled(value);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            addMessage("Connection lost.  Cause: Network Lost.");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            addMessage("Connection lost.  Reason: Service Disconnected");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Activity activity = getActivity();
        if (activity == null) { return; }

        // Error while connecting. Try to resolve using the pending intent returned.
        if (result.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED ||
                result.getErrorCode() == FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS) {
            try {
                // Request authentication
                result.startResolutionForResult(getActivity(), REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                addMessage("Exception connecting to the fitness service : " + e.getMessage());
            }
        } else {
            addMessage("Unknown connection issue. Code = " + result.getErrorCode());
        }
    }
}
