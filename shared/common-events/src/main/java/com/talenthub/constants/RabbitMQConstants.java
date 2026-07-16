package com.talenthub.constants;

public class RabbitMQConstants {
    public  static final String EXCHANGE_NAME = "talenthub.events";
    public static final String NOTIFICATION_QUEUE = "notification.application-created";
    public static final String ROUTE_KEY_NAME = "application.created";

    public static final String DLX_NAME = "talenthub.events.dlx";
    public static final String DLX_QUEUE = "notification.application.dlx";


    // dead latter exchange


}
