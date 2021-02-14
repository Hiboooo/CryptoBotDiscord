package fr.thibault.bot;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiClientFactory;

class Tool {
    public static String[] pwd = null;
    public static DiscordClient discordClient = null;
    public static GatewayDiscordClient gateway = null;
    public static DataBase dataBase = null;
    public static List<Ignorant> li = null;
    public static List<Long> ll = null;
    public static void init() {
        try {
            /**/Tool.log("Init(1/7)RecoveryPwd");
            BufferedReader br =
                new BufferedReader(new FileReader(new File(
                    new File(App.class.getProtectionDomain()
                                      .getCodeSource()
                                      .getLocation()
                                      .getPath()).getParent()+"/pwd.pwd"
                )));
            pwd = new String[6];
            for (int t=0; t<pwd.length; t++) {
                String tmp = br.readLine();
                pwd[t] = tmp.substring(tmp.indexOf(',')+1,tmp.length()-1);
            }
        } catch(Exception e) {log("Init(1/6)ERR"+e);}
        /**/Tool.log("Init(2/7)CreateDiscordClient");
        discordClient = DiscordClient.create(pwd[0]);
        gateway = discordClient.login().block();
        /**/Tool.log("Init(3/7)CreateListSymbol");
        Symbol.initListSymbol(Tool.getBinanceClient().getAllPrices());
        /**/Tool.log("Init(4/7)CreateDataBase");
        dataBase = new DataBase("jdbc:mariadb://localhost:3306/bddCB?user="+pwd[3]+"&password="+pwd[4]);
        /**/Tool.log("Init(5/7)CreatListUser");
        li = Collections.synchronizedList(Tool.dataBase.getAllDataBase());
        /**/Tool.log("Init(6/7)CreatBinanceTickersPrice");
        BinanceTickersPrice btp = new BinanceTickersPrice(30,li);
        /**/Tool.log("Init(7/7)StartBinanceTickersPrice");
        btp.start();
        ll = new ArrayList<Long>();
    }
    public static void log(String s) {System.out.println(s);}
    public static BinanceApiRestClient getBinanceClient() {
        Tool.log("InstanceBinanceApiCreat");
        return BinanceApiClientFactory.newInstance(pwd[1],pwd[2]).newRestClient();
    }
}

class MyEmoji {
    public static String getFaceStar() {
        try { return new String(new byte[]{-16,-97,-92,-87},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "";
    }
    public static String getCoin() {
        try { return new String(new byte[]{-16,-97,-86,-103},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "";
    }
    public static String getFusee() {
        try { return new String(new byte[]{-16,-97,-102,-128},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "";
    }
    public static String getWizard() {
        try { return new String(new byte[]{-16,-97,-89,-103,-30, -128, -115, -30, -103, -126, -17, -72, -113},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "";
    }
    public static String getHorloge() {
        try { return new String(new byte[]{-30, -113, -80},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "";
    }
    public static String getAlarm() {
        try { return new String(new byte[]{-16, -97, -108, -108},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "";
    }
    public static String getHand() {
        try { return new String(new byte[]{-16, -97, -111, -119},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "";
    }
    public static String getPoint() {
        try { return new String(new byte[]{-16, -97, -108, -71},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "";
    }
    public static String getFlecheMonte() {
        try { return new String(new byte[]{/*-30*/ -16, /*-113*/-97, /*-85*/-108, -68},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "";
    }
    public static String getFlecheDesc() {
        try { return new String(new byte[]{/*-30*/ -16, /*-113*/-97, /*-84*/-108, -67},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "";
    }
    public static String getNumber(char n) {
        if (n<'0'||n>'9') return "X";
        try { return new String(new byte[]{(byte)(48+n-'0'), -17, -72, -113, -30, -125, -93, 0},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "N";
    }
    public static String getNumber(double n) {
        String ret = "";
        for (String s=""+n; !s.isEmpty(); s=s.substring(1))
            if (s.charAt(0)=='.') ret+=getPoint();
            else ret+=getNumber(s.charAt(0));
        return ret;
    }
    public static String getNumber(int n) {
        String ret = "";
        for (String s=""+n; !s.isEmpty(); s=s.substring(1))
            ret+=getNumber(s.charAt(0));
        return ret;
    }
    public static String getNumber(String s) {
        try {
            return getNumber(Double.parseDouble(s));
        } catch (Exception e) { Tool.log(""+e); }
        return "Impossible Conversion";
    }
    public static String getCharacter(char a) {
        if (a<'a'||a>'z') return "X";
        try { return new String(new byte[]{-16, -97, -121, (byte)(-90+a-'a'), 0},"UTF-8"); }
        catch (Exception e) { Tool.log(""+e); }
        return "N";
    }
    public static String getCharacters(String s) {
        String ret = "";
        for (s=s.toLowerCase(); !s.isEmpty(); s=s.substring(1))
            ret+=getCharacter(s.charAt(0));
        return ret;
    }
    public static String convert(String s) {
        if (s.length()>2)
            try {
                if (s.charAt(0)=='C')
                    return getHand()+" "+getCharacters(s.substring(2));
                else if (s.charAt(0)=='N')
                    return getHand()+" "+getNumber(Double.parseDouble(s.substring(2)));
            } catch (Exception e) { Tool.log(""+e); }
        return "Impossible Conversion";
    }
}
