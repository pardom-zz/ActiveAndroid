package com.activeandroid.util;

import com.activeandroid.Model;

/**
 * Created by adifrancesco on 24/11/2015.
 */
public class Database {

    private static Database _instance;

    private Database() {}

    public static Database Instance() {
        if(_instance==null)
            _instance = new Database();

        return _instance;
    }

    public <T extends Model> Repository<T> Repository(Class<T> type) {
        return new Repository<T>(type);
    }
}
