package fr.thibault.bot;

import java.util.List;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;

class BinanceTickersPrice extends Thread {
    private int sec;
    private List<Ignorant> li;
    public BinanceTickersPrice(int sec, List<Ignorant> li) {
        this.sec = sec;
        this.li = li;
    }
    public void run() {
        BinanceApiRestClient client = Tool.getBinanceClient();
        List<TickerPrice> lastAllPrices = client.getAllPrices();
        while(true) {
            try{Thread.sleep(sec*1000);}catch(Exception e){Tool.log(""+e);}
            List<TickerPrice> allPrices = client.getAllPrices();
            for (int t=0; t<allPrices.size(); t++)
                for (Ignorant i: li)
                    for (Alarm a: i.la)
                        if (a.getSymbol().equals(allPrices.get(t).getSymbol())) {
                            String s = croise(
                                 Double.parseDouble(lastAllPrices.get(t).getPrice()),
                                 Double.parseDouble(allPrices.get(t).getPrice()),
                                 a.getPrice());
                            if (!s.isEmpty()) {
                                /**/Tool.log("AlarmeRing:"+i.getName()+"("+i.getId()+"):"+a);
                                i.send(MyEmoji.getAlarm()+" "+a+" "+MyEmoji.getHand()+" "+s+" "+MyEmoji.getAlarm());
                            }
                        }
            lastAllPrices = allPrices;
        }
    }
    public String croise(double lastPrice, double price, double target) {
        if (price==target) return ""+price;
        if (target>lastPrice&&target<price)
            //return MyEmoji.getFlecheMonte()+MyEmoji.getNumber(lastPrice)+" "+MyEmoji.getHand()+" "+MyEmoji.getNumber(price)+MyEmoji.getFlecheMonte();
            return MyEmoji.getFlecheMonte()+" "+lastPrice+"/"+price+" "+MyEmoji.getFlecheMonte();
        if (target<lastPrice&&target>price)
            //return MyEmoji.getFlecheDesc()+MyEmoji.getNumber(lastPrice)+MyEmoji.getHand()+MyEmoji.getNumber(price)+MyEmoji.getFlecheDesc();
            return MyEmoji.getFlecheDesc()+" "+lastPrice+"/"+price+" "+MyEmoji.getFlecheDesc();
        return "";
    }
}

class BinanceTool {
    public static String get(String s) {
        /**/Tool.log("BinanceTool:"+s);
        return  Tool.getBinanceClient().getPrice(s).getPrice();
    }
}
