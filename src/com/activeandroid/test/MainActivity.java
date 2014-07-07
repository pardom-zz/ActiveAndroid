package com.activeandroid.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.query.CreateView;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import java.util.List;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( new Select().from(com.activeandroid.test.Person.class).execute().size() == 0) {
            com.activeandroid.test.Family f1 = new com.activeandroid.test.Family();
            f1.name = "Baratheon";
            f1.save();

            com.activeandroid.test.Person p1 = new com.activeandroid.test.Person();
            p1.firstname = "Robert";
            p1.family = f1;
            p1.save();

            com.activeandroid.test.Person p2 = new com.activeandroid.test.Person();
            p2.firstname = "Jeoffrey";
            p2.family = f1;
            p2.save();
        }

        From query = new CreateView(com.activeandroid.test.PersonFamily.class).select("p.firstname as firstname", "f.name as lastname").from( com.activeandroid.test.Person.class ).as("p");
        query.innerJoin(com.activeandroid.test.Family.class).as("f").on("p.Family=f.Id");
        List<com.activeandroid.test.PersonFamily> assoc = query.executeToView();

        if( assoc != null ) {
            for (com.activeandroid.test.PersonFamily pf : assoc) {
                Log.d("SQL RESUL", pf.firstname + " " + pf.lastname);
            }
        }
        else {
            Log.d("SQL RESULT", "RESULT IS NULL");
        }

    }
}
