package com.daodao.map.helper;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.daodao.map.R;

import java.util.ArrayList;

/**
 * Created by lzd on 2016/11/9.
 */

public class PopViewHelper {

    public interface OnHistoryAddressSelectListener {
        void onHistoryAddressSelect(String address);
        void onHistoryAddressDelete(String address);
    }
    public static void showHistoryAddressPopWindow(Context context, View anchorView, final ArrayList<String> addresses, final OnHistoryAddressSelectListener listener) {
        View contentView = View.inflate(context, R.layout.pop_search_history, null);
        ListView lstView = (ListView) contentView.findViewById(R.id.pop_search_history);
        final PopupWindow popupWindow = new PopupWindow(contentView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();
        ColorDrawable dw = new ColorDrawable(0000000000);
        popupWindow.setBackgroundDrawable(dw);

        final HistoryAddressAdapter adapter = new HistoryAddressAdapter(context, addresses);
        lstView.setAdapter(adapter);

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    listener.onHistoryAddressSelect(addresses.get(position));
                }
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });

        lstView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    String address = addresses.get(position);
                    listener.onHistoryAddressDelete(address);
                    addresses.remove(address);
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            popupWindow.showAsDropDown(anchorView, 40, 0);
        }
    }

    private static class HistoryAddressAdapter extends BaseAdapter {
        ArrayList<String> addresses;
        Context context;
        public HistoryAddressAdapter(Context context, ArrayList<String> addresses) {
            this.context = context;
            this.addresses = addresses;
        }

        @Override
        public int getCount() {
            if (addresses == null) {
                return 0;
            }
            return addresses.size();
        }

        @Override
        public Object getItem(int position) {
            return addresses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = (View) View.inflate(context, R.layout.item_history_address, null);
            }
            TextView tvAddress = (TextView) convertView.findViewById(R.id.adapter_history_address);
            tvAddress.setText(addresses.get(position));
            return convertView;
        }
    }
}
