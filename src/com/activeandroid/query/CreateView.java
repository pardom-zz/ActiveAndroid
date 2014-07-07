package com.activeandroid.query;

import com.test.christophergastebois.activeandroid.ViewTable;

/**
 * Created by Christopher GATEBOIS on 07/07/2014.
 */
public class CreateView implements Sqlable {

    private Class<? extends ViewTable> mViewTable;

    public CreateView( Class<? extends ViewTable> viewTable ){
        mViewTable = viewTable;
    }

    public Select select( String... columns ){
        return new Select(mViewTable, columns);
    }

    @Override
    public String toSql() {
        return "";
    }
}
