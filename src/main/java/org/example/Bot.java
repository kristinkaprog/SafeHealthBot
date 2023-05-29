package org.example;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.*;
import java.util.*;

public class Bot extends TelegramLongPollingBot{
    Connection dbConnection;
    int count = 0;
    String fio = "";
    String medicine1 = "";

    String data = "";

    String timer1 = "";
    public Connection getDbConnection()
            throws ClassNotFoundException,SQLException{
        String connectionString = "jdbc:mysql://localhost:3306/safeHealth";
        Class.forName("com.mysql.cj.jdbc.Driver");

        dbConnection = DriverManager.getConnection(connectionString,
                "root","your_bd_password");
        return  dbConnection;
    }

    boolean v1 = false;
    boolean v11 = false;
    boolean v12 = false;
    boolean v21 = false;
    long chatId =0;
    @Override
    public String getBotUsername() {
        return "Client";
    }

    @Override
    public String getBotToken() {
        return "6103363175:AAHniuDPJ19H4qdGgBlHY8A4TcxT1Iq9jmc";
    }

    @Override
    public void onUpdateReceived(Update update) {

        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();
        var chatid = msg.getChatId();
        chatId = chatid;

        if (msg.hasText()){
            count++;
            if (msg.isCommand()){
                if (msg.getText().equals("/start")){
                    count=1;
                    if (count==1){
                    sendText(id,"Введите ваше ФИО");
                    }
                    if ((count==3)&&(msg.getText().equals("1"))){
                        v11 =true;
                    }else if ((count==3)&&(msg.getText().equals("2"))){
                        v12 = true;
                    }
                    v1 = true;
                }
                if (msg.getText().equals("/exit")){
                    sendText(id,"Всего доброго!");
                }

            }

            if (count==2){
                fio = msg.getText();
            }



            System.out.println(count);
            if ((count==3)&&(msg.getText().equals("1"))){
                v11 =true;
            }else if ((count==3)&&(msg.getText().equals("2"))){
                v12 = true;
            }

            if (v1){
                v1(id,update.getMessage());
            }else if (v11){
                v11(id,update.getMessage());
            } else if (v12){
                v12(id,update.getMessage());
            }

            if ((count!=4)&&(count!=2)){
                v21 = true;
            }

            if (count==4){
                String medicine = msg.getText();
                medicine1 = medicine;
            }


            if (v21){
                v21(id,update.getMessage());
            }

            if ((count==5)&&(msg.getText().equalsIgnoreCase("да"))){
                if (count == 5) {
                    sendText(id, "Введите время и дату, когда будет приходить оповещение " +
                            "( нужно ввести в виде ГГГГ-ММ-ДД ЧЧ:ММ:СС )");
                }
            }else if ((count==5)&&(msg.getText().equalsIgnoreCase("нет"))) {
                sendText(id,"Всего хорошего!");
            }
            if (count == 6) {
                String data1 = msg.getText();
                data = data1;
                setNapominalka(medicine1, data);
                WorkWithCalendar(id);
                sendText(id, "Ваше время успешно установлено!Спасибо за то, что воспользовались нашим ботом!");
            }


            }
        }



    public void v11(Long id,Message msg){
        if ((msg.hasText())&&(!msg.isCommand())){
            System.out.println("я в в11");
            WorkWithSidorov(id);
            v11 =false;
        }
    }

    public void v12(Long id,Message msg){
        if ((msg.hasText())&&(!msg.isCommand())){
            System.out.println("Я в в12");
            WorkWithSamoylov(id);
            v12 =false;
        }
    }

    public void v21(Long id,Message msg){
        if (msg.hasText()&&(!msg.isCommand())){
            if (count==3) {
                sendText(id, "Введите название лекарства, информацию  о котором хотите посмотреть или установить напоминалку ");
            }
            if (count==4) {
                WorkWithMedicines(id, msg);
                v21 = false;
            }

        }
    }

   public void v1(Long id, Message msg){
        if ((msg.hasText())&&(!msg.isCommand())) {
            WorkWithLogAndPas(id,msg);
            v1=false;
        }
    }


    public void WorkWithLogAndPas(Long id,Message msg){
        Client client = new Client();
        client.setFIO(msg.getText());
        ResultSet result = getClient(client);
        try{
            while (result.next()){
                String client1 =  result.getString("doctors");
                String[] doctors = client1.split(",");
                String alldoctors = "";
                if (doctors.length>0){
                    for (int i=0;i<doctors.length;i++){
                        String j = String.valueOf(i+1);
                        doctors[i] =" "+j+". "+doctors[i];
                        alldoctors = alldoctors+doctors[i];
                    }
                }
                sendText(id,"Ваш(и) врачи "+alldoctors+ ". Выберите цифру нужного врача");
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void  WorkWithSidorov(Long id){
        Doctors doctors = new Doctors();
        System.out.println(fio);
        doctors.setClientFio(fio);
        ResultSet result = getDoctorSidorov(doctors);
        try {
            while (result.next()){
                String meedicine= result.getString("Medicines");
                String[] medicines = meedicine.split(",");
                String allMedicines = "";
                if (medicines.length>0){
                    for (int i=0;i<medicines.length;i++){
                        String j = String.valueOf(i+1);
                        medicines[i] = " "+j+"."+ medicines[i];
                        allMedicines = allMedicines+medicines[i];
                    }
                }
                sendText(id,"Ваши выписанные лекарства: "+allMedicines);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void WorkWithMedicines(Long id,Message msg){
        Medicines medicines = new Medicines();
        medicines.setName(msg.getText());
        ResultSet result = getMedicine(medicines);
        try {
            while (result.next()){
                String medicine = result.getString("Instructions");
                sendText(id,"Информация о "+msg.getText()+" : \n "+medicine);
                sendText(id,"Вы хотите установить оповещение о приеме данного лекарства?");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void WorKWithNapominalka(Long chatId){
        forNapominalka forNapominalka = new forNapominalka();
        forNapominalka.setChatId(String.valueOf(chatId));
        ResultSet result = getTime(forNapominalka);
        try {
            while (result.next()){
                String time = result.getString("Data");
                String time1 = time.replaceAll(":"," ");
                String time3 = time1.replaceAll("-"," ");
                timer1 = time3;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void WorkWithCalendar(Long id){
        WorKWithNapominalka(chatId);
        System.out.println(timer1);
        String[] time2 = timer1.split(" ");
        for (int i=0;i<time2.length;i++){
            System.out.println(time2[i]);
        }

        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, Integer.parseInt(time2[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(time2[1])-1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(time2[2]));
        calendar.set(Calendar.HOUR, Integer.parseInt(time2[3]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time2[4]));
        calendar.set(Calendar.SECOND, Integer.parseInt(time2[5]));
        System.out.println(calendar.getTime());
        Timer timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println(calendar.getTime());
                sendText(id,"Вам пора принять "+medicine1);
            }
        };
        timer.schedule(timerTask,calendar.getTime());
    }

    public void  WorkWithSamoylov(Long id){
        Doctors doctors = new Doctors();
        doctors.setClientFio(fio);
        ResultSet result = getDoctorSamoylov(doctors);
        try {
            while (result.next()) {
                String meedicine = result.getString("Medicines");
                String[] medicines = meedicine.split(",");
                String allMedicines = "";
                if (medicines.length > 0) {
                    for (int i = 0; i < medicines.length; i++) {
                        String j = String.valueOf(i + 1);
                        medicines[i] = " " + j + "." + medicines[i];
                        allMedicines = allMedicines + medicines[i];
                    }
                }
                sendText(id, "Ваши выписанные лекарства: " + allMedicines);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet getClient(Client client){
        ResultSet resultSet = null;

        String select = "SELECT*FROM client WHERE Fio=?";

        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(select);
            prSt.setString(1,client.getFIO());

            resultSet = prSt.executeQuery();
        }catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return resultSet;
    }

    public ResultSet getDoctorSidorov(Doctors doctors){
        ResultSet resultSet = null;

        String select = "SELECT*FROM SidorovDoc WHERE ClientFio=?";

        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(select);
            prSt.setString(1,doctors.getClientFio());

            resultSet = prSt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return resultSet;
    }



    public void setNapominalka(String medicine,String Data){
        String insert = "INSERT INTO forNapominalka (chatId,medicine,Data) VALUES (?,?,?)";



        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insert);
            prSt.setString(1, String.valueOf(chatId));
            prSt.setString(2,medicine);
            prSt.setString(3,Data);

            prSt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public ResultSet getDoctorSamoylov(Doctors doctors){
        ResultSet resultSet =null;

        String select = "SELECT*FROM SamoylovDoc WHERE ClientFio=?";

        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(select);
            prSt.setString(1,doctors.getClientFio());

            resultSet = prSt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return resultSet;
    }

    public ResultSet getTime (forNapominalka forNapominalka){
        ResultSet resultSet = null;

        String select = "SELECT*FROM forNapominalka WHERE chatId=?";

        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(select);
            prSt.setString(1,forNapominalka.getChatId());

            resultSet = prSt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return resultSet;
    }
    public ResultSet getMedicine (Medicines medicines){
        ResultSet resultSet = null;

        String select = "SELECT*FROM Medicines WHERE Name=?";

        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(select);
            prSt.setString(1,medicines.getName());

            resultSet = prSt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return  resultSet;
    }

    public void sendText(Long who,String what){
        SendMessage sm = SendMessage.builder().chatId(who.toString()).text(what).build();
        try {
            execute(sm);
        }catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
    }



    public static void main(String[] args)throws TelegramApiException  {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot();
        botsApi.registerBot(bot);

        bot.sendText(your_id,"Добро пожаловать! Выберите в меню то действие, которое хотите выполнить");



    }

}
