package com.spring.demo.model;

import java.io.Serializable;

public class User  implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;

    public User() {
        // constructor vacío obligatorio
    }

    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }

}

