package com.batiot.helper;

/**
 * Created by bat on 07/10/16.
 */
public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.batiot.helper.foregroundservice.action.main";
        public static String PREV_ACTION = "com.batiot.helper.foregroundservice.action.prev";
        public static String PLAY_ACTION = "com.batiot.helper.foregroundservice.action.play";
        public static String STARTFOREGROUND_ACTION = "com.batiot.helper.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.batiot.helper.action.stopforeground";


        public static String SHELL_ACTION = "com.batiot.helper.commandservice.action.shell";
        public static String STARTCOMMAND_ACTION = "com.batiot.helper.action.startcommand";
        public static String STOPCOMMAND_ACTION = "com.batiot.helper.action.stopcommand";

    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}

