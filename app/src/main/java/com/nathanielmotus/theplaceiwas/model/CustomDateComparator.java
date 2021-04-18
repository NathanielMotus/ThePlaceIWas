package com.nathanielmotus.theplaceiwas.model;

import java.util.Comparator;

public class CustomDateComparator implements Comparator<CustomDate> {

    @Override
    public int compare(CustomDate o1, CustomDate o2) {
        if (o1.getYear()!=o2.getYear())
            return Integer.compare(o1.getYear(),o2.getYear());
        else if (o1.getMonth()!=o2.getMonth())
            return Integer.compare(o1.getMonth(),o2.getMonth());
        else return Integer.compare(o1.getDayOfMonth(),o2.getDayOfMonth());
    }
}
