package com.core.match.monitors;

import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 12/14/16.
 */
public abstract class PagedResult<T> {
    private int page;
    private int numPages;
    private int firstResult;
    private int lastResult;
    private int totalRecordCount;
    private List<T> records = new FastList<>();

    public void setPage(int page, int pageSize, boolean newestFirst, int totalRecordCount) {
        this.page = page;
        this.totalRecordCount = totalRecordCount;
        this.numPages = (int)Math.ceil(1.0 * totalRecordCount / pageSize);

        if (newestFirst) {
            firstResult = Math.max(0, totalRecordCount - page * pageSize);
            lastResult = totalRecordCount - (page - 1) * pageSize;
        }
        else {
            firstResult = (page - 1) * pageSize;
            lastResult = Math.min(totalRecordCount, page * pageSize);
        }
    }

    public void setPage(int page, int pageSize, boolean newestFirst) {
        setPage(page, pageSize, newestFirst, records.size());
    }

    public void setPage(int page, int pageSize, boolean newestFirst, List<T> records) {
        this.records = records;
        setPage(page, pageSize, newestFirst, records.size());
    }

    public List<T> getRecords() {
        return records;
    }

    public int getTotalRecordCount() {
        return totalRecordCount;
    }

    public int getPage() {
        return page;
    }

    public int getNumPages() {
        return numPages;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public int getLastResult() {
        return lastResult;
    }
}
