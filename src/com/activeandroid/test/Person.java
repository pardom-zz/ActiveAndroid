package com.activeandroid.test;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Christopher GATEBOIS on 04/07/2014.
 */
@Table(name = "Persons")
public class Person extends Model {

    @Column(name = "Firstname")
    public String firstname;

    @Column(name = "Family")
    public Family family;

}
