package com.dust.exmusic.datasort;

import com.dust.exmusic.dataclasses.MainDataClass;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataSorter {

    public void sortByLastModification(List<MainDataClass> primaryList) {
        Collections.sort(primaryList, new Comparator<MainDataClass>() {
            @Override
            public int compare(MainDataClass dataClass, MainDataClass t1) {
                if (dataClass.getLastModification() > t1.getLastModification()) {
                    return -1;
                } else if (dataClass.getLastModification() < t1.getLastModification()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    public void sortByName(List<MainDataClass> primaryList) {
        Collections.sort(primaryList, new Comparator<MainDataClass>() {
            @Override
            public int compare(MainDataClass dataClass, MainDataClass t1) {
                return dataClass.getMusicName().compareTo(t1.getMusicName());
            }
        });
    }

    public void sortByYear(List<MainDataClass> primaryList) {
        Collections.sort(primaryList, new Comparator<MainDataClass>() {
            @Override
            public int compare(MainDataClass dataClass, MainDataClass t1) {
                int firstYear;
                int lastYear;

                try {
                    firstYear = Integer.parseInt(dataClass.getYear());
                } catch (Exception e) {
                    firstYear = -1;
                }

                try {
                    lastYear = Integer.parseInt(t1.getYear());
                } catch (Exception e) {
                    lastYear = -1;
                }
                if (firstYear > lastYear)
                    return -1;

                if (firstYear < lastYear)
                    return 1;

                return 0;
            }
        });
    }

}
