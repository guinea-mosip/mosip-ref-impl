package io.mosip.authentication.demo.helper;

import io.mosip.authentication.demo.constant.AuthenticationUIConstant;
import org.springframework.stereotype.Component;

import java.util.ResourceBundle;

@Component
public class ApplicationMessageContext {

    private static ResourceBundle labelResourceBundles;

    private static ResourceBundle messageResourceBundles;

    public static ResourceBundle getLabelResourceBundles() {
        return labelResourceBundles;
    }

    public static ResourceBundle getMessageResourceBundles() {
        return messageResourceBundles;
    }

    public static  void loadResources() {
        labelResourceBundles = ResourceBundle.getBundle("labels", AuthenticationUIConstant.SUPPORTED_LANGUAGE);
        messageResourceBundles = ResourceBundle.getBundle("messages", AuthenticationUIConstant.SUPPORTED_LANGUAGE);

    }
}
