package com.shapun.screentranslater.adapter;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.shapun.screentranslater.R;
import java.util.List;

public class SelectedTextsAdapter extends RecyclerView.Adapter<SelectedTextsAdapter.ViewHolder> {
    public interface OnCloseClickListener {
        public void onCloseClicked(int pos);
    }

    private final List<AccessibilityNodeInfo> data;
    private OnCloseClickListener mOnCloseClickListener;

    /** Provide a reference to the type of views that you are using (custom ViewHolder). */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textview;
        public final ImageView img_close;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textview = (TextView) view.findViewById(R.id.textview);
            img_close = (ImageView) view.findViewById(R.id.img_close);
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public SelectedTextsAdapter(List<AccessibilityNodeInfo> data) {
        this.data = data;
    }

    public void setOnCloseClickListener(OnCloseClickListener listener) {
        this.mOnCloseClickListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view =
                LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.row_selectable_text, viewGroup, false);
        view.setLayoutParams(
                new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        TextView textview = viewHolder.textview;
        ImageView img_close = viewHolder.img_close;
        textview.setText(data.get(position).getText());
        final int pos = position;
        img_close.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnCloseClickListener != null)
                            mOnCloseClickListener.onCloseClicked(pos);
                    }
                });
        if (textview.getText().length() > 100) {
            makeTextViewResizable(viewHolder.textview, 3, "View More", true);
        }
    }

    public static void makeTextViewResizable(
            final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        String text;
                        int lineEndIndex;
                        ViewTreeObserver obs = tv.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);

                        if (maxLine == 0) {
                            lineEndIndex = tv.getLayout().getLineEnd(0);
                            text =
                                    tv.getText()
                                                    .subSequence(
                                                            0,
                                                            lineEndIndex - expandText.length() + 1)
                                            + " "
                                            + expandText;
                        } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                            lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                            text =
                                    tv.getText()
                                                    .subSequence(
                                                            0,
                                                            lineEndIndex - expandText.length() + 1)
                                            + " "
                                            + expandText;
                        } else {
                            lineEndIndex =
                                    tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                            text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                        }
                        tv.setText(text);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());
                        tv.setText(
                                addClickablePartTextViewResizable(
                                        new SpannableString(tv.getText().toString()),
                                        tv,
                                        lineEndIndex,
                                        expandText,
                                        viewMore),
                                TextView.BufferType.SPANNABLE);
                    }
                });
    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(
            final Spanned strSpanned,
            final TextView tv,
            final int maxLine,
            final String spanableText,
            final boolean viewMore) {
        String str = strSpanned.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (str.contains(spanableText)) {
            ssb.setSpan(
                    new ClickableSpan() {

                        @Override
                        public void onClick(View widget) {
                            tv.setLayoutParams(tv.getLayoutParams());
                            tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                            tv.invalidate();
                            if (viewMore) {
                                makeTextViewResizable(tv, -1, "View Less", false);
                            } else {
                                makeTextViewResizable(tv, 3, "View More", true);
                            }
                        }
                    },
                    str.indexOf(spanableText),
                    str.indexOf(spanableText) + spanableText.length(),
                    0);
        }

        return ssb;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }
}
