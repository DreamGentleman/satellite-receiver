package com.yxh.fangs.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.yxh.fangs.R;

import java.util.ArrayList;
import java.util.List;

public class LayoutDialogFragment extends DialogFragment {

    private OnConfirmListener listener;

    public LayoutDialogFragment setOnConfirmListener(OnConfirmListener listener) {
        this.listener = listener;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_layout_choice, null);

        CheckBox cbFishery = view.findViewById(R.id.cb_fishery);
        CheckBox cbCoast = view.findViewById(R.id.cb_coast);
        CheckBox cbLightning = view.findViewById(R.id.cb_lightning);
        CheckBox cbTyphoon = view.findViewById(R.id.cb_typhoon);
        CheckBox cbHeavyRain = view.findViewById(R.id.cb_heavy_rain);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnOk = view.findViewById(R.id.btnOk);

        btnCancel.setOnClickListener(v -> dismiss());

        btnOk.setOnClickListener(v -> {
            List<String> result = new ArrayList<>();

            if (cbFishery.isChecked()) result.add("渔场");
            if (cbCoast.isChecked()) result.add("沿岸");
            if (cbLightning.isChecked()) result.add("雷电");
            if (cbTyphoon.isChecked()) result.add("台风");
            if (cbHeavyRain.isChecked()) result.add("暴雨天气");

            if (listener != null) {
                listener.onConfirm(result);
            }
            dismiss();
        });

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (IllegalStateException ignore) {
            //  容错处理,不做操作
        }
    }
    public void dismissDialog() {
        if ( getActivity() != null && !getActivity().isFinishing()) {
            super.dismissAllowingStateLoss();
        }
    }
    public interface OnConfirmListener {
        void onConfirm(List<String> selectedList);
    }
}
