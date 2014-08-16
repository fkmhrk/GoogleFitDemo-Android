package jp.fkmsoft.demo.fitdemo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.android.gms.fitness.DataSourceListener;
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
import com.google.android.gms.fitness.SensorRequest;
import com.google.android.gms.fitness.Session;
import com.google.android.gms.fitness.Subscription;
import com.google.android.gms.fitness.Value;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fragment for top page
 */
public class TopFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final int REQUEST_OAUTH = 1000;
    private static final int REQUEST_LIST_DATASOURCES = 1001;
    private static final int REQUEST_SUBSCRIBE = 1002;
    private static final int REQUEST_LIST_SUBSCRIPTION = 1003;
    private static final int REQUEST_UNSUBSCRIBE = 1004;
    private static final int REQUEST_START_SESSION = 1005;
    private static final int REQUEST_READ_DATA = 1006;

    private static final int[] BUTTON_IDS = {
            R.id.button_sensor_api, R.id.button_register_listener, R.id.button_unregister_listener,
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

    private FoundDataSources mFoundDataSources = new FoundDataSources();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();

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

        setHasOptionsMenu(true);
    }

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.top, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_clear:
            clearMessage();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mApiClient.isConnected()) {
            mApiClient.connect();
        }
    }

    private void addMessage(String message) {
        TextView messageText = mMessageText;
        if (messageText == null) { return; }
        messageText.setText(messageText.getText().toString() + message + "\n");
    }

    private void clearMessage() {
        TextView messageText = mMessageText;
        if (messageText == null) { return; }
        messageText.setText("");
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.button_sensor_api:
                showListDataSourcesDialog();
                break;
            case R.id.button_register_listener:
                registerListener();
                break;
            case R.id.button_unregister_listener:
                unregisterListener(mFoundDataSources.mDataSourceListener);
                break;
            case R.id.button_subscribe:
                showSelectDataTypeDialog(REQUEST_SUBSCRIBE);
                break;
            case R.id.button_list_subscribe:
                showSelectDataTypeDialog(REQUEST_LIST_SUBSCRIPTION);
                break;
            case R.id.button_unsubscribe:
                showSelectDataTypeDialog(REQUEST_UNSUBSCRIBE);
                break;
            case R.id.button_start_session:
                showStartSessionDialog();
                break;
            case R.id.button_read_data:
                showSelectDataTypeDialog(REQUEST_READ_DATA);
                break;
            case R.id.button_insert_data:
                insertData();
                break;
            }
        }
    };

    private void showListDataSourcesDialog() {
        ListDataSourcesDialogFragment dialog = ListDataSourcesDialogFragment.newInstance(this, REQUEST_LIST_DATASOURCES);
        dialog.show(getFragmentManager(), null);
    }

    private void getDataSources(final int dataSourceType, final DataType dataType) {
        DataSourcesRequest req = new DataSourcesRequest.Builder()
                .setDataSourceTypes(dataSourceType)
                .setDataTypes(dataType)
                .build();
        // 2. Invoke the Sensors API with:
        // - The Google API client object
        // - The data sources request object
        PendingResult<DataSourcesResult> pendingResult =
                Fitness.SensorsApi.findDataSources(mApiClient, req);

        addMessage("getting available data sources");
        addMessage("\tDataSource Type : " + dataSourceType);
        addMessage("\tData Type : " + dataType.getName());
        // 3. Obtain the list of data sources asynchronously
        pendingResult.setResultCallback(new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                // update FoundDataSources
                mFoundDataSources.mDataSourceType = dataSourceType;
                mFoundDataSources.mDataType = dataType;
                mFoundDataSources.mDataSources = dataSourcesResult.getDataSources();
                // unregister a listener if necessary
                unregisterListener(mFoundDataSources.mDataSourceListener);
                mFoundDataSources.mDataSourceListener = null;

                addMessage("count of data sources : " + mFoundDataSources.mDataSources.size());
                for (DataSource ds : mFoundDataSources.mDataSources) {
                    String dsName = ds.getName();
                    Device device = ds.getDevice();
                    addMessage("Name:" + dsName + " device=" + device.getUid());
                }
            }
        });
    }

    private void registerListener() {
        if (mFoundDataSources.mDataSources == null ||
            mFoundDataSources.mDataSources.size() == 0) {
            addMessage("ERROR : no DataSource. Please find data sources.");
            return;
        }

        // 1. Create a listener object to be called when new data is available
        mFoundDataSources.mDataSourceListener = new DataSourceListener() {
            @Override
            public void onEvent(DataPoint dataPoint) {
                for (DataType.Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    addMessage(" value from data source : " + val.asFloat());
                }
            }
        };

        // 2. Build a sensor registration request object
        SensorRequest req = new SensorRequest.Builder()
                .setDataType(mFoundDataSources.mDataType)
                .setDataSource(mFoundDataSources.mDataSources.get(0)) // optional
                .setSamplingRate(10, TimeUnit.SECONDS)
                .build();

        // 3. Invoke the Sensors API with:
        // - The Google API client object
        // - The sensor registration request object
        // - The listener object
        PendingResult<Status> regResult =
                Fitness.SensorsApi.register(mApiClient, req, mFoundDataSources.mDataSourceListener);

        // 4. Check the result asynchronously
        regResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    // listener registered
                    addMessage("Listener is registered");
                } else {
                    // listener not registered
                    addMessage("failed to register a listener");
                }
            }
        });
    }

    private void unregisterListener(DataSourceListener listener) {
        if (listener == null) { return; }
        // 1. Invoke the Sensors API with:
        // - The Google API client object
        // - The listener object
        PendingResult<Status> pendingResult =
                Fitness.SensorsApi.unregister(mApiClient, listener);


        // 2. Check the result
        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    // listener removed
                    addMessage("Listener is unregistered");
                } else {
                    // listener not removed
                    addMessage("failed to unregister a listener");
                }
            }
        });
    }

    private void showSelectDataTypeDialog(int requestCode) {
        DataTypeDialogFragment dialog = DataTypeDialogFragment.newInstance(this, requestCode);
        dialog.show(getFragmentManager(), null);
    }

    private void subscribe(final DataType dataType) {
        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.google.android.gms.common.api.Status doInBackground(Void... voids) {
                PendingResult<com.google.android.gms.common.api.Status> pendingResult =
                        Fitness.RecordingApi.subscribe(mApiClient, dataType);

                // 2. Retrieve the result synchronously
                // (For the subscribe method, this call returns immediately)
                return pendingResult.await();
            }

            @Override
            protected void onPostExecute(com.google.android.gms.common.api.Status status) {
                super.onPostExecute(status);
                if (status.isSuccess()) {
                    addMessage("Subscribed " + dataType.getName());
                } else {
                    addMessage("Failed to subscribe " + dataType.getName());
                }
            }
        }.execute();
    }

    private void listSubscription(final DataType dataType) {
        new AsyncTask<Void, Void, ListSubscriptionsResult>() {

            @Override
            protected ListSubscriptionsResult doInBackground(Void... voids) {
                PendingResult<ListSubscriptionsResult> pendingResult =
                        Fitness.RecordingApi.listSubscriptions(mApiClient, dataType);

                // 2. Retrieve the list of subscriptions synchronously
                // (For the listSubscriptions method, this call returns immediately)
                return pendingResult.await();
            }

            @Override
            protected void onPostExecute(ListSubscriptionsResult listResult) {
                super.onPostExecute(listResult);
                List<Subscription> subscriptions = listResult.getSubscriptions();
                addMessage("count of subscriptions(" + dataType.getName() + ") : " + subscriptions.size());
                for (Subscription sc : subscriptions) {
                    // Get information about each subscription
                    DataType dt = sc.getDataType();
                    addMessage("dataType : " + dt.getName());
                }
            }
        }.execute();
    }

    private void unsubscribe(final DataType dataType) {
        new AsyncTask<Void, Void, Status>() {

            @Override
            protected com.google.android.gms.common.api.Status doInBackground(Void... voids) {
                PendingResult<com.google.android.gms.common.api.Status> pendingResult =
                        Fitness.RecordingApi.unsubscribe(mApiClient, dataType);

                // 2. Retrieve the result of the request synchronously
                // (For the unsubscribe method, this call returns immediately)
                return pendingResult.await();
            }

            @Override
            protected void onPostExecute(com.google.android.gms.common.api.Status status) {
                super.onPostExecute(status);
                if (status.isSuccess()) {
                    addMessage("Unsubscribed " + dataType.getName());
                } else {
                    addMessage("Failed to unsubscribe " + dataType.getName());
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

    private void readData(final DataType dataType) {
        // 1. Obtain start and end times
        // (In this example, the start time is one week before this moment)
        long WEEK_IN_MS = 1000 * 60 * 60 * 24 * 7;
        Date now = new Date();
        long endTime = now.getTime();
        long startTime = endTime - (WEEK_IN_MS);

        // 2. Create a data request specifying data types and a time range
        // (In this example, group the data to find how many steps were walked per day)
        DataReadRequest readreq = new DataReadRequest.Builder()
                .addAggregatedDefaultDataSource(dataType)
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
                .setDataType(DataTypes.STEP_COUNT_CUMULATIVE)
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
        if (resultCode != Activity.RESULT_OK) { return; }

        switch (requestCode) {
        case REQUEST_OAUTH:
            mApiClient.connect();
            return;
        case REQUEST_LIST_DATASOURCES: {
            int dataSourceType = data.getIntExtra(ListDataSourcesDialogFragment.EXTRA_DATASOURCE_TYPE, 0);
            int dataType = data.getIntExtra(ListDataSourcesDialogFragment.EXTRA_DATA_TYPE, 0);

            getDataSources(toDataSourceType(dataSourceType), toDataType(dataType));

            return;
        }
        case REQUEST_SUBSCRIBE: {
            int dataType = data.getIntExtra(DataTypeDialogFragment.EXTRA_DATA_TYPE, 0);
            subscribe(toDataType(dataType));
            return;
        }
        case REQUEST_LIST_SUBSCRIPTION: {
            int dataType = data.getIntExtra(DataTypeDialogFragment.EXTRA_DATA_TYPE, 0);
            listSubscription(toDataType(dataType));
            return;
        }
        case REQUEST_UNSUBSCRIBE: {
            int dataType = data.getIntExtra(DataTypeDialogFragment.EXTRA_DATA_TYPE, 0);
            unsubscribe(toDataType(dataType));
            return;
        }
        case REQUEST_START_SESSION:

            String name = data.getStringExtra(StartSessionDialog.EXTRA_NAME);
            String identifier = data.getStringExtra(StartSessionDialog.EXTRA_IDENTIFIER);
            String description = data.getStringExtra(StartSessionDialog.EXTRA_DESCRIPTION);

            startSession(name, identifier, description);
            return;
        case REQUEST_READ_DATA: {
            int dataType = data.getIntExtra(DataTypeDialogFragment.EXTRA_DATA_TYPE, 0);
            readData(toDataType(dataType));
            return;
        }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    private int toDataSourceType(int dataSourceType) {
        return dataSourceType == 0 ? DataSource.TYPE_RAW : DataSource.TYPE_DERIVED;
    }

    private DataType toDataType(int dataType) {
        switch (dataType) {
        case 0: return DataTypes.ACTIVITY_EDGE;
        case 1: return DataTypes.ACTIVITY_SAMPLE;
        case 2: return DataTypes.ACTIVITY_SEGMENT;
        case 3: return DataTypes.ACTIVITY_SUMMARY;
        case 4: return DataTypes.CALORIES_EXPENDED;
        case 5: return DataTypes.CYCLING_PEDALING_CADENCE;
        case 6: return DataTypes.CYCLING_PEDALING_CUMULATIVE;
        case 7: return DataTypes.CYCLING_WHEEL_REVOLUTION;
        case 8: return DataTypes.CYCLING_WHEEL_RPM;
        case 9: return DataTypes.DISTANCE_CUMULATIVE;
        case 10: return DataTypes.DISTANCE_DELTA;
        case 11: return DataTypes.HEART_RATE_BPM;
        case 12: return DataTypes.HEART_RATE_SUMMARY;
        case 13: return DataTypes.HEIGHT;
        case 14: return DataTypes.LOCATION;
        case 15: return DataTypes.LOCATION_BOUNDING_BOX;
        case 16: return DataTypes.POWER_SAMPLE;
        case 17: return DataTypes.SPEED;
        case 18: return DataTypes.SPEED_SUMMARY;
        case 19: return DataTypes.STEP_COUNT_CADENCE;
        case 20: return DataTypes.STEP_COUNT_CUMULATIVE;
        case 21: return DataTypes.STEP_COUNT_DELTA;
        case 22: return DataTypes.WEIGHT;
        case 23: return DataTypes.WEIGHT_SUMMARY;
        default: return DataTypes.ACTIVITY_EDGE;
        }
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
