package db.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.logging.*;

/**
 * Created by sergeybutorin on 20.02.17.
 */

@RestController
public class Controller {
    @RequestMapping(path = "/api", method = RequestMethod.GET)
    public void signup() {
        System.out.println("This is function");
        Connection connection = null;
        //URL к базе состоит из протокола:подпротокола://[хоста]:[порта_СУБД]/[БД] и других_сведений
        String url = "jdbc:postgresql://127.0.0.1:5432/docker";
        //Имя пользователя БД
        String name = "docker";
        //Пароль
        String password = "docker";
        try {
            //Загружаем драйвер
            Class.forName("org.postgresql.Driver");
            System.out.println("Драйвер подключен");
            //Создаём соединение
            connection = DriverManager.getConnection(url, name, password);
            System.out.println("Соединение установлено");
            //Для использования SQL запросов существуют 3 типа объектов:
            //1.Statement: используется для простых случаев без параметров
            Statement statement = null;

            statement = connection.createStatement();
            /*statement.executeQuery("CREATE TABLE users (" +
                    "    id    BIGSERIAL PRIMARY KEY," +
                    "    name  VARCHAR(128)," +
                    "    grade INT)");*/
            //Выполним запрос
            ResultSet result1 = statement.executeQuery(
                    "SELECT * FROM users where id >2 and id <10");
            //result это указатель на первую строку с выборки
            //чтобы вывести данные мы будем использовать
            //метод next() , с помощью которого переходим к следующему элементу
            System.out.println("Выводим statement");
            while (result1.next()) {
                System.out.println("Номер в выборке #" + result1.getRow()
                        + "\t Номер в базе #" + result1.getInt("id")
                        + "\t" + result1.getString("name"));
            }
            // Вставить запись
            statement.executeUpdate(
                    "INSERT INTO users(name) values('Vasya')");
            //Обновить запись
            statement.executeUpdate(
                    "UPDATE users SET name = 'admin' where id = 1");

            //2.PreparedStatement: предварительно компилирует запросы,
            //которые могут содержать входные параметры
            PreparedStatement preparedStatement = null;
            // ? - место вставки нашего значеня
            preparedStatement = connection.prepareStatement(
                    "SELECT * FROM users where id > ? and id < ?");
            //Устанавливаем в нужную позицию значения определённого типа
            preparedStatement.setInt(1, 2);
            preparedStatement.setInt(2, 10);
            //выполняем запрос
            ResultSet result2 = preparedStatement.executeQuery();

            System.out.println("Выводим PreparedStatement");
            while (result2.next()) {
                System.out.println("Номер в выборке #" + result2.getRow()
                        + "\t Номер в базе #" + result2.getInt("id")
                        + "\t" + result2.getString("name"));
            }

            preparedStatement = connection.prepareStatement(
                    "INSERT INTO users(name) values(?)");
            preparedStatement.setString(1, "user_name");
            //метод принимает значение без параметров
            //темже способом можно сделать и UPDATE
            preparedStatement.executeUpdate();
        } catch (Exception ex) {
            //выводим наиболее значимые сообщения
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
