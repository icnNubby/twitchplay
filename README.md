# Twitchplay(PlayStream app)
Android twitch client.

This project was made just for learning purposes.
You may use any parts of it if you want to.

Current techs used: alot of **RxJava**, **Retrofit** + **OkHttp**, **Exoplayer**, **Room**, **Picasso**, **LeakCanary**, di by **Dagger2**.
**MVP** pattern for presentation layer, **ViewModel** for recreation persistence.
**Clean** approach for whole application.
 
Also huge props to author of that parser
https://github.com/carlanton/m3u8-parser.

In order to get your favourite streams from your twitch account you'll have to login.

If you want to build that project in your environment, you have to create class SensitiveStorage in the ru.nubby.playstream package.
That class has to contain several methods:

    public static String getClientApiKey()
    public static String getClientSecret()
    public static String getDefaultChatBotName()
    public static String getDefaultChatBotToken()

    //this should return "client-id" string
    public static String getHeaderClientId()

    
  You can obtain first four of those at https://dev.twitch.tv/docs/api/. 
  
