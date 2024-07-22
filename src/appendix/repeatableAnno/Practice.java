package com.khc.practice.modernjava.appendix.repeatableAnno;


import java.util.Arrays;

@Author(name = "김")
@Author(name = "회")
@Author(name = "창")
public class Practice {

    public static void main(String[] args) {
         Author[] authors = Practice.class.getAnnotationsByType(Author.class);
//         System.out.println("name: "+author.name());
         Arrays.stream(authors).forEach(anno -> System.out.println(anno.name()));
    }
}
