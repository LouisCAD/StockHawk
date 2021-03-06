package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.Quote;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

/**
 * Created by sam_chordas on 10/6/15.
 * Credit to skyfishjy gist:
 * https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private Context mContext;
    private Typeface robotoLight;
    private boolean mShowPercent;
    private final Quote.Indexes mIndexes = new Quote.Indexes();
    private final ViewHolder.Host mHost;

    public <H extends Context & ViewHolder.Host> QuoteCursorAdapter(H host, Cursor cursor) {
        this(host, host, cursor);
    }

    public QuoteCursorAdapter(Context context, ViewHolder.Host host, Cursor cursor) {
        super(context, cursor);
        mContext = context;
        mHost = host;
        robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_quote, parent, false);
        ViewHolder vh = new ViewHolder(mHost, itemView);
        vh.symbol.setTypeface(robotoLight);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        viewHolder.bind(cursor, mIndexes, mShowPercent);
    }

    @Override
    public void onItemDismiss(int position) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
        mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
        notifyItemRemoved(position);
    }

    @Nullable
    @Override
    public Cursor swapCursor(@Nullable Cursor newCursor) {
        if (newCursor != null) mIndexes.set(newCursor);
        return super.swapCursor(newCursor);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void changeUnits() {
        mShowPercent = !mShowPercent;
        notifyItemRangeChanged(0, getItemCount());
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder, View.OnClickListener {
        public final TextView symbol;
        public final TextView bidPrice;
        public final TextView change;
        private final Quote mQuote = new Quote();
        private final Host mHost;

        public ViewHolder(Host host, View itemView) {
            super(itemView);
            mHost = host;
            symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
            bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
            change = (TextView) itemView.findViewById(R.id.change);
            itemView.setOnClickListener(this);
        }

        public void bind(Cursor c, Quote.Indexes i, boolean showPercent) {
            mQuote.set(c, i);
            symbol.setText(mQuote.symbol);
            bidPrice.setText(mQuote.bidPrice);
            change.setBackgroundResource(mQuote.isUp == 1 ?
                    R.drawable.percent_change_pill_green : R.drawable.percent_change_pill_red);
            change.setText(showPercent ? mQuote.percentChange : mQuote.change);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onClick(View v) {
            mHost.onClick(mQuote);
        }

        public interface Host {
            /**
             * @param quote use immediately or copy for future use as it's mutable.
             */
            void onClick(@NonNull Quote quote);
        }
    }
}
