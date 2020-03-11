package com.throwing.screen;

import com.throwing.screen.connector.ThrowingSendConnector;

public class JavaTest {

    public static void main(String[] args){
        ThrowingSendConnector sendConnector = new ThrowingSendConnector();
        sendConnector.search();
    }
}
