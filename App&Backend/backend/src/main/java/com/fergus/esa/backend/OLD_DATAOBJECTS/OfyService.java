package com.fergus.esa.backend.OLD_DATAOBJECTS;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * This class was adapted from the sample code found at:
 * https://github.com/GoogleCloudPlatform/MobileShoppingAssistant-sample/blob/master/MobileAssistantAndroidAppEngine/backend/src/main/java/com/google/sample/mobileassistantbackend/OfyService.java
 */
public final class OfyService {

    private OfyService() {
    }


    static {
        factory().register(ESATweet.class);
        factory().register(ESANews.class);
        factory().register(ESAEvent.class);
    }

    /**
     * Returns the Objectify service wrapper.
     */
    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }


    /**
     * Returns the Objectify factory service.
     */
    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
