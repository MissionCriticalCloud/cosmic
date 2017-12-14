package com.cloud.region;

import java.util.ArrayList;
import java.util.List;

public interface Region {

    int getId();

    String getName();

    void setName(String name);

    String getEndPoint();
}
