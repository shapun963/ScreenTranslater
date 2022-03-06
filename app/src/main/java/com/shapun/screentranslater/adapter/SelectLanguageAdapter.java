package com.shapun.screentranslater.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.mannan.translateapi.Language;
import com.shapun.screentranslater.preferences.Preferences;
import com.shapun.screentranslater.R;
import java.util.List;

public class SelectLanguageAdapter extends RecyclerView.Adapter<SelectLanguageAdapter.ViewHolder> {

    private final List<Language> data;
    /** Provide a reference to the type of views that you are using (custom ViewHolder). */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // public final CheckBox checkbox;
        public final TextView textview;

        public ViewHolder(View view) {
            super(view);
            textview = (TextView) view.findViewById(R.id.textview);
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public SelectLanguageAdapter(List<Language> data) {
        this.data = data;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.row_select_language, null);
        view.setLayoutParams(
                new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.textview.setText(data.get(position).name);
        viewHolder.itemView.setOnClickListener(
                v -> {
                    Context context = viewHolder.itemView.getContext();
                    Preferences.setLanguage(context,data.get(position));
					//this is temporary need to create interface and close it 
					((Activity)context).setResult(Activity.RESULT_OK);
					((Activity)context).finish();
                });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }
}
