package com.activeandroid.test;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Christopher GATEBOIS on 04/07/2014.
 */
@Table(name = "Families")
public class Family extends Model {

    @Column(name = "Name")
    public String name;

}
