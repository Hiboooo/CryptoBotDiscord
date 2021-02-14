package fr.thibault.bot;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.time.Instant;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Color;

public class App {
    public static void main( String[] args ) {
        //System.out.println(" "+java.util.Arrays.toString("".getBytes()));
        /**/Tool.log("START BOT");
        Tool.init();
        /**/Tool.log("RUN LISTEN MESSAGE");
        Tool.gateway.on(MessageCreateEvent.class).subscribe(event -> {
            ListenMessage.run(event.getMessage());
        });
        /**/Tool.log("RUN LISTEN REACTION");
        Tool.gateway.on(ReactionAddEvent.class).subscribe(event -> {
            /**/Tool.log("ReactionEvent:"+event.getUser().block().getUsername()+"("+event.getUser().block().getId().asLong()+")"+"->#"+event.getMessage().block().getContent().replaceAll("\n","#")+"#");
            User u = event.getMessage().block().getAuthor().get();
            if (u.isBot()&&u.getUsername().equals("CryptoWizard")&&
                Tool.ll.contains(event.getMessageId().asLong()))
                if (event.getEmoji().asUnicodeEmoji().get().getRaw().equals(MyEmoji.getFaceStar())) {
                    u = event.getUser().block();
                    Ignorant i = Ignorant.exist(Tool.li,u.getId().asLong());
                    if (i!=null) i.send("Tu es déjà inscrit petit boulet ^^");
                    else {
                        /**/Tool.log("NouvelleUtilisateur: "+u.getUsername()+"("+u.getId().asLong()+")");
                        Tool.dataBase.insertUser(u.getId().asLong());
                        i = new Ignorant(u);
                        i.send("Bienvenu, n'hésite pas à \"$?\" pour que je te rappelle mes commandes!");
                        Tool.li.add(i);
                    }
                }
        });
        Tool.gateway.onDisconnect().block();
    }
}

class ListenMessage {
    public static DiscussPresentation discussPres = null;
    public static void run(Message m) {
        MessageChannel cha = m.getChannel().block();
        User user = m.getAuthor().get();
        /**/Tool.log("MessageEvent:"+user.getUsername()+"("+user.getId().asLong()+")"+"->#"+m.getContent().replaceAll("\n","#")+"#");
        if (!m.getAuthor().get().isBot()) {
            boolean findPath = false;
            if (cha instanceof TextChannel && m.getContent().equals("$&presentation")) {
                discussPres = new DiscussPresentation(user,(TextChannel)cha);
                findPath = true;
            }
            if (!findPath&&discussPres!=null) {
                if (discussPres.end()) discussPres=null;
                else if (discussPres.filtre(m)) findPath = true;
            }
            if (!findPath&&m.getContent().length()>1&&m.getContent().charAt(0)=='$') {
                if (m.getContent().length()>=2&&m.getContent().charAt(1)=='?') printHelp(cha);
                else if (m.getContent().length()>3&&m.getContent().charAt(1)=='&') cha.createMessage(MyEmoji.convert(m.getContent().substring(2).toUpperCase())).block();
                else if (m.getContent().length()>=4&&m.getContent().substring(1,4).toLowerCase().equals("del")) {
                    Ignorant i = getUser(m);
                    if (i!=null) i.comDel(m.getContent().substring(4));
                }
                else if (m.getContent().length()>=5&&m.getContent().substring(1,5).toLowerCase().equals("list")) {
                    Ignorant i = getUser(m);
                    if (i!=null) i.comList();
                }
                else if (m.getContent().length()>=5&&m.getContent().substring(1,5).toLowerCase().equals("make")) {
                    Ignorant i = getUser(m);
                    if (i!=null) i.comMake(m.getContent().substring(5));
                }
                else {
                    List<String> ls = Symbol.getSymbolExist(m.getContent().substring(1),"");
                    if (ls.size()==0) cha.createMessage("Ca n'existe pas du tout").block();
                    else if (ls.size()==1) cha.createMessage(MyEmoji.getCharacters(m.getContent().substring(1).toUpperCase())+" "+MyEmoji.getHand()+" "+MyEmoji.getNumber(BinanceTool.get(ls.get(0)))).block();
                    else splitMessageSymbol(cha,ls);
                }
            }
            else if (m.getContent().length()==1&&m.getContent().charAt(0)=='$') {
                cha.createMessage("M'avez-vous INVOQUEEEEEE!!! Vous avez le droit à trois veux!!!").block();
                cha.createMessage("Non je rigole XD, tu as le droit à rien toi ptdrrr").block();
            }
        }
    }
    private static Ignorant getUser(Message m) {
        MessageChannel mc = m.getChannel().block();
        if (mc instanceof PrivateChannel) {
            for (Ignorant i: Tool.li)
                if (i.getId()==m.getAuthor().get().getId().asLong())
                    return i;
            mc.createMessage("Tu n'es pas inscrit, je suis désolé. "+
                "Pour pouvoir utiliser mes commandes de channel privé, il te suffit de t'inscrire, redirige toi sur mon message de présentation pour plus d'informations").block();
        }
        else mc.createMessage("Ce n'est pas un channel privé, tu ne peux pas utiliser cette commande ici!!!").block();
        return null;

    }
    private static void printHelp(MessageChannel mc) {
        mc.createEmbed(spec -> spec.setColor(Color.RED)
            .setAuthor("CryptoWizard", "https://youtu.be/ytWz0qVvBZ0", "https://i.pinimg.com/originals/8c/fc/c9/8cfcc9bc6aabfac026ae9e3cd2a21e94.png")
            .setTitle(MyEmoji.getWizard()+" Help  "+MyEmoji.getWizard()).setUrl("https://youtu.be/n2U3JHmzwe4")
            .setDescription("Le grand mage de la crypto à la rescousse!")
            .addField("$? (s'utilise partout)", "Cette commande permet de recevoir ce message.", false)
            .addField("$<symbol> (s'utilise partout)", "Permet de demander le cours d'une crypto monnaie.", false)
            .addField("$LIST (s'utilise dans votre channel privé)", "Affiche vos alarmes.", false)
            .addField("$MAKE <symbol> <price> (s'utilise dans votre channel privé)", "Créé une nouvelle alarme si le symbole existe.", false)
            .addField("$DEL <id> (s'utilise dans votre channel privé)", "Supprime votre alarme avec l'id fournie.", false)
            .setThumbnail("http://www.mynewoldself.com/wp-content/uploads/2014/06/help.jpg")
            .setFooter("Rien de grand ne s'est accompli dans le monde sans passion.", "https://i.pinimg.com/originals/8c/fc/c9/8cfcc9bc6aabfac026ae9e3cd2a21e94.png")
            .setTimestamp(Instant.now())
            ).block();
    }
    private static void splitMessageSymbol(MessageChannel mc, List<String> ls) {
        int size = 0;
        final int tailleMes = 1000;
        String ch="";
        for (String s: ls) {
            if (ch.length()+s.length()>tailleMes) {
                mc.createMessage(ch.substring(1)).block();
                ch="";
            }
            ch+=" "+s;
        }
        mc.createMessage(ch.substring(1)).block();
    }
}

class DiscussPresentation {
    public User u = null;
    public TextChannel tc = null;
    public DiscussPresentation(User u, TextChannel tc) {
        this.u = u;
        this.tc = tc;
        /**/Tool.log("DiscussPresentationInit:"+u.getUsername()+"("+u.getId().asLong()+")");
        u.getPrivateChannel().block().createMessage("Send password here").block();
    }
    public boolean filtre(Message m) {
        User um = m.getAuthor().get();
        MessageChannel cha = m.getChannel().block();
        if (u.equals(um)) {
            if (cha.equals(u.getPrivateChannel().block())) {
                if (m.getContent().equals(Tool.pwd[5])) {
                    u.getPrivateChannel().block().createMessage("Good Password").block();
                    presentation();
                }
                else {
                    u.getPrivateChannel().block().createMessage("Bad Password").block();
                    tc.createMessage("Impossible d'afficher la présentation").block();
                }
                /**/Tool.log("DiscussPresentationFin:"+u.getUsername()+"("+u.getId().asLong()+")");
                u = null;
                tc = null;
                return true;
            }
        }
        return false;
    }
    public boolean end() {return u==null&&tc==null;}
    private void presentation() {
        Tool.ll.add(tc.createEmbed(spec -> spec.setColor(Color.GREEN)
        .setAuthor("By. Hiboooo", "https://youtu.be/ytWz0qVvBZ0", "https://i.ebayimg.com/images/g/JAQAAOSwRoxXmoJr/s-l300.jpg")
        .setImage("https://cryptoast.fr/wp-content/uploads/2019/10/altcoins-crypto-monnaies.jpg")
        .setTitle(MyEmoji.getFusee()+MyEmoji.getCoin()+" CryptoWizard "+MyEmoji.getCoin()+MyEmoji.getFusee())
        .setUrl("https://youtu.be/n2U3JHmzwe4")
        .setDescription("Je ne suis pour l'instant qu'un petit bot créé pour simplifier un peu vos vies ^^.")
        .addField("A quoi je sers ?", "Avec ma capacité de discuter avec Binance je peux vous transmettre beaucoup d'informations intérressantes."+
            "\nActuellement je sais vous communiquez le cours de n'importe quelle crypto monnaie.\n"+
            "Mais j'ai aussi été pensé pour faire des alarmes pour suivre le marché à votre place et vous avertir lors d'un franchissement de prix.",false)
        .addField("Comment s'inscrire ?","Réagissez à ce message avec "+MyEmoji.getFaceStar()+" et je m'occupe du reste.",false)
        .addField("Vous êtes un peu perdu ?", "Je possède une commande: \"$?\". Celle-ci permet de recevoir le liste des commandes pour communiquer avec moi, "+
            "ainsi que de quelques informations pour chacune.",false)
        .setThumbnail("https://media4.giphy.com/media/YnkMcHgNIMW4Yfmjxr/giphy.gif")
        .setFooter("Rien de grand ne s'est accompli dans le monde sans passion.", "https://i.ebayimg.com/images/g/JAQAAOSwRoxXmoJr/s-l300.jpg")
        .setTimestamp(Instant.now())).block().getId()
        .asLong());
    }
}
