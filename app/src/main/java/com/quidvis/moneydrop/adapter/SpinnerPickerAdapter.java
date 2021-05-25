package com.quidvis.moneydrop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quidvis.moneydrop.R;

import java.util.ArrayList;

public class SpinnerPickerAdapter extends RecyclerView.Adapter<SpinnerPickerAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<String> arrayList;
    private int selectedItem = -1;

    public SpinnerPickerAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public SpinnerPickerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_picker_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpinnerPickerAdapter.ViewHolder holder, int position) {
        holder.item.setText(arrayList.get(position));
        if (position == selectedItem) {
            holder.item.setTextSize(20);
            holder.item.setTextColor(context.getResources().getColor(R.color.colorBlackLight));
            holder.container.setBackgroundColor(context.getResources().getColor(R.color.colorWhiteDark, null));
            holder.item.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_mark, 0);
            selectedItem = position;
        } else {
            holder.item.setTextSize(18);
            holder.item.setTextColor(context.getResources().getColor(R.color.colorWhiteDarkExtra, null));
            holder.container.setBackgroundColor(context.getResources().getColor(R.color.colorWhite, null));
            holder.item.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public String getSelectedItem() {
        return arrayList.get(selectedItem);
    }

    public int getSelectedItemPosition() {
        return selectedItem;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView item;
        LinearLayout container;

        public ViewHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.item);
            container = itemView.findViewById(R.id.container);
        }
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
        notifyDataSetChanged();
    }
}
