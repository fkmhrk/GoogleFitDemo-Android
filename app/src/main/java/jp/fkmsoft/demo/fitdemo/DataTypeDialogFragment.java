package jp.fkmsoft.demo.fitdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;

/**
 * Dialog for selecting DataType
 */
public class DataTypeDialogFragment extends DialogFragment {
    public static final String EXTRA_DATA_TYPE = "dataType";

    public static DataTypeDialogFragment newInstance(Fragment target, int requestCode) {
        DataTypeDialogFragment fragment = new DataTypeDialogFragment();
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
        builder.setView(R.layout.dialog_data_type);
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

            Spinner dataTypeSpinner = (Spinner) dialog.findViewById(R.id.spinner_data_type);
            int dataType = dataTypeSpinner.getSelectedItemPosition();

            Intent data = new Intent();
            data.putExtra(EXTRA_DATA_TYPE, dataType);

            target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        }
    };
}
