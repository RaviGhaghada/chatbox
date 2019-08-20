package com.example.client;


import java.io.Serializable;

public enum MessageType implements Serializable {
    CHAT,
    TYPING;

    private static final long serialVersionUID = 1L;
}
