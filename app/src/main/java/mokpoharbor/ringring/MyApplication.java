package mokpoharbor.ringring;


import android.app.Application;
import android.content.Context;

public class MyApplication extends Application //매니패스트에 .name으로 등록//
{
    static Context context;

    public static Context getContext() {
        return context; //자원을 반환.//
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this; //현재 어플리케이션의 자원을 얻어온다.//
    }
}