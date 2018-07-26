package com.muvit.passenger.GeoLocation.logger;

public interface LogNode {

    public void println(int priority, String tag, String msg, Throwable tr);

}
