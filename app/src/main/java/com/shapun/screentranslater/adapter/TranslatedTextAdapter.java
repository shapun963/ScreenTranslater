package com.shapun.screentranslater.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.shapun.screentranslater.R;
import java.util.List;
import java.util.Map;

public class TranslatedTextAdapter extends RecyclerView.Adapter<TranslatedTextAdapter.ViewHolder> {

    private List<Map<String, Object>> data;
    /** Provide a reference to the type of views that you are using (custom ViewHolder). */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // public final CheckBox checkbox;
        public final TextView view_original;
        public final TextView translated_text;

        public ViewHolder(View view) {
            super(view);

            // Define click listener for the ViewHolder's View
            // checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            view_original = view.findViewById(R.id.view_original);
            translated_text = view.findViewById(R.id.translated_text);
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public TranslatedTextAdapter(List<Map<String, Object>> data) {
        this.data = data;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.row_translated_text, null);
        view.setLayoutParams(
                new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        TextView translated_text = viewHolder.translated_text;
        TextView view_original = viewHolder.view_original;
        
        if ((boolean) data.get(position).get("show_translated")) {
                translated_text.setText(data.get(position).get("translated_text").toString());
				view_original.setText("View Original");
        } else {
            translated_text.setText(data.get(position).get("original_text").toString());
            view_original.setText("View Translated");
        }
        view_original.setOnClickListener(
                v -> {
                    if ((boolean) data.get(position).get("show_translated")) {
                        data.get(position).put("show_translated", false);
                    } else {
                        data.get(position).put("show_translated", true);
                    }
                    notifyItemChanged(position);
                });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }
}
