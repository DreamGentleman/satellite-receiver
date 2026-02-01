package com.yxh.fangs.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.yxh.fangs.R;
import com.yxh.fangs.data.repository.WeatherRepository;

public class WeatherDialogFragment extends DialogFragment {

    private final String message;
    private final String weatherPhenomenon;

    public WeatherDialogFragment(String message, String weatherPhenomenon) {
        this.message = message;
        this.weatherPhenomenon = weatherPhenomenon;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            dialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(requireContext(), R.style.FullScreenDialog);
        dialog.setContentView(R.layout.dialog_weather2);

        dialog.setCanceledOnTouchOutside(false);

        // 中心缩放动画
        View card = dialog.findViewById(R.id.cardAlert);
        if (card != null) {
            card.setScaleX(0.3f);
            card.setScaleY(0.3f);
            card.setAlpha(0f);
            card.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(220).start();
        }

        Button btn = dialog.findViewById(R.id.btnConfirm);
        if (btn != null) {
            btn.setOnClickListener(v -> dismiss());
        }
        ImageView ivWeather = dialog.findViewById(R.id.iv_weather);
        TextView tvWeather = dialog.findViewById(R.id.tv_weather);
        TextView tvMessage = dialog.findViewById(R.id.tv_message);
        ivWeather.setImageResource(WeatherRepository.getWeatherIcon(weatherPhenomenon));
        tvWeather.setText(weatherPhenomenon);
        tvMessage.setText(message);
        return dialog;
    }

    @Override
    public void dismiss() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            Dialog d = getDialog();
            if (d != null) {
                View card = d.findViewById(R.id.cardAlert);
                if (card != null) {
                    card.animate()
                            .scaleX(0.3f)
                            .scaleY(0.3f)
                            .alpha(0f)
                            .setDuration(180)
                            .withEndAction(super::dismiss)
                            .start();
                    return;
                }
            }
            super.dismiss();
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (IllegalStateException ignore) {
            //  容错处理,不做操作
            ignore.printStackTrace();
        }
    }
}
