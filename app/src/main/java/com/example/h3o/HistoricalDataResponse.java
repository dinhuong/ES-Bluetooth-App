package com.example.h3o;

import java.util.List;

/**
 * Created by Philipp Jahoda on 07/12/15.
 */
class HistoricalDataResponse {
    List<StockPrice> prices;
    boolean isPending;
    long firstTradeDate;
    String id;

    class StockPrice {
        long date;
        float open;
        float high;
        float low;
        float close;
        int volume;
        float adjclose;

    }
}
