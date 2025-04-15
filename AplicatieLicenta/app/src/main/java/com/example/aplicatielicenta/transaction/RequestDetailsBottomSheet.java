package com.example.aplicatielicenta.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.aplicatielicenta.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RequestDetailsBottomSheet extends BottomSheetDialogFragment {

    public interface OnRequestSubmitListener {
        void onRequestSubmitted(String pickupTimes, String message);
    }

    private OnRequestSubmitListener listener;

    public void setOnRequestSubmitListener(OnRequestSubmitListener listener) {
        this.listener = listener;
    }

    private EditText etPickupTimes, etMessage;
    private Button btnSend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_request_details, container, false);

        etPickupTimes = view.findViewById(R.id.et_pickup_times);
        etMessage = view.findViewById(R.id.et_message);
        btnSend = view.findViewById(R.id.btn_send_request);

        btnSend.setOnClickListener(v -> {
            String pickup = etPickupTimes.getText().toString().trim();
            String msg = etMessage.getText().toString().trim();

            if (listener != null) {
                listener.onRequestSubmitted(pickup, msg);
            }
            dismiss();
        });

        return view;
    }
}
