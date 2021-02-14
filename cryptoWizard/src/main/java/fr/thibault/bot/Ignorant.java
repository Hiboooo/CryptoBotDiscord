package fr.thibault.bot;

import java.util.List;
import java.util.ArrayList;
import discord4j.core.object.entity.User;
import discord4j.common.util.Snowflake;
import com.binance.api.client.domain.market.TickerPrice;

class Ignorant {
    private User u;
    public List<Alarm> la;
    public Ignorant(User u) {
        this.u = u;
        la = new ArrayList<Alarm>();
    }
    public Ignorant(long id) {
        this(new User(
            Tool.gateway,
            Tool.discordClient.getUserById(Snowflake.of(id)).getData().block()
        ));
    }
    public long getId() {return u.getId().asLong();}
    public String getName() {return u.getUsername();}
    public boolean alarmExist(Alarm a) {
        for (Alarm ma: la)
            if (a.equals(ma))
                return true;
        return false;
    }
    public boolean addAlarm(Alarm a) {
        if (alarmExist(a)) return false;
        la.add(a);
        return true;
    }
    public Alarm delAlarm(int i) {
        if (i<0||i>=la.size()) return null;
        Tool.dataBase.deleteAlert(getId(),la.get(i));
        return la.remove(i);
    }
    public String toString() {
        String s = ""+u.getUsername()+"("+getId()+")";
        for (Alarm a: la) s+="\n\t"+a;
        return s;
    }
    public void send(String s) {
        u.getPrivateChannel().block().createMessage(s).block();
    }
    public void comDel(String s) {
        if (s.length()<2) {send("Tu ne m'as pas donné d'argument."); return;}
        try{
            Alarm a = delAlarm(Integer.parseInt(s.substring(1))-1);
            if (a==null) send("Tu n'as pas d'alarme avec cet identifiant.");
            else send("Tu viens de supprimer l'alarme "+a+".");
        } catch(Exception e) {send("Tu ne m'as pas fourni de nombre.");}
    }
    public void comList() {
        if (la.size()==0) {send("Tu n'as pas d'alarme"); return;}
        String s="Tes Alarmes : \n";
        for (int t=0; t<la.size(); t++) s+=MyEmoji.getNumber(t+1)+": "+la.get(t).toString()+"\n";
        send(s);
    }
    public void comMake(String s) {
        if (s.length()<2) {send("Tu ne m'as pas donné d'arguments."); return;}
        String[] ss = s.substring(1).split(" ");
        if (ss.length==2) {
            if (Symbol.exist(ss[0])) {
                try {
                    Alarm a = new Alarm(new Symbol(ss[0]),Double.parseDouble(ss[1]));
                    if (!addAlarm(a)) send("L'alarme "+a+" existe déjà.");
                    else {
                        send("Tu viens de créer l'alarme "+a+".");
                        Tool.dataBase.insertAlert(getId(),a);
                    }
                } catch (Exception e) {send("Les arguments sont incorrects, tu ne m'as pas fourni de nombre.");}
            } else send("Les arguments sont incorrects, le symbols ne fait réfèrence à aucun marché.");
        } else send("Les arguments sont incorrects, je n'en compte pas 2.");
    }
    public static void printList(List<Ignorant> li) {
        for(Ignorant i: li)
            System.out.println(i);
    }
    public static Ignorant exist(List<Ignorant> li, long id) {
        for (Ignorant i: li)
            if (i.getId()==id)
                return i;
        return null;
    }
}

class Alarm {
    private Symbol s;
    private double price;
    public Alarm(Symbol s, double price) {
        this.s = s;
        this.price = price;
    }
    public String getSymbol() {return ""+s;}
    public double getPrice() {return price;}
    public String toString() {
        return s+"{"+price+"}";
        //return MyEmoji.getAlarm()+" "+MyEmoji.getCharacters(""+s)+" "+MyEmoji.getHand()+" "+MyEmoji.getNumber(price)+" "+MyEmoji.getAlarm();
    }
    public boolean equals(Alarm a) {
        if (s.equals(a.getSymbol())&&price == a.getPrice())
                return true;
        return false;
    }
}

class Symbol {
    private String s;
    private static List<String> ls = null;
    public Symbol(String s) {
        if (exist(s)) this.s=s.toUpperCase();
        else s="BTCEUR";
    }
    public static boolean exist(String s) {return getSymbolExist(s,"").size()==1;}
    public String toString() {return s;}
    public boolean equals(String s) {return this.s.equals(s);}
    public static void initListSymbol(List<TickerPrice> ltp) {
        ls = new ArrayList<String>();
        for (TickerPrice tp: ltp)
            ls.add(tp.getSymbol());
    }
    public static List<String> getSymbolExist(String bas1, String bas2) {
        bas1 = bas1.toUpperCase();
        bas2 = bas2.toUpperCase();
        if (bas1.equals("ALL")) bas1 = "";
        List<String> ls = new ArrayList<String>();
        for (String s: Symbol.ls)
            if (s.indexOf(bas1)!=-1&&s.indexOf(bas2)!=-1)
                ls.add(s);
        return ls;
    }
}
