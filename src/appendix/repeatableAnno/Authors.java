package com.khc.practice.modernjava.appendix.repeatableAnno;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public @interface Authors{
    Author[] value();
}
