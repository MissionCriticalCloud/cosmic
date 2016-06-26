package com.cloud.utils.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "test")
public class DbTestVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "fld_int")
    int fieldInt;

    @Column(name = "fld_long")
    Long fieldLong;

    @Column(name = "fld_string")
    String fieldString;

    public DbTestVO() {
    }

    public String getFieldString() {
        return fieldString;
    }

    public int getFieldInt() {
        return fieldInt;
    }

    public long getFieldLong() {
        return fieldLong;
    }
}
