package fr.thibault.bot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

class DataBase{
    private Connection conn;
    public DataBase(String url){
        conn = null;
        try{conn = DriverManager.getConnection(url);}
        catch(Exception e){Tool.log(""+e);}
    }
    private List<Ignorant> execute(String request, boolean creat) {
        List<Ignorant> li = new ArrayList<Ignorant>();
        if (conn==null) return li;
        try{
            ResultSet rs = conn.createStatement().executeQuery(request);
            if (creat)
                while (rs.next()){
                    long id = rs.getLong(2);
                    Alarm a = (rs.getString(3)==null)?null:new Alarm(new Symbol(rs.getString(3)),rs.getDouble(4));
                    boolean b = true;
                    if (a!=null)
                        for (Ignorant i: li)
                            if (i.getId()==id&&b) {
                                b=false;
                                i.addAlarm(a);
                            }
                    if (b) {
                        Ignorant i = new Ignorant(id);
                        if (a!=null) i.addAlarm(a);
                        li.add(i);
                    }
                }
        }
        catch(Exception e){/**/Tool.log("ErrorDataBase:"+e);}
		return li;
    }
    public List<Ignorant> getAllDataBase() {
        /**/Tool.log("ReadAllDataBase");
        return execute("SELECT * FROM users_alarm;",true);
    }
    public List<Ignorant> getAllUsers() /*Ignorant sans alertes*/ {
        /**/Tool.log("ReadAllUsersDataBase");
        return execute("SELECT * FROM users_alarm WHERE alert_type IS NULL;",true);
    }
    public boolean getUserExist(long id) {
        /**/Tool.log("ReadUserExistDataBase");
        return execute("SELECT * FROM users_alarm WHERE discord_id="+id+" && alert_type IS NULL;",true).size()==1;
    }
    public Ignorant getUser(long id) {
        /**/Tool.log("RechUserInDataBase");
        if (getUserExist(id))
            return execute("SELECT * FROM users_alarm WHERE discord_id="+id+" && alert_type IS NOT NULL;",true).get(0);
        return null;
    }
    public boolean getAlertFromUserExist(long id, Alarm a) {
        /**/Tool.log("RechAlertFromUserExistInDataBase");
        return execute("SELECT * FROM users_alarm WHERE discord_id="+id+" && alert_type=\""+a.getSymbol()+"\" && value="+a.getPrice()+";",true).size()==1;
    }
    public void insertUser(long id) {
        /**/Tool.log("InsertInDatatBase:"+id);
        if(!getUserExist(id))
            execute("INSERT INTO users_alarm (discord_id) VALUE ("+id+");",false);
    }
    public void insertAlert(long id, Alarm a) {
        /**/Tool.log("InsertInDataBase:"+id+"/"+a);
        if(getUserExist(id))
            if (!getAlertFromUserExist(id,a))
		          execute("INSERT INTO users_alarm (discord_id, alert_type, value) VALUE ("+id+", \""+a.getSymbol()+"\", "+a.getPrice()+");",false);
    }
    public void deleteAlert(long id, Alarm a) {
        /**/Tool.log("DeleteInDataBase:"+id+"/"+a);
        if (getUserExist(id))
            if (getAlertFromUserExist(id,a))
                execute("DELETE FROM users_alarm WHERE discord_id="+id+" && alert_type= \""+a.getSymbol()+"\" && value="+a.getPrice()+";",false);
    }
}
