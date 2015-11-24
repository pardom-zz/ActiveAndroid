package com.activeandroid.util;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Created by adifrancesco on 24/11/2015.
 */
public class Repository<T extends Model> {

    private Class<T> clazz;

    public Repository(Class<T> clazz)
    {
        this.clazz = clazz;
    }

    public void save(T m) {
        m.save();
    }

    public void save(List<T> m) {
        ActiveAndroid.beginTransaction();
        try {
            for(T item : m) {
                item.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        }
        finally {
            ActiveAndroid.endTransaction();
        }
    }

    public List<T> getAll() {
        return new Select()
                .from(clazz)
                .execute();
    }

    public T get(int id) {
        return T.load(clazz, id);
        /*
        return new Select()
                .from(clazz)
                .where("Id = ?", id)
                .execute();
        */
    }

    public T getById(int id) {
        return new Select()
                .from(clazz)
                .where("Id = ?", id)
                .executeSingle();
    }

    public From query() {
        return new Select()
                .from(clazz);
    }

    public void delete(long i) {
        T item = T.load(clazz, i);
        item.delete();
    }

    public void delete(T item) {
        item.delete();
    }
}
