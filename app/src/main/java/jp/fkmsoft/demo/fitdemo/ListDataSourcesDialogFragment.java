package jp.fkmsoft.demo.fitdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Spinner;

/**
 * Dialog for listing Data Sources
 */
public class ListDataSourcesDialogFragment extends DialogFragment {
    public static final String EXTRA_DATASOURCE_TYPE = "dataSourceType";
    public static final String EXTRA_DATA_TYPE = "dataType";

    public static ListDataSourcesDialogFragment newInstance(Fragment target, int requestCode) {
        ListDataSourcesDialogFragment fragment = new ListDataSourcesDialogFragment();
        fragment.setTargetFragment(target, requestCode);

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) { return null; }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(R.layout.dialog_list_datasources);
        builder.setPositiveButton(android.R.string.ok, mClickListener);
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    private final DialogInterface.OnClickListener mClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            Fragment target = getTargetFragment();
            if (target == null) { return; }

            Dialog dialog = getDialog();
            RadioGroup dataSourceTypeGroup = (RadioGroup) dialog.findViewById(R.id.radiogroup_datasource_type);
            int dataSourceType = (dataSourceTypeGroup.getCheckedRadioButtonId() == R.id.radio_datasource_raw) ? 0 : 1;

            Spinner dataTypeSpinner = (Spinner) dialog.findViewById(R.id.spinner_data_type);
            int dataType = dataTypeSpinner.getSelectedItemPosition();

            Intent data = new Intent();
            data.putExtra(EXTRA_DATASOURCE_TYPE, dataSourceType);
            data.putExtra(EXTRA_DATA_TYPE, dataType);

            target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        }
    };
}
