package com.khc.practice.modernjava.appendix.repeatableAnno;

import java.lang.annotation.*;

@Repeatable(Authors.class)
@Retention(RetentionPolicy.SOURCE)
public @interface Author {
    String name();
}



