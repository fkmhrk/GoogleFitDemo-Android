package jp.fkmsoft.demo.fitdemo;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

/**
 * Dialog for start session
 */
public class StartSessionDialog extends DialogFragment {
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_IDENTIFIER = "identifier";
    public static final String EXTRA_DESCRIPTION = "deescription";

    public static StartSessionDialog newInstance(Fragment target, int requestCode) {
        StartSessionDialog fragment = new StartSessionDialog();
        fragment.setTargetFragment(target, requestCode);

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) { return null; }

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_start_session);

        dialog.findViewById(R.id.button_cancel).setOnClickListener(mClickListener);
        dialog.findViewById(R.id.button_submit).setOnClickListener(mClickListener);

        return dialog;
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.button_cancel:
                dismiss();
                break;
            case R.id.button_submit:
                submit();
                break;
            }
        }
    };

    private void submit() {
        // get values from EditText
        Dialog dialog = getDialog();
        if (dialog == null) { return; }
        EditText nameEdit = (EditText) dialog.findViewById(R.id.edit_session_name);
        EditText identifierEdit = (EditText) dialog.findViewById(R.id.edit_session_identifier);
        EditText descriptionEdit = (EditText) dialog.findViewById(R.id.edit_session_description);

        String name = nameEdit.getText().toString();
        String identifier = identifierEdit.getText().toString();
        String description = descriptionEdit.getText().toString();

        Fragment target = getTargetFragment();
        if (target == null) {
            dismiss();
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_NAME, name);
        data.putExtra(EXTRA_IDENTIFIER, identifier);
        data.putExtra(EXTRA_DESCRIPTION, description);

        target.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);

        dismiss();
    }
}

