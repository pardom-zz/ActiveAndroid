package com.activeandroid.test;

import com.activeandroid.ViewTable;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.View;

/**
 * Created by Christopher GATEBOIS on 07/07/2014.
 */
@View(name="PersonFamily")
public class PersonFamily extends ViewTable {

    @Column(name="firstname")
    public String firstname;

    @Column(name="lastname")
    public String lastname;

}
