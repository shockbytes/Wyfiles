package mc.fhooe.at.wyfiles.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;

import mc.fhooe.at.wyfiles.R;

/**
 * @author Martin Macheiner
 *         Date: 26.12.2016.
 */

public class RematchDialogFragment extends DialogFragment {

    public interface OnRematchSelectedListener {

        void onRematchSelected();
    }

    private static final String ARG_TEXT_ID = "arg_text_id";
    private static final String ARG_ICON_ID = "arg_icon_id";

    public static RematchDialogFragment newInstance(@StringRes int textId, @DrawableRes int iconId) {

        RematchDialogFragment fragment = new RematchDialogFragment();
        Bundle args = new Bundle(2);
        args.putInt(ARG_TEXT_ID, textId);
        args.putInt(ARG_ICON_ID, iconId);
        fragment.setArguments(args);
        return fragment;
    }

    private int textId;
    private int iconId;

    private OnRematchSelectedListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            textId = getArguments().getInt(ARG_TEXT_ID);
            iconId = getArguments().getInt(ARG_ICON_ID);
        }
    }

    public void setOnRematchSelectedListener(OnRematchSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setIcon(iconId);
        builder.setMessage(textId);
        builder.setTitle(R.string.rematch);
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton(R.string.rematch, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (listener != null) {
                    listener.onRematchSelected();
                }
                dismiss();
            }
        });
        return builder.create();
    }
}
