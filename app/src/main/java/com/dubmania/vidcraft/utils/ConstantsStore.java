package com.dubmania.vidcraft.utils;

/**
 * Created by rat on 8/9/2015.
 */
public class ConstantsStore {

    public static final String BASE_URL = "http://kehbackend.appspot.com/v1/";

    public final static String VIDEO_BOARD_FRAGMENT_BUTTON_LEARNED = "com.monkeybusiness.vidscraft.videoboardfragment.button.learned";

    public final static String SHARED_KEY_USER_NAME = "com.dubmania.vidcraft.username";
    public final static String SHARED_KEY_USER_EMAIL = "com.dubmania.vidcraft.useremail";
    public final static String SHARED_KEY_USER_LOGIN = "com.dubmania.vidcraft.login";

    public final static String INTENT_FILE_PATH = "com.dubmania.vidcraft.share.filepath";
    public final static String VIDEO_ID = "com.dubmania.vidcraft.video.id";

    public final static String INTENT_BOARD_NAME = "com.dubmania.vidcraft.board.name";
    public final static String INTENT_BOARD_ID = "com.dubmania.vidcraft.board.id";
    public final static String INTENT_BOARD_USER_NAME = "com.dubmania.vidcraft.board.username";
    public final static String INTENT_BOARD_ICON = "com.dubmania.vidcraft.board.icon";
    public final static String INTENT_BOARD_USER = "com.dubmania.vidcraft.board.user";
    public final static String INTENT_BOARD_DELETED = "com.dubmania.vidcraft.board.deleted";

    public final static String INTENT_VIDEO_ID = "com.dubmania.vidcraft.video.id";
    public final static String INTENT_VIDEO_TITLE = "com.dubmania.vidcraft.video.title";
    public final static String INTENT_ADD_VIDEO_ACTION = "com.dubmania.vidcraft.addvideo.action";
    public final static int INTENT_ADD_VIDEO_IMPORT = 1;
    public final static int INTENT_ADD_VIDEO_RECORD = 0;

    public final static String INTENT_REPORT_ACTION = "com.dubmania.vidcraft.report.action";
    public final static int INTENT_REPORT = 0;
    public final static int INTENT_IMPROVE = 1;

    public final static String INTENT_INSTALL_LANGUAGE = "com.dubmania.vidcraft.addlanguage.language";
    public final static String INTENT_INSTALL_LANGUAGE_ID = "com.dubmania.vidcraft.addlanguage.id";

    private final static String ACCOUNT = "account/";
    private final static String VIDEO = "video/";
    private final static String BOARD = "board/";
    private final static String FEEDBACK = "feedback/";
    private final static String USER = "user/";
    private final static String CONFIG = "config/";

    public final static String URL_SEARCH_VIDEOS = "search";
    public final static String URL_TRENDING = "trending";
    public final static String URL_DISCOVER = "discover";
    public final static String URL_LANGUAGES = "language";

    public final static String URL_LOGIN =              ACCOUNT + "login";
    public final static String URL_USER_LOGOUT =        ACCOUNT + "logout";
    public final static String URL_REGISTER =           ACCOUNT + "signup";
    public final static String URL_RESET_PASSWORD =     ACCOUNT + "forgot";
    public final static String URL_VERIFY_USER =        ACCOUNT + "verify/user";
    public final static String URL_VERIFY_EMAIL =       ACCOUNT + "verify/email";
    public final static String URL_USER_LANGUAGES =     USER + CONFIG + "languages";

    public final static String URL_GET_LANGUAGES = "language";


    public final static String URL_VIDEO =          VIDEO + "video";
    public final static String URL_FAVORITE =       VIDEO + "favorite";
    public final static String URL_TAGS =           VIDEO + "tags";

    public final static String URL_BOARD =          BOARD + "board";
    public final static String URL_BOARD_VIDEO = BOARD + "video";

    public final static String URL_REPORT =             FEEDBACK + "report";
    public final static String URL_IMPROVE =            FEEDBACK + "improve";


    // HTTP params
    public final static String PARAM_USER = "user";
    public final static String PARAM_USER_ID = "id";
    public final static String PARAM_USER_NAME = "user_name";
    public final static String PARAM_USER_EMAIL = "user_email";
    public final static String PARAM_PASSWORD = "password";
    public final static String PARAM_DOB = "dob";

    public final static String PARAM_BOARD = "board";
    public final static String PARAM_BOARD_NAME = "name";
    public final static String PARAM_BOARD_LIST = "board_list";
    public final static String PARAM_BOARD_ID = "id";
    public final static String PARAM_BOARD_ICON = "icon";
    public final static String PARAM_BOARD_USER = "user_board";


    public final static String PARAM_VIDEO = "video";
    public final static String PARAM_VIDEO_ID = "id";
    public final static String PARAM_VIDEO_TITLE = "title";
    public final static String PARAM_VIDEO_FAV = "favorite";
    public final static String PARAM_VIDEO_THUMBNAIL = "thumbnail";
    public final static String PARAM_VIDEO_COUNTRY = "country";
    public final static String PARAM_VIDEO_LANGUAGE = "language";
    public final static String PARAM_VIDEO_LIST = "video_list";
    public final static String PARAM_VIDEO_UPLOAD_URL = "upload_url";

    public final static String PARAM_TAGS = "tags";
    public final static String PARAM_TAG_ID = "id";
    public final static String PARAM_TAG_NAME = "tag";

    public final static String PARAM_START = "start";
    public final static String PARAM_END = "end";
    public final static String PARAM_REGION = "region";
    public final static String PARAM_DISCOVER_VERSION = "version";
    public final static String PARAM_RESULT = "result";
    public final static String PARAM_TRENDING_CURSOR = "trending_cursor";
    public final static String PARAM_NEXT_CURSOR = "next_cursor";

    public final static String PARAM_LANGUAGE = "languages";
    public final static String PARAM_LANGUAGE_ID = "id";
    public final static String PARAM_LANGUAGE_TEXT = "language";
    public final static String PARAM_LANGUAGE_LIST = "list"; // change this later
    public final static String PARAM_COUNTRY_ID = "id";
    public final static String PARAM_COUNTRY_TEXT = "country";
    public final static String PARAM_COUNTRY_LIST = "country_list";

    public final static String PARAM_REPORT_REASON_CODE = "reason";
    public final static String PARAM_REPORT_NAME = "name";
    public final static String PARAM_REPORT_EMAIL = "email";
    public final static String PARAM_REPORT_DESC = "description";

    public final static String PARAM_IMPROVE_TITLE = "title";
    public final static String PARAM_IMPROVE_SAID = "said";
    public final static String PARAM_IMPROVE_NOISE = "title";
    public final static String PARAM_IMPROVE_CROP = "crop";
    public final static String PARAM_IMPROVE_LANGUAGE = "language";
    public final static String PARAM_IMPROVE_LANGUAGE_SELECTED = "selected";

    public final static String PARAM_ITEM_LIST = "item_list";
    public final static String PARAM_ITEM_TYPE = "type";
    public final static int PARAM_ITEM_TYPE_VIDEO = 0;
    public final static int PARAM_ITEM_TYPE_BOARD = 1;

    public final static int PARAM_FAILURE = 0;
    public final static int PARAM_SUCCESS = 1;
    public final static int PARAM_REGISTER_USER_EXIST = 1;
    public final static int PARAM_REGISTER_EMAIL_EXIST = 2;

    public final static int SHARE_APP_ID_MESSENGER = 0;
    public final static int SHARE_APP_ID_WHATSAPP = 1;
    public final static int SHARE_APP_ID_SAVE_GALLERY = 2;

    public final static double IMAGE_SCALE_RATIO_X = 265d/640d;
    public final static double IMAGE_SCALE_RATIO_Y = 32d/480d;


}

