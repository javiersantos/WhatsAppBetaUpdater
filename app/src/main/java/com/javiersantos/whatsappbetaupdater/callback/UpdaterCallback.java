package com.javiersantos.whatsappbetaupdater.callback;

import com.javiersantos.whatsappbetaupdater.enums.UpdaterError;
import com.javiersantos.whatsappbetaupdater.models.Update;

public interface UpdaterCallback {

    void onFinished(Update update, boolean isUpdateAvailable);
    void onLoading();
    void onError(UpdaterError error);

}
