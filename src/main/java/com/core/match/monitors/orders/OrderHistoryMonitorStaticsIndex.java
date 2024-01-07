package com.core.match.monitors.orders;

import com.core.match.msgs.MatchConstants;
import com.core.services.StaticsList;
import com.core.services.StaticsService;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.List;

/**
 * Created by jgreco on 12/7/15.
 */
public class OrderHistoryMonitorStaticsIndex<T extends StaticsList.StaticsObject> {
    private final List<IntArrayList> index = new FastList<>();
    private final StaticsService<T> service;
    private final String name;

    OrderHistoryMonitorStaticsIndex(StaticsService<T> service, String name) {
        this.service = service;
        this.name = name;
    }

    void create() {
        index.add(new IntArrayList());
    }

    void add(short serviceID, int orderID) {
        IntArrayList index = this.index.get(serviceID - MatchConstants.STATICS_START_INDEX);
        index.add(orderID);
    }

    void search(String value, IntArrayList results, IntArrayList oldResults) {
        T item = service.get(value);

        if (item == null) {
            results.clear();
            return;
        }

        IntArrayList indexResults = index.get(item.getID() - MatchConstants.STATICS_START_INDEX);

        if (oldResults == null) {
            for (int i=0; i<indexResults.size(); i++) {
                results.add(indexResults.get(i));
            }
        }
        else {
            // second through N result
            compareSortedArrays(results, oldResults, indexResults);
        }
    }

    public String getName() {
        return name;
    }

    private static void compareSortedArrays(IntArrayList results, IntArrayList array1, IntArrayList array2) {
        results.clear();

        for (int i=0, j=0; i<array1.size() && j<array2.size(); i++) {
            int array1Val = array1.get(i);

            while (j < array2.size() && array1Val > array2.get(j)) {
                // skip through
                j++;
            }

            if (j < array2.size() && array1Val == array2.get(j)) {
                results.add(array1Val);
            }
        }
    }
}