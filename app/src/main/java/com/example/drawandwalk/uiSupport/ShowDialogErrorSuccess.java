package com.example.drawandwalk.uiSupport;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.drawandwalk.R;

public class ShowDialogErrorSuccess {
    public static void ShowDialog(Context context, String title, String subTitle, boolean success) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.my_dialog);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View mV = inflater.inflate(R.layout.dailog_show_error, null);

            Button btnok = mV.findViewById(R.id.okbtn);
            TextView titlet = mV.findViewById(R.id.dialog_title);
            TextView desct = mV.findViewById(R.id.dailog_desc);
            ImageView imgicon = mV.findViewById(R.id.imgicon);
            titlet.setText(title);
            desct.setText(subTitle);
            builder.setView(mV);
            if (success) {
                imgicon.setImageResource(R.drawable.greentick);
            }

            final AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            if (!dialog.isShowing()) {
                dialog.show();
                dialog.getWindow().setLayout((int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            btnok.setOnClickListener(v -> dialog.dismiss()


            );
        } catch (Exception ignore) {
        }
    }
}
