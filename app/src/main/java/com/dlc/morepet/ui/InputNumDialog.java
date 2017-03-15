package com.dlc.morepet.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dlc.morepet.R;

/**
 * Created by Administrator on 2016/12/5.
 */

public class InputNumDialog extends Dialog {
    private Button btn_ok;
    private EditText et_num;

    public InputNumDialog(Context context, int themeResId) {
        super(context, themeResId);
        setCustomDialog();
    }

    public InputNumDialog(Context context) {
        this(context, R.style.loading_dialog);
    }

    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_num, null);
        btn_ok = (Button) mView.findViewById(R.id.btn_ok);
        et_num = (EditText) mView.findViewById(R.id.et_num);

        super.setContentView(mView);
    }

    /**
     * 添加设备键监听器
     *
     * @param listener
     */
    public void setOnConfirmListener(View.OnClickListener listener) {
        btn_ok.setOnClickListener(listener);
    }

    public String getNum() {
        if (et_num != null) {
            return et_num.getText().toString().trim();
        }
        return null;
    }

    public void clear() {
        if (et_num != null) {
            et_num.setText("");
        }
    }
}
